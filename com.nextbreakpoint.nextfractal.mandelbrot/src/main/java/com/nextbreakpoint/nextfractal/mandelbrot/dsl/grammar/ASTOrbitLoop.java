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

import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ASTOrbitLoop extends ASTObject {
	private int begin;
	private int end;
	private ASTConditionExpression expression;
	private List<ASTStatement> statements = new ArrayList<>(); 

	public ASTOrbitLoop(Token location, int begin, int end, ASTConditionExpression expression) {
		super(location);
		this.begin = begin;
		this.end = end;
		this.expression = expression;
	}

	public ASTConditionExpression getExpression() {
		return expression;
	}

	public void setExpression(ASTConditionExpression expression) {
		this.expression = expression;
	}

	public List<ASTStatement> getStatements() {
		return statements;
	}

	public void addStatement(ASTStatement statement) {
		statements.add(statement);
	}

	public int getBegin() {
		return begin;
	}

	public int getEnd() {
		return end;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("begin = ");
		builder.append(begin);
		builder.append(",");
		builder.append("end = ");
		builder.append(end);
		builder.append(",");
		builder.append("expression = [");
		builder.append(expression);
		builder.append("],");
		builder.append("statements = [");
		for (int i = 0; i < statements.size(); i++) {
			ASTStatement statement = statements.get(i);
			builder.append("{");
			builder.append(statement);
			builder.append("}");
			if (i < statements.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
}
