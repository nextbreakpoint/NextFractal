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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import org.antlr.v4.runtime.Token;

public class ASTConditionalStatement extends ASTStatement {
	private ASTConditionExpression conditionExp;
	private ASTStatementList thenStatementList;
	private ASTStatementList elseStatementList;

	public ASTConditionalStatement(Token location, ASTConditionExpression conditionExp, ASTStatementList thenStatementList) {
		super(location);
		this.conditionExp = conditionExp;
		this.thenStatementList = thenStatementList;
	}

	public ASTConditionalStatement(Token location, ASTConditionExpression conditionExp, ASTStatementList thenStatementList, ASTStatementList elseStatementList) {
		this(location, conditionExp, thenStatementList);
		this.elseStatementList = elseStatementList;
	}

	public ASTConditionExpression getConditionExp() {
		return conditionExp;
	}

	public ASTStatementList getThenStatementList() {
		return thenStatementList;
	}

	public ASTStatementList getElseStatementList() {
		return elseStatementList;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("if (");
		builder.append(conditionExp);
		builder.append(") {\n");
		builder.append(thenStatementList);
		if (elseStatementList != null) {
			builder.append("} else {\n");
			builder.append(elseStatementList);
		}
		builder.append("}");
		return builder.toString();
	}

	@Override
	public CompiledStatement compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}
}
