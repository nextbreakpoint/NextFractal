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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Dequeue;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import lombok.Getter;
import lombok.Setter;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.concurrent.atomic.AtomicLong;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage.isCurve;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage.isDrawing;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage.isMoveTo;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage.isVertex;

// astreplacement.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2009-2014 John Horigan - john@glyphic.com
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

@Getter
public class ASTCompiledPath extends ASTObject {
	public static final AtomicLong GLOBAL_PATH_UID = new AtomicLong(1);

	private final ASTPathCommand terminalCommand;
	private final Dequeue commandInfo;
	@Setter
    private PathStorage path;
	@Setter
	private CFStackRule parameters;
	@Setter
    private boolean useTerminal;
	@Setter
	private boolean cached;
	@Setter
    private Long pathUID;

	public ASTCompiledPath(CFDGSystem system, ASTWhere where) {
		super(system, where);
		terminalCommand = new ASTPathCommand(system, where);
		commandInfo = new Dequeue();
		path = new PathStorage();
		parameters = null;
		useTerminal = false;
		cached = false;
		pathUID = nextPathUID();
	}

    public static Long nextPathUID() {
		return GLOBAL_PATH_UID.incrementAndGet();
	}

	public void finish(boolean setAttr, CFDGRenderer renderer) {
		if (!renderer.isClosed()) {
			path.endPath();
			renderer.setClosed(true);
		}
		if (!renderer.isStop()) {
			path.startNewPath();
			renderer.setStop(true);
		}
		renderer.setWantMoveTo(true);
		renderer.setNextIndex(path.getTotalVertices());
		if (setAttr && renderer.isWantCommand()) {
			useTerminal = true;
			renderer.setWantCommand(false);
		}
	}

