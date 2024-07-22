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
package com.nextbreakpoint.nextfractal.core.javafx.viewer;

import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.ToolContext;
import javafx.scene.layout.HBox;

public class Toolbar extends HBox {
    protected final MetadataDelegate delegate;
    protected final EventBusPublisher publisher;
    protected final ToolContext<? extends Metadata> toolContext;
    protected final BooleanObservableValue animationProperty;
    protected final BooleanObservableValue captureProperty;

    public Toolbar(MetadataDelegate delegate, EventBusPublisher publisher, ToolContext<? extends Metadata> toolContext) {
        this.delegate = delegate;
        this.publisher = publisher;
        this.toolContext = toolContext;

        animationProperty = new BooleanObservableValue();
        animationProperty.setValue(false);

        captureProperty = new BooleanObservableValue();
        captureProperty.setValue(false);
    }

    public void setCaptureEnabled(boolean value) {
        captureProperty.setValue(value);
    }

    public void setAnimationEnabled(boolean value) {
        animationProperty.setValue(value);
    }

    public void bindSession(Session session) {
    }
}