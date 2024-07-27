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
package com.nextbreakpoint.nextfractal.core.javafx.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.Buffer;
import com.nextbreakpoint.nextfractal.core.graphics.Color;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;

public class JavaFXGraphicsFactory implements GraphicsFactory {
	@Override
	public String getName() {
		return "JavaFX";
	}

	@Override
	public Buffer createBuffer(int width, int height) {
		return new JavaFXBuffer(width, height);
	}

	@Override
	public GraphicsContext createGraphicsContext(Object context) {
		return new JavaFXGraphicsContext((javafx.scene.canvas.GraphicsContext)context);
	}

	@Override
	public AffineTransform createTranslateAffineTransform(double x, double y) {
		return new JavaFXAffineTransform(new Affine(Affine.translate(x, y)));
	}

	@Override
	public AffineTransform createRotateAffineTransform(double a, double centerX, double centerY) {
		return new JavaFXAffineTransform(new Affine(Affine.rotate(a, centerX, centerY)));
	}

	@Override
	public AffineTransform createScaleAffineTransform(double x, double y) {
		return new JavaFXAffineTransform(new Affine(Affine.scale(x, y)));
	}

	@Override
	public AffineTransform createAffineTransform() {
		return new JavaFXAffineTransform(new Affine());
	}

	@Override
	public AffineTransform createAffineTransform(double[] matrix) {
		return new JavaFXAffineTransform(new Affine(matrix, MatrixType.MT_2D_2x3, 0));
	}

	@Override
	public Color createColor(double red, double green, double blue, double opacity) {
		return new JavaFXColor(red, green, blue, opacity);
	}
}
