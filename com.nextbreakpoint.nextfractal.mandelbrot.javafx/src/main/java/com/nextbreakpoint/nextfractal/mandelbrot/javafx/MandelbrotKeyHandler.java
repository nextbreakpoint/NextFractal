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

import com.nextbreakpoint.nextfractal.core.javafx.KeyHandler;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import javafx.scene.input.KeyEvent;

public class MandelbrotKeyHandler implements KeyHandler {
    private final RenderingContext renderingContext;
    private final MetadataDelegate delegate;

    public MandelbrotKeyHandler(RenderingContext renderingContext, MetadataDelegate delegate) {
        this.renderingContext = renderingContext;
        this.delegate = delegate;
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case DIGIT1: {
                renderingContext.setZoomSpeed(1.005);
                break;
            }
            case DIGIT2: {
                renderingContext.setZoomSpeed(1.01);
                break;
            }
            case DIGIT3: {
                renderingContext.setZoomSpeed(1.025);
                break;
            }
            case DIGIT4: {
                renderingContext.setZoomSpeed(1.05);
                break;
            }
            case DIGIT5: {
                renderingContext.setZoomSpeed(1.10);
                break;
            }
            case T: {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final MandelbrotMetadata newMetadata = metadata.toBuilder().withOptions(metadata.getOptions()
                        .toBuilder().withShowTraps(!metadata.getOptions().isShowTraps()).build()).build();
                delegate.onMetadataChanged(newMetadata, false, true);
                break;
            }
            case O: {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final MandelbrotMetadata newMetadata = metadata.toBuilder().withOptions(metadata.getOptions()
                        .toBuilder().withShowOrbit(!metadata.getOptions().isShowOrbit()).build()).build();
                delegate.onMetadataChanged(newMetadata, false, true);
                break;
            }
            case P: {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final MandelbrotMetadata newMetadata = metadata.toBuilder().withOptions(metadata.getOptions()
                        .toBuilder().withShowPreview(!metadata.getOptions().isShowPreview() && !metadata.isJulia()).build()).build();
                delegate.onMetadataChanged(newMetadata, false, true);
                break;
            }
        }
        keyEvent.consume();
    }
}
