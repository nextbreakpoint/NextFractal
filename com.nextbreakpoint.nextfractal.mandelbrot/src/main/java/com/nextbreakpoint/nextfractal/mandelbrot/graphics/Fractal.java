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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import lombok.Getter;
import lombok.Setter;

public class Fractal {
	private final Scope scope = new Scope();
    @Getter
    private Orbit orbit;
    @Getter
    private Color color;
    @Setter
    @Getter
    private ComplexNumber point;

	public void initialize() {
		scope.empty();
		if (orbit != null) {
			orbit.init();
		}
		if (color != null) {
			color.init();
		}
	}

	public void setOrbit(Orbit orbit) {
		this.orbit = orbit;
		if (orbit != null) {
			orbit.setScope(scope);
		}
	}

	public void setColor(Color color) {
		this.color = color;
		if (color != null) {
			color.setScope(scope);
		}
	}

	public void renderOrbit(MutableNumber[] state, ComplexNumber x, ComplexNumber w) {
		orbit.reset();
		orbit.setX(x);
		orbit.setW(w);
		orbit.render(null);
		orbit.getState(state);
	}

	public float[] renderColor(ComplexNumber[] state) {
		color.reset();
		color.setState(state);
		color.render();
		return color.getColor();
	}

	public int getStateSize() {
		return orbit.stateSize();
	}

	public boolean isSolidGuessSupported() {
		return false;
	}

	public boolean isVerticalSymmetrySupported() {
		return false;
	}

	public boolean isHorizontalSymmetrySupported() {
		return false;
	}

	public double getVerticalSymmetryPoint() {
		return 0;
	}

	public double getHorizontalSymmetryPoint() {
		return 0;
	}

	public void clearScope() {
		scope.clear();
	}
}
