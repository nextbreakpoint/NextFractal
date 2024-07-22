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
import com.nextbreakpoint.nextfractal.core.encode.EncoderException;
import com.nextbreakpoint.nextfractal.core.encode.EncoderHandle;
import com.nextbreakpoint.nextfractal.core.encode.RAFEncoderContext;
import com.nextbreakpoint.nextfractal.core.export.ExportHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobState;
import com.nextbreakpoint.nextfractal.core.export.ExportProfileBuilder;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import com.nextbreakpoint.nextfractal.core.export.ExportState;
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
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

@Log
public class SimpleExportService extends AbstractExportService {
	private final Map<String, List<Future<ExportJobHandle>>> futures = new HashMap<>();
	private final Map<String, EncoderHandle> handles = new HashMap<>();

	private final ExportServiceDelegate delegate;
	private final ExportRenderer exportRenderer;

	public SimpleExportService(ExportServiceDelegate delegate, ThreadFactory threadFactory, ExportRenderer exportRenderer) {
		super(threadFactory);
		this.delegate = Objects.requireNonNull(delegate);
		this.exportRenderer = Objects.requireNonNull(exportRenderer);
	}

	@Override
	protected Collection<ExportHandle> updateInBackground(Collection<ExportHandle> exportHandles) {
		return exportHandles.stream().map(this::updateSession).filter(ExportHandle::isFinished).collect(Collectors.toList());
	}

	@Override
	protected void notifyUpdate(Collection<ExportHandle> exportHandles) {
		exportHandles.forEach(this::notifyUpdate);
	}

	@Override
	protected void resumeTasks(ExportHandle exportHandle) {
		dispatchJobs(exportHandle);
	}

	@Override
	protected void cancelTasks(ExportHandle exportHandle) {
		tasks(exportHandle.getSessionId()).ifPresent(tasks -> tasks.forEach(task -> task.cancel(true)));
	}

	private void notifyUpdate(ExportHandle exportHandle) {
		Platform.runLater(() -> delegate.notifyUpdate(exportHandle.getSession(), exportHandle.getState(), exportHandle.getProgress()));
	}

	private ExportHandle updateSession(ExportHandle exportHandle) {
		if (exportHandle.isReady()) {
			openSession(exportHandle);
			resetJobs(exportHandle);
			dispatchJobs(exportHandle);
			exportHandle.setState(ExportState.DISPATCHED);
		} else if (exportHandle.isInterrupted() && isTimeout(exportHandle)) {
			exportHandle.setState(ExportState.FINISHED);
		} else if (exportHandle.isCompleted() && isTimeout(exportHandle)) {
			exportHandle.setState(ExportState.FINISHED);
		} else if (exportHandle.isFailed() && exportHandle.isCancelled() && isTimeout(exportHandle)) {
			exportHandle.setState(ExportState.FINISHED);
		} else if (exportHandle.isDispatched() || exportHandle.isSuspended()) {
			updateDispatchedSession(exportHandle);
		}
		exportHandle.updateProgress();
		if (exportHandle.isFailed() || exportHandle.isFinished()) {
			removeTasks(exportHandle);
			closeSession(exportHandle);
		}
		return exportHandle;
	}

	private boolean isTimeout(ExportHandle exportHandle) {
		return System.currentTimeMillis() - exportHandle.getTimestamp() > 1500;
	}

	private void openSession(ExportHandle exportHandle) {
		try {
			if (!handles.containsKey(exportHandle.getSessionId())) {
				EncoderHandle handle = openEncoder(exportHandle);
				handles.put(exportHandle.getSessionId(), handle);
			}
		} catch (Exception e) {
			exportHandle.setState(ExportState.FAILED);
		}
	}

	private EncoderHandle openEncoder(ExportHandle exportHandle) throws IOException, EncoderException {
		final RandomAccessFile raf = new RandomAccessFile(exportHandle.getTmpFile(), "r");
		final int frameRate = exportHandle.getFrameRate();
		final int imageWidth = exportHandle.getSize().getWidth();
		final int imageHeight = exportHandle.getSize().getHeight();
		final RAFEncoderContext context = new RAFEncoderContext(raf, imageWidth, imageHeight, frameRate);
		return exportHandle.getEncoder().open(context, exportHandle.getFile());
	}

	private void closeSession(ExportHandle exportHandle) {
		try {
			final EncoderHandle handle = handles.remove(exportHandle.getSessionId());
			if (handle != null) {
				closeEncoder(exportHandle, handle);
			}
		} catch (Exception e) {
			exportHandle.setState(ExportState.FAILED);
		}
	}

