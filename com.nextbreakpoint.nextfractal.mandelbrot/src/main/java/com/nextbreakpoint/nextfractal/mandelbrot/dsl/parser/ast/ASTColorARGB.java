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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import lombok.Getter;

import java.text.DecimalFormat;

@Getter
public class ASTColorARGB {
	private static final DecimalFormat FORMAT = new DecimalFormat("0.##");

	private final float[] components;

	public ASTColorARGB(int argb) {
		components = Colors.asFloats(argb);
	}

	public ASTColorARGB(float a, float r, float g, float b) {
		components = new float[] { a, r, g, b };
	}

    public int getARGB() {
		return Colors.makeColor(components);
	}

	@Override
	public String toString() {
		return "(" + FORMAT.format(components[0]) + "," + FORMAT.format(components[1]) + "," + FORMAT.format(components[2]) + "," + FORMAT.format(components[3]) + ")";
	}
}
