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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler.CompilerUtils;

import java.util.Map;

import static com.nextbreakpoint.nextfractal.mandelbrot.core.Expression.opDiv;

public class DSLOperatorDiv extends DSLOperator {
	public DSLOperatorDiv(DSLToken token, DSLExpression exp1, DSLExpression exp2, int numberIndex) {
		super(token, exp1, exp2, numberIndex);
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return opDiv(exp1.evaluateReal(context, scope), exp2.evaluateReal(context, scope));
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return context.getNumber(numberIndex).set(opDiv(exp1.evaluateReal(context, scope), exp2.evaluateReal(context, scope)));
	}

	@Override
	public boolean isReal() {
		return true;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		CompilerUtils.compileRealMathOperator(context, scope, "/", exp1, exp2);
	}
}
