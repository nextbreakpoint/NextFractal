/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics;

import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class View {
	private Double4D translation;
	private Double4D rotation;
	private Double4D scale;
	private Integer4D state;
	private ComplexNumber point;
	private boolean julia;
	
	public View() {
		translation = new Double4D(0, 0, 1, 0);
		rotation = new Double4D(0, 0, 0, 0);
		scale = new Double4D(1, 1, 1, 1);
		state = new Integer4D(0, 0, 0, 0);
		point = new ComplexNumber(0, 0);
	}
}
