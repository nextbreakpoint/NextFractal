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
package com.nextbreakpoint.nextfractal.runtime.javafx.core;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderException;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderHandle;
import com.nextbreakpoint.nextfractal.core.encoder.RAFEncoderContext;
import com.nextbreakpoint.nextfractal.core.export.ExportJobHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobState;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import com.nextbreakpoint.nextfractal.runtime.export.AbstractExportService;
import com.nextbreakpoint.nextfractal.runtime.export.ExportServiceDelegate;
import javafx.application.Platform;
import lombok.extern.java.Log;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.logging.Level;

@Log
public class SimpleExportService extends AbstractExportService {
	private static final int MAX_CONSECUTIVE_FRAMES = 100;

	private final Map<String, List<Future<ExportJobHandle>>> futures = new HashMap<>();
	private final Map<String, EncoderHandle> encoders = new HashMap<>();

	private final ExportServiceDelegate delegate;
	private final ExportRenderer exportRenderer;

	public SimpleExportService(ExportRenderer exportRenderer, ExportServiceDelegate delegate) {
		super(ThreadUtils.createVirtualThreadFactory("Export Service"));
		this.delegate = Objects.requireNonNull(delegate);
		this.exportRenderer = Objects.requireNonNull(exportRenderer);
	}

	@Override
	public void dispose() {
		exportRenderer.dispose();
	}

	@Override
	protected void updateSessions(Collection<ExportSessionHandle> sessions) {
		sessions.forEach(this::updateSession);
	}

	@Override
	protected void notifyProgress(Collection<ExportSessionHandle> sessions) {
		sessions.forEach(this::notifySession);
	}

	@Override
	protected void resumeSession(ExportSessionHandle session) {
		dispatchJobs(session);
	}

	@Override
	protected void cancelSession(ExportSessionHandle session) {
		final List<Future<ExportJobHandle>> jobs = futures.get(session.getSessionId());
		if (jobs != null) {
			jobs.forEach(task -> task.cancel(true));
		}
	}

	private void notifySession(ExportSessionHandle session) {
		Platform.runLater(() -> delegate.notifyUpdate(session.getSession(), session.getState(), session.getProgress()));
	}

	private void updateSession(ExportSessionHandle session) {
		if (session.isDispatched() || session.isSuspended()) {
			updateDispatchedSession(session);
		}

		if (session.isReady()) {
			openSession(session);
			resetJobs(session);
			dispatchJobs(session);
			session.setState(ExportSessionState.DISPATCHED);
		}

		session.updateProgress();

		if (session.isFailed() || session.isCompleted() || session.isInterrupted()) {
			removeJobs(session);
			closeSession(session);
		}

		if ((session.isFailed() || session.isCompleted() || session.isInterrupted()) && isTimeout(session)) {
			session.setState(ExportSessionState.TERMINATED);
		}
	}

	private boolean isTimeout(ExportSessionHandle session) {
		return System.currentTimeMillis() - session.getTimestamp() > 5000;
	}

