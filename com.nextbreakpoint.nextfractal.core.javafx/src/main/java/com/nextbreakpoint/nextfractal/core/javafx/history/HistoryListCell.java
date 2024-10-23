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
package com.nextbreakpoint.nextfractal.core.javafx.history;

import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.RenderedImage;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Affine;

import java.text.SimpleDateFormat;

public class HistoryListCell extends ListCell<RenderedImage> {
    private final BorderPane pane;
    private final Canvas canvas;
    private final Label label;
    private final Tile tile;

    public HistoryListCell(Tile tile) {
        this.tile = tile;
        canvas = new Canvas(tile.tileSize().width(), tile.tileSize().height());
        label = new Label();
        label.getStyleClass().add("text-small");
        pane = new BorderPane();
        final VBox image = new VBox(4);
        image.setAlignment(Pos.CENTER);
        image.getChildren().add(canvas);
        pane.setLeft(image);
        final VBox labels = new VBox(4);
        labels.setAlignment(Pos.CENTER_RIGHT);
        labels.getChildren().add(label);
        pane.setCenter(labels);
    }

    @Override
    public void updateItem(RenderedImage bitmap, boolean empty) {
        super.updateItem(bitmap, empty);
        if (empty) {
            setGraphic(null);
        } else {
            if (bitmap.getPixels() != null) {
                final WritableImage image = new WritableImage(bitmap.getWidth(), bitmap.getHeight());
                image.getPixelWriter().setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), PixelFormat.getIntArgbInstance(), bitmap.getPixels(), (int) image.getWidth());
                final GraphicsContext g2d = canvas.getGraphicsContext2D();
                final Affine affine = new Affine();
                final int x = (tile.tileSize().width() - bitmap.getWidth()) / 2;
                final int y = (tile.tileSize().height() - bitmap.getHeight()) / 2;
                affine.append(Affine.translate(0, +image.getHeight() / 2 + y));
                affine.append(Affine.scale(1, -1));
                affine.append(Affine.translate(0, -image.getHeight() / 2 - y));
                g2d.setTransform(affine);
                g2d.drawImage(image, x, y);
            }
            final SimpleDateFormat df2 = new SimpleDateFormat("HH:mm:ss");
            label.setText(df2.format(bitmap.getTimestamp()));
            this.setGraphic(pane);
        }
    }
}
