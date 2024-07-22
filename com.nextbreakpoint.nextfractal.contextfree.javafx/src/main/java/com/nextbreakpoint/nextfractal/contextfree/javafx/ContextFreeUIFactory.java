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
package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeParamsStrategy;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeParserStrategy;
import com.nextbreakpoint.nextfractal.contextfree.renderer.RendererCoordinator;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.TileUtils;
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
import com.nextbreakpoint.nextfractal.core.javafx.render.JavaFXRendererFactory;
import com.nextbreakpoint.nextfractal.core.javafx.viewer.Toolbar;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ContextFreeUIFactory implements UIFactory {
	public static final String PLUGIN_ID = "ContextFree";

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public GridItemRenderer createRenderer(Bitmap bitmap) {
		Map<String, Integer> hints = new HashMap<String, Integer>();
		RendererTile tile = TileUtils.createRendererTile(bitmap.getWidth(), bitmap.getHeight());
		DefaultThreadFactory threadFactory = new DefaultThreadFactory("ContextFree Browser", true, Thread.MIN_PRIORITY);
		RendererCoordinator coordinator = new RendererCoordinator(threadFactory, new JavaFXRendererFactory(), tile, hints);
		CFDG cfdg = (CFDG)bitmap.getProperty("cfdg");
		Session session = (Session)bitmap.getProperty("session");
		coordinator.setInterpreter(new CFDGInterpreter(cfdg));
		coordinator.setSeed(((ContextFreeMetadata)session.getMetadata()).getSeed());
		coordinator.init();
		coordinator.run();
		return new GridItemRendererAdapter(coordinator);
	}

	@Override
	public BrowseBitmap createBitmap(Session session, RendererSize size) throws Exception {
		DSLParser compiler = new DSLParser();
		DSLParserResult report = compiler.parse(session.getScript());
		if (!report.getErrors().isEmpty()) {
			throw new RuntimeException("Failed to compile source");
		}
		BrowseBitmap bitmap = new BrowseBitmap(size.getWidth(), size.getHeight(), null);
		bitmap.setProperty("cfdg", report.getCFDG());
		bitmap.setProperty("session", session);
		return bitmap;
	}

	@Override
	public Either<String> loadResource(String resourceName) {
		return Command.of(() -> Objects.requireNonNull(getClass().getResource(resourceName)).toExternalForm()).execute();
	}

	@Override
	public ParserStrategy createParserStrategy() {
		return new ContextFreeParserStrategy();
	}

	@Override
	public ParamsStrategy createParamsStrategy() {
		return new ContextFreeParamsStrategy();
	}

	@Override
	public RenderingContext createRenderingContext() {
		final RenderingContext renderingContext = new RenderingContext();
		renderingContext.setZoomSpeed(1.025);
		return renderingContext;
	}

	@Override
	public MetadataDelegate createMetadataDelegate(EventBusPublisher publisher, Supplier<Session> supplier) {
		return new ContextFreeMetadataDelegate(publisher, supplier);
	}

	@Override
	public RenderingStrategy createRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height) {
		return new ContextFreeRenderingStrategy(renderingContext, delegate, width, height, 1, 1);
	}

	@Override
	public KeyHandler createKeyHandler(RenderingContext renderingContext, MetadataDelegate delegate) {
		return new ContextFreeKeyHandler(renderingContext, delegate);
	}

	@Override
	public Pane createRenderingPanel(RenderingContext renderingContext, int width, int height) {
		return new ContextFreeRenderingPanel(renderingContext, width, height);
	}

	@Override
	public Toolbar createToolbar(EventBusPublisher publisher, MetadataDelegate delegate, ToolContext<? extends Metadata> toolContext) {
		return new ContextFreeToolbar(delegate, publisher, (ContextFreeToolContext) toolContext);
	}

	@Override
	public ToolContext<? extends Metadata> createToolContext(RenderingContext renderingContext, RenderingStrategy renderingStrategy, MetadataDelegate delegate, int width, int height) {
		return new ContextFreeToolContext(renderingContext, (ContextFreeRenderingStrategy) renderingStrategy, delegate, width, height);
	}

	private static class GridItemRendererAdapter implements GridItemRenderer {
		private final RendererCoordinator coordinator;

		public GridItemRendererAdapter(RendererCoordinator coordinator) {
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
		public void drawImage(RendererGraphicsContext gc, int x, int y) {
			coordinator.drawImage(gc, x, y);
		}
	}
}
