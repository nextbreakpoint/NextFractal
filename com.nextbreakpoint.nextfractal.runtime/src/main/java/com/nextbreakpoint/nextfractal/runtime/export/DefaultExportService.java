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
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.export.ExportSessionState.COMPLETED;
import static com.nextbreakpoint.nextfractal.core.export.ExportSessionState.DISPATCHED;
import static com.nextbreakpoint.nextfractal.core.export.ExportSessionState.FAILED;
import static com.nextbreakpoint.nextfractal.core.export.ExportSessionState.INTERRUPTED;
import static com.nextbreakpoint.nextfractal.core.export.ExportSessionState.SUSPENDED;

@Log
public class DefaultExportService implements ExportService {
	private static final Set<ExportSessionState> SESSION_STATES = Set.of(COMPLETED, FAILED, INTERRUPTED);

	private final List<ExecutorService> executors = new ArrayList<>();
	private final Map<String, ExportSessionHandle> sessions = new HashMap<>();
	private final LinkedBlockingQueue<ExportSessionHandle> scheduleQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<ExportSessionHandle> dispatchQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<ExportSessionHandle> removeQueue = new LinkedBlockingQueue<>();
	private final ReentrantLock lock = new ReentrantLock();
	private final ExecutorService executor;
	private ExportServiceDelegate delegate;

	public DefaultExportService(ExportRenderer exportRenderer) {
        executors.add(ExecutorUtils.newFixedThreadPool(8, ThreadUtils.createVirtualThreadFactory("Export Session")));
        executors.add(ExecutorUtils.newFixedThreadPool(8, ThreadUtils.createVirtualThreadFactory("Export Session")));
        executors.add(ExecutorUtils.newFixedThreadPool(8, ThreadUtils.createVirtualThreadFactory("Export Session")));
        executors.add(ExecutorUtils.newFixedThreadPool(8, ThreadUtils.createVirtualThreadFactory("Export Session")));
		executor = ExecutorUtils.newThreadPerTaskExecutor(ThreadUtils.createVirtualThreadFactory("Export Service"));
		executor.submit(new DispatchSessionsTask(this::pollScheduleQueue, this::dispatchSession));
		executor.submit(new RemoveSessionsTask(this::pollRemoveQueue, this::removeSession));
		executor.submit(new ExportSessionTask(executors.get(0), this::pollDispatchQueue, this::updateSession, exportRenderer));
		executor.submit(new ExportSessionTask(executors.get(1), this::pollDispatchQueue, this::updateSession, exportRenderer));
		executor.submit(new ExportSessionTask(executors.get(2), this::pollDispatchQueue, this::updateSession, exportRenderer));
		executor.submit(new ExportSessionTask(executors.get(3), this::pollDispatchQueue, this::updateSession, exportRenderer));
	}

	public synchronized void setDelegate(ExportServiceDelegate delegate) {
		this.delegate = delegate;
	}

	public final void shutdown() {
		ExecutorUtils.shutdown(executor);
		executors.forEach(ExecutorUtils::shutdown);
	}

	public int getSessionCount() {
		return sessions.size();
	}

	public final void startSession(ExportSession session) {
		try {
			lock.lock();

			if (sessions.containsKey(session.getSessionId())) {
				log.log(Level.WARNING, "Session {0} exists already", session.getSessionId());
				return;
			}

			sessions.put(session.getSessionId(), new ExportSessionHandle(session));

			scheduleQueue.add(sessions.get(session.getSessionId()));
		} finally {
			lock.unlock();
		}
	}

	public final void stopSession(ExportSession session) {
		try {
			lock.lock();

			final ExportSessionHandle sessionHandle = sessions.get(session.getSessionId());

			if (sessionHandle == null) {
				log.log(Level.WARNING, "Session {0} does not exist", session.getSessionId());
				return;
			}

			if (sessionHandle.getState() == DISPATCHED) {
				sessionHandle.setCancelled(true);
			}

			if (sessionHandle.getState() == SUSPENDED) {
				executor.submit(new DelayedRemove(sessionHandle));
			}
		} finally {
			lock.unlock();
		}
	}

	public final void suspendSession(ExportSession session) {
		try {
			lock.lock();

			final ExportSessionHandle sessionHandle = sessions.get(session.getSessionId());

			if (sessionHandle == null) {
				log.log(Level.WARNING, "Session {0} does not exist", session.getSessionId());
				return;
			}

			if (sessionHandle.getState() == DISPATCHED) {
				sessionHandle.setSuspended(true);
			}
		} finally {
			lock.unlock();
		}
	}

	public final void resumeSession(ExportSession session) {
		try {
			lock.lock();

			final ExportSessionHandle sessionHandle = sessions.get(session.getSessionId());

			if (sessionHandle == null) {
				log.log(Level.WARNING, "Session {0} does not exist", session.getSessionId());
				return;
			}

			if (sessionHandle.getState() == SUSPENDED) {
				sessionHandle.setSuspended(false);

				scheduleQueue.add(sessionHandle);
			}
		} finally {
			lock.unlock();
		}
	}

	private void dispatchSession(ExportSessionHandle session) {
		try {
			lock.lock();

			session.setState(ExportSessionState.DISPATCHED);

			dispatchQueue.add(session);
		} finally {
			lock.unlock();
		}

		synchronized (this) {
			if (delegate != null) {
				delegate.notifyUpdate(session.getSession(), session.getState(), session.getProgress());
			}
		}
	}

	private void removeSession(ExportSessionHandle session) {
		try {
			lock.lock();

			sessions.remove(session.getSessionId());
		} finally {
			lock.unlock();
		}

		synchronized (this) {
			if (delegate != null) {
				delegate.notifyUpdate(session.getSession(), session.getState(), session.getProgress());
			}
		}
	}

	private void updateSession(ExportSessionHandle session) {
		try {
			lock.lock();

			if (SESSION_STATES.contains(session.getState())) {
				executor.submit(new DelayedRemove(session));
			}
		} finally {
			lock.unlock();
		}

		synchronized (this) {
			if (delegate != null) {
				delegate.notifyUpdate(session.getSession(), session.getState(), session.getProgress());
			}
		}
	}

	private void terminateSession(ExportSessionHandle session) {
		try {
			lock.lock();

			session.setState(ExportSessionState.TERMINATED);

			removeQueue.add(session);
		} finally {
			lock.unlock();
		}
	}

	private Optional<ExportSessionHandle> pollScheduleQueue() {
        try {
            return Optional.ofNullable(scheduleQueue.poll(30, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
        }
    }

	private Optional<ExportSessionHandle> pollDispatchQueue() {
		try {
			return Optional.ofNullable(dispatchQueue.poll(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private Optional<ExportSessionHandle> pollRemoveQueue() {
		try {
			return Optional.ofNullable(removeQueue.poll(30, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private class DelayedRemove implements Runnable {
		private final ExportSessionHandle session;

		public DelayedRemove(ExportSessionHandle session) {
			this.session = session;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(3000);

				log.log(Level.INFO, "Session {0} terminated", session.getSessionId());

				terminateSession(session);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
