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
import com.nextbreakpoint.nextfractal.core.render.Java2DRendererFactory;
import com.nextbreakpoint.nextfractal.core.render.Java2DRendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererAffine;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.Renderer;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererRegion;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererView;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

public class MandelbrotImageComposer implements ImageComposer {
    private static final Logger logger = Logger.getLogger(MandelbrotImageComposer.class.getName());

    private boolean aborted;
    private final boolean opaque;
    private final RendererTile tile;
    private final ThreadFactory threadFactory;

    public MandelbrotImageComposer(ThreadFactory threadFactory, RendererTile tile, boolean opaque) {
        this.tile = tile;
        this.opaque = opaque;
        this.threadFactory = threadFactory;
    }

    @Override
    public IntBuffer renderImage(String script, Metadata data) {
        MandelbrotMetadata metadata = (MandelbrotMetadata) data;
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
            DSLParser parser = new DSLParser(DSLParser.class.getPackage().getName() + ".generated", "Compile" + System.nanoTime());
            DSLParserResult result = parser.parse(script);
            DSLCompiler compiler = new DSLCompiler();
            Orbit orbit = compiler.compileOrbit(result).create();
            Color color = compiler.compileColor(result).create();
            Java2DRendererFactory renderFactory = new Java2DRendererFactory();
            Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
            if (metadata.getOptions().isShowPreview() && !metadata.isJulia()) {
                int previewWidth = (int) Math.rint(tile.getImageSize().getWidth() * metadata.getOptions().getPreviewSize().x());
                int previewHeight = (int) Math.rint(tile.getImageSize().getHeight() * metadata.getOptions().getPreviewSize().y());
                RendererSize tileSize = new RendererSize(previewWidth, previewHeight);
                int x = (int) Math.rint(metadata.getOptions().getPreviewOrigin().x() * tile.getImageSize().getWidth());
                int y = (int) Math.rint(metadata.getOptions().getPreviewOrigin().y() * tile.getImageSize().getHeight());
                RendererPoint tileOffset = new RendererPoint(x, tile.getImageSize().getHeight() - previewHeight + y);
                renderer.setPreviewTile(new RendererTile(tile.getImageSize(), tileSize, tileOffset, new RendererSize(0, 0)));
            }
            renderer.setOpaque(opaque);
            Double4D translation = metadata.getTranslation();
            Double4D rotation = metadata.getRotation();
            Double4D scale = metadata.getScale();
            Double2D constant = metadata.getPoint();
            Time time = metadata.getTime();
            boolean julia = metadata.isJulia();
            renderer.setOrbit(orbit);
            renderer.setColor(color);
            renderer.init();
            RendererView view = new RendererView();
            view.setTraslation(translation);
            view.setRotation(rotation);
            view.setScale(scale);
            view.setState(new Integer4D(0, 0, 0, 0));
            view.setJulia(julia);
            view.setPoint(new Number(constant.x(), constant.y()));
            renderer.setView(view);
            renderer.setTime(time);
            renderer.runTask();
            renderer.waitForTasks();
            Java2DRendererGraphicsContext renderContext = new Java2DRendererGraphicsContext(g2d);
            renderer.copyImage(renderContext);
            RendererRegion region = new RendererRegion(orbit.getInitialRegion());
            renderContext.setAffine(createTransform(renderFactory, tile));
            renderContext.setStrokeLine(tile.getImageSize().getWidth() * 0.002f, RendererGraphicsContext.CAP_BUTT, RendererGraphicsContext.JOIN_MITER, 1f);
            if (metadata.getOptions().isShowTraps()) {
                drawTraps(renderFactory, renderContext, tile.getImageSize(), region, metadata, orbit.getTraps());
            }
            if (metadata.getOptions().isShowOrbit()) {
                java.util.List<Number[]> states = renderOrbit(orbit, constant);
                drawOrbit(renderFactory, renderContext, tile.getImageSize(), region, metadata, states);
            }
            if (metadata.getOptions().isShowPoint()) {
                drawPoint(renderFactory, renderContext, tile.getImageSize(), region, metadata);
            }
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

    protected RendererAffine createTransform(RendererFactory factory, RendererTile tile) {
        final RendererSize imageSize = tile.getImageSize();
        final RendererPoint tileOffset = tile.getTileOffset();
        final int centerY = imageSize.getHeight() / 2;
        final RendererAffine affine = factory.createAffine();
        affine.append(factory.createTranslateAffine(0, +centerY));
        affine.append(factory.createScaleAffine(1, -1));
        affine.append(factory.createTranslateAffine(0, -centerY));
        affine.append(factory.createTranslateAffine(-tileOffset.getX(), tileOffset.getY()));
        return affine;
    }

    private java.util.List<Number[]> renderOrbit(Orbit orbit, Double2D point) {
        java.util.List<Number[]> states = new ArrayList<>();
        try {
            if (orbit != null) {
                Scope scope = new Scope();
                orbit.setScope(scope);
                orbit.init();
                orbit.setW(new Number(point.x(), point.y()));
                orbit.setX(orbit.getInitialPoint());
                orbit.render(states);
            }
        } catch (Throwable e) {
        }
        return states;
    }

    @Override
    public RendererSize getSize() {
        return tile.getTileSize();
    }

    public boolean isInterrupted() {
        return aborted;
    }

    private void drawPoint(Java2DRendererFactory factory, Java2DRendererGraphicsContext gc, RendererSize imageSize, RendererRegion region, MandelbrotMetadata metadata) {
        Number size = region.getSize();
        Number center = region.getCenter();
        double[] t = metadata.getTranslation().toArray();
        double[] r = metadata.getRotation().toArray();
        double tx = t[0];
        double ty = t[1];
        double tz = t[2];
        double a = -r[2] * Math.PI / 180;
        double dw = imageSize.getWidth();
        double dh = imageSize.getWidth() * size.i() / size.r();
        double cx = imageSize.getWidth() / 2d;
        double cy = imageSize.getHeight() / 2d;
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
        int d = (int) Math.rint(imageSize.getWidth() * 0.0025);
        gc.moveTo(x - d, y - d);
        gc.lineTo(x + d, y - d);
        gc.lineTo(x + d, y + d);
        gc.lineTo(x - d, y + d);
        gc.lineTo(x - d, y - d);
        gc.stroke();
    }

    private void drawOrbit(Java2DRendererFactory factory, Java2DRendererGraphicsContext gc, RendererSize imageSize, RendererRegion region, MandelbrotMetadata metadata, List<Number[]> states) {
        if (states.size() > 1) {
            Number size = region.getSize();
            Number center = region.getCenter();
            double[] t = metadata.getTranslation().toArray();
            double[] r = metadata.getRotation().toArray();
            double tx = t[0];
            double ty = t[1];
            double tz = t[2];
            double a = -r[2] * Math.PI / 180;
            double dw = imageSize.getWidth();
            double dh = imageSize.getWidth() * size.i() / size.r();
            double cx = imageSize.getWidth() / 2d;
            double cy = imageSize.getHeight() / 2d;
            gc.setStroke(factory.createColor(1, 0, 0, 1));
            Number[] state = states.get(0);
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

    private void drawTraps(Java2DRendererFactory factory, Java2DRendererGraphicsContext gc, RendererSize imageSize, RendererRegion region, MandelbrotMetadata metadata, java.util.List<Trap> traps) {
        if (!traps.isEmpty()) {
            Number size = region.getSize();
            Number center = region.getCenter();
            double[] t = metadata.getTranslation().toArray();
            double[] r = metadata.getRotation().toArray();
            double tx = t[0];
            double ty = t[1];
            double tz = t[2];
            double a = -r[2] * Math.PI / 180;
            double dw = imageSize.getWidth();
            double dh = imageSize.getWidth() * size.i() / size.r();
            double cx = imageSize.getWidth() / 2d;
            double cy = imageSize.getHeight() / 2d;
            gc.setStroke(factory.createColor(1, 1, 0, 1));
            for (Trap trap : traps) {
                java.util.List<Number> points = trap.toPoints();
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
