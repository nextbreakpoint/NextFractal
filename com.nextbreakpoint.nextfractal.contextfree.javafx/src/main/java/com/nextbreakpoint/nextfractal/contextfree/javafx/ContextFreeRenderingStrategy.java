package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.nextfractal.contextfree.core.ParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.contextfree.renderer.RendererCoordinator;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.render.RendererUtils;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingStrategy;
import com.nextbreakpoint.nextfractal.core.javafx.render.JavaFXRendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.scene.canvas.Canvas;
import lombok.extern.java.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Log
public class ContextFreeRenderingStrategy implements RenderingStrategy {
    private final RenderingContext renderingContext;
    private final MetadataDelegate delegate;
    private final int width;
    private final int height;
    private final int rows;
    private final int columns;
    private final JavaFXRendererFactory renderFactory;
    private RendererCoordinator coordinator;
    private boolean hasError;
    private String cfdgSource;
    private CFDG cfdg;

    public ContextFreeRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height, int rows, int columns) {
        this.renderingContext = renderingContext;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
        this.rows = rows;
        this.columns = columns;

        renderFactory = new JavaFXRendererFactory();

        Map<String, Integer> hints = new HashMap<>();
        coordinator = createRendererCoordinator(hints, RendererUtils.createRendererTile(width, height));
    }

    @Override
    public RendererFactory getRenderFactory() {
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
    public List<ParserError> updateCoordinators(Object result) {
        try {
            DSLParserResult parserResult = (DSLParserResult) result;
            hasError = !parserResult.getErrors().isEmpty();
            boolean[] changed = createCFDG(parserResult);
            boolean cfdgChanged = changed[0];
            if (cfdgChanged) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("CFDG is changed");
                }
            }
            if (coordinator != null) {
                coordinator.abort();
                coordinator.waitFor();
                if (cfdgChanged && cfdg != null) {
                    coordinator.setInterpreter(new CFDGInterpreter(cfdg));
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
            return List.of(new ParserError(ParserError.ErrorType.RUNTIME, 0, 0, 0, 0, "Can't render image"));
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

    private RendererCoordinator createRendererCoordinator(Map<String, Integer> hints, RendererTile tile) {
        return createRendererCoordinator(hints, tile, Thread.MIN_PRIORITY + 2, "ContextFree Coordinator");
    }

    private RendererCoordinator createRendererCoordinator(Map<String, Integer> hints, RendererTile tile, int priority, String name) {
        final DefaultThreadFactory threadFactory = createThreadFactory(name, priority);
        return new RendererCoordinator(threadFactory, renderFactory, tile, hints);
    }

    private DefaultThreadFactory createThreadFactory(String name, int priority) {
        return new DefaultThreadFactory(name, true, priority);
    }

    private boolean[] createCFDG(DSLParserResult report) throws ParserException {
        if (!report.getErrors().isEmpty()) {
            cfdgSource = null;
            throw new ParserException("Failed to compile source", report.getErrors());
        }
        boolean[] changed = new boolean[] { false, false };
        String newCFDG = report.getSource();
        changed[0] = !newCFDG.equals(cfdgSource);
        cfdgSource = newCFDG;
        cfdg = report.getCFDG();
        return changed;
    }

    private void redrawIfPixelsChanged(Canvas canvas) {
        if (coordinator.isPixelsChanged()) {
            RendererGraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            coordinator.drawImage(gc, 0, 0);
        }
    }
}
