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

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.contextfree.renderer.Renderer;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.render.Java2DRendererFactory;
import com.nextbreakpoint.nextfractal.core.render.Java2DRendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class ContextFreeImageComposer implements ImageComposer {
    private static final Logger logger = Logger.getLogger(ContextFreeImageComposer.class.getName());

    private boolean aborted;
    private boolean opaque;
    private RendererTile tile;
    private ThreadFactory threadFactory;

    public ContextFreeImageComposer(ThreadFactory threadFactory, RendererTile tile, boolean opaque) {
        this.tile = tile;
        this.opaque = opaque;
        this.threadFactory = threadFactory;
    }

    @Override
    public IntBuffer renderImage(String script, Metadata data) {
        ContextFreeMetadata metadata = (ContextFreeMetadata) data;
        RendererSize suggestedSize = tile.getTileSize();
        BufferedImage image = new BufferedImage(suggestedSize.getWidth(), suggestedSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
        IntBuffer buffer = IntBuffer.wrap(((DataBufferInt) image.getRaster().getDataBuffer()).getData());
        Graphics2D g2d = null;
        try {
            g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            DSLParser parser = new DSLParser();
            DSLParserResult report = parser.parse(script);
            DSLCompiler compiler = new DSLCompiler();
            CFDGInterpreter interpreter = compiler.compile(report);
            Java2DRendererFactory renderFactory = new Java2DRendererFactory();
            Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
            renderer.setInterpreter(interpreter);
            renderer.setSeed(metadata.getSeed());
            renderer.setOpaque(opaque);
            renderer.init();
            renderer.runTask();
            renderer.waitForTasks();
            renderer.copyImage(new Java2DRendererGraphicsContext(g2d));
            aborted = renderer.isInterrupted();
        } catch (Throwable e) {
            logger.severe(e.getMessage());
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        return buffer;
    }

    @Override
    public RendererSize getSize() {
        return tile.getTileSize();
    }

    @Override
    public boolean isInterrupted() {
        return aborted;
    }
}
