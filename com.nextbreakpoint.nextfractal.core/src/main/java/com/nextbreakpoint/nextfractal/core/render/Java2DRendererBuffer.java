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
package com.nextbreakpoint.nextfractal.core.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Java2DRendererBuffer implements RendererBuffer {
	private final BufferedImage image;
	private final Graphics2D g2d;
	
	public Java2DRendererBuffer(int witdh, int height) {
		image = new BufferedImage(witdh, height, BufferedImage.TYPE_INT_ARGB);
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
	public RendererImage getImage() {
		return new Java2DRendererImage(image);
	}
}
