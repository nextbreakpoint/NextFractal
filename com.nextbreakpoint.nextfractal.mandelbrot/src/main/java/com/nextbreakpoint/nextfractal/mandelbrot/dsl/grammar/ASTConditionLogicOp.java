/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import org.antlr.v4.runtime.Token;

public class ASTConditionLogicOp extends ASTConditionExpression {
	private String op;
	private ASTConditionExpression exp1;
	private ASTConditionExpression exp2;

	public ASTConditionLogicOp(Token location, String op, ASTConditionExpression exp1, ASTConditionExpression exp2) {
		super(location);
		this.op = op;
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	public String getOp() {
		return op;
	}
	
	public ASTConditionExpression getExp1() {
		return exp1;
	}

	public ASTConditionExpression getExp2() {
		return exp2;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(exp1);
		builder.append(op);
		if (exp2 != null) {
			builder.append(exp2);
		}
		return builder.toString();
	}

	public CompiledCondition compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}
}
