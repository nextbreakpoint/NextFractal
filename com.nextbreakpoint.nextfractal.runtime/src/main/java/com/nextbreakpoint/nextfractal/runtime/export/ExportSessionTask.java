package com.nextbreakpoint.nextfractal.runtime.export;

import com.nextbreakpoint.nextfractal.core.encoder.EncoderException;
import com.nextbreakpoint.nextfractal.core.export.ExportJobHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobState;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

@Log
public class ExportSessionTask implements Runnable {
    private static final int MAX_CONSECUTIVE_FRAMES = 100;

    private final Supplier<Optional<ExportSessionHandle>> pollQueue;
    private final Consumer<ExportSessionHandle> updateSession;
    private final ExportRenderer exportRenderer;
    private final ExecutorService executor;

    public ExportSessionTask(ExecutorService executor, Supplier<Optional<ExportSessionHandle>> pollQueue, Consumer<ExportSessionHandle> updateSession, ExportRenderer exportRenderer) {
        this.executor = Objects.requireNonNull(executor);
        this.pollQueue = Objects.requireNonNull(pollQueue);
        this.updateSession = Objects.requireNonNull(updateSession);
        this.exportRenderer = Objects.requireNonNull(exportRenderer);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                log.log(Level.FINE, "Polling dispatched sessions...");

                pollQueue.get().ifPresent(this::execute);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred while processing session", e);
        }
    }

    private void execute(ExportSessionHandle session) {
        try {
            log.log(Level.INFO, "Start processing session {0}", session.getSessionId());

            session.openEncoder();

            while (session.getState() == ExportSessionState.DISPATCHED) {
                log.log(Level.INFO, "Processing jobs for session {0}", session.getSessionId());

                if (session.isFrameCompleted()) {
                    resetJobs(session);
                }

                final List<CompletableFuture<ExportJobHandle>> futures = dispatchJobs(session);

                final CompletableFuture<Void> compositeFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}));

                printFrame(session);

                while (!compositeFuture.isDone()) {
                    try {
                        compositeFuture.get(5, TimeUnit.SECONDS);
                    } catch (TimeoutException _) {
                    }

                    session.updateProgress();

                    updateSession.accept(session);

                    log.log(Level.INFO, "Frame {0} of session {1}: {2}%", new Object[] { session.getFrameNumber(), session.getSessionId(), Math.rint(session.getProgress() * 100) });

                    if (session.isSuspended() || session.isCancelled()) {
                        futures.forEach(future -> cancelFrame(session, future));
                    }
                }

                if (session.isFrameCompleted()) {
                    log.log(Level.INFO, "Frame {0} of session {1} completed", new Object[] { session.getFrameNumber(), session.getSessionId() });

                    session.encode(session.getFrameNumber(), advanceFrame(session), session.getFrameCount());
                }

                if (session.isSessionCompleted()) {
                    session.setState(ExportSessionState.COMPLETED);
                } else if (session.isCancelled()) {
                    session.setState(ExportSessionState.INTERRUPTED);
                } else if (session.isSuspended()) {
                    session.setState(ExportSessionState.SUSPENDED);
                }
            }

            if (session.getState() != ExportSessionState.SUSPENDED) {
                session.closeEncoder();
            }
        } catch (EncoderException | IOException | ExecutionException e) {
            log.log(Level.WARNING, "Cannot process session", e);

            session.setState(ExportSessionState.FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.log(Level.WARNING, "Cannot process session", e);

            session.setState(ExportSessionState.FAILED);
        }

        log.log(Level.INFO, "Stop processing session {0}", session.getSessionId());

        session.updateProgress();

        updateSession.accept(session);
    }

    private static void cancelFrame(ExportSessionHandle session, CompletableFuture<ExportJobHandle> future) {
        try {
            future.cancel(false);
        } catch (CancellationException e) {
            log.log(Level.INFO, "Frame {0} of session {1} aborted", new Object[] { session.getFrameNumber(), session.getSessionId() });
        }
    }

    private static int advanceFrame(ExportSessionHandle session) {
        int count = 0;
        while (count++ < MAX_CONSECUTIVE_FRAMES && session.nextFrame() && !isLastFrame(session) && !isKeyFrame(session) && isRepeated(session)) {
            printFrame(session);
        }
        return count;
    }

    private static void printFrame(ExportSessionHandle session) {
        log.info("Session %s: Frame %d of %d".formatted(session.getSessionId(), session.getFrameNumber() + 1, session.getFrameCount()));
    }

    private static boolean isLastFrame(ExportSessionHandle session) {
        return session.getFrameNumber() == session.getFrameCount() - 1;
    }

    private static boolean isKeyFrame(ExportSessionHandle session) {
        return session.getSession().getFrames().get(session.getFrameNumber()).keyFrame();
    }

    private static boolean isRepeated(ExportSessionHandle session) {
        return session.getSession().getFrames().get(session.getFrameNumber()).repeated();
    }

    private List<CompletableFuture<ExportJobHandle>> dispatchJobs(ExportSessionHandle session) {
        return session.getJobs()
                .stream()
                .filter(job -> job.getState() != ExportJobState.COMPLETED)
                .collect(ArrayList::new, (list, job) -> list.add(dispatchJob(session, job)), ArrayList::addAll);
    }

    private CompletableFuture<ExportJobHandle> dispatchJob(ExportSessionHandle session, ExportJobHandle job) {
        return CompletableFuture.supplyAsync(() -> exportRenderer.execute(job, session.getCurrentFrame()), executor);
    }

    private void resetJobs(ExportSessionHandle session) {
        session.getJobs().forEach(job -> job.setState(ExportJobState.READY));
    }
}
