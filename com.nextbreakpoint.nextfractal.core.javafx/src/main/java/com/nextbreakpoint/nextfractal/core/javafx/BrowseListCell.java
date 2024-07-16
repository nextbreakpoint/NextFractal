/*
 * NextFractal 2.3.0
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

import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Affine;

public class BrowseListCell extends ListCell<Bitmap[]> {
	private HBox pane;
	private Canvas[] canvas;
	private RendererSize size;
	private RendererTile tile;
	private int numOfColumns;

	public BrowseListCell(int numOfColumns, RendererSize size, RendererTile tile) {
		this.size = size;
		this.tile = tile;
		this.numOfColumns = numOfColumns;
		pane = new HBox(0);
		canvas = new Canvas[numOfColumns];
		for (int i = 0 ; i < numOfColumns; i++) {
			canvas[i] = new Canvas(tile.getTileSize().getWidth(), tile.getTileSize().getHeight());
			pane.getChildren().add(canvas[i]);
		}
	}

	@Override
	public void updateItem(Bitmap[] bitmaps, boolean empty) {
		super.updateItem(bitmaps, empty);
		if (empty) {
			setGraphic(null);
		} else {
			for (int i = 0 ; i < numOfColumns; i++) {
				Bitmap bitmap = bitmaps[i];
				if (bitmap != null && bitmap.getPixels() != null) {
					renderFractal(canvas[i].getGraphicsContext2D(), bitmap);
				}
			}
			this.setGraphic(pane);
		}
	}

	private void renderFractal(GraphicsContext g2d, Bitmap bitmap) {
		WritableImage image = new WritableImage(size.getWidth(), size.getHeight());
		image.getPixelWriter().setPixels(0, 0, (int)image.getWidth(), (int)image.getHeight(), PixelFormat.getIntArgbInstance(), bitmap.getPixels(), (int)image.getWidth());
		Affine affine = new Affine();
		int x = (tile.getTileSize().getWidth() - size.getWidth()) / 2;
		int y = (tile.getTileSize().getHeight() - size.getHeight()) / 2;
		affine.append(Affine.translate(0, +image.getHeight() / 2 + y));
		affine.append(Affine.scale(1, -1));
		affine.append(Affine.translate(0, -image.getHeight() / 2 - y));
		g2d.setTransform(affine);
		g2d.drawImage(image, x, y);
	}
}
