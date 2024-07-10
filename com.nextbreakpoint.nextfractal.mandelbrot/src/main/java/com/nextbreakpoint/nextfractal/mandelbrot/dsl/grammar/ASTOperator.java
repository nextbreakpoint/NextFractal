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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import org.antlr.v4.runtime.Token;

public class ASTOperator extends ASTExpression {
	private String op;
	private ASTExpression exp1;
	private ASTExpression exp2;

	public ASTOperator(Token location, String op, ASTExpression exp) {
		super(location);
		this.op = op;
		this.exp1 = exp;
		this.exp2 = null;
	}

	public ASTOperator(Token location, String op, ASTExpression exp1, ASTExpression exp2) {
		super(location);
		this.op = op;
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	public String getOp() {
		return op;
	}
	
	public ASTExpression getExp1() {
		return exp1;
	}

	public ASTExpression getExp2() {
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

	@Override
	public CompiledExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}

	public boolean isReal() {
		if (op.equals("<>")) {
			return false;
		}
		return exp1.isReal() && (exp2 == null || exp2.isReal());
	}
}
