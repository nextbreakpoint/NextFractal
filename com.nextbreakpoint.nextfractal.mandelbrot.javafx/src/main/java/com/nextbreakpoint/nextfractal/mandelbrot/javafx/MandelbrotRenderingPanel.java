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

import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

public class MandelbrotRenderingPanel extends Pane {
    public MandelbrotRenderingPanel(RenderingContext renderingContext, int width, int height) {
        //TODO remove width and height
        final Canvas fractalCanvas = new Canvas(width, height);
        final GraphicsContext gcFractalCanvas = fractalCanvas.getGraphicsContext2D();
        gcFractalCanvas.setFill(javafx.scene.paint.Color.WHITESMOKE);
        gcFractalCanvas.fillRect(0, 0, width, height);

        final Canvas toolCanvas = new Canvas(width, height);
        final GraphicsContext gcToolCanvas = toolCanvas.getGraphicsContext2D();
        gcToolCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcToolCanvas.fillRect(0, 0, width, height);

        final Canvas orbitCanvas = new Canvas(width, height);
        final GraphicsContext gcOrbitCanvas = orbitCanvas.getGraphicsContext2D();
        gcOrbitCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcOrbitCanvas.fillRect(0, 0, width, height);
        orbitCanvas.setVisible(false);

        final Canvas trapCanvas = new Canvas(width, height);
        final GraphicsContext gcTrapCanvas = trapCanvas.getGraphicsContext2D();
        gcTrapCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcTrapCanvas.fillRect(0, 0, width, height);
        trapCanvas.setVisible(false);

        final Canvas pointCanvas = new Canvas(width, height);
        final GraphicsContext gcPointCanvas = pointCanvas.getGraphicsContext2D();
        gcPointCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcPointCanvas.fillRect(0, 0, width, height);
        pointCanvas.setVisible(false);

        final Canvas juliaCanvas = new Canvas(width, height);
        final GraphicsContext gcJuliaCanvas = juliaCanvas.getGraphicsContext2D();
        gcJuliaCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
        gcJuliaCanvas.fillRect(0, 0, width, height);
        juliaCanvas.setOpacity(0.8);
        juliaCanvas.setVisible(false);

        getChildren().add(fractalCanvas);
        getChildren().add(trapCanvas);
        getChildren().add(orbitCanvas);
        getChildren().add(pointCanvas);
        getChildren().add(juliaCanvas);
        getChildren().add(toolCanvas);

        renderingContext.registerCanvas("fractal", fractalCanvas);
        renderingContext.registerCanvas("julia", juliaCanvas);
        renderingContext.registerCanvas("orbit", orbitCanvas);
        renderingContext.registerCanvas("point", pointCanvas);
        renderingContext.registerCanvas("traps", trapCanvas);
        renderingContext.registerCanvas("tool", toolCanvas);
    }
}
