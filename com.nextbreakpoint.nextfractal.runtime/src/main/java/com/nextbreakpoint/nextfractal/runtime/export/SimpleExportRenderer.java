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

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.export.ExportJobHandle;
import com.nextbreakpoint.nextfractal.core.export.ExportJobState;
import com.nextbreakpoint.nextfractal.core.export.ExportProfile;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class SimpleExportRenderer implements ExportRenderer {
	private final ThreadFactory threadFactory;

	public SimpleExportRenderer(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	@Override
	public ExportJobHandle execute(ExportJobHandle job, AnimationFrame frame) {
		final ExportProfile profile = job.getJob().getProfile();

		try {
			log.log(Level.FINE, "Begin rendering tile {0}", job.toString());

			job.setState(ExportJobState.READY);

			final ImageComposer composer = createImageComposer(profile, frame);

			final IntBuffer pixels = composer.renderImage(frame.script(), frame.metadata());

			if (composer.isAborted()) {
				job.setState(ExportJobState.INTERRUPTED);
			} else {
				job.getJob().writePixels(composer.getSize(), pixels);
				job.setState(ExportJobState.COMPLETED);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Cannot render tile", e);
			job.setState(ExportJobState.FAILED);
		}

		log.log(Level.FINE, "End rendering tile {0}", job.toString());

		return job;
	}

	private ImageComposer createImageComposer(ExportProfile profile, AnimationFrame frame) {
		return Command.of(tryFindFactory(frame.pluginId()))
				.map(plugin -> plugin.createImageComposer(threadFactory, profile.createRenderTile(), false))
				.execute()
				.orElse(null);
	}
}
