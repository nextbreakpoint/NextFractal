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
package com.nextbreakpoint.nextfractal.core.javafx.render;

import com.nextbreakpoint.nextfractal.core.render.RendererBuffer;
import com.nextbreakpoint.nextfractal.core.render.RendererImage;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class JavaFXRendererBuffer implements RendererBuffer {
	private final WritableImage image;
	private final PixelWriter writer;
	
	public JavaFXRendererBuffer(int witdh, int height) {
		image = new WritableImage(witdh, height);
		writer = image.getPixelWriter();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void clear() {
//		int[] pixels = new int[getWidth() * getHeight()];
//		for (int i = 0; i < pixels.length; i++) {
//			pixels[i] = 0xFF000000; 
//		}
//		writer.setPixels(0, 0, getWidth(), getHeight(), PixelFormat.getIntArgbInstance(), pixels, 0, getWidth());
		for (int x = 0; x < getWidth(); x++) {
			for (int y = 0; y < getHeight(); y++) {
				writer.setArgb(x, y, 0xFF000000);
			}
		}
	}

	@Override
	public void update(int[] pixels) {
		if (pixels != null && pixels.length <= getWidth() * getHeight()) {
			writer.setPixels(0, 0, getWidth(), getHeight(), PixelFormat.getIntArgbInstance(), pixels, 0, getWidth());
		}
	}

	@Override
	public int getWidth() {
		return (int)image.getWidth();
	}
	
	@Override
	public int getHeight() {
		return (int)image.getHeight();
	}

	@Override
	public RendererImage getImage() {
		return new JavaFXRendererImage(image);
	}
}
