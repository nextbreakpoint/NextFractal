/*
 * NextFractal 2.1.2-rc1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

public class Java2DRendererImage implements RendererImage {
	private final BufferedImage image;
	
	public Java2DRendererImage(BufferedImage image) {
		this.image = image;
	}

	@Override
	public void draw(RendererGraphicsContext context, int x, int y) {
		((Java2DRendererGraphicsContext)context).getGraphicsContext().drawImage(image, x, y, null);
	}
	
	@Override
	public void draw(RendererGraphicsContext context, int x, int y, int w, int h) {
		((Java2DRendererGraphicsContext)context).getGraphicsContext().drawImage(image, x, y, w, h, null);
	}

	@Override
	public void getPixels(IntBuffer pixels) {
		pixels.put(((DataBufferInt)image.getRaster().getDataBuffer()).getData());
	}
}
