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

import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeSession;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Supplier;

public class ContextFreeMetadataDelegate implements MetadataDelegate {
    private final EventBusPublisher publisher;
    private final Supplier<Session> supplier;

    public ContextFreeMetadataDelegate(EventBusPublisher publisher, Supplier<Session> supplier) {
        this.publisher = publisher;
        this.supplier = supplier;
    }

    @Override
    public void onMetadataChanged(Metadata metadata, boolean continuous, boolean appendHistory) {
        final ContextFreeSession newSession = ((ContextFreeSession) supplier.get()).toBuilder().withTimestamp(Instant.now(Clock.systemUTC())).withMetadata((ContextFreeMetadata) metadata).build();
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(continuous).appendToHistory(appendHistory).build());
    }

    @Override
    public Metadata getMetadata() {
        return supplier.get().metadata();
    }

    @Override
    public Session newSession(Metadata metadata) {
        return ((ContextFreeSession) supplier.get()).toBuilder().withTimestamp(Instant.now(Clock.systemUTC())).withMetadata((ContextFreeMetadata) metadata).build();
    }

    @Override
    public boolean hasChanged(Session newSession) {
        return !supplier.get().equals(newSession);
    }

    @Override
    public void updateRenderingContext(RenderingContext renderingContext) {
    }
}
