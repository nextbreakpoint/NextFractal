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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.javafx.graphics.internal.JavaFXGraphicsFactory;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;

public class ProgressBar extends BorderPane {
    private final JavaFXGraphicsFactory factory;
    private final Canvas canvas;
    private float progress;

    public ProgressBar() {
        factory = new JavaFXGraphicsFactory();
        canvas = new Canvas(0, 0);
        setCenter(canvas);
        setMinSize(16, 16);
        getStyleClass().add("progress-bar");
        widthProperty().addListener((_, _, _) -> onSizeChanged());
        heightProperty().addListener((_, _, _) -> onSizeChanged());
        insetsProperty().addListener((_, _, _) -> onSizeChanged());
    }

    private void onSizeChanged() {
        final double innerWidth = getInnerWidth();
        final double innerHeight = getInnerHeight();
        if (innerWidth > 0 && innerHeight > 0) {
            if (innerWidth != canvas.getWidth() || innerHeight != canvas.getHeight()) {
                canvas.setWidth(innerWidth);
                canvas.setHeight(innerHeight);
            }
        }
    }

    protected double getInnerWidth() {
        return getWidth() - getInsets().getLeft() - getInsets().getRight();
    }

    protected double getInnerHeight() {
        return getHeight() - getInsets().getTop() - getInsets().getBottom();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        update();
    }

    public void update() {
        final var g2d = canvas.getGraphicsContext2D();
        final var gc = factory.createGraphicsContext(g2d);
        gc.clearRect(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight());
        gc.beginPath();
        gc.moveTo(8, (float) canvas.getHeight() / 2);
        gc.lineTo((float) canvas.getWidth() - 8, (float) canvas.getHeight() / 2);
        gc.setStrokeLine(2, GraphicsContext.CAP_ROUND, GraphicsContext.JOIN_ROUND, 0.2f);
        gc.setStroke(factory.createColor(0, 0, 0, 1));
        gc.stroke();
        gc.beginPath();
        gc.moveTo(8, (float) canvas.getHeight() / 2);
        gc.lineTo((float) canvas.getWidth() * progress - 8, (float) canvas.getHeight() / 2);
        gc.setStrokeLine(4, GraphicsContext.CAP_ROUND, GraphicsContext.JOIN_ROUND, 0.2f);
        gc.setStroke(factory.createColor(0, 0.9, 0.6, 1));
        gc.stroke();
    }
}
