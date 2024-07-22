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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.common;

import org.antlr.v4.runtime.Token;

public class CompiledPaletteElement {
	private float[] beginColor;
	private float[] endColor;
	private int steps;
	private CompiledExpression exp;
	private Token location;

	public CompiledPaletteElement(float[] beginColor, float[] endColor,	int steps, CompiledExpression exp, Token location) {
		this.location = location;
		this.beginColor = beginColor;
		this.endColor = endColor;
		this.steps = steps;
		this.exp = exp;
	}

	public float[] getBeginColor() {
		return beginColor;
	}

	public float[] getEndColor() {
		return endColor;
	}

	public int getSteps() {
		return steps;
	}

	public CompiledExpression getExp() {
		return exp;
	}

	public Token getLocation() {
		return location;
	}
}
