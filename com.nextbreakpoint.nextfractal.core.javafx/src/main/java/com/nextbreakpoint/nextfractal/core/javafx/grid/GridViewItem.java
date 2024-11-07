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
package com.nextbreakpoint.nextfractal.core.javafx.grid;

import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GridViewItem {
    private final Map<String, Object> properties = new HashMap<>();
    @Getter(AccessLevel.PROTECTED)
    private final GridViewCellRenderer renderer;
    @Setter
    @Getter(AccessLevel.PROTECTED)
    private volatile GridViewItemDelegate delegate;
    @Getter
    private float progress;
    private boolean hasErrors;

    public GridViewItem(GridViewCellRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
        renderer.setDelegate(this::onItemUpdated);
    }

    public Bundle getBundle() {
        return renderer.getBundle();
    }

    public void run() {
        renderer.run();
    }

    public void cancel() {
        renderer.cancel();
    }

    public void waitFor() throws InterruptedException {
        renderer.waitFor();
    }

    public void draw(GraphicsContext gc, int x, int y) {
        renderer.draw(gc, x, y);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void putProperty(String key, Object value) {
        properties.put(key, value);
    }

    public synchronized boolean hasErrors() {
        return hasErrors;
    }

    public synchronized boolean isCompleted() {
        return progress == 1f;
    }

    protected synchronized void onItemUpdated(float progress, List<ScriptError> errors) {
        this.progress = progress;
        this.hasErrors = !errors.isEmpty();
        if (delegate != null) {
            delegate.onItemUpdated(progress, hasErrors);
        }
    }
}
