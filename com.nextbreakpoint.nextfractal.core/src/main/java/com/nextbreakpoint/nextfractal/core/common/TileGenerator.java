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
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TileGenerator {
    private static final ThreadFactory THREAD_FACTORY = ThreadUtils.createThreadFactory(TileGenerator.class.getName());

    public static TileRequest createTileRequest(int size, int rows, int cols, int row, int col, Bundle bundle) {
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

    public static byte[] generatePNGImage(TileRequest request) throws Exception {
        final TileParameters parameters = createTileParameters(request);

        final RendererTile renderTile = parameters.createRenderTile();

        final Session session = request.session();

        final ImageComposer composer = Command.of(Plugins.tryFindFactory(session.pluginId()))
                .map(factory -> factory.createImageComposer(THREAD_FACTORY, renderTile, true))
                .execute()
                .orThrow()
                .optional()
                .orElseThrow();

        final IntBuffer pixels = composer.renderImage(session.script(), session.metadata());

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            writePNGImage(os, pixels, renderTile.tileSize());

            return os.toByteArray();
        }
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

    private static TileParameters createTileParameters(TileRequest request) {
        final int tileSize = request.size();
        final int rows = request.rows();
        final int cols = request.cols();
        final int row = request.row();
        final int col = request.col();

        return TileParameters.builder()
                .withImageWidth(tileSize * cols)
                .withImageHeight(tileSize * rows)
                .withTileWidth(tileSize)
                .withTileHeight(tileSize)
                .withTileOffsetX(tileSize * col)
                .withTileOffsetY(tileSize * row)
                .withBorderWidth(0)
                .withBorderHeight(0)
                .build();
    }

    private static void writePNGImage(ByteArrayOutputStream os, IntBuffer pixels, RendererSize tileSize) throws IOException {
        final BufferedImage image =  new BufferedImage(tileSize.width(), tileSize.height(), BufferedImage.TYPE_INT_ARGB);

        final int[] buffer = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();

        System.arraycopy(pixels.array(), 0, buffer, 0, tileSize.width() * tileSize.height());

        ImageIO.write(image, "PNG", os);
    }
}
