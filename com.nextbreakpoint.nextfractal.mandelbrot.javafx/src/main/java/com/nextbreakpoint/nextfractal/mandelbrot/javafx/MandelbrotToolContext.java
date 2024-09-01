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

import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;

public class MandelbrotToolContext implements ToolContext<MandelbrotMetadata> {
    private final MandelbrotRenderingStrategy renderingStrategy;
    private final RenderingContext renderingContext;
    private final int width;
    private final int height;
    private final MetadataDelegate delegate;

    public MandelbrotToolContext(RenderingContext renderingContext, MandelbrotRenderingStrategy renderingStrategy, MetadataDelegate delegate, int width, int height) {
        this.renderingStrategy = renderingStrategy;
        this.renderingContext = renderingContext;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
    }

    public ComplexNumber getInitialSize() {
        return renderingStrategy.getInitialSize();
    }

    public ComplexNumber getInitialCenter() {
        return renderingStrategy.getInitialCenter();
    }

    public double getZoomSpeed() {
        return renderingContext.getZoomSpeed();
    }

    public void setZoomSpeed(double zoomSpeed) {
        renderingContext.setZoomSpeed(zoomSpeed);
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
    public MandelbrotMetadata getMetadata() {
        return (MandelbrotMetadata) delegate.getMetadata();
    }

    @Override
    public void setPoint(MandelbrotMetadata metadata, boolean continuous, boolean appendHistory) {
        delegate.onMetadataChanged(metadata, continuous, appendHistory);
    }

    @Override
    public void setView(MandelbrotMetadata metadata, boolean continuous, boolean appendHistory) {
        delegate.onMetadataChanged(metadata, continuous, appendHistory);
    }

    @Override
    public void setTime(MandelbrotMetadata metadata, boolean continuous, boolean appendHistory) {
        delegate.onMetadataChanged(metadata, continuous, appendHistory);
    }
}
