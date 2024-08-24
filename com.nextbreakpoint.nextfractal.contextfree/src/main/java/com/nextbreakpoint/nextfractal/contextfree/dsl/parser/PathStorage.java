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

import lombok.Getter;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PathStorage implements Cloneable {
	private static final int CMD_STOP = 0;
	private static final int CMD_MOVE = 1;
	private static final int CMD_LINE = 2;
	private static final int CMD_CURVE = 3;
	private static final int CMD_CLOSE = 0xF;

	private final List<Vertex> vertices;

	@Getter
    private GeneralPath currentPath;

	public PathStorage() {
		this(new GeneralPath(), new ArrayList<>());
	}

	private PathStorage(GeneralPath currentPath, List<Vertex> vertices) {
		this.currentPath = currentPath;
		this.vertices = vertices;
	}

	public void startNewPath() {
		if (!isStop(vertices.getLast().command())) {
			vertices.add(new Vertex(new Point2D.Double(0, 0), CMD_STOP));
		}
	}

	public void closePath() {
		endPath();
	}

	public void endPath() {
		if (isVertex(vertices.getLast().command())) {
			vertices.add(new Vertex(new Point2D.Double(0, 0), CMD_CLOSE));
			currentPath.closePath();
		}
	}

	public void moveTo(Point2D.Double point) {
		vertices.add(new Vertex(point, CMD_MOVE));
		currentPath.moveTo((float)point.x, (float)point.y);
	}

	public void lineTo(Point2D.Double point) {
		vertices.add(new Vertex(point, CMD_LINE));
		currentPath.lineTo((float)point.x, (float)point.y);
	}

	//TODO remove
//	public void arcTo(double radiusX, double radiusY, double angle, boolean largeArc, boolean sweep, Point2D.Double point) {
//		vertices.add(new Vertex(point, 3));
//		currentPath.arcTo((float)radiusX, (float)radiusY, (float)angle, largeArc, sweep, (float)point.x, (float)point.y);
//	}

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
		vertices.add(new Vertex(point, CMD_CURVE));
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
		vertices.add(new Vertex(point, CMD_CURVE));
		currentPath.curveTo(ctrlPoint1.x, ctrlPoint1.y, ctrlPoint2.x, ctrlPoint2.y, point.x, point.y);
	}

	public void append(Shape shape) {
		final double[] coords = new double[2];
		for (PathIterator it = shape.getPathIterator(null, 0.1); !it.isDone(); it.next()) {
			final int segment = it.currentSegment(coords);
			if (segment == PathIterator.SEG_MOVETO) {
				vertices.add(new Vertex(new Point2D.Double(coords[0], coords[1]), CMD_MOVE));
			} else if (segment == PathIterator.SEG_LINETO) {
				vertices.add(new Vertex(new Point2D.Double(coords[0], coords[1]), CMD_LINE));
			}
		}
		currentPath.append(shape, true);
	}

	public void relToAbs(Point2D.Double point) {
		if (!vertices.isEmpty()) {
			Point2D.Double p = new Point2D.Double();
			if (isVertex(lastVertex(p))) {
				point.setLocation(point.x + p.x, point.y + p.y);
			}
		}
	}

	public int command(int index) {
		return vertices.get(index).command();
	}

	public int getTotalVertices() {
		return vertices.size();
	}

	public int vertex(int index, Point2D.Double point) {
		Vertex vertex = vertices.get(index);
		point.setLocation(vertex.point().x, vertex.point().y);
		return vertex.command();
	}

	public int lastVertex(Point2D.Double point) {
		if (vertices.isEmpty()) {
			return 0;
		}
		Vertex vertex = vertices.getLast();
		point.setLocation(vertex.point().x, vertex.point().y);
		return vertex.command();
	}

	public int prevVertex(Point2D.Double point) {
		if (vertices.size() < 2) {
			return 0;
		}
		Vertex vertex = vertices.get(vertices.size() - 2);
		point.setLocation(vertex.point().x, vertex.point().y);
		return vertex.command();
	}

	public void modifyVertex(int index, Point2D.Double point) {
		Vertex vertex = vertices.get(index);
		vertex.point().setLocation(point.x, point.y);
	}

	public static boolean isDrawing(int command) {
		return command >= CMD_LINE && command <= CMD_CURVE;
	}

	public static boolean isVertex(int command) {
		return command >= CMD_MOVE && command <= CMD_CURVE;
	}

	public static boolean isMoveTo(int command) {
		return command == CMD_MOVE;
	}

	public static boolean isCurve(int command) {
		return command == CMD_CURVE;
	}

	public static boolean isStop(int command) {
		return command == CMD_STOP;
	}

	public int lastCommand() {
		if (vertices.isEmpty()) {
			return 0;
		}
		return vertices.getLast().command();
	}

	public Object clone() {
		final List<Vertex> clonedVertices = vertices.stream()
				.map(Vertex::clone)
				.toList();
		return new PathStorage((GeneralPath) currentPath.clone(), clonedVertices);
	}

	//TODO verify align path
	public int alignPath(int idx) {
		if (idx >= getTotalVertices() || !isMoveTo(command(idx))) {
			return getTotalVertices();
		}

		final Point2D.Double start = new Point2D.Double(0, 0);

		for (; idx < getTotalVertices() && isMoveTo(command(idx)); ++idx) {
			vertex(idx, start);
		}

		while (idx < getTotalVertices() && isDrawing(command(idx))) {
			++idx;
		}

		final Point2D.Double point = new Point2D.Double(0, 0);

		if (isDrawing(vertex(idx - 1, point)) && isEqualEps(point.x, start.x, 1e-8) && isEqualEps(point.y, start.y, 1e-8)) {
			modifyVertex(idx - 1, start);
		}

		while (idx < getTotalVertices() && !isMoveTo(command(idx))) {
			++idx;
		}

		return idx;
	}

	private boolean isEqualEps(double v1, double v2, double epsilon) {
		boolean neg1 = v1 < 0.0;
		boolean neg2 = v2 < 0.0;

		if (neg1 != neg2)
			return Math.abs(v1) < epsilon && Math.abs(v2) < epsilon;

		int int1 = Math.getExponent(v1);
		int int2 = Math.getExponent(v2);

		int min12 = Math.min(int1, int2);

		v1 = v1 * Math.pow(2, -min12);
		v2 = v2 * Math.pow(2, -min12);

		return Math.abs(v1 - v2) < epsilon;
	}

	public void removeAll() {
		currentPath.reset();
		vertices.clear();
	}

	public Iterator<Vertex> getPathIterator() {
		return vertices.iterator();
	}
}
