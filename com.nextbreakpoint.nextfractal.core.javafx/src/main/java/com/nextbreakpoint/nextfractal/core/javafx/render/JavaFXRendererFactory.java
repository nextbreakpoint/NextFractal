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
package com.nextbreakpoint.nextfractal.core.javafx.render;

import com.nextbreakpoint.nextfractal.core.render.RendererAffine;
import com.nextbreakpoint.nextfractal.core.render.RendererBuffer;
import com.nextbreakpoint.nextfractal.core.render.RendererColor;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;

public class JavaFXRendererFactory implements RendererFactory {
	@Override
	public RendererBuffer createBuffer(int width, int height) {
		return new JavaFXRendererBuffer(width, height);
	}

	@Override
	public RendererGraphicsContext createGraphicsContext(Object context) {
		return new JavaFXRendererGraphicsContext((GraphicsContext)context);
	}

	@Override
	public RendererAffine createTranslateAffine(double x, double y) {
		return new JavaFXRendererAffine(new Affine(Affine.translate(x, y)));
	}

	@Override
	public RendererAffine createRotateAffine(double a, double centerX, double centerY) {
		return new JavaFXRendererAffine(new Affine(Affine.rotate(a, centerX, centerY)));
	}

	@Override
	public RendererAffine createScaleAffine(double x, double y) {
		return new JavaFXRendererAffine(new Affine(Affine.scale(x, y)));
	}

	@Override
	public RendererAffine createAffine() {
		return new JavaFXRendererAffine(new Affine());
	}

	@Override
	public RendererAffine createAffine(double[] matrix) {
		return new JavaFXRendererAffine(new Affine(matrix, MatrixType.MT_2D_2x3, 0));
	}

	@Override
	public RendererColor createColor(double red, double green, double blue, double opacity) {
		return new JavaFXRendererColor(red, green, blue, opacity);
	}
}
