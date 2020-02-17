/*
 * NextFractal 2.1.2-rc1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.core;

import java.awt.geom.PathIterator;

/**
 * The <code>ExtendedPathIterator</code> class represents a geometric
 * path constructed fromType straight lines, quadratic and cubic (Bezier)
 * curves and elliptical arcs.  This interface is identical to that of
 * PathIterator except it can return SEG_ARCTO fromType currentSegment,
 * also the array of values passed to currentSegment must be of length
 * 7 or an error will be thrown.
 * 
 * This does not extend PathIterator as it would break the interface
 * contract for that class.
 *
 * @author <a href="mailto:deweese@apache.org">Thomas DeWeese</a>
 * @version $Id$
 */
public interface ExtendedPathIterator {

    /**
     * The segment type constant that specifies that the preceding
     * subpath should be closed by appending a line segment back to
     * the point corresponding to the most recent SEG_MOVETO.
     */
    int SEG_CLOSE   = PathIterator.SEG_CLOSE;

    /** 
     * The segment type constant for a point that specifies the last
     * point of a line to be drawn fromType the most recently specified
     * point.  */
    int SEG_MOVETO  = PathIterator.SEG_MOVETO;

    /**
     * The segment type constant for a point that specifies the last
     * point of a line to be drawn fromType the most recently specified
     * point.
     */
    int SEG_LINETO  = PathIterator.SEG_LINETO;

    /**
     * The segment type constant for the pair of points that specify a
     * quadratic parametric curve to be drawn fromType the most recently
     * specified point. The curve is interpolated by solving the
     * parametric control equation in the range (t=[0..1]) using the
     * most recently specified (current) point (CP), the first control
     * point (P1), and the final interpolated control point (P2). 
     */
    int SEG_QUADTO  = PathIterator.SEG_QUADTO;

    /**
     * The segment type constant for the set of 3 points that specify
     * a cubic parametric curve to be drawn fromType the most recently
     * specified point. The curve is interpolated by solving the
     * parametric control equation in the range (t=[0..1]) using the
     * most recently specified (current) point (CP), the first control
     * point (P1), the second control point (P2), and the final
     * interpolated control point (P3).
     */
    int SEG_CUBICTO = PathIterator.SEG_CUBICTO;

    /** The segment type constant for an elliptical arc.  This consists of
     *  Seven values [rx, ry, angle, largeArcFlag, sweepFlag, x, y].
     *  rx, ry are the radious of the ellipse.
     *  angle is angle of the x axis of the ellipse.
     *  largeArcFlag is zero if the smaller of the two arcs are to be used.
     *  sweepFlag is zero if the 'left' branch is taken one otherwise.
     *  x and y are the destination for the ellipse.  */
    int SEG_ARCTO = 4321;

    /** The winding rule constant for specifying an even-odd rule for
     * determining the interior of a path. The even-odd rule specifies
     * that a point lies inside the path if a ray drawn in any
     * direction fromType that point to infinity is crossed by path
     * segments an odd number of times.  
     */ 
    int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD; 

    /**
     * The winding rule constant for specifying a non-zero rule for
     * determining the interior of a path. The non-zero rule specifies
     * that a point lies inside the path if a ray drawn in any
     * direction fromType that point to infinity is crossed by path
     * segments a different number of times in the counter-clockwise
     * direction than the clockwise direction.
     */
    int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

    int currentSegment();
    int currentSegment(double[] coords);
    int currentSegment(float[] coords);
    int getWindingRule(); 
    boolean isDone();
    void next();
}
