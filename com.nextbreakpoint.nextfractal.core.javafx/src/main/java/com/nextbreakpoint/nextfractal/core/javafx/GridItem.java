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

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

@Getter
public class GridItem {
	private final List<ScriptError> errors = new LinkedList<>();
	private File file;
	private Bitmap bitmap;
	private GridItemRenderer renderer;
	private Future<GridItem> loadItemFuture;
	private Future<GridItem> initItemFuture;
	private long lastChanged;
	private boolean selected;
	@Setter
	private volatile boolean aborted;
	@Setter
	private volatile boolean dirty;

    public synchronized void setFile(File file) {
		lastChanged = System.currentTimeMillis();
		this.file = file;
		dirty = true;
	}

    public synchronized void setBitmap(Bitmap bitmap) {
		lastChanged = System.currentTimeMillis();
		this.bitmap = bitmap;
		dirty = true;
	}

    public synchronized void setRenderer(GridItemRenderer renderer) {
		lastChanged = System.currentTimeMillis();
		this.renderer = renderer;
		dirty = true;
	}

    public synchronized void setLoadItemFuture(Future<GridItem> loadItemFuture) {
		lastChanged = System.currentTimeMillis();
		this.loadItemFuture = loadItemFuture;
	}

    public synchronized void setInitItemFuture(Future<GridItem> initItemFuture) {
		lastChanged = System.currentTimeMillis();
		this.initItemFuture = initItemFuture;
	}

    public synchronized void setErrors(List<ScriptError> errors) {
		this.errors.clear();
		this.errors.addAll(errors);
		dirty = true;
	}

    public synchronized void setSelected(boolean selected) {
		this.selected = selected;
		dirty = true;
	}
}
