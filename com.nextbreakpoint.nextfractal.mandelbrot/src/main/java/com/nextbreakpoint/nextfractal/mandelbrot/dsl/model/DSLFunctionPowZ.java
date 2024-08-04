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

import static com.nextbreakpoint.nextfractal.mandelbrot.core.Expression.funcPow;

public class DSLFunctionPowZ extends DSLFunction {
	public DSLFunctionPowZ(DSLToken token, DSLExpressionContext context, DSLExpression[] arguments) {
		super(token, arguments, context.newNumberIndex());
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		throw new DSLException("Can't assign function output to real number", token);
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return funcPow(context.getNumber(numberIndex), arguments[0].evaluate(context, scope), arguments[1].evaluateReal(context, scope));
	}

	@Override
	public boolean isReal() {
		return false;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("funcPow");
		context.append("(");
		if (arguments.length != 2) {
			throw new DSLException("Invalid number of arguments: " + token.getText(), token);
		}
		if (!arguments[1].isReal()) {
			throw new DSLException("Invalid type of arguments: " + token.getText(), token);
		}
		if (!arguments[0].isReal()) {
			context.append("getNumber(");
			context.append(numberIndex);
			context.append("),");
		}
		arguments[0].compile(context, scope);
		context.append(",");
		arguments[1].compile(context, scope);
		context.append(")");
	}
}
