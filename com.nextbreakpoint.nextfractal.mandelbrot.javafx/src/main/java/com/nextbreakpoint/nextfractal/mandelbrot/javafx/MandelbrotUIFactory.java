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
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.Bitmap;
import com.nextbreakpoint.nextfractal.core.javafx.BrowseBitmap;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.GridItemRenderer;
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
import java.util.function.Supplier;

public class MandelbrotUIFactory implements UIFactory {
	public static final String PLUGIN_ID = "Mandelbrot";

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public GridItemRenderer createRenderer(Bitmap bitmap) {
		final MandelbrotSession session = (MandelbrotSession)bitmap.getProperty("session");
		final Map<String, Integer> hints = new HashMap<>();
		hints.put(Coordinator.KEY_TYPE, Coordinator.VALUE_REALTIME);
		hints.put(Coordinator.KEY_MULTITHREAD, Coordinator.VALUE_SINGLE_THREAD);
		final Tile tile = GraphicsUtils.createTile(bitmap.getWidth(), bitmap.getHeight());
		final DefaultThreadFactory threadFactory = new DefaultThreadFactory("Mandelbrot Browser", true, Thread.MIN_PRIORITY);
		final GraphicsFactory graphicsFactory = GraphicsUtils.findGraphicsFactory("JavaFX");
		final Coordinator coordinator = new Coordinator(threadFactory, graphicsFactory, tile, hints);
		final Orbit orbit = (Orbit)bitmap.getProperty("orbit");
		final Color color = (Color)bitmap.getProperty("color");
		coordinator.setOrbitAndColor(orbit, color);
		coordinator.init();
		final MandelbrotMetadata data = (MandelbrotMetadata) session.metadata();
		final View view = new View();
		view.setTranslation(data.getTranslation());
		view.setRotation(data.getRotation());
		view.setScale(data.getScale());
		view.setState(new Integer4D(0, 0, 0, 0));
		view.setPoint(new ComplexNumber(data.getPoint().x(), data.getPoint().y()));
		view.setJulia(data.isJulia());
		coordinator.setView(view);
		coordinator.run();
		return new GridItemRendererAdapter(coordinator);
	}

	@Override
	public BrowseBitmap createBitmap(Session session, Size size) throws Exception {
		final DSLParser parser = new DSLParser(getPackageName(), getClassName());
		final DSLParserResult parserResult = parser.parse(session.script());
		final Orbit orbit = parserResult.orbitClassFactory().create();
		final Color color = parserResult.colorClassFactory().create();
		final BrowseBitmap bitmap = new BrowseBitmap(size.width(), size.height(), null);
		bitmap.setProperty("orbit", orbit);
		bitmap.setProperty("color", color);
		bitmap.setProperty("session", session);
		return bitmap;
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
		final int[] cells = optimalRowsAndCols(Runtime.getRuntime().availableProcessors());

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
		return Integer.getInteger("mandelbrot.renderer.rows", cells[0]);
	}

	private static Integer getCols(int[] cells) {
		return Integer.getInteger("mandelbrot.renderer.cols", cells[1]);
	}

	private static int[] optimalRowsAndCols(int processors) {
		if (processors > 8) {
			return new int[] { 3, 3 };
		} else if (processors >= 4) {
			return new int[] { 2, 2 };
		} else {
			return new int[] { 1, 1 };
		}
	}

	private static class GridItemRendererAdapter implements GridItemRenderer {
		private final Coordinator coordinator;

		public GridItemRendererAdapter(Coordinator coordinator) {
			this.coordinator = coordinator;
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
		public boolean isPixelsChanged() {
			return coordinator.isPixelsChanged();
		}

		@Override
		public void drawImage(GraphicsContext gc, int x, int y) {
			coordinator.drawImage(gc, x, y);
		}
	}

	private String getClassName() {
		return "C" + System.nanoTime();
	}

	private String getPackageName() {
		return DSLParser.class.getPackage().getName() + ".generated";
	}
}
