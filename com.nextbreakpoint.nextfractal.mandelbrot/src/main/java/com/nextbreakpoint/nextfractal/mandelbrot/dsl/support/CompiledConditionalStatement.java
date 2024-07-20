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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.support;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterContext;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledConditionalStatement extends CompiledStatement {
	private CompiledCondition condition;
	private List<CompiledStatement> thenStatements;
	private List<CompiledStatement> elseStatements;
	private Map<String, CompilerVariable> newThenScope;
	private Map<String, CompilerVariable> newElseScope;
	
	public CompiledConditionalStatement(CompiledCondition condition, List<CompiledStatement> thenStatements, List<CompiledStatement> elseStatements, Token location) {
		super(location);
		this.condition = condition;
		this.thenStatements = thenStatements;
		this.elseStatements = elseStatements;
	}

	@Override
	public boolean evaluate(InterpreterContext context, Map<String, CompilerVariable> scope) {
		boolean stop = false;
		if (condition.evaluate(context, scope)) {
			newThenScope = new HashMap<String, CompilerVariable>(scope);
			for (CompiledStatement statement : thenStatements) {
				stop = statement.evaluate(context, newThenScope);
				if (stop) {
					break;
				}
			}
		} else {
			newElseScope = new HashMap<String, CompilerVariable>(scope);
			for (CompiledStatement statement : elseStatements) {
				stop = statement.evaluate(context, newElseScope);
				if (stop) {
					break;
				}
			}
		}
		return stop;
	}
}
