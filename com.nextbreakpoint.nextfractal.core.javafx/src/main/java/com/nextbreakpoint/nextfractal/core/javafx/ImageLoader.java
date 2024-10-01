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
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@Log
public class ImageLoader {
	private final ExecutorService executor;
	private Future<Void> future;
	@Getter
	private final File file;
	@Getter
	private final Size size;
	@Setter
	private volatile RendererDelegate delegate;
	private volatile ImageRenderer renderer;

	public ImageLoader(ExecutorService executor, File file, Size size) {
		this.executor = Objects.requireNonNull(executor);
		this.file = Objects.requireNonNull(file);
		this.size = Objects.requireNonNull(size);
	}

	public void run() {
		if (future == null) {
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
		} finally {
	        future = null;
			renderer = null;
		}
	}

	public void drawImage(GraphicsContext gc, int x, int y) {
		if (renderer != null) {
			renderer.drawImage(gc, x, y);
		} else {
			gc.clearRect(0, 0, size.width(), size.height());
		}
	}

	// this method is executed in a worker thread
	private Void renderImage() {
		try {
			log.log(Level.INFO, "Start rendering image {0}", file);
			renderer = loadBundle(file)
					.flatMap(bundle -> createImageDescriptor(bundle, size))
					.flatMap(descriptor -> createImageRenderer(descriptor, this::onImageUpdated))
					.execute()
					.orThrow()
					.get();
			renderer.run();
			renderer.waitFor();
			if (renderer.isCompleted()) {
				log.log(Level.INFO, "Finish rendering image {0}", file);
			} else {
				log.log(Level.INFO, "Abort rendering image {0}", file);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (Exception _) {
			// TODO ideally we want to ignore only the parse exceptions caused by thread interruption
		}
		return null;
	}

	// this method is executed in a worker thread
	private void onImageUpdated(float progress, List<ScriptError> errors) {
		Platform.runLater(() -> {
			if (delegate != null) {
				delegate.onImageUpdated(progress, errors);
			}
		});
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

	public void dump() {
		if (renderer != null) {
			log.info("completed " + renderer.isCompleted());
		} else {
			log.info("not completed");
		}
	}
}
