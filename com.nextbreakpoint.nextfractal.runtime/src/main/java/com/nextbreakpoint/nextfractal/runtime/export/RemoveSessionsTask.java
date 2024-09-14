package com.nextbreakpoint.nextfractal.runtime.export;

import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import lombok.extern.java.Log;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

@Log
public class RemoveSessionsTask implements Runnable {
    private final Supplier<Optional<ExportSessionHandle>> pollQueue;
    private final Consumer<ExportSessionHandle> removeSession;

    public RemoveSessionsTask(Supplier<Optional<ExportSessionHandle>> pollQueue, Consumer<ExportSessionHandle> removeSession) {
        this.pollQueue = Objects.requireNonNull(pollQueue);
        this.removeSession = Objects.requireNonNull(removeSession);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                log.log(Level.FINE, "Polling expired sessions...");

                pollQueue.get().ifPresent(session -> {
                    log.log(Level.FINE, "Found expired session {0}", session.getSessionId());

                    removeSession.accept(session);
                });
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred while processing expired session", e);
        }
    }
}
