package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotOptions;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotSession;

import java.util.function.Supplier;

class MandelbrotMetadataDelegate implements MetadataDelegate {
    private final EventBusPublisher publisher;
    private final Supplier<Session> supplier;

    public MandelbrotMetadataDelegate(EventBusPublisher publisher, Supplier<Session> supplier) {
        this.publisher = publisher;
        this.supplier = supplier;
    }

    @Override
    public void onMetadataChanged(Metadata metadata, boolean continuous, boolean appendHistory) {
        final MandelbrotSession newSession = ((MandelbrotSession) supplier.get()).toBuilder().withMetadata((MandelbrotMetadata) metadata).build();
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(continuous).appendToHistory(appendHistory).build());
    }

    @Override
    public Metadata getMetadata() {
        return supplier.get().metadata();
    }

    @Override
    public Session newSession(Metadata metadata) {
        return ((MandelbrotSession) supplier.get()).toBuilder().withMetadata((MandelbrotMetadata) metadata).build();
    }

    @Override
    public boolean hasChanged(Session newSession) {
        return !supplier.get().equals(newSession);
    }

    @Override
    public void updateRenderingContext(RenderingContext renderingContext) {
        final MandelbrotOptions options = ((MandelbrotMetadata) supplier.get().metadata()).getOptions();

        renderingContext.getCanvas("julia").setVisible(options.isShowPreview());
        renderingContext.getCanvas("orbit").setVisible(options.isShowOrbit());
        renderingContext.getCanvas("point").setVisible(options.isShowPoint());
        renderingContext.getCanvas("traps").setVisible(options.isShowTraps());
    }
}
