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
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;

import java.util.Map;

import static com.nextbreakpoint.nextfractal.mandelbrot.core.Expression.opNeg;

public class DSLOperatorNeg extends DSLOperator {
	public DSLOperatorNeg(DSLToken token, DSLExpression exp, int numberIndex) {
		super(token, exp, null, numberIndex);
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return -exp1.evaluateReal(context, scope);
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return opNeg(context.getNumber(numberIndex), exp1.evaluate(context, scope));
	}

	@Override
	public boolean isReal() {
		return exp1.isReal();
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		if (exp1.isReal()) {
			context.append("-");
			exp1.compile(context, scope);
		} else {
			context.append("opNeg");
			context.append("(");
			context.append("getNumber(");
			context.append(numberIndex);
			context.append("),");
			exp1.compile(context, scope);
			context.append(")");
		}
	}
}
