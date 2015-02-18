package com.nextbreakpoint.nextfractal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

public class ExportService {
	private final List<ExportSession> sessions = new ArrayList<>();
	private DispatchService dispatchService;
	private ThreadFactory threadFactory;
	private volatile Thread thread;
	private volatile boolean running;
	private int tileSize;
	
	public ExportService(ThreadFactory threadFactory, DispatchService dispatchService, int tileSize) {
		this.threadFactory = threadFactory;
		this.dispatchService = dispatchService;
		this.tileSize = tileSize;
	}

	/**
	 * @param runnable
	 * @return
	 */
	protected Thread createThread(Runnable runnable) {
		return threadFactory.newThread(runnable);
	}

	public void start() {
		if (thread == null) {
			thread = createThread(new ProcessSessions());
			running = true;
			thread.start();
		}
	}

	public void stop() {
		running = false;
		if (thread != null) {
			thread.interrupt();
			try {
				thread.join();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			thread = null;
		}
	}

	public int getTileSize() {
		return tileSize;
	}

	public void runSession(ExportSession exportSession) {
		addSession(exportSession);
	}

	private synchronized void addSession(ExportSession exportSession) {
		sessions.add(exportSession);
		notify();
	}

	private synchronized ExportSession findOneSession() {
		for (ExportSession session : sessions) {
			if (!session.isSuspended()) {
				return session;
			}
		}
		return null;
	}

	private synchronized void terminateSession(ExportSession exportSession) {
		sessions.remove(exportSession);
		exportSession.dispose();
	}

	private synchronized void waitForSession() {
		try {
			wait(1000);
		} catch (InterruptedException e) {
		}
	}

	private void processSession(ExportSession session) {
		processJobs(session);
		session.updateState();
		if (session.isTerminated()) {
			terminateSession(session);
		}
	}

	private void processJobs(ExportSession session) {
		for (ExportJob job : session.getJobs()) {
			if (isSessionValid(session)) {
				break;
			}
			if (!job.isCompleted()) {
				dispatchJob(job);
			}
		}
	}

	private boolean isSessionValid(ExportSession session) {
		return session.isTerminated() || session.isSuspended();
	}
	
	private void dispatchJob(ExportJob job) {
		dispatchService.dispatch(job);
	}

	private class ProcessSessions implements Runnable {
		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			while (running) {
				ExportSession exportSession = null;
				while (running && (exportSession = findOneSession()) == null) {
					waitForSession();
				}
				if (running && exportSession != null) {
					processSession(exportSession);
				}
			}
		}
	}
}
