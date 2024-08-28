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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Renderer;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import lombok.extern.java.Log;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class ContextFreeImageComposer implements ImageComposer {
    private boolean aborted;
    private final boolean opaque;
    private final Tile tile;
    private final ThreadFactory threadFactory;

    public ContextFreeImageComposer(ThreadFactory threadFactory, Tile tile, boolean opaque) {
        this.tile = tile;
        this.opaque = opaque;
        this.threadFactory = threadFactory;
    }

    @Override
    public IntBuffer renderImage(String script, Metadata data) {
        final ContextFreeMetadata metadata = (ContextFreeMetadata) data;
        final Size suggestedSize = tile.tileSize();
        final BufferedImage image = new BufferedImage(suggestedSize.width(), suggestedSize.height(), BufferedImage.TYPE_INT_ARGB);
        final IntBuffer buffer = IntBuffer.wrap(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        Graphics2D g2d = null;
        try {
            g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            final CFParser parser = new CFParser();
            final CFParserResult parserResult = parser.parse(script);
            final CFDGImage cfdgImage = parserResult.classFactory().create();
            final GraphicsFactory renderFactory = GraphicsUtils.findGraphicsFactory("Java2D");
            final Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
            renderer.setImage(cfdgImage);
            renderer.setSeed(metadata.getSeed());
            renderer.setOpaque(opaque);
            renderer.init();
            renderer.runTask();
            renderer.waitForTasks();
            renderer.copyImage(renderFactory.createGraphicsContext(g2d));
            aborted = renderer.isInterrupted();
        } catch (CFParserException e) {
            log.log(Level.WARNING, e.getMessage(), e);
        } catch (Throwable e) {
            log.severe(e.getMessage());
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        return buffer;
    }

    @Override
    public Size getSize() {
        return tile.tileSize();
    }

    @Override
    public boolean isInterrupted() {
        return aborted;
    }
}
