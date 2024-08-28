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

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Affine;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClipListCell extends ListCell<Bitmap> {
	private final BorderPane pane;
	private final Canvas canvas;
	private final Label label;
	private final Tile tile;
	@Setter
	private ClipListCellDelegate delegate;

	public ClipListCell(Tile tile) {
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

		final ClipListCell thisCell = this;

		setOnDragDetected(event -> {
			if (getItem() == null) {
				return;
			}

			final Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
			dragboard.setDragView(getWritableImage(getItem()));
			final ClipboardContent content = new ClipboardContent();
			content.put(DataFormat.PLAIN_TEXT, getItem().getId().toString());
			dragboard.setContent(content);

			event.consume();
		});

		setOnDragOver(event -> {
			if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});

		setOnDragEntered(event -> {
			if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
				setOpacity(0.3);
			}
		});

		setOnDragExited(event -> {
			if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
				setOpacity(1);
			}
		});

		setOnDragDropped(event -> {
			if (getItem() == null) {
				return;
			}

			final Dragboard db = event.getDragboard();

			boolean success = false;

			if (db.hasString()) {
				final Map<String, Bitmap> itemsMap = getListView().getItems().stream().collect(Collectors.toMap(bitmap -> bitmap.getId().toString(), bitmap -> bitmap));
				final List<String> itemsIds = getListView().getItems().stream().map(bitmap -> bitmap.getId().toString()).collect(Collectors.toList());
				final int draggedIdx = itemsIds.indexOf(db.getString());
				final int thisIdx = itemsIds.indexOf(getItem().getId().toString());
				itemsIds.remove(draggedIdx);
				itemsIds.add(thisIdx, db.getString());
				final List<Bitmap> newItems = new ArrayList();
				itemsIds.forEach(itemId -> newItems.add(itemsMap.get(itemId)));
				getListView().getItems().setAll(newItems);
				success = true;
				if (delegate != null) {
					delegate.clipMoved(draggedIdx, thisIdx);
				}
			}

			event.setDropCompleted(success);

			event.consume();
		});

		setOnDragDone(DragEvent::consume);
	}

	@Override
	public void updateItem(Bitmap bitmap, boolean empty) {
		super.updateItem(bitmap, empty);
		if (empty) {
			setGraphic(null);
		} else {
			if (bitmap.getPixels() != null) {
				final WritableImage image = getWritableImage(bitmap);
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
			final AnimationClip clip = (AnimationClip)bitmap.getProperty("clip");
			final long durationInSeconds = clip.duration() / 1000;
			final long minutes = (long) Math.rint(durationInSeconds / 60.0);
			if (minutes <= 2) {
				label.setText(durationInSeconds == 0 ? clip.duration() + " millis" : durationInSeconds == 1 ? "1 second" : durationInSeconds + " seconds");
			} else {
				label.setText(minutes + " minutes");
			}
			this.setGraphic(pane);
		}
	}

	private WritableImage getWritableImage(Bitmap bitmap) {
		final WritableImage image = new WritableImage(bitmap.getWidth(), bitmap.getHeight());
		image.getPixelWriter().setPixels(0, 0, (int)image.getWidth(), (int)image.getHeight(), PixelFormat.getIntArgbInstance(), bitmap.getPixels(), (int)image.getWidth());
		return image;
	}
}
