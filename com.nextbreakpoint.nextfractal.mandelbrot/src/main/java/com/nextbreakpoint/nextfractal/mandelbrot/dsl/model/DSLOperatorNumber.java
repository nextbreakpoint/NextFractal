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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;

import java.util.Map;

public class DSLOperatorNumber extends DSLOperator {
	public DSLOperatorNumber(DSLToken token, DSLExpression exp1, DSLExpression exp2, int numberIndex) {
		super(token, exp1, exp2, numberIndex);
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return 0;
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return context.getNumber(numberIndex).set(exp1.evaluateReal(context, scope), exp2.evaluateReal(context, scope));
	}

	@Override
	public boolean isReal() {
		return false;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		if (!exp1.isReal()) {
			throw new DSLException("Invalid expression type: " + exp1.token.getText(), exp1.token);
		}
		if (!exp2.isReal()) {
			throw new DSLException("Invalid expression type: " + exp2.token.getText(), exp2.token);
		}
		context.append("getNumber(");
		context.append(numberIndex);
		context.append(").set");
		context.append("(");
		exp1.compile(context, scope);
		context.append(",");
		exp2.compile(context, scope);
		context.append(")");
	}
}
