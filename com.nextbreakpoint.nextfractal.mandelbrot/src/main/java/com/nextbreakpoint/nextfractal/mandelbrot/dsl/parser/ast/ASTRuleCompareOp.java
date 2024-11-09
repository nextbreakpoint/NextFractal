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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class ASTRuleCompareOp extends ASTRuleExpression {
	private final String op;
	private final ASTExpression exp1;
	private final ASTExpression exp2;
	
	public ASTRuleCompareOp(Token location, String op, ASTExpression exp1, ASTExpression exp2) {
		super(location);
		this.op = op;
		this.exp1 = exp1;
		this.exp2 = exp2;
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
	public DSLCondition resolve(ASTResolver resolver) {
		return resolver.resolve(this);
	}
}
