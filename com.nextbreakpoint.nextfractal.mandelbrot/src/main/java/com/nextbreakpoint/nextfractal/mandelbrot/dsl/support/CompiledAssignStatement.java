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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class CompiledAssignStatement extends CompiledStatement {
	private String name;
	private CompiledExpression exp;
	
	public CompiledAssignStatement(String name, CompiledExpression exp, Map<String, CompilerVariable> variables, Token location) {
		super(location);
		this.name = name;
		this.exp = exp;
		CompilerVariable var = variables.get(name);
		if (var != null) {
			if (var.isReal() && !exp.isReal()) {
				throw new ASTException("Can't assign expression", getLocation());
			}
		}
	}

	@Override
	public boolean evaluate(InterpreterContext context, Map<String, CompilerVariable> scope) {
		CompilerVariable var = scope.get(name);
		if (var != null) {
			if (var.isReal() && exp.isReal()) {
				var.setValue(exp.evaluateReal(context, scope));
			} else if (!var.isReal()) {
				var.setValue(exp.evaluate(context, scope));
			} else {
				throw new ASTException("Can't assign expression", getLocation());
			}
		} else {
			var = new CompilerVariable(name, exp.isReal(), false);
			scope.put(name, var);
			if (var.isReal() && exp.isReal()) {
				var.setValue(exp.evaluateReal(context, scope));
			} else {
				var.setValue(exp.evaluate(context, scope));
			}
		}
		return false;
	}
}
