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
package com.nextbreakpoint.nextfractal.core.common;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

public class TileGenerator {
    private static final ThreadFactory threadFactory = new DefaultThreadFactory(TileGenerator.class.getName(), true, Thread.MIN_PRIORITY);

    private TileGenerator() {}

    public static TileRequest createTileRequest(int size, int rows, int cols, int row, int col, Bundle bundle) throws Exception {
        validateParameters(size, cols, rows, row, col);

        return TileRequest.builder()
                .withRows(rows)
                .withCols(cols)
                .withRow(row)
                .withCol(col)
                .withSize(size)
                .withTaskId(UUID.randomUUID())
                .withSession(bundle.session())
                .build();
    }

    private static void validateParameters(int size, int rows, int cols, int row, int col) {
        if (size < 64 || size > 512) {
            throw new RuntimeException("Invalid image size");
        }
        if (row < 0 || row > rows - 1) {
            throw new RuntimeException("Invalid row index " + row);
        }
        if (col < 0 || col > cols - 1) {
            throw new RuntimeException("Invalid col index " + col);
        }
    }

    private static TileContext createTileContext(TileRequest request) {
        final int tileSize = request.getSize();
        final int rows = request.getRows();
        final int cols = request.getCols();
        final int row = request.getRow();
        final int col = request.getCol();
        final TileContext context = TileContext.builder()
                .withQuality(1)
                .withImageWidth(tileSize * cols)
                .withImageHeight(tileSize * rows)
                .withTileWidth(tileSize)
                .withTileHeight(tileSize)
                .withTileOffsetX(tileSize * col)
                .withTileOffsetY(tileSize * row)
                .withBorderWidth(0)
                .withBorderHeight(0)
                .withRow(row)
                .withCol(col)
                .withRows(rows)
                .withCols(cols)
                .withSession(request.getSession())
                .withTaskId(request.getTaskId())
                .build();
        return context;
    }

    private static void writePNGImage(ByteArrayOutputStream baos, IntBuffer pixels, RendererSize tileSize) throws IOException {
        final BufferedImage image =  new BufferedImage(tileSize.getWidth(), tileSize.getHeight(), BufferedImage.TYPE_INT_ARGB);

        final int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

        System.arraycopy(pixels.array(), 0, buffer, 0, tileSize.getWidth() * tileSize.getHeight());

        ImageIO.write(image, "PNG", baos);
    }

    public static byte[] generateImage(TileRequest request) throws Exception {
        final TileContext context = createTileContext(request);

        final ImageComposer composer = Command.of(Plugins.tryFindFactory(context.getSession().getPluginId()))
                .map(factory -> factory.createImageComposer(threadFactory, context.getTile(), true))
                .execute()
                .orThrow()
                .orElse(null);

        final IntBuffer pixels = composer.renderImage(context.getSession().getScript(), context.getSession().getMetadata());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        writePNGImage(baos, pixels, context.getTile().getTileSize());

        return baos.toByteArray();
    }
}
