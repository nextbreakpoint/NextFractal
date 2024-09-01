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
