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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Dequeue;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class ASTCompiledPath {
	private static Long globalPathUID = 100L;
	@Setter
    @Getter
    private PathStorage pathStorage = new PathStorage();
	@Getter
    private final Dequeue commandInfo;
	@Getter
    private final ASTPathCommand terminalCommand;
	@Setter
    @Getter
    private boolean cached;
	@Getter
    @Setter
    private boolean useTerminal;
	@Setter
    @Getter
    private CFStackRule parameters;
	@Setter
    @Getter
    private Long pathUID;
	private final CFDGDriver driver;

	public ASTCompiledPath(CFDGDriver driver, Token location) {
		this.driver = driver;
		commandInfo = new Dequeue();
		terminalCommand = new ASTPathCommand(driver, location);
		parameters = null;
		cached = false;
		useTerminal = false;
		pathUID = nextPathUID();
	}

    public static Long nextPathUID() {
		return globalPathUID = globalPathUID + 1;
	}

	public void finish(boolean setAttr, CFDGRenderer renderer) {
		if (!renderer.isClosed()) {
			pathStorage.endPath();
			renderer.setClosed(true);
		}
		if (renderer.isStop()) {
			pathStorage.startNewPath();
			renderer.setClosed(true);
		}
		renderer.setWantMoveTo(true);
		renderer.setNextIndex(pathStorage.getTotalVertices());
		if (setAttr && renderer.isWantCommand()) {
			useTerminal = true;
			renderer.setWantCommand(false);
		}
	}

	public void addPathOp(ASTPathOp pathOp, double[] data, Shape parent, boolean tr, CFDGRenderer renderer) {
		// Process the parameters for ARCTO/ARCREL
		double radiusX = 0.0, radiusY = 0.0, angle = 0.0;
		boolean sweep = (pathOp.getFlags() & FlagType.CF_ARC_CW.getMask()) == 0;
		boolean largeArc = (pathOp.getFlags() & FlagType.CF_ARC_LARGE.getMask()) != 0;
		Point2D.Double p0 = new Point2D.Double(data[0], data[1]);
		Point2D.Double p1 = new Point2D.Double(data[2], data[3]);
		Point2D.Double p2 = new Point2D.Double(data[4], data[5]);
		if (pathOp.getPathOp() == PathOp.ARCTO || pathOp.getPathOp() == PathOp.ARCREL) {
			if (pathOp.getArgCount() == 5) {
				radiusX = data[2];
				radiusY = data[3];
				angle = data[4] * 0.0174532925199;
			} else {
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
		renderer.setIndex(renderer.getNextIndex());

		if (renderer.getIndex() == pathStorage.getTotalVertices()) {
			pathStorage.startNewPath();
		}

		// If the op is anything other than a CLOSEPOLY then we are opening up a new path sequence.
		renderer.setClosed(false);
		renderer.setStop(false);

		// This new path op needs to be covered by a command, either from the cfdg file or default.
		renderer.setWantCommand(true);

		if (pathOp.getPathOp() == PathOp.CLOSEPOLY) {
			if (pathStorage.getTotalVertices() > 1 && pathStorage.isDrawing(pathStorage.lastCommnand())) {
				// Find the MOVETO/MOVEREL that is the start of the current path sequence
				// and reset LastPoint to that.
				int last = pathStorage.getTotalVertices() - 1;
				int cmd = 0;
				for (int i = last - 1; i >= 0; i--) {
					cmd = pathStorage.command(i);
					if (cmd == 1) {
						pathStorage.vertex(i, renderer.getLastPoint());
						break;
					}
				}

				if (cmd != 1) {
					driver.error("CLOSEPOLY: Unable to find a MOVETO/MOVEREL for start of path", pathOp.getLocation());
				}

				// If this is an aligning CLOSEPOLY then change the last vertex to
				// exactly match the first vertex in the path sequence
				if ((pathOp.getFlags() & FlagType.CF_ALIGN.getMask()) != 0)  {
					pathStorage.modifyVertex(last, renderer.getLastPoint());
				}
			} else if ((pathOp.getFlags() & FlagType.CF_ALIGN.getMask()) != 0)  {
				driver.error("Nothing to align to", pathOp.getLocation());
			}

			pathStorage.closePath();
			renderer.setClosed(true);
			renderer.setWantMoveTo(true);
			return;
		}

		// Insert an implicit MOVETO unless the pathOp is a MOVETO/MOVEREL
		if (renderer.isWantMoveTo() && pathOp.getPathOp().ordinal() > PathOp.MOVEREL.ordinal()) {
			renderer.setWantMoveTo(false);
			pathStorage.moveTo(renderer.getLastPoint());
		}

		switch (pathOp.getPathOp()) {
			case MOVEREL:
				pathStorage.relToAbs(p0);
            case MOVETO:
				pathStorage.moveTo(p0);
				renderer.setWantMoveTo(false);
				break;
			case LINEREL:
				pathStorage.relToAbs(p0);
			case LINETO:
				pathStorage.lineTo(p0);
				break;
			case ARCREL:
				pathStorage.relToAbs(p0);
			case ARCTO:
				if (!pathStorage.isVertex(pathStorage.lastVertex(p1)) || (tr && parent.getWorldState().getTransform().getDeterminant() < 1e-10)) {
					break;
				}
				// Transforming an arc as they are parameterized by AGG is VERY HARD.
				// So instead we insert the arc and then transform the bezier curves
				// that are used to approximate the arc. But first we have to inverse
				// transform the starting point to match the untransformed arc.
				// Afterwards the starting point is restored to its original value.
				if (tr) {
					try {
						AffineTransform transform = parent.getWorldState().getTransform();
						AffineTransform inverseTr = transform.createInverse();
						inverseTr.transform(p1, p1);

						Arc2D arc = ExtendedGeneralPath.computeArc(p1.x, p1.y, radiusX, radiusY, angle, largeArc, sweep, p0.x, p0.y);
						AffineTransform t = AffineTransform.getRotateInstance(Math.toRadians(angle), arc.getCenterX(), arc.getCenterY());
						t.concatenate(transform);
						java.awt.Shape s = t.createTransformedShape(arc);

						transform.transform(p0, p0);
						pathStorage.append(s, p0);
					} catch (NoninvertibleTransformException e) {
						driver.fail("Can't invert transform", pathOp.getLocation());
					}
				} else {
					Arc2D arc = ExtendedGeneralPath.computeArc(p1.x, p1.y, radiusX, radiusY, angle, largeArc, sweep, p0.x, p0.y);
					pathStorage.append(arc, p0);
//					pathStorage.arcTo(radiusX, radiusY, angle, largeArc, sweep, p0);
				}
				break;
			case CURVEREL:
				pathStorage.relToAbs(p0);
				pathStorage.relToAbs(p1);
				pathStorage.relToAbs(p2);
			case CURVETO:
				if ((pathOp.getFlags() & FlagType.CF_CONTINUOUS.getMask()) != 0 && pathStorage.isCurve(pathStorage.lastVertex(p2)) ) {
					driver.error("Smooth curve operations must be preceded by another curve operation", pathOp.getLocation());
					break;
				}
				switch (pathOp.getArgCount()) {
					case 2:
						pathStorage.curve3(p0);
						break;
					case 4:
						if ((pathOp.getFlags() & FlagType.CF_CONTINUOUS.getMask()) != 0) {
							pathStorage.curve4(p1, p0);
						} else {
							pathStorage.curve3(p1, p0);
						}
						break;
					case 6:
						pathStorage.curve4(p1, p2, p0);
						break;
				}
				break;
            case UNKNOWN, CLOSEPOLY:
            default:
				break;
		}

		pathStorage.lastVertex(renderer.getLastPoint());
	}
}
