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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingStrategy;
import com.nextbreakpoint.nextfractal.core.common.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Coordinator;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.View;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import javafx.scene.canvas.Canvas;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.EXECUTE;

@Log
public class MandelbrotRenderingStrategy implements RenderingStrategy {
    private final GraphicsFactory renderFactory;
    private final Coordinator[] coordinators;
    private Coordinator juliaCoordinator;
    private final int width;
    private final int height;
    private final int rows;
    private final int columns;
    private volatile boolean redrawTraps;
    private volatile boolean redrawOrbit;
    private volatile boolean redrawPoint;
    private volatile boolean hasError;
    private final RenderingContext renderingContext;
    private final MetadataDelegate delegate;
    private ClassFactory<Orbit> orbitFactory;
    private ClassFactory<Color> colorFactory;
    private String astOrbit;
    private String astColor;
    private List<ComplexNumber[]> states = new ArrayList<>();

    public MandelbrotRenderingStrategy(RenderingContext renderingContext, MetadataDelegate delegate, int width, int height, int rows, int columns) {
        this.renderingContext = renderingContext;
        this.delegate = delegate;
        this.width = width;
        this.height = height;
        this.rows = rows;
        this.columns = columns;

        renderFactory = GraphicsUtils.findGraphicsFactory("JavaFX");

        final Map<String, Integer> hints = Map.of(Coordinator.KEY_TYPE, Coordinator.VALUE_REALTIME);
        coordinators = createCoordinators(rows, columns, hints);

        final Map<String, Integer> juliaHints = Map.of(Coordinator.KEY_TYPE, Coordinator.VALUE_REALTIME);
        juliaCoordinator = createJuliaRendererCoordinator(juliaHints, GraphicsUtils.createTile(200, 200));
    }

    public ComplexNumber getInitialCenter() {
        return coordinators[0].getInitialCenter();
    }

    public ComplexNumber getInitialSize() {
        return coordinators[0].getInitialSize();
    }

    @Override
    public GraphicsFactory getRenderFactory() {
        return renderFactory;
    }

    @Override
    public void updateAndRedraw(long timestampInMillis) {
        if (!hasError && coordinators[0] != null && coordinators[0].isInitialized()) {
            redrawIfPixelsChanged(renderingContext.getCanvas("fractal"));
            redrawIfJuliaPixelsChanged(renderingContext.getCanvas("julia"));
            redrawIfPointChanged(renderingContext.getCanvas("point"));
            redrawIfOrbitChanged(renderingContext.getCanvas("orbit"));
            redrawIfTrapChanged(renderingContext.getCanvas("traps"));
            redrawIfToolChanged(renderingContext.getCanvas("tool"));
            if (!renderingContext.isPlayback() && renderingContext.getTool() != null) {
                renderingContext.getTool().update(timestampInMillis, renderingContext.isTimeAnimation());
            }
        }
    }

