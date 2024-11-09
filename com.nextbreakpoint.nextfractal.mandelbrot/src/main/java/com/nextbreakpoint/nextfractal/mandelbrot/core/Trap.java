/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.core;

import lombok.Getter;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

public class Trap {
	private final Path2D.Double path2d = new Path2D.Double();
	@Getter
    private final ComplexNumber center;
	
	public Trap(ComplexNumber center) {
		this.center = center;
	}

    public Trap moveTo(ComplexNumber x) {
		path2d.moveTo(x.r(), -x.i());
		return this;
	}

	public Trap lineTo(ComplexNumber x) {
		path2d.lineTo(x.r(), -x.i());
		return this;
	}

	public Trap arcTo(ComplexNumber p, ComplexNumber x) {
		path2d.curveTo(p.r(), -p.i(), x.r(), -x.i(), x.r(), -x.i());
		return this;
	}

	public Trap quadTo(ComplexNumber p, ComplexNumber x) {
		path2d.quadTo(p.r(), -p.i(), x.r(), -x.i());
		return this;
	}

	public Trap curveTo(ComplexNumber p, ComplexNumber q, ComplexNumber x) {
		path2d.curveTo(p.r(), -p.i(), q.r(), -q.i(), x.r(), -x.i());
		return this;
	}

	public Trap moveRel(ComplexNumber x) {
		path2d.moveTo(path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i());
		return this;
	}

	public Trap lineRel(ComplexNumber x) {
		path2d.lineTo(path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i());
		return this;
	}

	public Trap arcRel(ComplexNumber p, ComplexNumber x) {
		path2d.curveTo(path2d.getCurrentPoint().getX() + p.r(), path2d.getCurrentPoint().getY() - p.i(), path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i(), path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i());
		return this;
	}

	public Trap quadRel(ComplexNumber p, ComplexNumber x) {
		path2d.quadTo(path2d.getCurrentPoint().getX() + p.r(), path2d.getCurrentPoint().getY() - p.i(), path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i());
		return this;
	}

	public Trap curveRel(ComplexNumber p, ComplexNumber q, ComplexNumber x) {
		path2d.curveTo(path2d.getCurrentPoint().getX() + p.r(), path2d.getCurrentPoint().getY() - p.i(), path2d.getCurrentPoint().getX() + q.r(), path2d.getCurrentPoint().getY() - q.i(), path2d.getCurrentPoint().getX() + x.r(), path2d.getCurrentPoint().getY() - x.i());
		return this;
	}

	public Trap close() {
		path2d.closePath();
		return this;
	}
	
	public boolean contains(ComplexNumber x) {
		return path2d.contains(x.r() - center.r(), x.i() - center.i());
	}

	public List<ComplexNumber> toPoints() {
		final PathIterator iterator = path2d.getPathIterator(AffineTransform.getTranslateInstance(center.r(), -center.i()), 0.005);
		final List<ComplexNumber> points = new ArrayList<>();
		final double[] segment = new double[6];
		while (!iterator.isDone()) {
            switch (iterator.currentSegment(segment)) {
                case PathIterator.SEG_LINETO -> points.add(new ComplexNumber(segment[0], segment[1]));
                case PathIterator.SEG_MOVETO -> points.add(new ComplexNumber(segment[0], segment[1]));
                case PathIterator.SEG_CLOSE -> {
                    final ComplexNumber number = points.getFirst();
                    points.add(new ComplexNumber(number.r(), number.i()));
                }
                default -> {
                }
            }
			iterator.next();
		}
		return points;
	}
}
