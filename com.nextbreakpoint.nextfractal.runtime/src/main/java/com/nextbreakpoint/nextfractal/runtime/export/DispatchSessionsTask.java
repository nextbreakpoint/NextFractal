package com.nextbreakpoint.nextfractal.runtime.export;

import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import lombok.extern.java.Log;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

@Log
public class DispatchSessionsTask implements Runnable {
    private final Supplier<Optional<ExportSessionHandle>> pollQueue;
    private final Consumer<ExportSessionHandle> dispatchSession;

    public DispatchSessionsTask(Supplier<Optional<ExportSessionHandle>> pollQueue, Consumer<ExportSessionHandle> dispatchSession) {
        this.pollQueue = Objects.requireNonNull(pollQueue);
        this.dispatchSession = Objects.requireNonNull(dispatchSession);
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                log.log(Level.FINE, "Polling scheduled sessions...");

                pollQueue.get().ifPresent(session -> {
                    log.log(Level.FINE, "Found scheduled session {0}", session.getSessionId());

                    dispatchSession.accept(session);
                });
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "An error occurred while processing scheduled session", e);
        }
    }
}
