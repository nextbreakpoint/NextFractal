/*
 * NextFractal 2.3.1
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

public class ASTConditionalExpression extends ASTExpression {
	private ASTConditionExpression conditionExp;
	private ASTExpression thenExp;
	private ASTExpression elseExp;

	public ASTConditionalExpression(Token location, ASTConditionExpression conditionExp, ASTExpression thenExp, ASTExpression elseExp) {
		super(location);
		this.conditionExp = conditionExp;
		this.thenExp = thenExp;
		this.elseExp = elseExp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(conditionExp);
		builder.append(" ? ");
		builder.append(thenExp);
		builder.append(" : ");
		builder.append(elseExp);
		return builder.toString();
	}

	@Override
	public CompiledExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}

	public ASTConditionExpression getConditionExp() {
		return conditionExp;
	}

	public ASTExpression getThenExp() {
		return thenExp;
	}

	public ASTExpression getElseExp() {
		return elseExp;
	}
}
