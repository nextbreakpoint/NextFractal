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
package com.nextbreakpoint.nextfractal.contextfree.graphics;

import com.nextbreakpoint.nextfractal.contextfree.core.Bounds;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.Color;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import lombok.Getter;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import static org.apache.batik.ext.awt.geom.ExtendedPathIterator.SEG_CLOSE;
import static org.apache.batik.ext.awt.geom.ExtendedPathIterator.SEG_CUBICTO;
import static org.apache.batik.ext.awt.geom.ExtendedPathIterator.SEG_LINETO;
import static org.apache.batik.ext.awt.geom.ExtendedPathIterator.SEG_MOVETO;
import static org.apache.batik.ext.awt.geom.ExtendedPathIterator.SEG_QUADTO;

public class Canvas implements CFCanvas {
    private final GraphicsContext context;
    private final GraphicsFactory factory;
    @Getter
    private final int width;
    @Getter
    private final int height;
    private AffineTransform normTransform;

    public Canvas(GraphicsFactory factory, Object context, int width, int height) {
        this.context = factory.createGraphicsContext(context);
        this.factory = factory;
        this.width = width;
        this.height = height;
    }

    public void primitive(int shapeType, double[] color, java.awt.geom.AffineTransform transform) {
        context.save();

        AffineTransform affine = factory.createAffineTransform();
        affine.append(normTransform);

        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        AffineTransform newAffine = factory.createAffineTransform(matrix);
        affine.append(newAffine);

        affine.setAffineTransform(context);

        PrimShape primShape = PrimShape.getShapeMap().get(shapeType);
        if (primShape != null) {
            Color c = factory.createColor((float) color[0], (float) color[1], (float) color[2], (float) color[3]);
            context.setFill(c);
            fill(primShape.getPath(), GraphicsContext.EVEN_ODD);
        } else {
            throw new RuntimeException("Unexpected shape " + shapeType);
        }

        context.restore();
    }

    public void path(double[] color, java.awt.geom.AffineTransform transform, GeneralPath path, long flags, double strokeWidth, double miterLimit) {
        context.save();

        AffineTransform affine = factory.createAffineTransform();
        affine.append(normTransform);

        Shape shape;

        if ((flags & (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) == (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) {
            path.setWindingRule(GraphicsContext.EVEN_ODD);
        } else {
            path.setWindingRule(GraphicsContext.NON_ZERO);
        }

        if ((flags & FlagType.CF_FILL.getMask()) != 0) {
            shape = path;
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            AffineTransform newAffine = factory.createAffineTransform(matrix);
            affine.append(newAffine);
        } else if ((flags & FlagType.CF_ISO_WIDTH.getMask()) != 0) {
            double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
            context.setStrokeLine((float) (strokeWidth * scale), mapToCap(flags), mapToJoin(flags), (float) miterLimit);
            shape = path;
            double[] matrix = new double[6];
            transform.getMatrix(matrix);
            AffineTransform newAffine = factory.createAffineTransform(matrix);
            affine.append(newAffine);
        } else {
            context.setStrokeLine((float) strokeWidth, mapToCap(flags), mapToJoin(flags), (float) miterLimit);
            shape = path.createTransformedShape(transform);
        }

        affine.setAffineTransform(context);

        Color c = factory.createColor((float) color[0], (float) color[1], (float) color[2], (float) color[3]);
        if ((flags & FlagType.CF_FILL.getMask()) != 0) {
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
        PathIterator iterator = path.getPathIterator(new java.awt.geom.AffineTransform());
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
            return GraphicsContext.JOIN_MITER;
        } else if ((flags & FlagType.CF_ROUND_JOIN.getMask()) != 0) {
            return GraphicsContext.JOIN_ROUND;
        } else if ((flags & FlagType.CF_BEVEL_JOIN.getMask()) != 0) {
            return GraphicsContext.JOIN_BEVEL;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    private int mapToCap(long flags) {
        if ((flags & FlagType.CF_BUTT_CAP.getMask()) != 0) {
            return GraphicsContext.CAP_BUTT;
        } else if ((flags & FlagType.CF_ROUND_CAP.getMask()) != 0) {
            return GraphicsContext.CAP_ROUND;
        } else if ((flags & FlagType.CF_SQUARE_CAP.getMask()) != 0) {
            return GraphicsContext.CAP_SQUARE;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    @Override
    public void clear(double[] backgroundColor) {
        Color c = factory.createColor((float) backgroundColor[0], (float) backgroundColor[1], (float) backgroundColor[2], (float) backgroundColor[3]);
        context.setFill(c);
        context.fillRect(0, 0, getWidth(), getHeight());
    }

    public void start(boolean first, double[] backgroundColor, int currWidth, int currHeight) {
        normTransform = factory.createTranslateAffineTransform(0, getHeight());
        normTransform.append(factory.createScaleAffineTransform(1, -1));
        normTransform.append(factory.createTranslateAffineTransform(-(currWidth - getWidth()) / 2f, -(currHeight - getHeight()) / 2f));
    }

    public void end() {
    }

    @Override
    public void tileTransform(Bounds bounds) {
    }
}
