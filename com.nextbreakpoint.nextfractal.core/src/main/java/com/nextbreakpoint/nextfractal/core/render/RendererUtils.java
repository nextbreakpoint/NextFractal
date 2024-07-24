package com.nextbreakpoint.nextfractal.core.render;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RendererUtils {
    public static RendererTile createRendererTile(int width, int height, int rows, int cols, int row, int col) {
        final int tileWidth = Math.round((float) width / (float) cols);
        final int tileHeight = Math.round((float) height / (float) rows);

        final RendererSize imageSize = new RendererSize(width, height);
        final RendererSize tileSize = new RendererSize(tileWidth, tileHeight);
        final RendererSize tileBorder = new RendererSize(0, 0);
        final int offsetX = Math.round(((float) col * (float) width) / (float) cols);
        final int offsetY = Math.round(((float) row * (float) height) / (float) rows);
        final RendererPoint tileOffset = new RendererPoint(offsetX, offsetY);

        return new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
    }

    public static RendererTile createRendererTile(int width, int height) {
        final RendererSize imageSize = new RendererSize(width, height);
        final RendererSize tileSize = new RendererSize(width, height);
        final RendererSize tileBorder = new RendererSize(0, 0);
        final RendererPoint tileOffset = new RendererPoint(0, 0);

        return new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
    }

    public static RendererTile createRendererTile(int size) {
        return createRendererTile(size, size);
    }

    public static RendererTile createRendererTile(double width) {
        return createRendererTile(computeSize(width, 0.05));
    }

    private static int computeSize(double width, double percentage) {
        return (int) Math.rint(width * percentage);
    }
}
