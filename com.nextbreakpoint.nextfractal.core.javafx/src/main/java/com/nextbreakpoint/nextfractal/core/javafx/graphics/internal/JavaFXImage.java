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
package com.nextbreakpoint.nextfractal.core.javafx.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

import java.nio.IntBuffer;

public class JavaFXImage implements Image {
    private final WritableImage image;

    public JavaFXImage(WritableImage image) {
        this.image = image;
    }

    @Override
    public void draw(GraphicsContext context, int x, int y) {
        ((JavaFXGraphicsContext) context).getGraphicsContext().drawImage(image, x, y);
    }

    @Override
    public void draw(GraphicsContext context, int x, int y, int w, int h) {
        ((JavaFXGraphicsContext) context).getGraphicsContext().drawImage(image, x, y, w, h);
    }

    @Override
    public void getPixels(IntBuffer pixels) {
        image.getPixelReader().getPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), WritablePixelFormat.getIntArgbInstance(), pixels, (int) image.getWidth());
    }
}