	private void openSession(ExportSessionHandle session) {
		try {
			if (!encoders.containsKey(session.getSessionId())) {
				final EncoderHandle encoder = openEncoder(session);
				encoders.put(session.getSessionId(), encoder);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Cannot open encoder", e);
			session.setState(ExportSessionState.FAILED);
		}
	}

	private void closeSession(ExportSessionHandle session) {
		try {
			final EncoderHandle encoder = encoders.get(session.getSessionId());
			if (encoder != null) {
				closeEncoder(session, encoder);
				encoders.remove(session.getSessionId());
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Cannot close encoder", e);
			session.setState(ExportSessionState.FAILED);
		}
	}

	private EncoderHandle openEncoder(ExportSessionHandle session) throws IOException, EncoderException {
		final RandomAccessFile raf = new RandomAccessFile(session.getTmpFile(), "r");
		final String sessionId = session.getSessionId();
		final int frameRate = session.getFrameRate();
		final int imageWidth = session.getSize().width();
		final int imageHeight = session.getSize().height();
		final RAFEncoderContext context = new RAFEncoderContext(sessionId, raf, imageWidth, imageHeight, frameRate);
		return session.getEncoder().open(context, session.getFile());
	}

	private void closeEncoder(ExportSessionHandle session, EncoderHandle encoder) throws EncoderException {
		try {
			session.getEncoder().close(encoder);
		} finally {
			if (!session.getTmpFile().delete()) {
				log.log(Level.WARNING, "Cannot delete temporary file: " + session.getTmpFile());
			}
		}
	}

	private void updateDispatchedSession(ExportSessionHandle session) {
		final List<Future<ExportJobHandle>> jobs = futures.get(session.getSessionId());
		if (jobs != null) {
			futures.put(session.getSessionId(), removeTerminatedJobs(jobs));
			if (futures.get(session.getSessionId()).isEmpty()) {
				updateSessionState(session);
			}
		}
	}

	private void updateSessionState(ExportSessionHandle session) {
		if (session.isCancelled()) {
			session.setState(ExportSessionState.INTERRUPTED);
			return;
		}

		if (session.isSessionCompleted()) {
			log.info("Session %s: Frame %d of %d".formatted(session.getSessionId(), session.getFrameNumber() + 1, session.getFrameCount()));
			final int index = session.getFrameNumber();
			tryEncodingFrame(session, index, 1)
					.execute()
					.observe()
					.onSuccess(_ -> session.setState(ExportSessionState.COMPLETED))
					.onFailure(_ -> session.setState(ExportSessionState.FAILED))
					.get();
			return;
        }

		if (session.isFrameCompleted()) {
			log.info("Session %s: Frame %d of %d".formatted(session.getSessionId(), session.getFrameNumber() + 1, session.getFrameCount()));
			final int index = session.getFrameNumber();
			final int count = advanceFrame(session);
			tryEncodingFrame(session, index, count)
					.execute()
					.observe()
					.onSuccess(_ -> session.setState(ExportSessionState.READY))
					.onFailure(_ -> session.setState(ExportSessionState.FAILED))
					.get();
			return;
		}

		session.setState(ExportSessionState.SUSPENDED);
	}

	private int advanceFrame(ExportSessionHandle session) {
		int count = 0;
		do {
			log.info("Session %s: Frame %d of %d".formatted(session.getSessionId(), session.getFrameNumber() + 1, session.getFrameCount()));
		} while (count++ < MAX_CONSECUTIVE_FRAMES && session.nextFrame() && !isLastFrame(session) && !isKeyFrame(session) && isRepeated(session));
		return count;
	}

	private boolean isLastFrame(ExportSessionHandle session) {
		return session.getFrameNumber() == session.getFrameCount() - 1;
	}

	private boolean isKeyFrame(ExportSessionHandle session) {
		return session.getSession().getFrames().get(session.getFrameNumber()).keyFrame();
	}

	private boolean isRepeated(ExportSessionHandle session) {
		return session.getSession().getFrames().get(session.getFrameNumber()).repeated();
	}

	private void resetJobs(ExportSessionHandle session) {
		session.getJobs().forEach(job -> job.setState(ExportJobState.READY));
	}

	private Command<ExportSessionHandle> tryEncodingFrame(ExportSessionHandle session, int index, int count) {
		return Command.of(() -> encodeData(session, index, count));
	}

	private List<Future<ExportJobHandle>> removeTerminatedJobs(List<Future<ExportJobHandle>> jobs) {
		return jobs.stream().filter(exportJobHandleFuture -> !exportJobHandleFuture.isDone()).toList();
	}

	private void dispatchJobs(ExportSessionHandle session) {
		session.getJobs().stream().filter(job -> !job.isCompleted()).forEach(job -> dispatchJob(session, job));
	}

	private void dispatchJob(ExportSessionHandle session, ExportJobHandle job) {
		final List<Future<ExportJobHandle>> newJobs = getOrCreate(session.getSessionId());
		newJobs.add(exportRenderer.dispatch(job, session.getCurrentFrame()));
		futures.put(session.getSessionId(), newJobs);
	}

	private List<Future<ExportJobHandle>> getOrCreate(String sessionId) {
		final List<Future<ExportJobHandle>> jobs = futures.get(sessionId);
		if (jobs != null) {
			return new ArrayList<>(jobs);
		} else {
			return new ArrayList<>();
		}
	}

	private void removeJobs(ExportSessionHandle session) {
		futures.remove(session.getSessionId());
	}

	private ExportSessionHandle encodeData(ExportSessionHandle session, int frameIndex, int repeatFrameCount) throws EncoderException {
		final EncoderHandle encoder = encoders.get(session.getSessionId());
		session.getEncoder().encode(encoder, frameIndex, repeatFrameCount, session.getFrameCount());
		return session;
	}
}
