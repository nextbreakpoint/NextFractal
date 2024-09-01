/*
 * NextFractal 2.3.2
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

import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import lombok.extern.java.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

@Log
public abstract class AbstractExportService implements ExportService {
	private final HashMap<String, ExportSessionHandle> exportHandles = new LinkedHashMap<>();
	private final ReentrantLock lock = new ReentrantLock();
	private final ScheduledExecutorService executor;
	
	public AbstractExportService(ThreadFactory threadFactory) {
		executor = ExecutorUtils.newSingleThreadScheduledExecutor(Objects.requireNonNull(threadFactory));
		executor.scheduleAtFixedRate(this::updateSessions, 1000, 100, TimeUnit.MILLISECONDS);
		executor.scheduleWithFixedDelay(this::removeCompletedSessions, 1000, 5000, TimeUnit.MILLISECONDS);
	}

	public final void shutdown() {
		ExecutorUtils.shutdown(executor);
		dispose();
	}

	public final void startSession(ExportSession session) {
		try {
			lock.lock();
			final ExportSessionHandle exportHandle = exportHandles.getOrDefault(session.getSessionId(), new ExportSessionHandle(session));
			if (exportHandle.getState() != ExportSessionState.SUSPENDED) {
				throw new IllegalStateException("Session is not suspended");
			}
			exportHandle.setState(ExportSessionState.READY);
			exportHandle.setCancelled(false);
			exportHandles.put(session.getSessionId(), exportHandle);
		} finally {
			lock.unlock();
		}
	}

	public final void stopSession(ExportSession session) {
		try {
			lock.lock();
			final ExportSessionHandle exportHandle = exportHandles.get(session.getSessionId());
			if (exportHandle != null) {
				exportHandle.setCancelled(true);
				cancelSession(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	public final void suspendSession(ExportSession session) {
		try {
			lock.lock();
			final ExportSessionHandle exportHandle = exportHandles.get(session.getSessionId());
			if (exportHandle != null) {
				exportHandle.setCancelled(false);
				cancelSession(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	public final void resumeSession(ExportSession session) {
		try {
			lock.lock();
			final ExportSessionHandle exportHandle = exportHandles.get(session.getSessionId());
			if (exportHandle != null) {
				if (exportHandle.getState() != ExportSessionState.SUSPENDED) {
					throw new IllegalStateException("Session is not suspended");
				}
				exportHandle.setState(ExportSessionState.DISPATCHED);
				exportHandle.setCancelled(false);
				resumeSession(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	private void updateSessions() {
		try {
			try {
				lock.lock();
				updateSessions(exportHandles.values());
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't update sessions", e);
		}
	}

	private void removeCompletedSessions() {
		try {
			try {
				lock.lock();
				final LinkedList<ExportSessionHandle> removedHandles = new LinkedList<>();
				exportHandles.values().forEach(session -> {
					if (session.isTerminated() && session.isExpired()) {
						removedHandles.add(session);
					}
				});
				removedHandles.forEach(session -> exportHandles.remove(session.getSessionId()));
			} finally {
				lock.unlock();
			}
			notifyProgress(exportHandles.values());
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't update sessions", e);
		}
	}

	protected abstract void updateSessions(Collection<ExportSessionHandle> sessions);

	protected abstract void notifyProgress(Collection<ExportSessionHandle> sessions);

	protected abstract void resumeSession(ExportSessionHandle session);

	protected abstract void cancelSession(ExportSessionHandle session);

	protected abstract void dispose();

	public int getSessionCount() {
		return exportHandles.size();
	}
}
