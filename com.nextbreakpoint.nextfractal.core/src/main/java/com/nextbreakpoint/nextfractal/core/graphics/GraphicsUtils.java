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
package com.nextbreakpoint.nextfractal.core.graphics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ServiceLoader;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphicsUtils {
    public static Tile createTile(int width, int height, int rows, int cols, int row, int col) {
        final int tileWidth = Math.round((float) width / (float) cols);
        final int tileHeight = Math.round((float) height / (float) rows);

        final Size imageSize = new Size(width, height);
        final Size tileSize = new Size(tileWidth, tileHeight);
        final Size tileBorder = new Size(0, 0);
        final int offsetX = Math.round(((float) col * (float) width) / (float) cols);
        final int offsetY = Math.round(((float) row * (float) height) / (float) rows);
        final Point tileOffset = new Point(offsetX, offsetY);

        return new Tile(imageSize, tileSize, tileOffset, tileBorder);
    }

    public static Tile createTile(int width, int height) {
        final Size imageSize = new Size(width, height);
        final Size tileSize = new Size(width, height);
        final Size tileBorder = new Size(0, 0);
        final Point tileOffset = new Point(0, 0);

        return new Tile(imageSize, tileSize, tileOffset, tileBorder);
    }

    public static Tile createTile(int size) {
        return createTile(size, size);
    }

    public static Tile createTile(double width) {
        return createTile(computeSize(width, 0.05));
    }

    public static GraphicsFactory findGraphicsFactory(String name) {
        return getGraphicsFactories()
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(graphicsFactory -> graphicsFactory.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    private static int computeSize(double width, double percentage) {
        return (int) Math.rint(width * percentage);
    }

    private static ServiceLoader<GraphicsFactory> getGraphicsFactories() {
        return ServiceLoader.load(GraphicsFactory.class);
    }

}
