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
package com.nextbreakpoint.nextfractal.core.javafx.browse;

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.javafx.TextUtils;
import com.nextbreakpoint.nextfractal.core.javafx.graphics.internal.JavaFXGraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewCell;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItemDelegate;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class BrowseGridViewCell extends GridViewCell implements GridViewItemDelegate {
    private final JavaFXGraphicsFactory factory;
    private final Canvas overlayCanvas;
    private final Canvas itemCanvas;
    private final Label label1;
    private final Label label2;
    private final BorderPane overlay;
    private final BorderPane details;

    public BrowseGridViewCell(int index, int width, int height) {
        super(index, width, height);
        factory = new JavaFXGraphicsFactory();
        overlayCanvas = new Canvas(0, 0);
        itemCanvas = new Canvas(0, 0);
        label1 = new Label();
        label2 = new Label();
        BorderPane title = new BorderPane();
        title.setBottom(label1);
        title.getStyleClass().add("grid-view-cell-title");
        details = new BorderPane();
        details.setBottom(label2);
        details.getStyleClass().add("grid-view-cell-details");
        overlay = new BorderPane();
        overlay.setTop(title);
        overlay.setBottom(details);
        overlay.getStyleClass().add("grid-view-cell-overlay");
        overlayCanvas.setMouseTransparent(true);
        final StackPane stack = new StackPane();
        stack.getChildren().add(itemCanvas);
        stack.getChildren().add(overlay);
        stack.getChildren().add(overlayCanvas);
        setCenter(stack);
    }

    @Override
    protected void onSizeChanged() {
        final double innerWidth = getInnerWidth();
        final double innerHeight = getInnerHeight();
        if (innerWidth > 0 && innerHeight > 0) {
            if (innerWidth != itemCanvas.getWidth() || innerHeight != itemCanvas.getHeight()) {
                itemCanvas.setWidth(innerWidth);
                itemCanvas.setHeight(innerHeight);
                overlayCanvas.setWidth(innerWidth);
                overlayCanvas.setHeight(innerHeight);
                forceUpdate();
            }
        }
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
        if (item != null && item instanceof BrowseGridViewItem viewItem) {
            if (isDirty()) {
                drawItem(viewItem);
                drawOverlay(viewItem);
                updateLabel(viewItem);
                overlay.setVisible(true);
                details.setVisible(hasClips(viewItem));
                setDirty(false);
            }
        } else {
            if (isDirty()) {
                clearCanvas();
                overlay.setVisible(false);
                details.setVisible(false);
                setDirty(false);
            }
        }
    }

    private void drawItem(BrowseGridViewItem viewItem) {
        final var g2d = itemCanvas.getGraphicsContext2D();
        final var gc = factory.createGraphicsContext(g2d);
        viewItem.draw(gc, 0, 0);
    }

    private void updateLabel(BrowseGridViewItem viewItem) {
        label1.setText(TextUtils.formatInstant(viewItem.getFile().lastModified()));
        label1.setTooltip(new Tooltip(viewItem.getFile().getAbsolutePath()));
        label2.setText(hasClips(viewItem) ? TextUtils.formatDuration(getDurationInSeconds(viewItem)) : "");
        label2.setTooltip(new Tooltip(viewItem.getFile().getAbsolutePath()));
    }

    private float getDurationInSeconds(BrowseGridViewItem viewItem) {
        return clipsDuration(viewItem) / 1000f;
    }

    private boolean hasClips(BrowseGridViewItem viewItem) {
        return viewItem.getBundle() != null && viewItem.getBundle().clips() != null && !viewItem.getBundle().clips().isEmpty();
    }

    private long clipsDuration(BrowseGridViewItem viewItem) {
        return hasClips(viewItem) ? viewItem.getBundle().clips().stream().mapToLong(AnimationClip::duration).sum() : 0L;
    }

    private void clearCanvas() {
        itemCanvas.getGraphicsContext2D()
                .clearRect(0, 0, itemCanvas.getWidth(), itemCanvas.getHeight());
        overlayCanvas.getGraphicsContext2D()
                .clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
    }

    private void drawOverlay(BrowseGridViewItem viewItem) {
        final var g2d = overlayCanvas.getGraphicsContext2D();
        g2d.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());

        if (isFocus()) {
            g2d.setStroke(Color.BLUE);
            g2d.setLineWidth(5);
            g2d.strokeRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        } else if (isSelected()) {
            g2d.setStroke(Color.YELLOW);
            g2d.setLineWidth(5);
            g2d.strokeRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
        }

        final String message = getMessage(viewItem);
        if (message != null) {
            g2d.setFill(Color.DARKGRAY);
            g2d.setTextAlign(TextAlignment.CENTER);
            g2d.fillText(message, overlayCanvas.getWidth() / 2, overlayCanvas.getHeight() / 2);
        }
    }

    private String getMessage(BrowseGridViewItem viewItem) {
        if (viewItem.hasErrors()) {
            return "Error";
        } else if (!viewItem.isCompleted()) {
            return "Rendering...";
        } else {
            return null;
        }
    }
}
