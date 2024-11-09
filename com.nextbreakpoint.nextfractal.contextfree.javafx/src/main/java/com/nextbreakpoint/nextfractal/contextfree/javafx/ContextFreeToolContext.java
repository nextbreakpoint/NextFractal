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

import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;

public class ContextFreeToolContext implements ToolContext<ContextFreeMetadata> {
    private final RenderingContext renderingContext;
    private final ContextFreeRenderingStrategy renderingStrategy;
    private final MetadataDelegate delegate;
    private final int width;
    private final int height;

    public ContextFreeToolContext(RenderingContext renderingContext, ContextFreeRenderingStrategy renderingStrategy, MetadataDelegate delegate, int width, int height) {
        this.renderingContext = renderingContext;
        this.renderingStrategy = renderingStrategy;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public GraphicsFactory getGraphicsFactory() {
        return renderingStrategy.getGraphicsFactory();
    }

    @Override
    public ContextFreeMetadata getMetadata() {
        return (ContextFreeMetadata) delegate.getMetadata();
    }

    @Override
    public void setPoint(ContextFreeMetadata metadata, boolean continuous, boolean appendHistory) {
    }

    @Override
    public void setTime(ContextFreeMetadata metadata, boolean continuous, boolean appendHistory) {
    }

    @Override
    public void setView(ContextFreeMetadata metadata, boolean continuous, boolean appendHistory) {
    }
}
