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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;

import java.util.Map;

public class DSLTrapCondition extends DSLCondition {
	private final String name;
	private final DSLExpression exp;

	public DSLTrapCondition(DSLToken token, String name, DSLExpression exp) {
		super(token);
		this.name = name;
        this.exp = exp;
	}

	@Override
	public boolean evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		final Trap trap = context.getTrap(name);
		if (trap == null) {
			//TODO should it produce an error?
			return false;
		}
		return trap.contains(exp.evaluate(context, scope));
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("trap");
		context.append(name.toUpperCase().substring(0, 1));
		context.append(name.substring(1));
		context.append(".contains(");
		exp.compile(context, scope);
		context.append(")");
	}
}
