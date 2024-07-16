/*
 * NextFractal 2.3.0
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

import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererImage;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

import java.nio.IntBuffer;

public class JavaFXRendererImage implements RendererImage {
	private final WritableImage image;
	
	public JavaFXRendererImage(WritableImage image) {
		this.image = image;
	}

	@Override
	public void draw(RendererGraphicsContext context, int x, int y) {
		((JavaFXRendererGraphicsContext)context).getGraphicsContext().drawImage(image, x, y);
	}
	
	@Override
	public void draw(RendererGraphicsContext context, int x, int y, int w, int h) {
		((JavaFXRendererGraphicsContext)context).getGraphicsContext().drawImage(image, x, y, w, h);
	}

	@Override
	public void getPixels(IntBuffer pixels) {
		image.getPixelReader().getPixels(0, 0, (int)image.getWidth(), (int)image.getHeight(), WritablePixelFormat.getIntArgbInstance(), pixels, (int)image.getWidth());
	}
}
