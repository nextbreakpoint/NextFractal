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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static java.awt.geom.PathIterator.SEG_CLOSE;
import static java.awt.geom.PathIterator.SEG_LINETO;
import static java.awt.geom.PathIterator.SEG_MOVETO;

public class Bounds {
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    public Bounds() {
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
    }

    public Bounds(Bounds bounds) {
        minX = bounds.minX;
        minY = bounds.minY;
        maxX = bounds.maxX;
        maxY = bounds.maxY;
    }

    public Bounds(double minX, double minY, double maxX, double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public Bounds(AffineTransform transform, java.awt.Shape path, double scale, CommandInfo info) {
        if (!boundingRect(transform, path, scale, info)) {
            invalidate();
        }
    }

    private boolean boundingRect(AffineTransform transform, java.awt.Shape path, double scale, CommandInfo info) {
        double accuracy = scale * 0.1;

        Rectangle2D bounds = getRectangle2D(transform, path, accuracy);

        if ((info.getFlags() & FlagType.CF_FILL.getMask()) != 0) {
            minX = bounds.getMinX();
            minY = bounds.getMinY();
            maxX = bounds.getMaxX();
            maxY = bounds.getMaxY();
        } else if ((info.getFlags() & FlagType.CF_ISO_WIDTH.getMask()) != 0) {
            double pathScale = Math.sqrt(Math.abs(transform.getDeterminant()));
            double v = info.getStrokeWidth() * pathScale / 2;
            minX = bounds.getMinX() - v;
            minY = bounds.getMinY() - v;
            maxX = bounds.getMaxX() + v;
            maxY = bounds.getMaxY() + v;
        } else {
            double v = info.getStrokeWidth() / 2;
            minX = bounds.getMinX() - v;
            minY = bounds.getMinY() - v;
            maxX = bounds.getMaxX() + v;
            maxY = bounds.getMaxY() + v;
        }

        return minX <= maxX && minY <= maxY;
    }

    private Rectangle2D getRectangle2D(AffineTransform transform, Shape path, double accuracy) {
        double scale = Math.sqrt(Math.abs(transform.getDeterminant()));

        double minX = 1;
        double minY = 1;
        double maxX = 0;
        double maxY = 0;
        boolean first = true;

        double[] coords = new double[2];

        for (PathIterator iterator = path.getPathIterator(transform, accuracy * scale); !iterator.isDone(); iterator.next()) {
            switch (iterator.currentSegment(coords)) {
                case SEG_MOVETO:
                case SEG_LINETO:
                    if (first) {
                        minX = coords[0];
                        maxX = coords[0];
                        minY = coords[1];
                        maxY = coords[1];
                        first = false;
                    } else {
                        if (coords[0] < minX) {
                            minX = coords[0];
                        }
                        if (coords[1] < minY) {
                            minY = coords[1];
                        }
                        if (coords[0] > maxX) {
                            maxX = coords[0];
                        }
                        if (coords[1] > maxY) {
                            maxY = coords[1];
                        }
                    }
                    break;
                case SEG_CLOSE:
                    break;
            }
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public void invalidate() {
        minX = Double.POSITIVE_INFINITY;
        minY = Double.POSITIVE_INFINITY;
    }

    public boolean valid() {
        return Double.isFinite(minX) && Double.isFinite(minY) && Double.isFinite(maxX) && Double.isFinite(maxY);
    }

    public Bounds interpolate(Bounds other, double alpha) {
        double beta = 1.0 - alpha;

        Bounds bounds = new Bounds();

        if (!valid() || !other.valid()) return bounds;

        bounds.maxX = beta * maxX + alpha * other.maxX;
        bounds.minX = beta * minX + alpha * other.minX;
        bounds.maxY = beta * maxY + alpha * other.maxY;
        bounds.minY = beta * minY + alpha * other.minY;

        return bounds;
    }

    public Bounds dilate(double dilation) {
        Bounds bounds = new Bounds();

        Point2D.Double center = new Point2D.Double((minX + maxX) * 0.5, (minY + maxY) * 0.5);

        bounds.minX = dilation * (minX - center.x) + center.x;
        bounds.maxX = dilation * (maxX - center.x) + center.x;
        bounds.minY = dilation * (minY - center.y) + center.y;
        bounds.maxY = dilation * (maxY - center.y) + center.y;

        return bounds;
    }

    public Bounds slewCenter(Bounds other, double alpha) {
        Bounds bounds = new Bounds();

        if (!valid() || !other.valid()) return bounds;

        double offsetX = alpha * ((other.maxX + other.minX) - (maxX + minX)) / 2.0;
        double offsetY = alpha * ((other.maxY + other.minY) - (maxY + minY)) / 2.0;

        double absX = Math.abs(offsetX);
        double absY = Math.abs(offsetY);

        bounds.maxX = maxX + absX + offsetX;
        bounds.minX = minX - absX + offsetX;
        bounds.maxY = maxY + absY + offsetY;
        bounds.minY = minY - absY + offsetY;

        return bounds;
    }

    public void gather(Bounds other, double weight) {
        if (!other.valid()) return;

        if (!valid()) {
            maxX = weight * other.maxX;
            minX = weight * other.minX;
            maxY = weight * other.maxY;
            minY = weight * other.minY;
        } else {
            maxX += weight * other.maxX;
            minX += weight * other.minX;
            maxY += weight * other.maxY;
            minY += weight * other.minY;
        }
    }

    public double computeScale(int[] width, int[] height, double borderX, double borderY, boolean modify, AffineTransform transform, boolean exact) {
        double scale = 0;

        double virtualWidth = maxX - minX;
        double virtualHeight = maxY - minY;

        double targetWidth = width[0] - 2.0 * borderX;
        double targetHeight = height[0] - 2.0 * borderY;

        if (!valid()) virtualWidth = virtualHeight = 1.0;

        int newWidth = width[0];
        int newHeight = height[0];

        if (virtualWidth / targetWidth > virtualHeight / targetHeight)
        {
            scale = targetWidth / virtualWidth;
            newHeight = (int)Math.floor(scale * virtualHeight + 2.0 * borderY + 0.5);
            if (!exact) {
                newHeight = newHeight + ((newHeight ^ height[0]) & 0x1);
            }
            if (modify) {
                height[0] = newHeight;
            }
        }
        else {
            scale = targetHeight / virtualHeight;
            newWidth = (int)Math.floor(scale * virtualWidth + 2.0 * borderX + 0.5);
            if (!exact) {
                newWidth = newWidth + ((newWidth ^ width[0]) & 0x1);
            }
            if (modify) {
                width[0] = newWidth;
            }
        }

        if (transform != null) {
            double offsetX = scale * (maxX + minX) / 2.0 - newWidth / 2.0;
            double offsetY = scale * (maxY + minY) / 2.0 - newHeight / 2.0;
            transform.setToScale(scale, scale);
            transform.preConcatenate(AffineTransform.getTranslateInstance(-offsetX, -offsetY));
        }

        return scale;
    }

    public void update(AffineTransform transform, double scale, CommandInfo info) {
        merge(new Bounds(transform, info.getPathStorage().getGeneralPath(), scale, info));
    }

    public void merge(Bounds bounds) {
        if (valid() && bounds.valid()) {
            if (bounds.minX < minX) minX = bounds.minX;
            if (bounds.maxX > maxX) maxX = bounds.maxX;
            if (bounds.minY < minY) minY = bounds.minY;
            if (bounds.maxY > maxY) maxY = bounds.maxY;
        } else if (bounds.valid()) {
            minX = bounds.minX;
            maxX = bounds.maxX;
            minY = bounds.minY;
            maxY = bounds.maxY;
        }
    }

    public void merge(double x, double y) {
        if (valid()) {
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        } else {
            minX = maxX = x;
            minY = maxY = y;
        }
    }

    public void merge(Point2D point) {
        merge(point.getX(), point.getY());
    }

    public boolean overlaps(Bounds bounds) {
        return !(bounds.minX > maxX || bounds.maxX < minX || bounds.minY > maxY || bounds.maxY < minY);
    }
}
