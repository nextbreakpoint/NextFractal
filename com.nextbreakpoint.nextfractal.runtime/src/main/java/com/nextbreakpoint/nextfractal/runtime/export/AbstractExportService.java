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

import com.nextbreakpoint.nextfractal.core.export.ExportHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportState;
import lombok.extern.java.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

@Log
public abstract class AbstractExportService implements ExportService {
	private final HashMap<String, ExportHandle> sessions = new LinkedHashMap<>();
	private final List<ExportHandle> finishedSessions = new LinkedList<>();
	private final ReentrantLock lock = new ReentrantLock();
	private final ScheduledExecutorService executor;
	
	public AbstractExportService(ThreadFactory threadFactory) {
		executor = Executors.newSingleThreadScheduledExecutor(Objects.requireNonNull(threadFactory));
		executor.scheduleAtFixedRate(this::lockAndUpdateSessions, 1000, 250, TimeUnit.MILLISECONDS);
		executor.scheduleWithFixedDelay(this::notifyUpdateSessions, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	public final void shutdown() {
		executor.shutdownNow();
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public final void startSession(ExportSession session) {
		try {
			lock.lock();
			ExportHandle exportHandle = sessions.get(session.getSessionId());
			if (exportHandle == null) {
				exportHandle = new ExportHandle(session);
			}
			if (exportHandle.getState() != ExportState.SUSPENDED) {
				throw new IllegalStateException("Session is not suspended");
			}
			exportHandle.setState(ExportState.READY);
			exportHandle.setCancelled(false);
			sessions.put(session.getSessionId(), exportHandle);
		} finally {
			lock.unlock();
		}
	}

	public final void stopSession(ExportSession session) {
		try {
			lock.lock();
			ExportHandle exportHandle = sessions.get(session.getSessionId());
			if (exportHandle != null) {
				exportHandle.setCancelled(true);
				cancelTasks(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	public final void suspendSession(ExportSession session) {
		try {
			lock.lock();
			ExportHandle exportHandle = sessions.get(session.getSessionId());
			if (exportHandle != null) {
				exportHandle.setCancelled(false);
				cancelTasks(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	public final void resumeSession(ExportSession session) {
		try {
			lock.lock();
			ExportHandle exportHandle = sessions.get(session.getSessionId());
			if (exportHandle != null) {
				if (exportHandle.getState() != ExportState.SUSPENDED) {
					throw new IllegalStateException("Session is not suspended");
				}
				exportHandle.setState(ExportState.DISPATCHED);
				exportHandle.setCancelled(false);
				resumeTasks(exportHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	private void lockAndUpdateSessions() {
		try {
			LinkedList<ExportHandle> copyOfSessions = new LinkedList<>();
			try {
				lock.lock();
				copyOfSessions.addAll(sessions.values());
			} finally {
				lock.unlock();
			}
			Collection<ExportHandle> finished = updateInBackground(copyOfSessions);
			try {
				lock.lock();
				finishedSessions.addAll(finished);
			} finally {
				lock.unlock();
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't update sessions", e);
		}
	}

	private void notifyUpdateSessions() {
		try {
			LinkedList<ExportHandle> copyOfSessions = new LinkedList<>();
			try {
				lock.lock();
				copyOfSessions.addAll(sessions.values());
				finishedSessions.forEach(session -> sessions.remove(session.getSessionId()));
				finishedSessions.clear();
			} finally {
				lock.unlock();
			}
			notifyUpdate(copyOfSessions);
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't notify updates", e);
		}
	}

	protected abstract Collection<ExportHandle> updateInBackground(Collection<ExportHandle> holders);

	protected abstract void notifyUpdate(Collection<ExportHandle> holders);

	protected abstract void resumeTasks(ExportHandle exportHandle);

	protected abstract void cancelTasks(ExportHandle exportHandle);

	public int getSessionCount() {
		return sessions.size();
	}
}
