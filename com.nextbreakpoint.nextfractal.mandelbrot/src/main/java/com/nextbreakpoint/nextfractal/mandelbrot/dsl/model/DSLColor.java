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

import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@SuperBuilder(setterPrefix = "with", toBuilder = true)
public class DSLColor extends DSLObject {
	private final Collection<VariableDeclaration> colorVariables;
	private final Collection<VariableDeclaration> stateVariables;
	private final DSLExpressionContext expressionContext;
	private float[] backgroundColor;
	private boolean julia;
	private List<DSLRule> rules;
	private List<DSLPalette> palettes;
	private DSLColorInt init;

	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		for (DSLPalette palette : palettes) {
			context.append("private Palette palette");
			context.append(palette.getName().toUpperCase().substring(0, 1));
			context.append(palette.getName().substring(1));
			context.append(";\n");
		}
		context.append("public void init() {\n");
		int i = 0;
		for (VariableDeclaration var : stateVariables) {
			if (var.real()) {
				context.append("double ");
				context.append(var.name());
				context.append(" = getRealVariable(");
				context.append(i++);
				context.append(");\n");
			} else {
				context.append("final MutableNumber ");
				context.append(var.name());
				context.append(" = getVariable(");
				context.append(i++);
				context.append(");\n");
			}
		}
		i = 0;
		for (VariableDeclaration var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.real()) {
					context.append("double ");
					context.append(var.name());
					context.append(" = 0;\n");
				} else {
					context.append("final MutableNumber ");
					context.append(var.name());
					context.append(" = getNumber(");
					context.append(i++);
					context.append(");\n");
				}
			}
		}
		for (DSLPalette palette : palettes) {
			palette.compile(context, scope);
		}
		context.append("}\n");
		for (VariableDeclaration var : stateVariables) {
			scope.put(var.name(), var);
		}
		context.append("public void render() {\n");
		i = 0;
		for (VariableDeclaration var : stateVariables) {
			if (var.real()) {
				context.append("double ");
				context.append(var.name());
				context.append(" = getRealVariable(");
				context.append(i++);
				context.append(");\n");
			} else {
				context.append("final MutableNumber ");
				context.append(var.name());
				context.append(" = getVariable(");
				context.append(i++);
				context.append(");\n");
			}
		}
		i = 0;
		for (VariableDeclaration var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.real()) {
					context.append("double ");
					context.append(var.name());
					context.append(" = 0;\n");
				} else {
					context.append("final MutableNumber ");
					context.append(var.name());
					context.append(" = getNumber(");
					context.append(i++);
					context.append(");\n");
				}
			}
		}
		context.append("setColor(color(");
		context.append(backgroundColor[0]);
		context.append(",");
		context.append(backgroundColor[1]);
		context.append(",");
		context.append(backgroundColor[2]);
		context.append(",");
		context.append(backgroundColor[3]);
		context.append("));\n");
		if (init != null) {
			init.compile(context, scope);
		}
		for (DSLRule rule : rules) {
			rule.compile(context, scope);
		}
		context.append("}\n");
		context.append("protected MutableNumber[] createNumbers() {\n");
		context.append("return new MutableNumber[");
		context.append(context.getNumberCount());
		context.append("];\n");
		context.append("}\n");
		context.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
		context.append("public boolean useTime() {\nreturn ");
		context.append(context.colorUseTime());
		context.append(";\n}\n");
	}
}
