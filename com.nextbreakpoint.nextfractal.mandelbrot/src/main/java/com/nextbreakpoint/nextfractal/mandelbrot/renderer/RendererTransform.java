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
package com.nextbreakpoint.nextfractal.mandelbrot.renderer;

import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;

public class RendererTransform {
	private final double[] matrix = new double[9];
	
	public RendererTransform() {
		setMatrix(1, 0, 0, 0, 1, 0, 0, 0, 1);
	}

	private RendererTransform(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
		setMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	private void setMatrix(double m00, double m01, double m02, double m10, double m11, double m12, double m20, double m21, double m22) {
		matrix[0] = m00;
		matrix[1] = m01;
		matrix[2] = m02;
		matrix[3] = m10;
		matrix[4] = m11;
		matrix[5] = m12;
		matrix[6] = m20;
		matrix[7] = m21;
		matrix[8] = m22;
	}

	public static RendererTransform newIdentity() {
		return new RendererTransform(1, 0, 0, 0, 1, 0, 0, 0, 1);
	}
	
	public static RendererTransform newTraslate(double tx, double ty) {
		return new RendererTransform(1, 0, tx, 0, 1, ty, 0, 0, 1);
	}
	
	public static RendererTransform newScale(double sx, double sy) {
		return new RendererTransform(sx, 0, 0, 0, sy, 0, 0, 0, 1);
	}
	
	public static RendererTransform newRotate(double rz) {
		return new RendererTransform(Math.cos(rz), -Math.sin(rz), 0, Math.sin(rz), Math.cos(rz), 0, 0, 0, 1);
	}
	
	public void traslate(double tx, double ty) {
		double m00 = matrix[0];
		double m01 = matrix[1];
		double m02 = matrix[0] * tx + matrix[1] * ty + matrix[2];
		double m10 = matrix[3];
		double m11 = matrix[4];
		double m12 = matrix[3] * tx + matrix[4] * ty + matrix[5];
		double m20 = matrix[6];
		double m21 = matrix[7];
		double m22 = matrix[6] * tx + matrix[7] * ty + matrix[8];
		setMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public void scale(double sx, double sy) {
		double m00 = matrix[0] * sx;
		double m01 = matrix[1] * sy;
		double m02 = matrix[2];
		double m10 = matrix[3] * sx;
		double m11 = matrix[4] * sy;
		double m12 = matrix[5];
		double m20 = matrix[6] * sx;
		double m21 = matrix[7] * sy;
		double m22 = matrix[8];
		setMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public void rotate(double rz) {
		double cz = Math.cos(rz);
		double sz = Math.sin(rz);
		double m00 = matrix[0] * cz + matrix[1] * sz;
		double m01 = matrix[0] * -sz + matrix[1] * cz;
		double m02 = matrix[2];
		double m10 = matrix[3] * cz + matrix[4] * sz;
		double m11 = matrix[3] * -sz + matrix[4] * cz;
		double m12 = matrix[5];
		double m20 = matrix[6] * cz + matrix[7] * sz;
		double m21 = matrix[6] * -sz + matrix[7] * cz;
		double m22 = matrix[8];
		setMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public void transform(double[] p) {
		double m0 = matrix[0] * p[0] + matrix[1] * p[1] + matrix[2];
		double m1 = matrix[3] * p[0] + matrix[4] * p[1] + matrix[5];
		double m2 = matrix[6] * p[0] + matrix[7] * p[1] + matrix[8];
		p[0] = m0 / m2;
		p[1] = m1 / m2;
	}

	public void transform(MutableNumber p) {
		double m0 = matrix[0] * p.r() + matrix[1] * p.i() + matrix[2];
		double m1 = matrix[3] * p.r() + matrix[4] * p.i() + matrix[5];
		double m2 = matrix[6] * p.r() + matrix[7] * p.i() + matrix[8];
		p.set(m0 / m2, m1 / m2);
	}

	public void concat(RendererTransform t) {
		double m00 = matrix[0] * t.matrix[0] + matrix[1] * t.matrix[3] + matrix[2] * t.matrix[6];
		double m01 = matrix[0] * t.matrix[1] + matrix[1] * t.matrix[4] + matrix[2] * t.matrix[7];
		double m02 = matrix[0] * t.matrix[2] + matrix[1] * t.matrix[5] + matrix[2] * t.matrix[8];
		double m10 = matrix[3] * t.matrix[0] + matrix[4] * t.matrix[3] + matrix[5] * t.matrix[6];
		double m11 = matrix[3] * t.matrix[1] + matrix[4] * t.matrix[4] + matrix[5] * t.matrix[7];
		double m12 = matrix[3] * t.matrix[2] + matrix[4] * t.matrix[5] + matrix[5] * t.matrix[8];
		double m20 = matrix[6] * t.matrix[0] + matrix[7] * t.matrix[3] + matrix[8] * t.matrix[6];
		double m21 = matrix[6] * t.matrix[1] + matrix[7] * t.matrix[4] + matrix[8] * t.matrix[7];
		double m22 = matrix[6] * t.matrix[2] + matrix[7] * t.matrix[5] + matrix[8] * t.matrix[8];
		setMatrix(m00, m01, m02, m10, m11, m12, m20, m21, m22);
	}

	public void set(RendererTransform newTransform) {
		System.arraycopy(newTransform.matrix, 0, matrix, 0, matrix.length);
	}
}
