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
package com.nextbreakpoint.nextfractal.core.render;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class Java2DRendererFactory implements RendererFactory {
	@Override
	public RendererBuffer createBuffer(int width, int height) {
		return new Java2DRendererBuffer(width, height);
	}

	@Override
	public RendererGraphicsContext createGraphicsContext(Object context) {
		return new Java2DRendererGraphicsContext((Graphics2D)context);
	}

	@Override
	public RendererAffine createTranslateAffine(double x, double y) {
		return new Java2DRendererAffine(AffineTransform.getTranslateInstance(x, y));
	}

	@Override
	public RendererAffine createRotateAffine(double a, double centerX, double centerY) {
		return new Java2DRendererAffine(AffineTransform.getRotateInstance(a, centerX, centerY));
	}

	@Override
	public RendererAffine createScaleAffine(double x, double y) {
		return new Java2DRendererAffine(AffineTransform.getScaleInstance(x, y));
	}

	@Override
	public RendererAffine createAffine() {
		return new Java2DRendererAffine(new AffineTransform());
	}

	@Override
	public RendererAffine createAffine(double[] matrix) {
		return new Java2DRendererAffine(new AffineTransform(matrix));
	}

	@Override
	public RendererColor createColor(double red, double green, double blue, double opacity) {
		return new Java2DRendererColor(red, green, blue, opacity);
	}
}
