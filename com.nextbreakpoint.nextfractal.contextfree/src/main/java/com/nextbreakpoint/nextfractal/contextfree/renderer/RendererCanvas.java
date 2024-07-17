/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.contextfree.renderer;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Bounds;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CommandInfo;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType;
import com.nextbreakpoint.nextfractal.core.render.RendererAffine;
import com.nextbreakpoint.nextfractal.core.render.RendererColor;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import static com.nextbreakpoint.nextfractal.contextfree.core.ExtendedPathIterator.SEG_CLOSE;
import static com.nextbreakpoint.nextfractal.contextfree.core.ExtendedPathIterator.SEG_CUBICTO;
import static com.nextbreakpoint.nextfractal.contextfree.core.ExtendedPathIterator.SEG_LINETO;
import static com.nextbreakpoint.nextfractal.contextfree.core.ExtendedPathIterator.SEG_MOVETO;
import static com.nextbreakpoint.nextfractal.contextfree.core.ExtendedPathIterator.SEG_QUADTO;

public class RendererCanvas implements CFCanvas {
    private RendererGraphicsContext context;
    private RendererFactory factory;
    private int width;
    private int height;
    private RendererAffine normTransform;

    public RendererCanvas(RendererFactory factory, Object context, int width, int height) {
        this.context = factory.createGraphicsContext(context);
        this.factory = factory;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void primitive(int shapeType, double[] color, AffineTransform transform) {
        context.save();

        RendererAffine affine = factory.createAffine();
        affine.append(normTransform);

        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        RendererAffine newAffine = factory.createAffine(matrix);
        affine.append(newAffine);

        affine.setAffine(context);

        PrimShape primShape = PrimShape.getShapeMap().get(shapeType);
        if (primShape != null) {
            RendererColor c = factory.createColor((float) color[0], (float) color[1], (float) color[2], (float) color[3]);
            context.setFill(c);
            fill(primShape.getPath(), RendererGraphicsContext.EVEN_ODD);
        } else {
            throw new RuntimeException("Unexpected shape " + shapeType);
        }

        context.restore();
    }

    public void path(double[] color, AffineTransform transform, CommandInfo info) {
        context.save();

        RendererAffine affine = factory.createAffine();
        affine.append(normTransform);

        Shape shape;

        GeneralPath path = info.getPathStorage().getGeneralPath();

        if ((info.getFlags() & (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) == (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) {
            path.setWindingRule(RendererGraphicsContext.EVEN_ODD);
        } else {
            path.setWindingRule(RendererGraphicsContext.NON_ZERO);
        }

        if ((info.getFlags() & FlagType.CF_FILL.getMask()) != 0) {
            shape = path;
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            RendererAffine newAffine = factory.createAffine(matrix);
            affine.append(newAffine);
        } else if ((info.getFlags() & FlagType.CF_ISO_WIDTH.getMask()) != 0) {
            double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
            context.setStrokeLine((float)(info.getStrokeWidth() * scale), mapToCap(info.getFlags()), mapToJoin(info.getFlags()), (float)info.getMiterLimit());
            shape = path;
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            RendererAffine newAffine = factory.createAffine(matrix);
            affine.append(newAffine);
        } else {
            context.setStrokeLine((float)info.getStrokeWidth(), mapToCap(info.getFlags()), mapToJoin(info.getFlags()), (float)info.getMiterLimit());
            shape = path.createTransformedShape(transform);
        }

        affine.setAffine(context);

        RendererColor c = factory.createColor((float) color[0], (float) color[1], (float) color[2], (float) color[3]);
        if ((info.getFlags() & FlagType.CF_FILL.getMask()) != 0) {
            context.setFill(c);
            fill(shape, path.getWindingRule());
        } else {
            context.setStroke(c);
            stroke(shape, path.getWindingRule());
        }

        context.restore();
    }

    private void fill(Shape path, int windingRule) {
        context.beginPath();
        createPath(path);
        context.setWindingRule(windingRule);
        context.fill();
    }

    private void stroke(Shape path, int windingRule) {
        context.beginPath();
        createPath(path);
        context.setWindingRule(windingRule);
        context.stroke();
    }

    private void createPath(Shape path) {
        PathIterator iterator = path.getPathIterator(new AffineTransform());
        float[] coords = new float[20];
        while (!iterator.isDone()) {
            int code = iterator.currentSegment(coords);
            switch (code) {
                case SEG_MOVETO: {
                    context.moveTo(coords[0], coords[1]);
                    break;
                }
                case SEG_LINETO: {
                    context.lineTo(coords[0], coords[1]);
                    break;
                }
                case SEG_QUADTO: {
                    context.quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                }
                case SEG_CUBICTO: {
                    context.cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                }
//                case SEG_ARCTO: {
//                    context.arcTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6]);
//                    break;
//                }
                case SEG_CLOSE: {
                    context.closePath();
                    break;
                }
            }
            iterator.next();
        }
    }

    private int mapToJoin(long flags) {
        if ((flags & FlagType.CF_MITER_JOIN.getMask()) != 0) {
            return RendererGraphicsContext.JOIN_MITER;
        } else if ((flags & FlagType.CF_ROUND_JOIN.getMask()) != 0) {
            return RendererGraphicsContext.JOIN_ROUND;
        } else if ((flags & FlagType.CF_BEVEL_JOIN.getMask()) != 0) {
            return RendererGraphicsContext.JOIN_BEVEL;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    private int mapToCap(long flags) {
        if ((flags & FlagType.CF_BUTT_CAP.getMask()) != 0) {
            return RendererGraphicsContext.CAP_BUTT;
        } else if ((flags & FlagType.CF_ROUND_CAP.getMask()) != 0) {
            return RendererGraphicsContext.CAP_ROUND;
        } else if ((flags & FlagType.CF_SQUARE_CAP.getMask()) != 0) {
            return RendererGraphicsContext.CAP_SQUARE;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    @Override
    public void clear(double[] backgroundColor) {
        RendererColor c = factory.createColor((float) backgroundColor[0], (float) backgroundColor[1], (float) backgroundColor[2], (float) backgroundColor[3]);
        context.setFill(c);
        context.fillRect(0, 0, getWidth(), getHeight());
    }

    public void start(boolean first, double[] backgroundColor, int currWidth, int currHeight) {
        normTransform = factory.createTranslateAffine(0, getHeight());
        normTransform.append(factory.createScaleAffine(1, -1));
        normTransform.append(factory.createTranslateAffine(-(currWidth - getWidth()) / 2, -(currHeight - getHeight()) / 2));
    }

    public void end() {
    }

    @Override
    public void tileTransform(Bounds bounds) {
    }
}
