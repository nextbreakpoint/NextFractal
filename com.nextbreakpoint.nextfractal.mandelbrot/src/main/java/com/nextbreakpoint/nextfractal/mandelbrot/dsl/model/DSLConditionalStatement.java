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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DSLConditionalStatement extends DSLStatement {
	private final DSLCondition condition;
	private final List<DSLStatement> thenStatements;
	private final List<DSLStatement> elseStatements;

    public DSLConditionalStatement(DSLToken token, DSLCondition condition, List<DSLStatement> thenStatements, List<DSLStatement> elseStatements) {
		super(token);
		this.condition = condition;
		this.thenStatements = thenStatements;
		this.elseStatements = elseStatements;
	}

	@Override
	public boolean evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		boolean stop = false;
		if (condition.evaluate(context, scope)) {
            Map<String, Variable> newThenScope = new HashMap<>(scope);
			for (DSLStatement statement : thenStatements) {
				stop = statement.evaluate(context, newThenScope);
				if (stop) {
					break;
				}
			}
		} else {
            Map<String, Variable> newElseScope = new HashMap<>(scope);
			for (DSLStatement statement : elseStatements) {
				stop = statement.evaluate(context, newElseScope);
				if (stop) {
					break;
				}
			}
		}
		return stop;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("if (");
		condition.compile(context, scope);
		context.append(") {\n");
		if (thenStatements != null) {
			Map<String, VariableDeclaration> newScope = new HashMap<>(scope);
			for (DSLStatement innerStatement : thenStatements) {
				innerStatement.compile(context, newScope);
			}
		}
		if (elseStatements != null) {
			context.append("} else {\n");
			Map<String, VariableDeclaration> newScope = new HashMap<>(scope);
			for (DSLStatement innerStatement : elseStatements) {
				innerStatement.compile(context, newScope);
			}
		}
		context.append("}\n");
	}
}