	private void closeEncoder(ExportHandle exportHandle, EncoderHandle encoderHandle) throws EncoderException {
		try {
			exportHandle.getEncoder().close(encoderHandle);
		} finally {
			exportHandle.getTmpFile().delete();
		}
	}

	private void updateDispatchedSession(ExportHandle exportHandle) {
		tasks(exportHandle.getSessionId()).map(this::removeTerminatedTasks)
			.filter(List::isEmpty).ifPresent(tasks -> updateSessionState(exportHandle));
	}

	private void updateSessionState(ExportHandle exportHandle) {
		if (exportHandle.isCancelled()) {
			exportHandle.setState(ExportState.INTERRUPTED);
		} else if (exportHandle.isSessionCompleted()) {
			log.info("Frame " + (exportHandle.getFrameNumber() + 1) + " of " + exportHandle.getFrameCount());
			final int index = exportHandle.getFrameNumber();
			tryEncodeFrame(exportHandle, index, 1)
					.execute()
					.observe()
					.onSuccess(s -> exportHandle.setState(ExportState.COMPLETED))
					.onFailure(e -> exportHandle.setState(ExportState.FAILED))
					.get();
        } else if (exportHandle.isFrameCompleted()) {
			final int index = exportHandle.getFrameNumber();
			int count = 0;
			do {
				log.info("Frame " + (exportHandle.getFrameNumber() + 1) + " of " + exportHandle.getFrameCount());
				exportHandle.nextFrame();
				count += 1;
			} while (count < 100 && !isLastFrame(exportHandle) && !isKeyFrame(exportHandle) && !isTimeAnimation(exportHandle));
			tryEncodeFrame(exportHandle, index, count)
					.execute()
					.observe()
					.onSuccess(s -> exportHandle.setState(ExportState.READY))
					.onFailure(e -> exportHandle.setState(ExportState.FAILED))
					.get();
		} else {
			exportHandle.setState(ExportState.SUSPENDED);
        }
	}

	private boolean isLastFrame(ExportHandle exportHandle) {
		return exportHandle.getFrameNumber() == exportHandle.getFrameCount() - 1;
	}

	private boolean isKeyFrame(ExportHandle exportHandle) {
		return exportHandle.getSession().getFrames().get(exportHandle.getFrameNumber()).isKeyFrame();
	}

	private boolean isTimeAnimation(ExportHandle exportHandle) {
		return exportHandle.getSession().getFrames().get(exportHandle.getFrameNumber()).isTimeAnimation();
	}

	private void resetJobs(ExportHandle exportHandle) {
		exportHandle.getJobs().forEach(job -> job.setState(ExportJobState.READY));
	}

	private Command<ExportHandle> tryEncodeFrame(ExportHandle exportHandle, int index, int count) {
		return Command.of(() -> encodeData(exportHandle, index, count));
	}

	private List<Future<ExportJobHandle>> removeTerminatedTasks(List<Future<ExportJobHandle>> tasks) {
		tasks.removeAll(tasks.stream().filter(Future::isDone).toList());
		return tasks;
	}

	private void dispatchJobs(ExportHandle exportHandle) {
		exportHandle.getJobs().stream().filter(job -> !job.isCompleted()).forEach(job -> dispatchTasks(exportHandle, job));
	}

	private void dispatchTasks(ExportHandle exportHandle, ExportJobHandle exportJob) {
		final List<Future<ExportJobHandle>> tasks = tasks(exportHandle.getSessionId()).orElse(new ArrayList<>());
		final ExportProfileBuilder builder = ExportProfileBuilder.fromProfile(exportJob.getJob().getProfile());
		builder.withPluginId(exportHandle.getCurrentPluginId());
		builder.withMetadata(exportHandle.getCurrentMetadata());
		builder.withScript(exportHandle.getCurrentScript());
		exportJob.setProfile(builder.build());
		tasks.add(exportRenderer.dispatch(exportJob));
		futures.put(exportHandle.getSessionId(), tasks);
	}

	private void removeTasks(ExportHandle exportHandle) {
		futures.remove(exportHandle.getSessionId());
	}

	private Optional<List<Future<ExportJobHandle>>> tasks(String sessionId) {
		return Optional.ofNullable(futures.get(sessionId));
	}

	private ExportHandle encodeData(ExportHandle exportHandle, int index, int count) throws EncoderException {
		exportHandle.getEncoder().encode(handles.get(exportHandle.getSessionId()), index, count);
		return exportHandle;
	}
}
