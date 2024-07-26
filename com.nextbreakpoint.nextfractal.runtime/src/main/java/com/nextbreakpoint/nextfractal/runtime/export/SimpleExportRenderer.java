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

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.export.ExportJobHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobState;
import com.nextbreakpoint.nextfractal.core.export.ExportProfile;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import lombok.extern.java.Log;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class SimpleExportRenderer implements ExportRenderer {
	private final ThreadFactory threadFactory;

	private final ExecutorCompletionService<ExportJobHandle> service;

	public SimpleExportRenderer(ThreadFactory threadFactory) {
		this.threadFactory = Objects.requireNonNull(threadFactory);
		service = new ExecutorCompletionService<>(createExecutorService(threadFactory));
	}

	@Override
	public Future<ExportJobHandle> dispatch(ExportJobHandle job, AnimationFrame frame) {
		return service.submit(new ProcessExportJob(job, frame));
	}

	private ExecutorService createExecutorService(ThreadFactory threadFactory) {
		return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
	}

	private ImageComposer createImageComposer(ExportProfile profile, AnimationFrame frame) {
		return Command.of(tryFindFactory(frame.pluginId()))
				.map(plugin -> plugin.createImageComposer(threadFactory, profile.createRenderTile(), false))
				.execute()
				.orElse(null);
	}

	private class ProcessExportJob implements Callable<ExportJobHandle> {
		private final ExportJobHandle job;
		private final AnimationFrame frame;

		public ProcessExportJob(ExportJobHandle job, AnimationFrame frame) {
			this.job = Objects.requireNonNull(job);
			this.frame = Objects.requireNonNull(frame);
			job.setState(ExportJobState.READY);
		}

		@Override
		public ExportJobHandle call() {
			return Command.of(() -> processJob(job))
					.execute()
					.observe()
					.onFailure(this::processError)
					.get()
					.orElse(job);
		}

		private void processError(Throwable e) {
			log.log(Level.WARNING, "Failed to render tile", e);
			job.setState(ExportJobState.FAILED, e);
		}

		private ExportJobHandle processJob(ExportJobHandle job) throws IOException {
			log.fine(job.toString());
			ImageComposer composer = createImageComposer(job.getJob().getProfile(), frame);
			IntBuffer pixels = composer.renderImage(frame.script(), frame.metadata());
			if (composer.isInterrupted()) {
                job.setState(ExportJobState.INTERRUPTED);
            } else {
                job.getJob().writePixels(composer.getSize(), pixels);
                job.setState(ExportJobState.COMPLETED);
            }
            return job;
		}
	}
}
