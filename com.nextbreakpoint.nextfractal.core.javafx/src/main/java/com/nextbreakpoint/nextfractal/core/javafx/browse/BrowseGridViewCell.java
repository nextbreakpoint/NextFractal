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
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItemDelegate;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import static com.nextbreakpoint.nextfractal.core.javafx.Icons.createSVGIcon;

public class BrowseGridViewCell extends GridViewCell implements GridViewItemDelegate {
	private final JavaFXGraphicsFactory factory;
	private final Canvas canvas;
	private final StackPane iconOverlay;

	public BrowseGridViewCell(int index, int width, int height) {
		super(index, width, height);
		factory = new JavaFXGraphicsFactory();
		canvas = new Canvas(width, height);
		final double margin = width / 40d;
		final double size = width / 6d;
		final Color color1 = Color.web("rgb(255,255,255)");
		final Color color2 = Color.web("rgb(0,0,0)");
		final Node icon1 = createSVGIcon("/movie.svg", size, color1);
		final Node icon2 = createSVGIcon("/movie.svg", size, color2);
		final StackPane stack = new StackPane();
		final HBox overlay = new HBox(4);
		final VBox icons = new VBox(4);
		iconOverlay = new StackPane();
		iconOverlay.getChildren().add(icon2);
		iconOverlay.getChildren().add(icon1);
		iconOverlay.setVisible(false);
		icon2.setTranslateX(1);
		icon2.setTranslateY(1);
        overlay.setAlignment(Pos.CENTER_RIGHT);
		icons.setAlignment(Pos.TOP_LEFT);
		icons.setMinWidth(size);
		icons.setMinHeight(height);
		icons.setMaxWidth(size);
		icons.setMaxHeight(size);
		icons.setPrefWidth(size);
		icons.setPrefHeight(height);
		icons.getChildren().add(makeVSpacer(margin));
		icons.getChildren().add(iconOverlay);
		icons.getChildren().add(makeVSpacer(margin));
		overlay.getChildren().add(makeHSpacer(margin));
		overlay.getChildren().add(icons);
		overlay.getChildren().add(makeHSpacer(margin));
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

	private static Region makeVSpacer(double margin) {
		final Region spacer = new Region();
		spacer.setPrefHeight(margin);
		return spacer;
	}

	private static Region makeHSpacer(double margin) {
		final Region spacer = new Region();
		spacer.setPrefWidth(margin);
		return spacer;
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
				setDirty(false);
                iconOverlay.setVisible(hasClips(browseItem));
			}
		} else {
			if (isDirty()) {
				clearCanvas();
				setDirty(false);
			}
		}
	}

	private boolean hasClips(BrowseGridViewItem browseItem) {
		return browseItem.getBundle() != null && browseItem.getBundle().clips() != null && !browseItem.getBundle().clips().isEmpty();
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