    @Override
    public void updateCoordinators(Session session, boolean continuous, boolean timeAnimation) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) session.metadata();
        final Double4D translation = metadata.getTranslation();
        final Double4D rotation = metadata.getRotation();
        final Double4D scale = metadata.getScale();
        final Double2D point = metadata.getPoint();
        final Time time = metadata.time();
        final boolean julia = metadata.isJulia();
        abortCoordinators();
        joinCoordinators();
        for (Coordinator coordinator : coordinators) {
            if (coordinator != null) {
                final View view = new View();
                view.setTranslation(translation);
                view.setRotation(rotation);
                view.setScale(scale);
                view.setState(new Integer4D(0, 0, continuous ? 1 : 0, timeAnimation ? 1 : 0));
                view.setJulia(julia);
                view.setPoint(new ComplexNumber(point.x(), point.y()));
                coordinator.setView(view);
//				if (timeAnimation) {
                coordinator.setTime(time);
//				}
            }
        }
        startCoordinators();
        if (metadata.getOptions().isShowPreview() && !julia && juliaCoordinator != null) {
            juliaCoordinator.abort();
            juliaCoordinator.waitFor();
            final View view = new View();
            view.setTranslation(new Double4D(new double[]{0, 0, 1, 0}));
            view.setRotation(new Double4D(new double[]{0, 0, 0, 0}));
            view.setScale(new Double4D(new double[]{1, 1, 1, 1}));
            view.setState(new Integer4D(0, 0, continuous ? 1 : 0, timeAnimation ? 1 : 0));
            view.setJulia(true);
            view.setPoint(new ComplexNumber(point.x(), point.y()));
            juliaCoordinator.setView(view);
//			if (timeAnimation) {
            juliaCoordinator.setTime(time);
//			}
            juliaCoordinator.run();
        }
        states = renderOrbit(point);
        redrawOrbit = true;
        redrawPoint = true;
        redrawTraps = true;
        if (!julia && !continuous) {
//			states = renderOrbit(point);
            if (log.isLoggable(Level.FINE)) {
                log.fine("Orbit: point = " + point + ", length = " + states.size());
            }
        }
    }

    @Override
    public List<ScriptError> updateCoordinators(ParserResult result) {
        try {
            hasError = !result.errors().isEmpty();
            if (hasError) {
                this.astOrbit = null;
                this.astColor = null;
                this.orbitFactory = null;
                this.colorFactory = null;
                return List.of(new ScriptError(EXECUTE, 0, 0, 0, 0, "Can't render image"));
            }
            final boolean[] changed = createOrbitAndColor(result);
            final boolean orbitChanged = changed[0];
            final boolean colorChanged = changed[1];
            if (orbitChanged) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Orbit algorithm is changed");
                }
            }
            if (colorChanged) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Color algorithm is changed");
                }
            }
