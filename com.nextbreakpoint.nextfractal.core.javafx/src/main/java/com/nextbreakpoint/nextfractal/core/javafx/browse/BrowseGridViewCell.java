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
package com.nextbreakpoint.nextfractal.core.javafx.browse;

import com.nextbreakpoint.nextfractal.core.javafx.graphics.internal.JavaFXGraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewCell;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class BrowseGridViewCell extends GridViewCell {
	private final JavaFXGraphicsFactory factory;
	private final Canvas canvas;

	public BrowseGridViewCell(int index, int width, int height) {
		super(index, width, height);
		factory = new JavaFXGraphicsFactory();
		canvas = new Canvas(width, height);
		setCenter(canvas);
	}

	@Override
	public void unbindItem() {
		if (item != null) {
			item.setDelegate(null);
		}
		super.unbindItem();
	}

	@Override
	public void bindItem(GridViewItem item) {
		super.bindItem(item);
		if (item != null) {
			item.setDelegate(this::update);
		}
	}

	@Override
	public void update() {
		if (item != null && item instanceof BrowseGridViewItem browseItem) {
			final var g2d = canvas.getGraphicsContext2D();
			final var gc = factory.createGraphicsContext(g2d);
			g2d.setFill(Color.WHITE);
			browseItem.drawImage(gc, 0, 0);
			drawMessage(browseItem);
			drawOverlay(browseItem);
		} else {
			clearCanvas();
		}
	}

	private void clearCanvas() {
		final var g2d = canvas.getGraphicsContext2D();
		g2d.setFill(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	}

	private void drawOverlay(BrowseGridViewItem browseItem) {
		if (browseItem.isSelected()) {
			final var g2d = canvas.getGraphicsContext2D();
			g2d.setStroke(Color.YELLOW);
			g2d.setLineWidth(5);
			g2d.strokeRect(0, 0, getWidth(), getHeight());
		}
	}

	private void drawMessage(BrowseGridViewItem browseItem) {
		final String message = getMessage(browseItem);
        if (message != null) {
			final var g2d = canvas.getGraphicsContext2D();
			g2d.setFill(Color.DARKGRAY);
			g2d.setTextAlign(TextAlignment.CENTER);
			g2d.fillText(message, getWidth() / 2, getHeight() / 2);
		}
	}

	private String getMessage(BrowseGridViewItem browseItem) {
		if (browseItem.hasErrors()) {
			return "Error";
		} else if (!browseItem.isCompleted()) {
			return "Rendering...";
		} else {
			return null;
		}
	}
}
