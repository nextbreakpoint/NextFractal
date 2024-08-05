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

public class DSLConditionalExpression extends DSLExpression {
	private final DSLCondition condition;
	private final DSLExpression thenExp;
	private final DSLExpression elseExp;
	
	public DSLConditionalExpression(DSLToken token, DSLCondition condition, DSLExpression thenExp, DSLExpression elseExp, int numberIndex) {
		super(token);
		this.condition = condition;
		this.thenExp = thenExp;
		this.elseExp = elseExp;
	}

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return condition.evaluate(context, scope) ? thenExp.evaluateReal(context, scope) : elseExp.evaluateReal(context, scope);
	}
	
	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return condition.evaluate(context, scope) ? thenExp.evaluate(context, scope) : elseExp.evaluate(context, scope);
	}

	@Override
	public boolean isReal() {
		return thenExp.isReal() && elseExp.isReal();
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("(");
		context.append("(");
		condition.compile(context, scope);
		context.append(")");
		context.append("?");
		thenExp.compile(context, scope);
		context.append(":");
		elseExp.compile(context, scope);
		context.append(")");
	}
}
