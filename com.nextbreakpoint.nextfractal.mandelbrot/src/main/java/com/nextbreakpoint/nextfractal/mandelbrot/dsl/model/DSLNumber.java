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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;

import java.util.Map;

public class DSLNumber extends DSLExpression {
	private final double r;
	private final double i;
	private final int numberIndex;

	public DSLNumber(DSLToken token, double r, double i, int numberIndex) {
		super(token);
		this.r = r;
		this.i = i;
		this.numberIndex = numberIndex;
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return r;
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		MutableNumber number = context.getNumber(numberIndex);
		number.set(r, i);
		return number;
	}

	@Override
	public boolean isReal() {
		return i == 0;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		if (isReal()) {
			context.append(r);
		} else {
			context.append("getNumber(");
			context.append(numberIndex);
			context.append(").set(");
			context.append(r);
			context.append(",");
			context.append(i);
			context.append(")");
		}
	}
}
