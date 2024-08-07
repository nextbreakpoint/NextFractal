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

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

public class DSLTrap {
	protected final DSLToken token;
	@Getter
	@Setter
    private String name;
	@Getter
	@Setter
    private ComplexNumber center;
	@Getter
	@Setter
    private List<DSLTrapOp> operators;

	public DSLTrap(DSLToken token) {
		this.token = token;
	}

	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("private Trap trap");
		context.append(name.toUpperCase().substring(0, 1));
		context.append(name.substring(1));
		context.append(" = trap(number(");
		context.append(center);
		context.append("))");
		for (DSLTrapOp operator : operators) {
			context.append(".");
			operator.compile(context, scope);
		}
		context.append(";\n");
	}
}
