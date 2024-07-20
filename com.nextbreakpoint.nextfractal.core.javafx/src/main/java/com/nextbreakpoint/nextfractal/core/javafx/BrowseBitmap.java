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

import java.nio.IntBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BrowseBitmap implements Bitmap {
	private final UUID uuid;
	private final int width;
	private final int height;
	private final Map<String, Object> properties = new HashMap<>();
	private final Date timestamp;
	private final IntBuffer pixels;
	private double progress;

	public BrowseBitmap(int width, int height, IntBuffer pixels) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
		timestamp = new Date();
		uuid = UUID.randomUUID();
	}

	public IntBuffer getPixels() {
		return pixels;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

	@Override
	public UUID getId() {
		return uuid;
	}
}
