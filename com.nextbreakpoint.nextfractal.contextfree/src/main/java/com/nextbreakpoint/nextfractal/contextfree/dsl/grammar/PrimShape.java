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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimShape extends PathStorage {
	private static List<String> shapeNames = new ArrayList<>();
	private static Map<Integer, PrimShape> shapeMap = new HashMap<>();

	static {
		shapeNames.add("CIRCLE");
		shapeNames.add("SQUARE");
		shapeNames.add("TRIANGLE");
		shapeNames.add("FILL");

		double h = 0.5 / Math.cos(Math.PI/6.0);
		double hp = h;
		double hn = -h / 2.0;
		double t = Math.sqrt(2.0) / 4.0;

		GeneralPath squarePath = new GeneralPath();
		squarePath.moveTo(0.5, 0.5);
		squarePath.lineTo(-0.5, 0.5);
		squarePath.lineTo(-0.5, -0.5);
		squarePath.lineTo(0.5, -0.5);
		squarePath.closePath();

		GeneralPath trianglePath = new GeneralPath();
		trianglePath.moveTo(0.0, hp);
		trianglePath.lineTo(-0.5, hn);
		trianglePath.lineTo(0.5, hn);
		trianglePath.closePath();

		GeneralPath circlePath = new GeneralPath();
		circlePath.append(new Ellipse2D.Double(-0.5, -0.5, 1.0, 1.0), false);

		PrimShape circle = new PrimShape(circlePath);
		PrimShape square = new PrimShape(squarePath);
		PrimShape triangle = new PrimShape(trianglePath);

		circle.moveTo(new Point2D.Double(0.5, 0.0));
		circle.lineTo(new Point2D.Double(t, t));
		circle.lineTo(new Point2D.Double(0.0, 0.5));
		circle.lineTo(new Point2D.Double(-t, t));
		circle.lineTo(new Point2D.Double(-0.5, 0.0));
		circle.lineTo(new Point2D.Double(-t, -t));
		circle.lineTo(new Point2D.Double(0.0, -0.5));
		circle.lineTo(new Point2D.Double(t, -t));

		square.moveTo(new Point2D.Double(0.5, 0.5));
		square.lineTo(new Point2D.Double(-0.5, 0.5));
		square.lineTo(new Point2D.Double(-0.5, -0.5));
		square.lineTo(new Point2D.Double(0.5, -0.5));

		triangle.moveTo(new Point2D.Double(0.0, hp));
		triangle.lineTo(new Point2D.Double(-0.5, hn));
		triangle.lineTo(new Point2D.Double(0.5, hn));

		shapeMap.put(0, circle);
		shapeMap.put(1, square);
		shapeMap.put(2, triangle);
	}

	private GeneralPath path;

	public PrimShape(GeneralPath path) {
		this.path = path;
	}

	public static boolean isPrimShape(int shapeType) {
		return shapeType < 4;
	}

	public static Map<Integer, PrimShape> getShapeMap() {
		return shapeMap;
	}

	public static List<String> getShapeNames() {
		return shapeNames;
	}

	public GeneralPath getPath() {
		return path;
	}
}
