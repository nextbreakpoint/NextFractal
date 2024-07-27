package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;

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
    public GraphicsFactory getRendererFactory() {
        return renderingStrategy.getRenderFactory();
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
