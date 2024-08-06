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

import com.nextbreakpoint.nextfractal.contextfree.core.Bounds;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import lombok.extern.java.Log;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
    public void primitive(int shapeType, double[] color, AffineTransform transform) {
        if (shapeType == ShapeType.FillType.ordinal()) {
            canvas.primitive(shapeType, color, transform);
            return;
        }
        for (Point2D.Double tile : tileList) {
            AffineTransform t = AffineTransform.getTranslateInstance(tile.x, tile.y);
            t.concatenate(transform);
            canvas.primitive(shapeType, color, t);
        }
    }

    @Override
    public void path(double[] color, AffineTransform transform, GeneralPath path, long flags, double strokeWidth, double miterLimit) {
        for (Point2D.Double tile : tileList) {
            AffineTransform t = AffineTransform.getTranslateInstance(tile.x, tile.y);
            t.concatenate(transform);
            canvas.path(color, t, path, flags, strokeWidth, miterLimit);
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
        canvas.end();
    }

    public void setScale(double scale) {
        AffineTransform t = AffineTransform.getScaleInstance(scale, scale);

        // Generate the tiling transform in pixel units
        transformOffset = new AffineTransform(transform);
        transformOffset.concatenate(t);

        // The invert transform can transform coordinates from the pixel unit tiling
        // to the unit square tiling.
        if (frieze != FriezeType.NoFrieze) {
            transformInvert = AffineTransform.getScaleInstance(transformOffset.getScaleX() == 0.0 ? 0.0 : 1 / transformOffset.getScaleX(), transformOffset.getScaleY() == 0.0 ? 0.0 : 1 / transformOffset.getScaleY());
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

        Point2D.Double p = new Point2D.Double(centx, centy);

        transformInvert.transform(p, p);          // transform to unit square tesselation
        centx = Math.floor(p.x + 0.5);                 // round to nearest integer
        centy = Math.floor(p.y + 0.5);                 // round to nearest integer

        tileList.clear();

        double dx = -centx;
        double dy = -centy;

        Point2D.Double o = new Point2D.Double(dx, dy);

        transformOffset.transform(o, o);

        tileList.add(o);

        Bounds rect = new Bounds(-5, -5, width + 9, height + 9);

        if (frieze != FriezeType.NoFrieze) {
            centx += centy;
            for (int offset = 1; ; ++offset) {
                boolean hit = false;
                for (int side : Arrays.asList(-1, 1)) {
                    double x = offset * side - centx;
                    Point2D.Double d = new Point2D.Double(x, x);
                    transformOffset.transform(d, d);
                    // If the tile might touch the canvas then record it
                    Bounds shape = new Bounds(bounds.getMinX() + d.x, bounds.getMinY() + d.y, bounds.getMaxX() + d.x, bounds.getMaxY() + d.y);
                    if (shape.overlaps(rect)) {
                        hit = true;
                        tileList.add(d);
                    }
                }
                if (!hit) return;
            }
        }

        for (int ring = 1; ; ring++) {
            boolean hit = false;
            for (int pos = -ring; pos < ring; pos++) {
                List<Point2D.Double> points = List.of(new Point2D.Double(pos, -ring), new Point2D.Double(ring, pos), new Point2D.Double(-pos, ring), new Point2D.Double(-ring, -pos));
                for (Point2D.Double point : points) {
                    // Find where this tile is on the canvas
                    Point2D.Double d = new Point2D.Double(point.x - centx, point.y - centy);
                    transformOffset.transform(d, d);
                    // If the tile might touch the canvas then record it
                    Bounds shape = new Bounds(bounds.getMinX() + d.x, bounds.getMinY() + d.y, bounds.getMaxX() + d.x, bounds.getMaxY() + d.y);
                    if (shape.overlaps(rect)) {
                        hit = true;
                        tileList.add(d);
                    }
                }
            }
            if (!hit) return;
        }
    }

    @Override
    public void clear(double[] backgroundColor) {
        canvas.clear(backgroundColor);
    }

    public List<Point2D.Double> getTesselation(int w, int h, int x1, int y1, boolean flipY) {
        List<Point2D.Double> tessPoints = new ArrayList<>();

        // Produce an integer version of mOffset that is centered in the w x h screen
        AffineTransform tess = new AffineTransform(width, Math.floor(transformOffset.getShearY() + 0.5), Math.floor(transformOffset.getShearX() + 0.5), flipY ? -height : height, x1, y1);

        Bounds screen = new Bounds(0, 0, w - 1, h - 1);

        if (frieze == FriezeType.FriezeX) {
            tess.scale(1, 0);
        }
        if (frieze == FriezeType.FriezeY) {
            tess.scale(0, 1);
        }

        tessPoints.add(new Point2D.Double(x1, y1));

        if (frieze != FriezeType.NoFrieze) {
            for (int offset = 1; ; ++offset) {
                boolean hit = false;
                for (int side : Arrays.asList(-1, 1)) {
                    int x = offset * side;
                    Point2D.Double d = new Point2D.Double(x, x);
                    tess.transform(d, d);
                    // If the tile is visible then record it
                    int px = (int) Math.floor(d.x + 0.5);
                    int py = (int) Math.floor(d.y + 0.5);
                    Bounds tile = new Bounds(px, py, px + width - 1, py + height - 1);
                    if (tile.overlaps(screen)) {
                        hit = true;
                        tessPoints.add(d);
                    }
                }
                if (!hit) return tessPoints;
            }
        }

        // examine rings of tile units around the center unit until you encounter a
        // ring that doesn't have any tile units that intersect the screen. Then stop.
        for (int ring = 1; ; ring++) {
            boolean hit = false;
            for (int pos = -ring; pos < ring; pos++) {
                List<Point2D.Double> points = List.of(new Point2D.Double(pos, -ring), new Point2D.Double(ring, pos), new Point2D.Double(-pos, ring), new Point2D.Double(-ring, -pos));
                for (Point2D.Double point : points) {
                    // Find where this tile is on the canvas
                    Point2D.Double d = new Point2D.Double(point.x, point.y);
                    tess.transform(d, d);
                    // If the tile is visible then record it
                    int px = (int) Math.floor(d.x + 0.5);
                    int py = (int) Math.floor(d.y + 0.5);
                    Bounds tile = new Bounds(px, py, px + width - 1, py + height - 1);
                    if (tile.overlaps(screen)) {
                        hit = true;
                        tessPoints.add(d);
                    }
                }
            }
            if (!hit) return tessPoints;
        }
    }
}
