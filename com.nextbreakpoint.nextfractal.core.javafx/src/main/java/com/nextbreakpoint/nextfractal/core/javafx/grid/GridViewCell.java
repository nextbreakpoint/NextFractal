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
package com.nextbreakpoint.nextfractal.core.javafx.grid;

import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;

public abstract class GridViewCell extends BorderPane {
    protected final int index;
    protected GridViewItem item;
    @Getter
    @Setter
    private boolean dirty;
    @Setter
    @Getter
    private boolean focus;
    @Setter
    @Getter
    private boolean selected;

    public GridViewCell(int index, int width, int height) {
        this.index = index;

        getStyleClass().add("grid-view-cell");

        setMinSize(width, height);
        setMaxSize(width, height);
        setPrefSize(width, height);

        widthProperty().addListener((_, _, _) -> onSizeChanged());
        heightProperty().addListener((_, _, _) -> onSizeChanged());
        insetsProperty().addListener((_, _, _) -> onSizeChanged());
    }

    protected abstract void onSizeChanged();

    protected double getInnerWidth() {
        return getWidth() - getInsets().getLeft() - getInsets().getRight();
    }

    protected double getInnerHeight() {
        return getHeight() - getInsets().getTop() - getInsets().getBottom();
    }

    public void forceUpdate() {
        dirty = true;
        update();
    }

    public abstract void update();

    public void unbindItem() {
        item = null;
        dirty = true;
    }

    public void bindItem(GridViewItem item) {
        if (this.item != item) {
            this.item = item;
            dirty = true;
        }
    }
}