	public void addPathOp(ASTPathOp pathOp, double[] data, Shape parent, boolean tr, CFDGRenderer renderer) {
		// Process the parameters for ARCTO/ARCREL
		double radiusX = 0.0, radiusY = 0.0, angle = 0.0;
		boolean sweep = (pathOp.getFlags() & FlagType.CF_ARC_CW.getMask()) == 0;
		final boolean largeArc = (pathOp.getFlags() & FlagType.CF_ARC_LARGE.getMask()) != 0;
		final Point2D.Double p0 = new Point2D.Double(data[0], data[1]);
		final Point2D.Double p1 = new Point2D.Double(data[2], data[3]);
		final Point2D.Double p2 = new Point2D.Double(data[4], data[5]);
		if (pathOp.getPathOp() == PathOp.ARCTO || pathOp.getPathOp() == PathOp.ARCREL) {
			if (pathOp.getArgCount() == 5) {
				// If the radii are specified then use the ellipse ARCxx form
				radiusX = data[2];
				radiusY = data[3];
				angle = data[4] * 0.0174532925199;
			} else {
				// Otherwise use the circle ARCxx form
				radiusX = data[2];
				radiusY = data[2];
				angle = 0.0;
			}
			if (radiusX < 0.0 || radiusY < 0.0) {
				radiusX = Math.abs(radiusX);
				radiusY = Math.abs(radiusY);
				sweep = !sweep;
			}
		} else if (tr) {
			parent.getWorldState().getTransform().transform(p0, p0);
			parent.getWorldState().getTransform().transform(p1, p1);
			parent.getWorldState().getTransform().transform(p2, p2);
		}

		// If this is the first path operation following a path command then set the
		// path index used by subsequent path commands to the path sequence that the
		// current path operation is part of.
		// If this is not the first path operation following a path command then this
		// line does nothing.
		if (renderer.getIndex() != renderer.getNextIndex()) {
			// Force start new path because of different behaviour of GeneralPath in Java
			path.getCurrentPath().reset();
		}
		renderer.setIndex(renderer.getNextIndex());

		// If the op is anything other than a CLOSEPOLY then we are opening up a new path sequence.
		renderer.setClosed(false);
		renderer.setStop(false);

		// This new path op needs to be covered by a command, either from the cfdg file or default.
		renderer.setWantCommand(true);

		if (pathOp.getPathOp() == PathOp.CLOSEPOLY) {
			if (path.getTotalVertices() > 1 && isDrawing(path.lastCommand())) {
				// Find the MOVETO/MOVEREL that is the start of the current path sequence
				// and reset LastPoint to that.
				int last = path.getTotalVertices() - 1;
				int cmd = 0;
				for (int i = last - 1; i >= 0 && isVertex(cmd = path.command(i)); --i) {
					if (isMoveTo(cmd)) {
						path.vertex(i, renderer.getLastPoint());
						break;
					}
				}
				if (!isMoveTo(cmd)) {
					system.error("CLOSEPOLY: Unable to find a MOVETO/MOVEREL for start of path", pathOp.getWhere());
				}
				// If this is an aligning CLOSEPOLY then change the last vertex to
				// exactly match the first vertex in the path sequence
				if ((pathOp.getFlags() & FlagType.CF_ALIGN.getMask()) != 0)  {
					path.modifyVertex(last, renderer.getLastPoint());
				}
			} else if ((pathOp.getFlags() & FlagType.CF_ALIGN.getMask()) != 0)  {
				system.error("Nothing to align to", pathOp.getWhere());
			}
			path.closePath();
			renderer.setClosed(true);
			renderer.setWantMoveTo(true);
			return;
		}

		// Insert an implicit MOVETO unless the pathOp is a MOVETO/MOVEREL
		if (renderer.isWantMoveTo() && pathOp.getPathOp().ordinal() > PathOp.MOVEREL.ordinal()) {
			renderer.setWantMoveTo(false);
			path.moveTo(renderer.getLastPoint());
		}

		switch (pathOp.getPathOp()) {
			case MOVEREL:
				path.relToAbs(p0);
				// fall through
            case MOVETO:
				path.moveTo(p0);
				renderer.setWantMoveTo(false);
				break;
			case LINEREL:
				path.relToAbs(p0);
				// fall through
			case LINETO:
				path.lineTo(p0);
				break;
			case ARCREL:
				path.relToAbs(p0);
				// fall through
			case ARCTO:
				if (!isVertex(path.lastVertex(p1)) || (tr && parent.getWorldState().getTransform().getDeterminant() < 1e-10)) {
					break;
				}
				// Transforming an arc as they are parameterized by AGG is VERY HARD.
				// So instead we insert the arc and then transform the bezier curves
				// that are used to approximate the arc. But first we have to inverse
				// transform the starting point to match the untransformed arc.
				// Afterwards the starting point is restored to its original value.
				if (tr) {
					try {
						//TODO verify that code is equivalent to original
						final AffineTransform transform = parent.getWorldState().getTransform();
						final AffineTransform inverseTr = transform.createInverse();
						inverseTr.transform(p1, p1);
						final Arc2D arc = ExtendedGeneralPath.computeArc(p1.x, p1.y, radiusX, radiusY, angle, largeArc, sweep, p0.x, p0.y);
						final AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle), arc.getCenterX(), arc.getCenterY());
						t.concatenate(transform);
						final java.awt.Shape s = t.createTransformedShape(arc);
						transform.transform(p0, p0);
						path.append(s);
					} catch (NoninvertibleTransformException e) {
						system.error("Cannot invert transform", pathOp.getWhere());
					}
				} else {
					//TODO verify that code is equivalent to original
					final Arc2D arc = ExtendedGeneralPath.computeArc(p1.x, p1.y, radiusX, radiusY, angle, largeArc, sweep, p0.x, p0.y);
					path.append(arc);
				}
				break;
			case CURVEREL:
				path.relToAbs(p0);
				path.relToAbs(p1);
				path.relToAbs(p2);
			case CURVETO:
				if ((pathOp.getFlags() & FlagType.CF_CONTINUOUS.getMask()) != 0 && isCurve(path.lastVertex(p2)) ) {
					system.error("Smooth curve operations must be preceded by another curve operation", pathOp.getWhere());
				}
				switch (pathOp.getArgCount()) {
					case 2:
						path.curve3(p0);
						break;
					case 4:
						if ((pathOp.getFlags() & FlagType.CF_CONTINUOUS.getMask()) != 0) {
							path.curve4(p1, p0);
						} else {
							path.curve3(p1, p0);
						}
						break;
					case 6:
						path.curve4(p1, p2, p0);
						break;
				}
				break;
            case UNKNOWN, CLOSEPOLY:
            default:
				break;
		}

		path.lastVertex(renderer.getLastPoint());
	}
}
