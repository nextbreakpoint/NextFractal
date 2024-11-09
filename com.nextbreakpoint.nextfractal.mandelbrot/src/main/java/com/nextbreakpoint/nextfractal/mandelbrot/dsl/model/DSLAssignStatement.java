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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;

import java.util.Map;

public class DSLAssignStatement extends DSLStatement {
	private final String name;
	private final int numberIndex;
	private final DSLExpression exp;

	public DSLAssignStatement(DSLToken token, String name, DSLExpression exp, int numberIndex) {
		super(token);
		this.name = name;
		this.exp = exp;
		this.numberIndex = numberIndex;
	}

	@Override
	public boolean evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		Variable var = scope.get(name);
		if (var != null) {
			if (var.isReal() && exp.isReal()) {
				var.setValue(exp.evaluateReal(context, scope));
			} else if (!var.isReal()) {
				var.setValue(exp.evaluate(context, scope));
			} else {
				throw new DSLException("Can't assign expression: " + exp.token.getText(), exp.token);
			}
		} else {
			var = new Variable(name, exp.isReal());
			scope.put(name, var);
			if (var.isReal() && exp.isReal()) {
				var.setValue(exp.evaluateReal(context, scope));
			} else {
				var.setValue(exp.evaluate(context, scope));
			}
		}
		return false;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		VariableDeclaration var = scope.get(name);
		if (var != null) {
			if (var.real() && exp.isReal()) {
				context.append(name);
				context.append(" = real(");
				exp.compile(context, scope);
				context.append(");\n");
			} else if (!var.real() && !exp.isReal()) {
				context.append(name);
				context.append(".set(");
				exp.compile(context, scope);
				context.append(");\n");
			} else if (!var.real() && exp.isReal()) {
				context.append(name);
				context.append(".set(");
				exp.compile(context, scope);
				context.append(");\n");
			} else if (var.real() && !exp.isReal()) {
				throw new DSLException("Can't assign expression: " + exp.token.getText(), exp.token);
			}
		} else {
			var = new VariableDeclaration(name, exp.isReal(), false);
			scope.put(name, var);
			if (var.real()) {
				context.append("double ");
				context.append(name);
				context.append(" = real(");
				exp.compile(context, scope);
				context.append(");\n");
			} else {
				context.append("final MutableNumber ");
				context.append(name);
				context.append(" = getNumber(");
				context.append(numberIndex);
				context.append(").set(");
				exp.compile(context, scope);
				context.append(");\n");
			}
		}
	}
}
