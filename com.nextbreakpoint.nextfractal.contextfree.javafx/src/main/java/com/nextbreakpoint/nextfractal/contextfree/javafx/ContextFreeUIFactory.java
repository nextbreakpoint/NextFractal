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
package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Coordinator;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeParamsStrategy;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeParserStrategy;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeSession;
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
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

public class ContextFreeUIFactory implements UIFactory {
	public static final String PLUGIN_ID = "ContextFree";

	@Override
	public String getId() {
		return PLUGIN_ID;
	}

	@Override
	public ImageRenderer createImageRenderer(ImageDescriptor descriptor, RendererDelegate delegate) {
		final ContextFreeSession session = (ContextFreeSession) descriptor.getSession();
		final ContextFreeMetadata metadata = (ContextFreeMetadata) session.metadata();
		final Map<String, Integer> hints = new HashMap<>();
		final Tile tile = GraphicsUtils.createTile(descriptor.getWidth(), descriptor.getHeight());
		final ThreadFactory threadFactory = ThreadUtils.createPlatformThreadFactory("ContextFree Browser");
		final GraphicsFactory graphicsFactory = GraphicsUtils.findGraphicsFactory("JavaFX");
		final Coordinator coordinator = new Coordinator(threadFactory, graphicsFactory, tile, hints);
		final CFDGImage cfdgImage = (CFDGImage)descriptor.getProperty("image");
		coordinator.setImage(cfdgImage, metadata.getSeed());
		coordinator.setDelegate(delegate);
		coordinator.init();
		return new RendererAdapter(coordinator);
	}

	@Override
	public ImageDescriptor createImageDescriptor(Session session, Size size) throws Exception {
		final CFParser compiler = new CFParser();
		final CFParserResult report = compiler.parse(session.script());
		final ImageDescriptor descriptor = new ImageDescriptor(session, size.width(), size.height());
		descriptor.setProperty("image", report.classFactory().create());
		return descriptor;
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
}
