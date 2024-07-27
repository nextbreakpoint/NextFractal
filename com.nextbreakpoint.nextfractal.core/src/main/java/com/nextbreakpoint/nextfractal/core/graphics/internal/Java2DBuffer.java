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
package com.nextbreakpoint.nextfractal.core.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.Buffer;
import com.nextbreakpoint.nextfractal.core.graphics.Image;

import java.awt.*;
import java.awt.image.DataBufferInt;

public class Java2DBuffer implements Buffer {
	private final java.awt.image.BufferedImage image;
	private final Graphics2D g2d;
	
	public Java2DBuffer(int width, int height) {
		image = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();
	}

	@Override
	public void dispose() {
		if (g2d != null) {
			g2d.dispose();
		}
	}

	@Override
	public void clear() {
		g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
	}

	@Override
	public void update(int[] pixels) {
		if (pixels != null && pixels.length <= getWidth() * getHeight()) {
			System.arraycopy(pixels, 0, ((DataBufferInt)image.getRaster().getDataBuffer()).getData(), 0, pixels.length);
		}
	}

	@Override
	public int getWidth() {
		return image.getWidth();
	}
	
	@Override
	public int getHeight() {
		return image.getHeight();
	}

	@Override
	public Image getImage() {
		return new Java2DImage(image);
	}
}
