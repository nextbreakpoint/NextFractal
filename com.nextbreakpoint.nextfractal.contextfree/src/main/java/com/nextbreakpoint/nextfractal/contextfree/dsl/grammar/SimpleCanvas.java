/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType;
import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class SimpleCanvas implements CFCanvas {
    private Graphics2D g2d;
    private RendererTile tile;
    private RendererSize size;
    private AffineTransform normTransform;

    public SimpleCanvas(Graphics2D g2d, RendererTile tile) {
        this.g2d = g2d;
        this.tile = tile;
        final RendererSize tileSize = tile.getTileSize();
        final RendererSize borderSize = tile.getBorderSize();
        final int width = tileSize.getWidth() + borderSize.getWidth() * 2;
        final int height = tileSize.getHeight() + borderSize.getHeight() * 2;
        size = new RendererSize(width, height);
    }

    public int getWidth() {
        return size.getWidth();
    }

    public int getHeight() {
        return size.getHeight();
    }

    public void primitive(int shapeType, double[] color, AffineTransform transform) {
        try {
            g2d.setColor(new Color((float) color[0], (float) color[1], (float) color[2], (float) color[3]));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        AffineTransform oldTransform = g2d.getTransform();

        AffineTransform t = new AffineTransform(normTransform);

        t.concatenate(transform);

        g2d.setTransform(t);

        PrimShape primShape = PrimShape.getShapeMap().get(shapeType);

        if (primShape != null) {
            g2d.fill(primShape.getPath());
        } else {
            throw new RuntimeException("Unexpected shape " + shapeType);
        }

        g2d.setTransform(oldTransform);
    }

    public void path(double[] color, AffineTransform transform, CommandInfo info) {
        try {
            g2d.setColor(new Color((float) color[0], (float) color[1], (float) color[2], (float) color[3]));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        AffineTransform oldTransform = g2d.getTransform();

        AffineTransform t = new AffineTransform(normTransform);

        GeneralPath path = info.getPathStorage().getGeneralPath();

        java.awt.Shape shape = path;

        if ((info.getFlags() & (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) == (FlagType.CF_EVEN_ODD.getMask() | FlagType.CF_FILL.getMask())) {
            path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
        } else {
            path.setWindingRule(GeneralPath.WIND_NON_ZERO);
        }

        if ((info.getFlags() & FlagType.CF_FILL.getMask()) != 0) {
            t.concatenate(transform);
        } else if ((info.getFlags() & FlagType.CF_ISO_WIDTH.getMask()) != 0) {
            double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
            g2d.setStroke(new BasicStroke((float)(info.getStrokeWidth() * scale), mapToCap(info.getFlags()), mapToJoin(info.getFlags()), (float)info.getMiterLimit()));
            t.concatenate(transform);
        } else {
            double scale = Math.sqrt(Math.abs(transform.getDeterminant()));
            g2d.setStroke(new BasicStroke((float)(info.getStrokeWidth() * scale), mapToCap(info.getFlags()), mapToJoin(info.getFlags()), (float)info.getMiterLimit()));
            shape = path.createTransformedShape(transform);
        }

        g2d.setTransform(t);

        if ((info.getFlags() & FlagType.CF_FILL.getMask()) != 0) {
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

    public void start(boolean first, double[] backgroundColor, int currWidth, int currHeight) {
        final RendererSize imageSize = tile.getImageSize();
        final RendererSize borderSize = tile.getBorderSize();
        final RendererPoint tileOffset = tile.getTileOffset();
        normTransform = new AffineTransform();
//        normTransform.translate(0, imageSize.getHeight());
//        normTransform.scale(1, -1);
        normTransform.translate(-tileOffset.getX() + borderSize.getWidth(), -tileOffset.getY() + borderSize.getHeight());
        normTransform.translate(-(currWidth - imageSize.getWidth()) / 2, -(currHeight - imageSize.getHeight()) / 2);
//        normTransform.translate(0, imageSize.getHeight() / 2);
//        normTransform.scale(1, -1);
//        normTransform.translate(0, -imageSize.getHeight() / 2);
    }

    public void clear(double[] backgroundColor) {
        g2d.setColor(new Color((float)backgroundColor[0], (float)backgroundColor[1], (float)backgroundColor[2], (float)backgroundColor[3]));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    public void end() {
        g2d.dispose();
    }

    public void tileTransform(Bounds bounds) {
    }
}
