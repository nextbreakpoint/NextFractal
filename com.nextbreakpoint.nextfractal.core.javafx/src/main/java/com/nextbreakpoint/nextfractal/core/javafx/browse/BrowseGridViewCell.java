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

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.javafx.graphics.internal.JavaFXGraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewCell;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItemDelegate;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class BrowseGridViewCell extends GridViewCell implements GridViewItemDelegate {
	private final JavaFXGraphicsFactory factory;
	private final Canvas canvas;
	private final Label label;

	public BrowseGridViewCell(int index, int width, int height) {
		super(index, width, height);
		factory = new JavaFXGraphicsFactory();
		canvas = new Canvas(width, height);
		label = new Label();
		label.setBackground(Background.fill(Color.WHITE));
		label.setFont(Font.font(16));
		label.setOpacity(0);
		label.setPadding(new Insets(4, 4, 4, 4));
		final BorderPane overlay = new BorderPane();
		overlay.setBottom(label);
		final StackPane stack = new StackPane();
		stack.getChildren().add(canvas);
		stack.getChildren().add(overlay);
		setCenter(stack);
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
			item.setDelegate(this);
		}
	}

	@Override
	public void update() {
		updateCell();
	}

	@Override
	public void onItemUpdated(float progress, boolean failed) {
		setDirty(true);
		updateCell();
	}

	@Override
	public void onItemSelected(boolean selected) {
		setDirty(true);
		updateCell();
	}

	private void updateCell() {
		if (item != null && item instanceof BrowseGridViewItem browseItem) {
			if (isDirty()) {
				final var g2d = canvas.getGraphicsContext2D();
				final var gc = factory.createGraphicsContext(g2d);
				g2d.setFill(Color.WHITE);
				browseItem.draw(gc, 0, 0);
				drawMessage(browseItem);
				drawOverlay(browseItem);
				updateLabel(browseItem);
				setDirty(false);
			}
		} else {
			if (isDirty()) {
				clearCanvas();
				setDirty(false);
			}
		}
	}

	private void updateLabel(BrowseGridViewItem browseItem) {
		if (hasClips(browseItem)) {
			label.setText("%.2fs".formatted(duration(browseItem) / 1000f));
			label.setOpacity(0.8);
		} else {
			label.setText("");
			label.setOpacity(0.0);
		}
	}

	private boolean hasClips(BrowseGridViewItem browseItem) {
		return browseItem.getBundle() != null && browseItem.getBundle().clips() != null && !browseItem.getBundle().clips().isEmpty();
	}

	private long duration(BrowseGridViewItem browseItem) {
		return hasClips(browseItem) ? browseItem.getBundle().clips().stream().mapToLong(AnimationClip::duration).sum() : 0L;
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
