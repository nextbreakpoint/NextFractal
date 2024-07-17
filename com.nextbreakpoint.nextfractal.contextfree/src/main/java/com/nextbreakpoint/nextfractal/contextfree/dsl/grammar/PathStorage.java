/*
 * NextFractal 2.3.0
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PathStorage implements Cloneable {
	private GeneralPath currentPath = new GeneralPath();
	private List<Vertex> vertices = new ArrayList<>();

	public void startNewPath() {
		currentPath = new GeneralPath();
		vertices.clear();
	}

	public void closePath() {
		endPath();
	}

	public void endPath() {
		currentPath.closePath();
	}

	public void moveTo(Point2D.Double point) {
		vertices.add(new Vertex(point, 1));
		currentPath.moveTo((float)point.x, (float)point.y);
	}

	public void lineTo(Point2D.Double point) {
		vertices.add(new Vertex(point, 2));
		currentPath.lineTo((float)point.x, (float)point.y);
	}

//	public void arcTo(double radiusX, double radiusY, double angle, boolean largeArc, boolean sweep, Point2D.Double point) {
//		vertices.add(new Vertex(point, 3));
//		currentPath.arcTo((float)radiusX, (float)radiusY, (float)angle, largeArc, sweep, (float)point.x, (float)point.y);
//	}

	public void relToAbs(Point2D.Double point) {
		if (vertices.size() > 0) {
			Point2D.Double p = new Point2D.Double();
			if (isVertex(lastVertex(p))) {
				point.setLocation(point.x + p.x, point.y + p.y);
			}
		}
	}

	public int command(int index) {
		return vertices.get(index).command;
	}

	public int getTotalVertices() {
		return vertices.size();
	}

	public int vertex(int index, Point2D.Double point) {
		Vertex vertex = vertices.get(index);
		point.setLocation(vertex.point.x, vertex.point.y);
		return vertex.command;
	}

	public int lastVertex(Point2D.Double point) {
		if (vertices.size() < 1) {
			return 0;
		}
		Vertex vertex = vertices.get(vertices.size() - 1);
		point.setLocation(vertex.point.x, vertex.point.y);
		return vertex.command;
	}

	public int prevVertex(Point2D.Double point) {
		if (vertices.size() < 2) {
			return 0;
		}
		Vertex vertex = vertices.get(vertices.size() - 2);
		point.setLocation(vertex.point.x, vertex.point.y);
		return vertex.command;
	}

	public void modifyVertex(int index, Point2D.Double point) {
		Vertex vertex = vertices.get(index);
		vertex.point.setLocation(point.x, point.y);
	}

	public boolean isDrawing(int command) {
		return command >= 2 && command <= 3;
	}

	public boolean isVertex(int command) {
		return command >= 1 && command <= 3;
	}

	public boolean isCurve(int command) {
		return command == 3;
	}

	public void curve3(Point2D.Double point) {
		Point2D.Double p0 = new Point2D.Double();
		if (isVertex(lastVertex(p0))) {
			Point2D.Double p1 = new Point2D.Double();
			if (isCurve(prevVertex(p1))) {
				p1.setLocation(p0.x + p0.x - p1.x, p0.y + p0.y - p1.y);
			} else {
				p1.setLocation(p0);
			}
			curve3(p1, point);
		}
	}

	public void curve3(Point2D.Double ctrlPoint1, Point2D.Double point) {
		vertices.add(new Vertex(ctrlPoint1, 3));
		vertices.add(new Vertex(point, 3));
		currentPath.curveTo(ctrlPoint1.x, ctrlPoint1.y, ctrlPoint1.x, ctrlPoint1.y, point.x, point.y);
	}

	public void curve4(Point2D.Double ctrlPoint2, Point2D.Double point) {
		Point2D.Double p0 = new Point2D.Double();
		if (isVertex(lastVertex(p0))) {
			Point2D.Double p1 = new Point2D.Double();
			if (isCurve(prevVertex(p1))) {
				p1.setLocation(p0.x + p0.x - p1.x, p0.y + p0.y - p1.y);
			} else {
				p1.setLocation(p0);
			}
			curve4(p1, ctrlPoint2, point);
		}
	}

	public void curve4(Point2D.Double ctrlPoint1, Point2D.Double ctrlPoint2, Point2D.Double point) {
		vertices.add(new Vertex(ctrlPoint1, 3));
		vertices.add(new Vertex(ctrlPoint2, 3));
		vertices.add(new Vertex(point, 3));
		currentPath.curveTo(ctrlPoint1.x, ctrlPoint1.y, ctrlPoint2.x, ctrlPoint2.y, point.x, point.y);
	}

	public PathIterator getPathIterator() {
		return getGeneralPath().getPathIterator(new AffineTransform());
	}

	public GeneralPath getGeneralPath() {
		return currentPath;
	}

	public void append(Shape shape, Point2D.Double point) {
		vertices.add(new Vertex(point, 3));
		currentPath.append(shape, true);
	}

	public int lastCommnand() {
		if (vertices.size() == 0) {
			return 0;
		}
		return vertices.get(vertices.size() - 1).command;
	}

	public Object clone() {
		PathStorage storage = new PathStorage();
		storage.currentPath = (GeneralPath) currentPath.clone();
		return storage;
	}

	private class Vertex {
		Point2D.Double point;
		int command;

		public Vertex(Point2D.Double point, int command) {
			this.point = point;
			this.command = command;
		}
	}
}
