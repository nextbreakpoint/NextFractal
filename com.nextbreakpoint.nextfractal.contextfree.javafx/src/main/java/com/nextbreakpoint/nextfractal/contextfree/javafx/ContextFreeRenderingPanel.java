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

import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class ContextFreeRenderingPanel extends Pane {
    public ContextFreeRenderingPanel(RenderingContext renderingContext, int width, int height) {
        final Canvas fractalCanvas = new Canvas(width, height);
        final GraphicsContext gcFractalCanvas = fractalCanvas.getGraphicsContext2D();
        gcFractalCanvas.setFill(javafx.scene.paint.Color.WHITESMOKE);
        gcFractalCanvas.fillRect(0, 0, width, height);

        final Canvas toolCanvas = new Canvas(width, height);
        final GraphicsContext gcToolCanvas = toolCanvas.getGraphicsContext2D();
        gcToolCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcToolCanvas.fillRect(0, 0, width, height);

        getChildren().add(fractalCanvas);
        getChildren().add(toolCanvas);

        renderingContext.registerCanvas("fractal", fractalCanvas);
        renderingContext.registerCanvas("tool", toolCanvas);
    }
}
