/*
 * NextFractal 2.3.1
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

import com.nextbreakpoint.nextfractal.core.common.SourceError;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class GridItem {
	private volatile long lastChanged;
	private volatile boolean aborted;
	private volatile boolean selected;
	private volatile boolean dirty;
	private volatile File file;
	private volatile BrowseBitmap bitmap;
	private volatile GridItemRenderer renderer;
	private volatile Future<GridItem> loadItemFuture;
	private volatile Future<GridItem> initItemFuture;
	private volatile List<SourceError> errors = new LinkedList<>();

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		lastChanged = System.currentTimeMillis();
		this.file = file;
		dirty = true;
	}

	public BrowseBitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(BrowseBitmap bitmap) {
		lastChanged = System.currentTimeMillis();
		this.bitmap = bitmap;
		dirty = true;
	}

	public GridItemRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(GridItemRenderer renderer) {
		lastChanged = System.currentTimeMillis();
		this.renderer = renderer;
		dirty = true;
	}

	public Future<GridItem> getLoadItemFuture() {
		return loadItemFuture;
	}

	public void setLoadItemFuture(Future<GridItem> loadItemFuture) {
		lastChanged = System.currentTimeMillis();
		this.loadItemFuture = loadItemFuture;
	}

	public Future<GridItem> getInitItemFuture() {
		return initItemFuture;
	}

	public void setInitItemFuture(Future<GridItem> initItemFuture) {
		lastChanged = System.currentTimeMillis();
		this.initItemFuture = initItemFuture;
	}

	public long getLastChanged() {
		return lastChanged;
	}

	public List<SourceError> getErrors() {
		return errors;
	}

	public void setErrors(List<SourceError> errors) {
		this.errors.clear();
		this.errors.addAll(errors);
		dirty = true;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	public boolean isAborted() {
		return aborted;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		dirty = true;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
}
