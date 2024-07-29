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
package com.nextbreakpoint.nextfractal.mandelbrot.core;

import java.util.ArrayList;
import java.util.List;

public class Palette {
	private static final float[] DEFAULT = new float[] { 1, 0, 0, 0 };
	
	private final List<PaletteElement> elements = new ArrayList<>();
	private float[][] table;
	
	public int getSize() {
		return table.length;
	}

	public float[] get(double n) {
		if (table != null) {
			return table[((int)Math.rint(Math.abs(n))) % table.length];
		}
		return DEFAULT;
	}

	public Palette build() {
		int size = 0;
		for (PaletteElement element : elements) {
			size += element.steps();
		}
		if (size > 0) {
			table = new float[size][4];
			int i = 0;
			for (PaletteElement element : elements) {
				int steps = element.steps();
				for (int step = 0; step < steps; step++) {
					float[] bc = element.beginColor();
					float[] ec = element.endColor();
					double vc = clamp(element.expression().evaluate(step / (double)(steps - 1)));
					float a = (float)((ec[0] - bc[0]) * vc + bc[0]);
					float r = (float)((ec[1] - bc[1]) * vc + bc[1]);
					float g = (float)((ec[2] - bc[2]) * vc + bc[2]);
					float b = (float)((ec[3] - bc[3]) * vc + bc[3]);
					table[i++] = new float[] { a, r, g, b };
				}
			}
		}
		return this;
	}

	private double clamp(double value) {
		return value < 0 ? 0 : value > 1 ? 1 : value;
	}

	public Palette add(PaletteElement element) {
		elements.add(element);
		return this;
	}
}
