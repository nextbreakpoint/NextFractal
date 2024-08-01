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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionContext;
import lombok.Builder;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder(setterPrefix = "width", toBuilder = true)
public class DSLOrbit {
	private final Token location;
	@Getter
	private final DSLOrbitBegin begin;
	@Getter
	private final DSLOrbitLoop loop;
	@Getter
	private final DSLOrbitEnd end;
	@Getter
	private final Collection<VariableDeclaration> orbitVariables;
	@Getter
	private final Collection<VariableDeclaration> stateVariables;
	@Getter
	private final ExpressionContext expressionContext;
	@Getter
    private final ComplexNumber[] region;
	@Getter
    private final List<DSLTrap> traps;

	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("public void init() {\n");
		context.append("setInitialRegion(");
		context.append("number(");
		context.append(region[0]);
		context.append("),number(");
		context.append(region[1]);
		context.append("));\n");
		for (VariableDeclaration var : stateVariables) {
			context.append("addVariable(");
			context.append(var.getName());
			context.append(");\n");
		}
		context.append("resetTraps();\n");
		for (DSLTrap trap : traps) {
			context.append("addTrap(trap");
			context.append(trap.getName().toUpperCase().substring(0, 1));
			context.append(trap.getName().substring(1));
			context.append(");\n");
		}
		context.append("}\n");
		for (VariableDeclaration var : scope.values()) {
			scope.put(var.getName(), var);
			if (var.isCreate()) {
				if (var.isReal()) {
					context.append("private double ");
					context.append(var.getName());
					context.append(" = 0.0;\n");
				} else {
					context.append("private final MutableNumber ");
					context.append(var.getName());
					context.append(" = getNumber(");
					context.append(context.newNumberIndex());
					context.append(").set(0.0,0.0);\n");
				}
			}
		}
		for (DSLTrap trap : traps) {
			trap.compile(context, scope);
		}
		context.append("public void render(List<ComplexNumber[]> states) {\n");
		if (begin != null) {
			begin.compile(context, scope);
		}
		if (loop != null) {
			loop.compile(context, scope);
		}
		if (end != null) {
			end.compile(context, scope);
		}
		int i = 0;
		for (VariableDeclaration var : stateVariables) {
			context.append("setVariable(");
			context.append(i++);
			context.append(",");
			context.append(var.getName());
			context.append(");\n");
		}
		context.append("}\n");
		context.append("protected MutableNumber[] createNumbers() {\n");
		context.append("return new MutableNumber[");
		context.append(context.getNumberCount());
		context.append("];\n");
		context.append("}\n");
		context.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
		context.append("public boolean useTime() {\nreturn ");
		context.append(context.orbitUseTime());
		context.append(";\n}\n");
	}
}
