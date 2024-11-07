/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.core.javafx.misc;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class TabPane extends Pane {
    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<>() {
        @Override
        protected void invalidated() {
            setEventHandler(ActionEvent.ACTION, get());
        }

        @Override
        public Object getBean() {
            return TabPane.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    private boolean entered;

    public TabPane(ImageView image) {
        final Canvas canvas = new Canvas();
        getChildren().add(canvas);
        getChildren().add(image);

        widthProperty().addListener((_, _, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            canvas.setHeight(getHeight());
            image.setFitWidth(newValue.doubleValue() / 3);
            image.setLayoutX(newValue.doubleValue() / 3);
            redraw(canvas);
        });

        heightProperty().addListener((_, _, newValue) -> {
            canvas.setWidth(getWidth());
            canvas.setHeight(newValue.doubleValue());
            image.setFitHeight(newValue.doubleValue() * 2 / 3);
            image.setLayoutY(newValue.doubleValue() * 0.1);
            redraw(canvas);
        });

        addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            entered = true;
            redraw(canvas);
            e.consume();
        });

        addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            entered = false;
            redraw(canvas);
            e.consume();
        });

        addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            onActionProperty().getValue().handle(new ActionEvent(TabPane.this, null));
            e.consume();
        });
    }

    private void redraw(Canvas canvas) {
        final double width = getWidth();
        final double height = getHeight();
        final GraphicsContext g2d = canvas.getGraphicsContext2D();
        g2d.clearRect(0, 0, width, height);
        g2d.setFill(new Color(1, 1, 1, entered ? 0.9 : 0.7));
        g2d.fillOval(0, -height, width, height * 2);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set(value);
    }
}
