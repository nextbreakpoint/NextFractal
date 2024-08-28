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
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Coordinator;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.EXECUTE;

@Log
public class ContextFreeRenderingStrategy implements RenderingStrategy {
    private final RenderingContext renderingContext;
    private final MetadataDelegate delegate;
    private final int width;
    private final int height;
    private final int rows;
    private final int columns;
    private final GraphicsFactory renderFactory;
    private Coordinator coordinator;
    private boolean hasError;
    private String cfdgSource;
    private CFDGImage cfdgImage;

    public ContextFreeRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height, int rows, int columns) {
        this.renderingContext = renderingContext;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
        this.rows = rows;
        this.columns = columns;

        renderFactory = GraphicsUtils.findGraphicsFactory("JavaFX");

        final Map<String, Integer> hints = new HashMap<>();
        coordinator = createRendererCoordinator(hints, GraphicsUtils.createTile(width, height));
    }

    @Override
    public GraphicsFactory getRenderFactory() {
        return renderFactory;
    }

    @Override
    public void updateAndRedraw(long timestampInMillis) {
        if (!hasError && coordinator != null && coordinator.isInitialized()) {
            redrawIfPixelsChanged(renderingContext.getCanvas("fractal"));
            if (!renderingContext.isPlayback() && renderingContext.getTool() != null) {
                renderingContext.getTool().update(timestampInMillis, renderingContext.isTimeAnimation());
            }
        }
    }

    @Override
    public void updateCoordinators(Session session, boolean continuous, boolean timeAnimation) {
//        if (coordinator != null) {
//            coordinator.abort();
//            coordinator.waitFor();
//            if (cfdg != null) {
//                coordinator.setInterpreter(new CFDGInterpreter(cfdg));
//                coordinator.setSeed(((ContextFreeMetadata) delegate.getMetadata()).getSeed());
//            }
//            coordinator.init();
//            coordinator.run();
//        }
    }

    @Override
    public List<ScriptError> updateCoordinators(ParserResult result) {
        try {
            hasError = !result.errors().isEmpty();
            final boolean[] changed = createCFDG(result);
            final boolean cfdgChanged = changed[0];
            if (cfdgChanged) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("CFDG is changed");
                }
            }
            if (coordinator != null) {
                coordinator.abort();
                coordinator.waitFor();
                if (cfdgChanged && cfdgImage != null) {
                    coordinator.setImage(cfdgImage);
                    coordinator.setSeed(((ContextFreeMetadata)delegate.getMetadata()).getSeed());
                }
                coordinator.init();
                coordinator.run();
                return coordinator.getErrors();
            }
        } catch (Exception e) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Can't render image: " + e.getMessage());
            }
            return List.of(new ScriptError(EXECUTE, 0, 0, 0, 0, "Can't render image"));
        }
        return Collections.emptyList();
    }

    @Override
    public void disposeCoordinators() {
        if (coordinator != null) {
            coordinator.dispose();
            coordinator = null;
        }
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile) {
        return createRendererCoordinator(hints, tile, Thread.MIN_PRIORITY + 2, "ContextFree Coordinator");
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile, int priority, String name) {
        final DefaultThreadFactory threadFactory = ThreadUtils.createThreadFactory(name, priority);
        return new Coordinator(threadFactory, renderFactory, tile, hints);
    }

    private boolean[] createCFDG(ParserResult result) throws Exception {
        if (!result.errors().isEmpty()) {
            cfdgSource = null;
            throw new CFParserException("Failed to compile source", result.errors());
        }
        final boolean[] changed = new boolean[] { false, false };
        final CFParserResult parserResult = (CFParserResult) result.result();
        final String source = parserResult.source();
        changed[0] = !source.equals(cfdgSource);
        cfdgSource = source;
        cfdgImage = parserResult.classFactory().create();
        return changed;
    }

    private void redrawIfPixelsChanged(Canvas canvas) {
        if (coordinator.isPixelsChanged()) {
            final GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            coordinator.drawImage(gc, 0, 0);
        }
    }
}
