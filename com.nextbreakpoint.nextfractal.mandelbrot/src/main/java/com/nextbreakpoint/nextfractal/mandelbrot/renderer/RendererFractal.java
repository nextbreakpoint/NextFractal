/*
 * NextFractal 2.3.1
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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;

public class RendererFractal {
	private final Scope scope = new Scope();
	private Orbit orbit;
	private Color color;
	private Number point;

	/**
	 * 
	 */
	public void initialize() {
		scope.empty();
		if (orbit != null) {
			orbit.init();
		}
		if (color != null) {
			color.init();
		}
	}

	/**
	 * @return
	 */
	public Orbit getOrbit() {
		return orbit;
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @param orbit
	 */
	public void setOrbit(Orbit orbit) {
		this.orbit = orbit;
		if (orbit != null) {
			orbit.setScope(scope);
		}
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		if (color != null) {
			color.setScope(scope);
		}
	}

	/**
	 * @param state
	 * @param x
	 * @param w
	 */
	public void renderOrbit(MutableNumber[] state, Number x, Number w) {
		orbit.reset();
		orbit.setX(x);
		orbit.setW(w);
		orbit.render(null);
		orbit.getState(state);
	}

	/**
	 * @param state
	 * @return
	 */
	public float[] renderColor(Number[] state) {
		color.reset();
		color.setState(state);
		color.render();
		return color.getColor();
	}

	/**
	 * @return
	 */
	public int getStateSize() {
		return orbit.stateSize();
	}

	/**
	 * @return
	 */
	public boolean isSolidGuessSupported() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isVerticalSymetrySupported() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isHorizontalSymetrySupported() {
		return false;
	}

	/**
	 * @return
	 */
	public double getVerticalSymetryPoint() {
		return 0;
	}

	/**
	 * @return
	 */
	public double getHorizontalSymetryPoint() {
		return 0;
	}

	/**
	 * 
	 */
	public void clearScope() {
		scope.clear();
	}

	/**
	 * @return
	 */
	public Number getPoint() {
		return point;
	}

	/**
	 * @param point
	 */
	public void setPoint(Number point) {
		this.point = point;
	}
}
