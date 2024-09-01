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
package com.nextbreakpoint.nextfractal.core.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.Buffer;
import com.nextbreakpoint.nextfractal.core.graphics.Color;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;

import java.awt.Graphics2D;

public class Java2DGraphicsFactory implements GraphicsFactory {
	@Override
	public String getName() {
		return "Java2D";
	}

	@Override
	public Buffer createBuffer(int width, int height) {
		return new Java2DBuffer(width, height);
	}

	@Override
	public GraphicsContext createGraphicsContext(Object context) {
		return new Java2DGraphicsContext((Graphics2D)context);
	}

	@Override
	public AffineTransform createTranslateAffineTransform(double x, double y) {
		return new Java2DAffineTransform(java.awt.geom.AffineTransform.getTranslateInstance(x, y));
	}

	@Override
	public AffineTransform createRotateAffineTransform(double a, double centerX, double centerY) {
		return new Java2DAffineTransform(java.awt.geom.AffineTransform.getRotateInstance(a, centerX, centerY));
	}

	@Override
	public AffineTransform createScaleAffineTransform(double x, double y) {
		return new Java2DAffineTransform(java.awt.geom.AffineTransform.getScaleInstance(x, y));
	}

	@Override
	public AffineTransform createAffineTransform() {
		return new Java2DAffineTransform(new java.awt.geom.AffineTransform());
	}

	@Override
	public AffineTransform createAffineTransform(double[] matrix) {
		return new Java2DAffineTransform(new java.awt.geom.AffineTransform(matrix));
	}

	@Override
	public Color createColor(double red, double green, double blue, double opacity) {
		return new Java2DColor(red, green, blue, opacity);
	}
}
