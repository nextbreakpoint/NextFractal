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
package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Coordinator;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingStrategy;
import javafx.scene.canvas.Canvas;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class ContextFreeRenderingStrategy implements RenderingStrategy {
    private final List<ScriptError> errors = new ArrayList<>();
    private final GraphicsFactory graphicsFactory;
    private final RenderingContext context;
    private final MetadataDelegate delegate;
    private final int width;
    private final int height;
    private final int rows;
    private final int columns;
    private Coordinator coordinator;
    private boolean hasError;
    private String source;
    private CFDGImage image;

    public ContextFreeRenderingStrategy(RenderingContext context, MetadataDelegate delegate, int width, int height, int rows, int columns) {
        this.context = context;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
        this.rows = rows;
        this.columns = columns;

        graphicsFactory = GraphicsUtils.findGraphicsFactory("JavaFX");

        coordinator = createRendererCoordinator(new HashMap<>(), GraphicsUtils.createTile(width, height));
    }

    @Override
    public GraphicsFactory getGraphicsFactory() {
        return graphicsFactory;
    }

    @Override
    public void updateAndRedraw(long timestampInMillis) {
        if (!hasError && coordinator != null && coordinator.isInitialized()) {
            redrawIfPixelsChanged(context.getCanvas("fractal"));
            if (!context.isPlayback() && context.getTool() != null) {
                context.getTool().update(timestampInMillis, context.isTimeAnimation());
            }
        }
    }

    @Override
    public void updateCoordinators(Session session, boolean continuous, boolean timeAnimation) {
    }

    @Override
    public void updateCoordinators(ParserResult result) {
        try {
            hasError = !result.errors().isEmpty();
            if (hasError) {
                source = null;
                image = null;
                return;
            }
            final CFParserResult parserResult = (CFParserResult) result.result();
            final String source = parserResult.source();
            final boolean changed = !source.equals(this.source);
            this.source = source;
            image = parserResult.classFactory().create();
            if (changed) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("CFDG is changed");
                }
            }
            if (coordinator != null) {
                coordinator.abort();
                coordinator.waitFor();
                if (changed && image != null) {
                    coordinator.setImage(image, ((ContextFreeMetadata)delegate.getMetadata()).getSeed());
                }
                coordinator.init();
                coordinator.run();
            }
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Can't render image", e);
            }
        }
    }

    @Override
    public void disposeCoordinators() {
        if (coordinator != null) {
            coordinator.dispose();
            coordinator = null;
        }
    }

    @Override
    public List<ScriptError> getErrors() {
        if (coordinator != null) {
            return coordinator.getErrors();
        }
        return errors;
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile) {
        return createRendererCoordinator(hints, tile, Thread.MIN_PRIORITY, "ContextFree Coordinator");
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile, int priority, String name) {
        final ThreadFactory threadFactory = ThreadUtils.createPlatformThreadFactory(name, priority);
        return new Coordinator(threadFactory, graphicsFactory, tile, hints);
    }

    private void redrawIfPixelsChanged(Canvas canvas) {
        if (coordinator.hasImageChanged()) {
            final GraphicsContext gc = graphicsFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            coordinator.drawImage(gc, 0, 0);
        }
    }
}