//			if (!orbitChanged && !colorChanged) {
//				log.info("Orbit or color algorithms are not changed");
//				return;
//			}
            final MandelbrotMetadata oldMetadata = (MandelbrotMetadata) delegate.getMetadata();
            final Double4D translation = oldMetadata.getTranslation();
            final Double4D rotation = oldMetadata.getRotation();
            final Double4D scale = oldMetadata.getScale();
            final Double2D point = oldMetadata.getPoint();
            final Time time = oldMetadata.time();
            final boolean julia = oldMetadata.isJulia();
            abortCoordinators();
            if (juliaCoordinator != null) {
                juliaCoordinator.abort();
            }
            joinCoordinators();
            if (juliaCoordinator != null) {
                juliaCoordinator.waitFor();
            }
            for (Coordinator coordinator : coordinators) {
                if (coordinator != null) {
                    if (Boolean.getBoolean("com.nextbreakpoint.nextfractal.mandelbrot.javafx.smart-render-disabled")) {
                        final Orbit orbit = orbitFactory.create();
                        final Color color = colorFactory.create();
                        coordinator.setOrbitAndColor(orbit, color);
                    } else {
                        if (orbitChanged) {
                            final Orbit orbit = orbitFactory.create();
                            final Color color = colorFactory.create();
                            coordinator.setOrbitAndColor(orbit, color);
                        } else if (colorChanged) {
                            final Color color = colorFactory.create();
                            coordinator.setColor(color);
                        }
                    }
                    coordinator.init();
                    final View view = new View();
                    view.setTranslation(translation);
                    view.setRotation(rotation);
                    view.setScale(scale);
                    view.setState(new Integer4D(0, 0, 0, 0));
                    view.setJulia(julia);
                    view.setPoint(new ComplexNumber(point.x(), point.y()));
                    coordinator.setView(view);
                    coordinator.setTime(time);
                }
            }
            if (juliaCoordinator != null) {
                if (Boolean.getBoolean("com.nextbreakpoint.nextfractal.mandelbrot.javafx.smart-render-disabled")) {
                    final Orbit orbit = orbitFactory.create();
                    final Color color = colorFactory.create();
                    juliaCoordinator.setOrbitAndColor(orbit, color);
                } else {
                    if (orbitChanged) {
                        final Orbit orbit = orbitFactory.create();
                        final Color color = colorFactory.create();
                        juliaCoordinator.setOrbitAndColor(orbit, color);
                    } else if (colorChanged) {
                        final Color color = colorFactory.create();
                        juliaCoordinator.setColor(color);
                    }
                }
                juliaCoordinator.init();
                final View view = new View();
                view.setTranslation(translation);
                view.setRotation(rotation);
                view.setScale(scale);
                view.setState(new Integer4D(0, 0, 0, 0));
                view.setJulia(true);
                view.setPoint(new ComplexNumber(point.x(), point.y()));
                juliaCoordinator.setView(view);
                juliaCoordinator.setTime(time);
            }
            startCoordinators();
            if (juliaCoordinator != null) {
                juliaCoordinator.run();
            }
            states = renderOrbit(point);
            redrawTraps = true;
            redrawOrbit = true;
            redrawPoint = true;
            if (!julia) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Orbit: point = " + point + ", length = " + states.size());
                }
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
        for (Coordinator coordinator : coordinators) {
            if (coordinator != null) {
                coordinator.abort();
            }
        }
        if (juliaCoordinator != null) {
            juliaCoordinator.abort();
        }
        for (int i = 0; i < coordinators.length; i++) {
            if (coordinators[i] != null) {
                coordinators[i].waitFor();
                coordinators[i].dispose();
                coordinators[i] = null;
            }
        }
        if (juliaCoordinator != null) {
            juliaCoordinator.waitFor();
            juliaCoordinator.dispose();
            juliaCoordinator = null;
        }
    }

    private Coordinator[] createCoordinators(int rows, int columns, Map<String, Integer> hints) {
        final Coordinator[] coordinators = new Coordinator[rows * columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                final Tile tile = GraphicsUtils.createTile(width, height, rows, columns, row, column);
                final Coordinator rendererCoordinator = createRendererCoordinator(hints, tile);
                coordinators[row * columns + column] = rendererCoordinator;
            }
        }
        return coordinators;
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile) {
        return createRendererCoordinator(hints, tile, Thread.MIN_PRIORITY + 2, "Mandelbrot Coordinator");
    }

    private Coordinator createJuliaRendererCoordinator(Map<String, Integer> hints, Tile tile) {
        return createRendererCoordinator(hints, tile, Thread.MIN_PRIORITY + 1, "Julia Coordinator");
    }

    private void abortCoordinators() {
        visitCoordinators(_ -> true, Coordinator::abort);
    }

    private void joinCoordinators() {
        visitCoordinators(_ -> true, Coordinator::waitFor);
    }

    private void startCoordinators() {
        visitCoordinators(_ -> true, Coordinator::run);
    }

    private void visitCoordinators(Predicate<Coordinator> predicate, Consumer<Coordinator> consumer) {
        Arrays.stream(coordinators)
                .filter(coordinator -> coordinator != null && predicate.test(coordinator))
                .forEach(consumer);
    }

    private Coordinator createRendererCoordinator(Map<String, Integer> hints, Tile tile, int priority, String name) {
        final DefaultThreadFactory threadFactory = createThreadFactory(name, priority);
        return new Coordinator(threadFactory, renderFactory, tile, hints);
    }

    private DefaultThreadFactory createThreadFactory(String name, int priority) {
        return new DefaultThreadFactory(name, true, priority);
    }

    private boolean[] createOrbitAndColor(ParserResult result) {
        try {
            final DSLParserResult compilerResult = (DSLParserResult) result.result();
            final boolean[] changed = new boolean[]{false, false};
            final String newASTOrbit = compilerResult.orbitDSL();
            changed[0] = !newASTOrbit.equals(astOrbit);
            astOrbit = newASTOrbit;
            final String newASTColor = compilerResult.colorDSL();
            changed[1] = !newASTColor.equals(astColor);
            astColor = newASTColor;
            orbitFactory = compilerResult.orbitClassFactory();
            colorFactory = compilerResult.colorClassFactory();
            return changed;
        } catch (Exception e) {
            astOrbit = null;
            astColor = null;
            orbitFactory = null;
            colorFactory = null;
            throw e;
        }
    }

    private List<ComplexNumber[]> renderOrbit(Double2D point) {
        final List<ComplexNumber[]> states = new ArrayList<>();
        try {
            if (orbitFactory != null) {
                final Orbit orbit = orbitFactory.create();
                if (orbit != null) {
                    final Scope scope = new Scope();
                    orbit.setScope(scope);
                    orbit.init();
                    orbit.setW(new ComplexNumber(point.x(), point.y()));
                    orbit.setX(orbit.getInitialPoint());
                    orbit.render(states);
                }
            }
        } catch (Throwable e) {
            log.log(Level.WARNING, "Failed to render orbit", e);
        }
        return states;
    }

    private void redrawIfPixelsChanged(Canvas canvas) {
        final GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
        visitCoordinators(Coordinator::isPixelsChanged, coordinator -> coordinator.drawImage(gc, 0, 0));
    }

    private void redrawIfJuliaPixelsChanged(Canvas canvas) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        if (!metadata.isJulia() && juliaCoordinator != null && juliaCoordinator.isPixelsChanged()) {
            final GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            final double dw = canvas.getWidth();
            final double dh = canvas.getHeight();
            gc.clearRect(0, 0, (int) dw, (int) dh);
            juliaCoordinator.drawImage(gc, 0, 0);
//			Number size = juliaCoordinator.getInitialSize();
//			Number center = juliaCoordinator.getInitialCenter();
//			gc.setStroke(renderFactory.createColor(1, 1, 0, 1));
        }
    }

    private void redrawIfPointChanged(Canvas canvas) {
        if (redrawPoint) {
            redrawPoint = false;
            final ComplexNumber size = coordinators[0].getInitialSize();
            final ComplexNumber center = coordinators[0].getInitialCenter();
            final GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            if (states.size() > 1) {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final double[] t = metadata.getTranslation().toArray();
                final double[] r = metadata.getRotation().toArray();
                final double tx = t[0];
                final double ty = t[1];
                final double tz = t[2];
                final double a = -r[2] * Math.PI / 180;
                final double dw = canvas.getWidth();
                final double dh = canvas.getHeight();
                gc.clearRect(0, 0, (int) dw, (int) dh);
                final double cx = dw / 2;
                final double cy = dh / 2;
                gc.setStrokeLine(((float) dw) * 0.002f, GraphicsContext.CAP_BUTT, GraphicsContext.JOIN_MITER, 1f);
                gc.setStroke(renderFactory.createColor(1, 1, 0, 1));
                final double[] point = metadata.getPoint().toArray();
                final double zx = point[0];
                final double zy = point[1];
                final double px = (zx - tx - center.r()) / (tz * size.r());
                final double py = (zy - ty - center.i()) / (tz * size.r());
                final double qx = Math.cos(a) * px + Math.sin(a) * py;
                final double qy = Math.cos(a) * py - Math.sin(a) * px;
                final int x = (int) Math.rint(qx * dw + cx);
                final int y = (int) Math.rint(cy - qy * dh);
                gc.beginPath();
                gc.moveTo(x - 2, y - 2);
                gc.lineTo(x + 2, y - 2);
                gc.lineTo(x + 2, y + 2);
                gc.lineTo(x - 2, y + 2);
                gc.lineTo(x - 2, y - 2);
                gc.stroke();
            }
        }
    }

    private void redrawIfOrbitChanged(Canvas canvas) {
        if (redrawOrbit) {
            redrawOrbit = false;
            ComplexNumber size = coordinators[0].getInitialSize();
            ComplexNumber center = coordinators[0].getInitialCenter();
            GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            if (states.size() > 1) {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final double[] t = metadata.getTranslation().toArray();
                final double[] r = metadata.getRotation().toArray();
                final double tx = t[0];
                final double ty = t[1];
                final double tz = t[2];
                final double a = -r[2] * Math.PI / 180;
                final double dw = canvas.getWidth();
                final double dh = canvas.getHeight();
                gc.clearRect(0, 0, (int) dw, (int) dh);
                final double cx = dw / 2;
                final double cy = dh / 2;
                gc.setStrokeLine(((float) dw) * 0.002f, GraphicsContext.CAP_BUTT, GraphicsContext.JOIN_MITER, 1f);
                gc.setStroke(renderFactory.createColor(1, 0, 0, 1));
                ComplexNumber[] state = states.getFirst();
                double zx = state[0].r();
                double zy = state[0].i();
                double px = (zx - tx - center.r()) / (tz * size.r());
                double py = (zy - ty - center.i()) / (tz * size.r());
                double qx = Math.cos(a) * px + Math.sin(a) * py;
                double qy = Math.cos(a) * py - Math.sin(a) * px;
                int x = (int) Math.rint(qx * dw + cx);
                int y = (int) Math.rint(cy - qy * dh);
                gc.beginPath();
                gc.moveTo(x, y);
                for (int i = 1; i < states.size(); i++) {
                    state = states.get(i);
                    zx = state[0].r();
                    zy = state[0].i();
                    px = (zx - tx - center.r()) / (tz * size.r());
                    py = (zy - ty - center.i()) / (tz * size.r());
                    qx = Math.cos(a) * px + Math.sin(a) * py;
                    qy = Math.cos(a) * py - Math.sin(a) * px;
                    x = (int) Math.rint(qx * dw + cx);
                    y = (int) Math.rint(cy - qy * dh);
                    gc.lineTo(x, y);
                }
                gc.stroke();
            }
        }
    }

    private void redrawIfTrapChanged(Canvas canvas) {
        if (redrawTraps) {
            redrawTraps = false;
            final ComplexNumber size = coordinators[0].getInitialSize();
            final ComplexNumber center = coordinators[0].getInitialCenter();
            final GraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
            if (states.size() > 1) {
                final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
                final double[] t = metadata.getTranslation().toArray();
                final double[] r = metadata.getRotation().toArray();
                final double tx = t[0];
                final double ty = t[1];
                final double tz = t[2];
                final double a = -r[2] * Math.PI / 180;
                final double dw = canvas.getWidth();
                final double dh = canvas.getHeight();
                gc.clearRect(0, 0, (int) dw, (int) dh);
                gc.setStrokeLine(((float) dw) * 0.002f, GraphicsContext.CAP_BUTT, GraphicsContext.JOIN_MITER, 1f);
                gc.setStroke(renderFactory.createColor(1, 1, 0, 1));
                final List<Trap> traps = coordinators[0].getTraps();
                for (Trap trap : traps) {
                    final List<ComplexNumber> points = trap.toPoints();
                    if (!points.isEmpty()) {
                        double zx = points.getFirst().r();
                        double zy = points.getFirst().i();
                        double cx = dw / 2;
                        double cy = dh / 2;
                        double px = (zx - tx - center.r()) / (tz * size.r());
                        double py = (zy - ty - center.i()) / (tz * size.r());
                        double qx = Math.cos(a) * px + Math.sin(a) * py;
                        double qy = Math.cos(a) * py - Math.sin(a) * px;
                        int x = (int) Math.rint(qx * dw + cx);
                        int y = (int) Math.rint(cy - qy * dh);
                        gc.beginPath();
                        gc.moveTo(x, y);
                        for (int i = 1; i < points.size(); i++) {
                            zx = points.get(i).r();
                            zy = points.get(i).i();
                            px = (zx - tx - center.r()) / (tz * size.r());
                            py = (zy - ty - center.i()) / (tz * size.r());
                            qx = Math.cos(a) * px + Math.sin(a) * py;
                            qy = Math.cos(a) * py - Math.sin(a) * px;
                            x = (int) Math.rint(qx * dw + cx);
                            y = (int) Math.rint(cy - qy * dh);
                            gc.lineTo(x, y);
                        }
                        gc.stroke();
                    }
                }
            }
        }
    }

    private void redrawIfToolChanged(Canvas canvas) {
        if (renderingContext.getTool() != null && renderingContext.getTool().isChanged()) {
            renderingContext.getTool().draw(renderFactory.createGraphicsContext(canvas.getGraphicsContext2D()));
        }
    }
}
