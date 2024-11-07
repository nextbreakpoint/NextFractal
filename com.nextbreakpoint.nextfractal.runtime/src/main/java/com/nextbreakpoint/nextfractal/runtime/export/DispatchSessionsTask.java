/*
 * NextFractal 2.4.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
