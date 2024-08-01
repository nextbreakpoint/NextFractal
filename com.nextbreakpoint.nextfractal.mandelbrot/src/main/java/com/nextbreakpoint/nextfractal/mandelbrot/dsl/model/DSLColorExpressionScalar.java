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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class DSLColorExpressionScalar extends DSLColorExpression {
	private final DSLExpression exp1;
	private final DSLExpression exp2;
	private final DSLExpression exp3;
	private final DSLExpression exp4;
	
	public DSLColorExpressionScalar(Token location, DSLExpression exp1, DSLExpression exp2, DSLExpression exp3, DSLExpression exp4) {
		super(location);
		this.exp1 = exp1;
		this.exp2 = exp2;
		this.exp3 = exp3;
		this.exp4 = exp4;
	}

	public float[] evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		if (exp1 != null && exp2 != null && exp3 != null && exp4 != null) {
			return new float[] { (float)exp1.evaluateReal(context, scope), (float)exp2.evaluateReal(context, scope), (float)exp3.evaluateReal(context, scope), (float)exp4.evaluateReal(context, scope) }; 
		} else if (exp1 != null && exp2 != null && exp3 != null) {
			return new float[] { 1, (float)exp1.evaluateReal(context, scope), (float)exp2.evaluateReal(context, scope), (float)exp3.evaluateReal(context, scope) }; 
		} else if (exp1 != null) {
			return new float[] { 1, (float)exp1.evaluateReal(context, scope), (float)exp1.evaluateReal(context, scope), (float)exp1.evaluateReal(context, scope) }; 
		}
		return new float[] { 1, 0, 0, 0 };
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("color(");
		exp1.compile(context, scope);
		if (exp2 != null) {
			context.append(",");
			exp2.compile(context, scope);
		}
		if (exp3 != null) {
			context.append(",");
			exp3.compile(context, scope);
		}
		if (exp4 != null) {
			context.append(",");
			exp4.compile(context, scope);
		}
		context.append(")");
	}
}
