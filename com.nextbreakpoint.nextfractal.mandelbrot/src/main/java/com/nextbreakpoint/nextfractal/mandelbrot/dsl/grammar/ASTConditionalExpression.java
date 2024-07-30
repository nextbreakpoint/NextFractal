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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class ASTConditionalExpression extends ASTExpression {
	private final ASTConditionExpression conditionExp;
	private final ASTExpression thenExp;
	private final ASTExpression elseExp;

	public ASTConditionalExpression(Token location, ASTConditionExpression conditionExp, ASTExpression thenExp, ASTExpression elseExp) {
		super(location);
		this.conditionExp = conditionExp;
		this.thenExp = thenExp;
		this.elseExp = elseExp;
	}

	@Override
	public String toString() {
        return conditionExp + " ? " + thenExp + " : " + elseExp;
	}

	@Override
	public CompiledExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}

}
