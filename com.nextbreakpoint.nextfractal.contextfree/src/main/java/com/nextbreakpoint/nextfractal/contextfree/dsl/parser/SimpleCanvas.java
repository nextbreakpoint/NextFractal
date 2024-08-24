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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import lombok.extern.java.Log;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

@Log
public class SimpleCanvas implements CFCanvas {
    private final Graphics2D g2d;
    private final Tile tile;
    private final Size size;
    private AffineTransform normTransform;

    public SimpleCanvas(Graphics2D g2d, Tile tile) {
        this.g2d = g2d;
        this.tile = tile;
        final Size tileSize = tile.tileSize();
        final Size borderSize = tile.borderSize();
        final int width = tileSize.width() + borderSize.width() * 2;
        final int height = tileSize.height() + borderSize.height() * 2;
        size = new Size(width, height);
    }

    @Override
    public int getWidth() {
        return size.width();
    }

    @Override
    public int getHeight() {
        return size.height();
    }

    @Override
    public void primitive(int shapeType, double[] color, AffineTransform transform, int blend) {
        try {
            g2d.setColor(new Color((float) color[0], (float) color[1], (float) color[2], (float) color[3]));
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Can't set color", e);
        }

        //TODO implement blend

        final AffineTransform oldTransform = g2d.getTransform();

        final AffineTransform t = new AffineTransform(normTransform);

        t.concatenate(transform);

        g2d.setTransform(t);

        final PrimShape primShape = PrimShape.getShapeMap().get(shapeType);

        if (primShape != null) {
            g2d.fill(primShape.getPath());
        } else {
            throw new RuntimeException("Unexpected shape " + shapeType);
        }

        g2d.setTransform(oldTransform);
    }

    @Override
    public void path(double[] color, AffineTransform transform, GeneralPath path, long flags, double strokeWidth, double miterLimit, int blend) {
        try {
            g2d.setColor(new Color((float) color[0], (float) color[1], (float) color[2], (float) color[3]));
        } catch (IllegalArgumentException e) {
            log.log(Level.WARNING, "Can't set color", e);
        }

        //TODO implement blend

        final AffineTransform oldTransform = g2d.getTransform();

        final AffineTransform t = new AffineTransform(normTransform);

        java.awt.Shape shape = path;

        if ((flags & (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) == (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) {
            path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
        } else {
            path.setWindingRule(GeneralPath.WIND_NON_ZERO);
        }

        if ((flags & FlagType.CF_FILL.getMask()) != 0) {
            t.concatenate(transform);
        } else {
            final int cap = mapToCap(flags);
            final int join = mapToJoin(flags);
            if ((flags & FlagType.CF_ISO_WIDTH.getMask()) != 0) {
                final double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
                g2d.setStroke(new BasicStroke((float) (strokeWidth * scale), cap, join, (float) miterLimit));
                t.concatenate(transform);
            } else {
                final double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
                g2d.setStroke(new BasicStroke((float) (strokeWidth * scale), cap, join, (float) miterLimit));
                shape = path.createTransformedShape(transform);
            }
        }

        g2d.setTransform(t);

        if ((flags & FlagType.CF_FILL.getMask()) != 0) {
            g2d.fill(shape);
        } else {
            g2d.draw(shape);
        }

        g2d.setTransform(oldTransform);
    }

    private int mapToJoin(long flags) {
        if ((flags & FlagType.CF_MITER_JOIN.getMask()) != 0) {
            return BasicStroke.JOIN_MITER;
        } else if ((flags & FlagType.CF_ROUND_JOIN.getMask()) != 0) {
            return BasicStroke.JOIN_ROUND;
        } else if ((flags & FlagType.CF_BEVEL_JOIN.getMask()) != 0) {
            return BasicStroke.JOIN_BEVEL;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    private int mapToCap(long flags) {
        if ((flags & FlagType.CF_BUTT_CAP.getMask()) != 0) {
            return BasicStroke.CAP_BUTT;
        } else if ((flags & FlagType.CF_ROUND_CAP.getMask()) != 0) {
            return BasicStroke.CAP_ROUND;
        } else if ((flags & FlagType.CF_SQUARE_CAP.getMask()) != 0) {
            return BasicStroke.CAP_SQUARE;
        } else {
            throw new RuntimeException("Invalid flags " + flags);
        }
    }

    @Override
    public void start(boolean first, double[] backgroundColor, int currWidth, int currHeight) {
        final Size imageSize = tile.imageSize();
        final Size borderSize = tile.borderSize();
        final Point tileOffset = tile.tileOffset();
        normTransform = new AffineTransform();
        normTransform.translate(-tileOffset.x() + borderSize.width(), -tileOffset.y() + borderSize.height());
        normTransform.translate(-(currWidth - imageSize.width()) / 2d, -(currHeight - imageSize.height()) / 2d);
    }

    @Override
    public void clear(double[] backgroundColor) {
        g2d.setColor(new Color((float)backgroundColor[0], (float)backgroundColor[1], (float)backgroundColor[2], (float)backgroundColor[3]));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    @Override
    public void drawRect(double x, double y, double width, double height) {
        g2d.setColor(new Color(1, 1, 1, 0.9f));
        final AffineTransform oldTransform = g2d.getTransform();
        g2d.setTransform(normTransform);
        g2d.setStroke(new BasicStroke((float) 1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 2));
        g2d.draw(new Rectangle2D.Double(x, y, width, height));
        g2d.setTransform(oldTransform);
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public void end() {
        g2d.dispose();
    }
}
