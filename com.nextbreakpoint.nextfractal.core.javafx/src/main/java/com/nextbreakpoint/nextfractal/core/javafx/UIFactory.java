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

import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridItemRenderer;
import com.nextbreakpoint.nextfractal.core.javafx.viewer.Toolbar;
import javafx.scene.layout.Pane;

import java.util.function.Supplier;

public interface UIFactory {
	String getId();

	GridItemRenderer createRenderer(Bitmap bitmap) throws Exception;

	Bitmap createBitmap(Session session, Size size) throws Exception;

	Either<String> loadResource(String resourceName);

	ParserStrategy createParserStrategy();

	ParamsStrategy createParamsStrategy();

	RenderingContext createRenderingContext();

	MetadataDelegate createMetadataDelegate(EventBusPublisher publisher, Supplier<Session> supplier);

	RenderingStrategy createRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height);

	KeyHandler createKeyHandler(RenderingContext renderingContext, MetadataDelegate delegate);

	Pane createRenderingPanel(RenderingContext renderingContext, int width, int height);

	Toolbar createToolbar(EventBusPublisher publisher, MetadataDelegate delegate, ToolContext<? extends Metadata> toolContext);

	ToolContext<? extends Metadata> createToolContext(RenderingContext renderingContext, RenderingStrategy renderingStrategy, MetadataDelegate delegate, int width, int height);
}
