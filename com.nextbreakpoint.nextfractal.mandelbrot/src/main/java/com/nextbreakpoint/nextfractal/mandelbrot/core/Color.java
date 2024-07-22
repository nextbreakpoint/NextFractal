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

import com.nextbreakpoint.nextfractal.core.common.Time;

public abstract class Color {
	protected final float[] color = new float[] { 1f, 0f, 0f, 0f };
	protected MutableNumber[] numbers;
	protected Scope scope;
	protected boolean julia;
	protected Time time = new Time(0, 1);;

	public Color() {
		initializeStack();
	}

	protected void initializeStack() {
		numbers = createNumbers();
		if (numbers != null) {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = new MutableNumber();
			}
		}
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public MutableNumber getVariable(int index) {
		return scope.getVariable(index);
	}

	public double getRealVariable(int index) {
		return scope.getVariable(index).r();
	}

	public float[] setColor(float[] color) {
		for (int i = 0; i < 4; i++) {
			this.color[i] = (float)Math.min(1, Math.max(0, color[i]));
		}
		return this.color;
	}
	
	public float[] addColor(double opacity, float[] color) {
		double a = opacity * color[0];
		double q = 1 - a;
		for (int i = 0; i < 4; i++) {
			this.color[i] = (float)Math.min(1, Math.max(0, q * this.color[i] + color[i] * a));
		}
		return this.color;
	}
	
	public float[] getColor() {
		return color;
	}

	public Palette palette() {
		return new Palette();
	}

	public PaletteElement element(float[] beginColor, float[] endColor, int steps, PaletteExpression expression) {
		return new PaletteElement(beginColor, endColor, steps, expression);
	}

	public float[] color(double x) {
		return new float[] { 1f, (float) x, (float) x, (float) x };
	}

	public float[] color(double r, double g, double b) {
		return new float[] { 1f, (float) r, (float) g, (float) b };
	}

	public float[] color(double a, double r, double g, double b) {
		return new float[] { (float) a, (float) r, (float) g, (float) b };
	}

	public void setState(Number[] state) {
		scope.setState(state);
	}

	public MutableNumber getNumber(int index) {
		return numbers[index];
	}
	
	public void reset() {
	}

	public boolean isJulia() {
		return julia;
	}

	public void setJulia(boolean julia) {
		this.julia = julia;
	}
	
	public abstract void init();

	public abstract void render();

	protected abstract MutableNumber[] createNumbers();

    public abstract boolean useTime();

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}
}
