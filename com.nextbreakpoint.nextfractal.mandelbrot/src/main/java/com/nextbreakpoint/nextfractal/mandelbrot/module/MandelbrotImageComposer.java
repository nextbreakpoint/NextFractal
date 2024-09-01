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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Region;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Renderer;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.View;
import lombok.extern.java.Log;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class MandelbrotImageComposer implements ImageComposer {
    private boolean aborted;
    private final boolean opaque;
    private final Tile tile;
    private final ThreadFactory threadFactory;

    public MandelbrotImageComposer(ThreadFactory threadFactory, Tile tile, boolean opaque) {
        this.tile = tile;
        this.opaque = opaque;
        this.threadFactory = threadFactory;
    }

    @Override
    public IntBuffer renderImage(String script, Metadata data) {
        MandelbrotMetadata metadata = (MandelbrotMetadata) data;
        Size suggestedSize = tile.tileSize();
        BufferedImage image = new BufferedImage(suggestedSize.width(), suggestedSize.height(), BufferedImage.TYPE_INT_ARGB);
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
            final DSLParser parser = new DSLParser(DSLParser.getPackageName(), DSLParser.getClassName());
            final DSLParserResult parserResult = parser.parse(script);
            final Orbit orbit = parserResult.orbitClassFactory().create();
            final Color color = parserResult.colorClassFactory().create();
            GraphicsFactory renderFactory = GraphicsUtils.findGraphicsFactory("Java2D");
            Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
            if (metadata.getOptions().isShowPreview() && !metadata.isJulia()) {
                int previewWidth = (int) Math.rint(tile.imageSize().width() * metadata.getOptions().getPreviewSize().x());
                int previewHeight = (int) Math.rint(tile.imageSize().height() * metadata.getOptions().getPreviewSize().y());
                Size tileSize = new Size(previewWidth, previewHeight);
                int x = (int) Math.rint(metadata.getOptions().getPreviewOrigin().x() * tile.imageSize().width());
                int y = (int) Math.rint(metadata.getOptions().getPreviewOrigin().y() * tile.imageSize().height());
                Point tileOffset = new Point(x, tile.imageSize().height() - previewHeight + y);
                renderer.setPreviewTile(new Tile(tile.imageSize(), tileSize, tileOffset, new Size(0, 0)));
            }
            renderer.setOpaque(opaque);
            Double4D translation = metadata.getTranslation();
            Double4D rotation = metadata.getRotation();
            Double4D scale = metadata.getScale();
            Double2D constant = metadata.getPoint();
            Time time = metadata.time();
            boolean julia = metadata.isJulia();
            renderer.setOrbit(orbit);
            renderer.setColor(color);
            renderer.init();
            View view = new View();
            view.setTranslation(translation);
            view.setRotation(rotation);
            view.setScale(scale);
            view.setState(new Integer4D(0, 0, 0, 0));
            view.setJulia(julia);
            view.setPoint(new ComplexNumber(constant.x(), constant.y()));
            renderer.setView(view);
            renderer.setTime(time);
            renderer.runTask();
            renderer.waitForTask();
            if (renderer.isAborted() || renderer.isInterrupted()) {
                aborted = true;
                return buffer;
            }
            GraphicsContext renderContext = renderFactory.createGraphicsContext(g2d);
            renderer.copyImage(renderContext);
            Region region = new Region(orbit.getInitialRegion());
            renderContext.setAffineTransform(createTransform(renderFactory, tile));
            renderContext.setStrokeLine(tile.imageSize().width() * 0.002f, GraphicsContext.CAP_BUTT, GraphicsContext.JOIN_MITER, 1f);
            if (metadata.getOptions().isShowTraps()) {
                drawTraps(renderFactory, renderContext, tile.imageSize(), region, metadata, orbit.getTraps());
            }
            if (metadata.getOptions().isShowOrbit()) {
                java.util.List<ComplexNumber[]> states = renderOrbit(orbit, constant);
                drawOrbit(renderFactory, renderContext, tile.imageSize(), region, metadata, states);
            }
            if (metadata.getOptions().isShowPoint()) {
                drawPoint(renderFactory, renderContext, tile.imageSize(), region, metadata);
            }
        } catch (Throwable e) {
            log.log(Level.WARNING, "Can't render image", e);
            aborted = true;
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        return buffer;
    }

    protected AffineTransform createTransform(GraphicsFactory factory, Tile tile) {
        final Size imageSize = tile.imageSize();
        final Point tileOffset = tile.tileOffset();
        final int centerY = imageSize.height() / 2;
        final AffineTransform affine = factory.createAffineTransform();
        affine.append(factory.createTranslateAffineTransform(0, +centerY));
        affine.append(factory.createScaleAffineTransform(1, -1));
        affine.append(factory.createTranslateAffineTransform(0, -centerY));
        affine.append(factory.createTranslateAffineTransform(-tileOffset.x(), tileOffset.y()));
        return affine;
    }

    private java.util.List<ComplexNumber[]> renderOrbit(Orbit orbit, Double2D point) {
        java.util.List<ComplexNumber[]> states = new ArrayList<>();
        try {
            if (orbit != null) {
                Scope scope = new Scope();
                orbit.setScope(scope);
                orbit.init();
                orbit.setW(new ComplexNumber(point.x(), point.y()));
                orbit.setX(orbit.getInitialPoint());
                orbit.render(states);
            }
        } catch (Throwable e) {
        }
        return states;
    }

    @Override
    public Size getSize() {
        return tile.tileSize();
    }

    @Override
    public boolean isAborted() {
        return aborted;
    }

    private void drawPoint(GraphicsFactory factory, GraphicsContext gc, Size imageSize, Region region, MandelbrotMetadata metadata) {
        ComplexNumber size = region.getSize();
        ComplexNumber center = region.getCenter();
        double[] t = metadata.getTranslation().toArray();
        double[] r = metadata.getRotation().toArray();
        double tx = t[0];
        double ty = t[1];
        double tz = t[2];
        double a = -r[2] * Math.PI / 180;
        double dw = imageSize.width();
        double dh = imageSize.width() * size.i() / size.r();
        double cx = imageSize.width() / 2d;
        double cy = imageSize.height() / 2d;
        gc.setStroke(factory.createColor(1, 1, 0, 1));
        double[] point = metadata.getPoint().toArray();
        double zx = point[0];
        double zy = point[1];
        double px = (zx - tx - center.r()) / (tz * size.r());
        double py = (zy - ty - center.i()) / (tz * size.r());
        double qx = Math.cos(a) * px + Math.sin(a) * py;
        double qy = Math.cos(a) * py - Math.sin(a) * px;
        int x = (int) Math.rint(qx * dw + cx);
        int y = (int) Math.rint(cy - qy * dh);
        gc.beginPath();
        int d = (int) Math.rint(imageSize.width() * 0.0025);
        gc.moveTo(x - d, y - d);
        gc.lineTo(x + d, y - d);
        gc.lineTo(x + d, y + d);
        gc.lineTo(x - d, y + d);
        gc.lineTo(x - d, y - d);
        gc.stroke();
    }

    private void drawOrbit(GraphicsFactory factory, GraphicsContext gc, Size imageSize, Region region, MandelbrotMetadata metadata, List<ComplexNumber[]> states) {
        if (states.size() > 1) {
            ComplexNumber size = region.getSize();
            ComplexNumber center = region.getCenter();
            double[] t = metadata.getTranslation().toArray();
            double[] r = metadata.getRotation().toArray();
            double tx = t[0];
            double ty = t[1];
            double tz = t[2];
            double a = -r[2] * Math.PI / 180;
            double dw = imageSize.width();
            double dh = imageSize.width() * size.i() / size.r();
            double cx = imageSize.width() / 2d;
            double cy = imageSize.height() / 2d;
            gc.setStroke(factory.createColor(1, 0, 0, 1));
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

    private void drawTraps(GraphicsFactory factory, GraphicsContext gc, Size imageSize, Region region, MandelbrotMetadata metadata, java.util.List<Trap> traps) {
        if (!traps.isEmpty()) {
            ComplexNumber size = region.getSize();
            ComplexNumber center = region.getCenter();
            double[] t = metadata.getTranslation().toArray();
            double[] r = metadata.getRotation().toArray();
            double tx = t[0];
            double ty = t[1];
            double tz = t[2];
            double a = -r[2] * Math.PI / 180;
            double dw = imageSize.width();
            double dh = imageSize.width() * size.i() / size.r();
            double cx = imageSize.width() / 2d;
            double cy = imageSize.height() / 2d;
            gc.setStroke(factory.createColor(1, 1, 0, 1));
            for (Trap trap : traps) {
                java.util.List<ComplexNumber> points = trap.toPoints();
                if (!points.isEmpty()) {
                    double zx = points.getFirst().r();
                    double zy = points.getFirst().i();
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
