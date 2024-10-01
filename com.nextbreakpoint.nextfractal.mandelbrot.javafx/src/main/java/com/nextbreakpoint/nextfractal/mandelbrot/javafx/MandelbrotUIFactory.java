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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.RendererDelegate;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.ImageDescriptor;
import com.nextbreakpoint.nextfractal.core.javafx.ImageRenderer;
import com.nextbreakpoint.nextfractal.core.javafx.KeyHandler;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingStrategy;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;
import com.nextbreakpoint.nextfractal.core.javafx.UIFactory;
import com.nextbreakpoint.nextfractal.core.javafx.viewer.Toolbar;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Coordinator;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.View;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotParamsStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotParserStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotSession;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_RENDERING_COLS;
import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_RENDERING_ROWS;

public class MandelbrotUIFactory implements UIFactory {
	public static final String PLUGIN_ID = "Mandelbrot";

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public ImageRenderer createImageRenderer(ImageDescriptor descriptor, RendererDelegate delegate) {
		final MandelbrotSession session = (MandelbrotSession) descriptor.getSession();
		final MandelbrotMetadata metadata = (MandelbrotMetadata) session.metadata();
		final Map<String, Integer> hints = new HashMap<>();
		hints.put(Coordinator.KEY_TYPE, Coordinator.VALUE_REALTIME);
		hints.put(Coordinator.KEY_MULTITHREAD, Coordinator.VALUE_SINGLE_THREAD);
		final Tile tile = GraphicsUtils.createTile(descriptor.getWidth(), descriptor.getHeight());
		final ThreadFactory threadFactory = ThreadUtils.createPlatformThreadFactory("Mandelbrot Browser");
		final GraphicsFactory graphicsFactory = GraphicsUtils.findGraphicsFactory("JavaFX");
		final Coordinator coordinator = new Coordinator(threadFactory, graphicsFactory, tile, hints);
		final Orbit orbit = (Orbit)descriptor.getProperty("orbit");
		final Color color = (Color)descriptor.getProperty("color");
		coordinator.setOrbitAndColor(orbit, color);
		coordinator.setDelegate(delegate);
		coordinator.init();
		final View view = new View();
		view.setTranslation(metadata.getTranslation());
		view.setRotation(metadata.getRotation());
		view.setScale(metadata.getScale());
		view.setState(new Integer4D(0, 0, 0, 0));
		view.setPoint(new ComplexNumber(metadata.getPoint().x(), metadata.getPoint().y()));
		view.setJulia(metadata.isJulia());
		coordinator.setView(view);
		return new RendererAdapter(coordinator);
	}

	@Override
	public ImageDescriptor createImageDescriptor(Session session, Size size) throws Exception {
		final DSLParser parser = new DSLParser(getPackageName(), getClassName());
		final DSLParserResult parserResult = parser.parse(session.script());
		final Orbit orbit = parserResult.orbitClassFactory().create();
		final Color color = parserResult.colorClassFactory().create();
		final ImageDescriptor descriptor = new ImageDescriptor(session, size.width(), size.height());
		descriptor.setProperty("orbit", orbit);
		descriptor.setProperty("color", color);
		return descriptor;
	}

	@Override
	public Either<String> loadResource(String resourceName) {
		return Command.of(() -> Objects.requireNonNull(getClass().getResource(resourceName)).toExternalForm()).execute();
	}

	@Override
	public ParserStrategy createParserStrategy() {
		return new MandelbrotParserStrategy();
	}

	@Override
	public ParamsStrategy createParamsStrategy() {
		return new MandelbrotParamsStrategy();
	}

	@Override
	public RenderingContext createRenderingContext() {
		final RenderingContext renderingContext = new RenderingContext();
		renderingContext.setZoomSpeed(1.025);
		return renderingContext;
	}

	@Override
	public MetadataDelegate createMetadataDelegate(EventBusPublisher publisher, Supplier<Session> supplier) {
		return new MandelbrotMetadataDelegate(publisher, supplier);
	}

	@Override
	public RenderingStrategy createRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height) {
		final int[] cells = optimalRowsAndCols(width, height, Runtime.getRuntime().availableProcessors());
		return new MandelbrotRenderingStrategy(renderingContext, delegate, width, height, getRows(cells), getCols(cells));
	}

	@Override
	public KeyHandler createKeyHandler(RenderingContext renderingContext, MetadataDelegate delegate) {
		return new MandelbrotKeyHandler(renderingContext, delegate);
	}

	@Override
	public Pane createRenderingPanel(RenderingContext renderingContext, int width, int height) {
		return new MandelbrotRenderingPanel(renderingContext, width, height);
	}

	@Override
	public Toolbar createToolbar(EventBusPublisher publisher, MetadataDelegate delegate, ToolContext<? extends Metadata> toolContext) {
		return new MandelbrotToolbar(delegate, publisher, (MandelbrotToolContext) toolContext);
	}

	@Override
	public ToolContext<? extends Metadata> createToolContext(RenderingContext renderingContext, RenderingStrategy renderingStrategy, MetadataDelegate delegate, int width, int height) {
		return new MandelbrotToolContext(renderingContext, (MandelbrotRenderingStrategy) renderingStrategy, delegate, width, height);
	}

	private static Integer getRows(int[] cells) {
		return Integer.getInteger(PROPERTY_MANDELBROT_RENDERING_ROWS, cells[0]);
	}

	private static Integer getCols(int[] cells) {
		return Integer.getInteger(PROPERTY_MANDELBROT_RENDERING_COLS, cells[1]);
	}

	private static int[] optimalRowsAndCols(int width, int height, int processors) {
		final int nRows = width / 512;
		final int nCols = height / 512;
		if (processors >= 16) {
			return new int[]{Math.min(3, nRows), Math.min(3, nCols)};
		} else if (processors >= 8) {
			return new int[] { Math.min(2, nRows), Math.min(2, nCols) };
		} else {
			return new int[] { 1, 1 };
		}
	}

	private static class RendererAdapter implements ImageRenderer {
		private final Coordinator coordinator;

		public RendererAdapter(Coordinator coordinator) {
			this.coordinator = coordinator;
		}

		@Override
		public void run() {
			coordinator.run();
		}

		@Override
		public void abort() {
			coordinator.abort();
		}

		@Override
		public void waitFor() {
			coordinator.waitFor();
		}

		@Override
		public void dispose() {
			coordinator.dispose();
		}

		@Override
		public boolean hasImageChanged() {
			return coordinator.hasImageChanged();
		}

		@Override
		public void drawImage(GraphicsContext gc, int x, int y) {
			coordinator.drawImage(gc, x, y);
		}

		@Override
		public boolean isInterrupted() {
			return coordinator.isInterrupted();
		}

		@Override
		public boolean isCompleted() {
			return coordinator.getProgress() == 1;
		}
	}

	private String getClassName() {
		return "C" + System.nanoTime();
	}

	private String getPackageName() {
		return DSLParser.class.getPackage().getName() + ".generated";
	}
}
