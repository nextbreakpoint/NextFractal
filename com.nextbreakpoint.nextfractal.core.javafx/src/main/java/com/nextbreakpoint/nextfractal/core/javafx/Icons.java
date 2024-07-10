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
package com.nextbreakpoint.nextfractal.core.javafx;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;

import java.io.InputStream;

public class Icons {
    private Icons() {}

    public static ImageView createIconImage(String name, double percentage) {
        InputStream stream = Icons.class.getResourceAsStream(name);
        return createIconImage(stream, percentage);
    }

    public static ImageView createIconImage(String name) {
        return createIconImage(name, computeOptimalIconPercentage());
    }

    public static ImageView createIconImage(InputStream stream, double percentage) {
        int size = (int)Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);
        ImageView image = new ImageView(new Image(stream));
        image.setSmooth(true);
        image.setFitWidth(size);
        image.setFitHeight(size);
        return image;
    }

    public static ImageView createIconImage(InputStream stream) {
        return createIconImage(stream, computeOptimalIconPercentage());
    }

    public static double computeOptimalIconPercentage() {
        double size = 0.019;

        Screen screen = Screen.getPrimary();

        if (screen.getDpi() > 100 || screen.getVisualBounds().getWidth() > 1200) {
            size = 0.020;
        }

        if (screen.getDpi() > 200 || screen.getVisualBounds().getWidth() > 2400) {
            size = 0.022;
        }

        return size;
    }

    public static double computeOptimalLargeIconPercentage() {
        double size = 0.021;

        Screen screen = Screen.getPrimary();

        if (screen.getDpi() > 100 || screen.getVisualBounds().getWidth() > 1200) {
            size = 0.022;
        }

        if (screen.getDpi() > 200 || screen.getVisualBounds().getWidth() > 2400) {
            size = 0.024;
        }

        return size;
    }
}
