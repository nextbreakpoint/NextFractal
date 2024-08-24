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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import lombok.extern.java.Log;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

// tiledCanvas.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2006-2016 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA

@Log
public class TiledCanvas implements CFCanvas {
    private final List<Point2D.Double> tileList;
    private final AffineTransform transform;
    private AffineTransform transformInvert;
    private AffineTransform transformOffset;
    private final FriezeType frieze;
    private final CFCanvas canvas;
    private int width;
    private int height;

    public TiledCanvas(CFCanvas canvas, AffineTransform transform, FriezeType frieze) {
        this.canvas = canvas;
        this.frieze = frieze;
        this.transform = transform;
        transformInvert = new AffineTransform();
        transformOffset = new AffineTransform();
        tileList = new ArrayList<>();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void primitive(int shapeType, double[] color, AffineTransform transform, int blend) {
        if (shapeType == PrimShapeType.fillType.getType()) {
            canvas.primitive(shapeType, color, transform, blend);
            return;
        }
        for (Point2D.Double tile : tileList) {
            final AffineTransform t = AffineTransform.getTranslateInstance(tile.x, tile.y);
            t.concatenate(transform);
            canvas.primitive(shapeType, color, t, blend);
        }
    }

    @Override
    public void path(double[] color, AffineTransform transform, GeneralPath path, long flags, double strokeWidth, double miterLimit, int blend) {
        for (Point2D.Double tile : tileList) {
            final AffineTransform t = AffineTransform.getTranslateInstance(tile.x, tile.y);
            t.concatenate(transform);
            canvas.path(color, t, path, flags, strokeWidth, miterLimit, blend);
        }
    }

    @Override
    public void start(boolean first, double[] backgroundColor, int currWidth, int currHeight) {
        width = currWidth;
        height = currHeight;
        canvas.start(first, backgroundColor, currWidth, currHeight);
    }

    @Override
    public void end() {
        canvas.drawRect(0, 0, width, height);
        canvas.end();
    }

    @Override
    public void clear(double[] backgroundColor) {
        canvas.clear(backgroundColor);
    }

    @Override
    public void drawRect(double x, double y, double width, double height) {
        canvas.drawRect(x, y, width, height);
    }

    public void setScale(double scale) {
        final AffineTransform t = AffineTransform.getScaleInstance(scale, scale);

        // Generate the tiling transform in pixel units
        transformOffset = new AffineTransform(transform);
        transformOffset.concatenate(t);

        // The invert transform can transform coordinates from the pixel unit tiling
        // to the unit square tiling.
        if (frieze != FriezeType.NoFrieze) {
            transformInvert.setToScale(transformOffset.getScaleX() == 0.0 ? 0.0 : 1 / transformOffset.getScaleX(), transformOffset.getScaleY() == 0.0 ? 0.0 : 1 / transformOffset.getScaleY());
        } else {
            transformInvert = new AffineTransform(transformOffset);
            try {
                transformInvert.invert();
            } catch (NoninvertibleTransformException e) {
                log.log(Level.WARNING, "Can't invert transform", e);
            }
        }
    }

    public void tileTransform(Bounds bounds) {
        double centx = (bounds.getMinX() + bounds.getMaxX()) * 0.5;
        double centy = (bounds.getMinY() + bounds.getMaxY()) * 0.5;

        final Point2D.Double p = new Point2D.Double(centx, centy);
        transformInvert.transform(p, p);          // transform to unit square tesselation
        centx = Math.floor(p.x + 0.5);                 // round to nearest integer
        centy = Math.floor(p.y + 0.5);                 // round to nearest integer

        tileList.clear();

        final double dx = -centx;
        final double dy = -centy;

        final Point2D.Double o = new Point2D.Double(dx, dy);
        transformOffset.transform(o, o);
        tileList.add(o);

        final Bounds canvas = new Bounds(-5, -5, width + 9, height + 9);

        if (frieze != FriezeType.NoFrieze)
            centx = centy = centx + centy;      // one will be zero, set them both to the other one

        for (int ring = 1; ; ring++) {
            boolean hit = false;
            if (frieze != FriezeType.NoFrieze) {
                // Works for x frieze and y frieze, the other dimension gets zeroed
                hit = checkTile(bounds, canvas,  ring - centx,  ring - centy);
                hit = checkTile(bounds, canvas, -ring - centx, -ring - centy) || hit;
            } else {
                for (int pos = -ring; pos < ring; pos++) {
                    hit = checkTile(bounds, canvas,   pos - centx, -ring - centy) || hit;
                    hit = checkTile(bounds, canvas,  ring - centx,   pos - centy) || hit;
                    hit = checkTile(bounds, canvas,  -pos - centx,  ring - centy) || hit;
                    hit = checkTile(bounds, canvas, -ring - centx,  -pos - centy) || hit;
                }
            }
            if (!hit) return;
        }
    }

    private boolean checkTile(Bounds bounds, Bounds canvas, double dx, double dy) {
        final Point2D.Double d = new Point2D.Double(dx, dy);
        transformOffset.transform(d, d);
        final Bounds shape = new Bounds(bounds.getMinX() + d.x, bounds.getMinY() + d.y, bounds.getMaxX() + d.x, bounds.getMaxY() + d.y);
        final boolean hit = shape.overlaps(canvas);
        if (hit) {
            tileList.add(d);
        }
        return hit;
    }

    private boolean checkTileInt(Bounds screen, AffineTransform screenTessellation, int x, int y, List<Point2D.Double> points) {
        final Point2D.Double d = new Point2D.Double(x, y);
        screenTessellation.transform(d, d);
        final int px = (int) Math.floor(d.x + 0.5);
        final int py = (int) Math.floor(d.y + 0.5);
        final Bounds shape = new Bounds(px, py, width - 1 + px, height - 1 + py);
        final boolean hit = shape.overlaps(screen);
        if (hit) {
            points.add(d);
        }
        return hit;
    }

    public List<Point2D.Double> getTesselation(int w, int h, int x1, int y1, boolean flipY) {
        final List<Point2D.Double> tessPoints = new ArrayList<>();

        // Produce an integer version of mOffset that is centered in the w x h screen
        final AffineTransform tess = new AffineTransform(width, Math.floor(transformOffset.getShearY() + 0.5), Math.floor(transformOffset.getShearX() + 0.5), flipY ? -height : height, x1, y1);

        final Bounds screen = new Bounds(0, 0, w - 1, h - 1);

        if (frieze == FriezeType.FriezeX) {
            tess.scale(1, 0);
        }
        if (frieze == FriezeType.FriezeY) {
            tess.scale(0, 1);
        }

        tessPoints.add(new Point2D.Double(x1, y1));

        for (int ring = 1; ; ring++) {
            boolean hit = false;
            if (frieze != FriezeType.NoFrieze) {
                // Works for x frieze and y frieze, the other dimension gets zeroed
                hit = checkTileInt(screen, tess,  ring,  ring, tessPoints);
                hit = checkTileInt(screen, tess, -ring, -ring, tessPoints) || hit;
            } else {
                for (int pos = -ring; pos < ring; pos++) {
                    hit = checkTileInt(screen, tess,   pos, -ring, tessPoints) || hit;
                    hit = checkTileInt(screen, tess,  ring,   pos, tessPoints) || hit;
                    hit = checkTileInt(screen, tess,  -pos,  ring, tessPoints) || hit;
                    hit = checkTileInt(screen, tess, -ring,  -pos, tessPoints) || hit;
                }
            }

            if (!hit) break;
        }

        return tessPoints;
    }
}
