package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
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

    public Number getInitialSize() {
        return renderingStrategy.getInitialSize();
    }

    public Number getInitialCenter() {
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
    public GraphicsFactory getRendererFactory() {
        return renderingStrategy.getRenderFactory();
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
