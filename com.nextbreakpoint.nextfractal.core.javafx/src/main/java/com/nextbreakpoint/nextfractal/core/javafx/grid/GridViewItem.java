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

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.javafx.ImageLoader;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GridViewItem {
    private final Map<String, Object> properties = new HashMap<>();
    protected final ImageLoader imageLoader;
    @Setter
    private volatile GridViewItemDelegate delegate;

    public GridViewItem(ImageLoader imageLoader) {
        this.imageLoader = Objects.requireNonNull(imageLoader);
        imageLoader.setDelegate(this::onItemUpdated);
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public void run() {
        imageLoader.run();
    }

    public void cancel() {
        imageLoader.cancel();
    }

    public void waitFor() throws InterruptedException {
        imageLoader.waitFor();
    }

    public void drawImage(GraphicsContext gc, int x, int y) {
        imageLoader.drawImage(gc, x, y);
    }

    protected void onItemUpdated(float progress, List<ScriptError> errors) {
        if (delegate != null) {
            delegate.onItemUpdated();
        }
    }

    protected void onItemSelected() {
        if (delegate != null) {
            delegate.onItemSelected();
        }
    }
}
