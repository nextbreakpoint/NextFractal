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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.RendererDelegate;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@Log
public class ImageLoader {
	@Getter
	private final File file;
	private final Size size;
	private Future<Void> future;
	@Setter
	// the delegate must be configured before invoking run
	private RendererDelegate delegate;
	private volatile ImageRenderer renderer;

	public ImageLoader(File file, Size size) {
		this.file = Objects.requireNonNull(file);
		this.size = Objects.requireNonNull(size);
	}

	public void run(ExecutorService executor) {
		if (future == null) {
			// we load and parse the file in a new thread
			future = executor.submit(this::renderImage);
		}
	}

	public void cancel() {
		if (future != null) {
			future.cancel(true);
		}
	}

	public void waitFor() throws InterruptedException {
		try {
			if (future != null) {
				future.get();
			}
		} catch (CancellationException _) {
		} catch (ExecutionException e) {
			log.log(Level.WARNING, "Can't load image", e);
		}
		future = null;
		renderer = null;
	}

	public void drawImage(GraphicsContext gc, int x, int y) {
		try {
			// please note that the renderer is assigned from another thread
			if (renderer != null) {
				renderer.drawImage(gc, x, y);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't draw image", e);
		}
	}

	// this method is executed in a worker thread
	private Void renderImage() {
		try {
			log.log(Level.INFO, "Start rendering image {0}", file);
			renderer = loadBundle(file)
					.flatMap(bundle -> createImageDescriptor(bundle, size))
					.flatMap(descriptor -> createImageRenderer(descriptor, delegate))
					.execute()
					.orThrow()
					.get();
			renderer.waitFor();
			log.log(Level.INFO, "Finish rendering image {0}", file);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.log(Level.WARNING, "Can't render image", e);
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't render image", e);
		}
		return null;
	}

	private static Command<Bundle> loadBundle(File file) {
		return Command.of(FileManager.loadBundle(file));
	}

	private static Command<ImageDescriptor> createImageDescriptor(Bundle bundle, Size size) {
		return Command.of(tryFindFactory(bundle.session().pluginId()))
				.flatMap(factory -> Command.of(() -> factory.createImageDescriptor(bundle.session(), size)));
	}

	private static Command<ImageRenderer> createImageRenderer(ImageDescriptor descriptor, RendererDelegate delegate) {
		return Command.of(tryFindFactory(descriptor.getSession().pluginId()))
				.flatMap(factory -> Command.of(() -> factory.createImageRenderer(descriptor, delegate)));
	}
}
