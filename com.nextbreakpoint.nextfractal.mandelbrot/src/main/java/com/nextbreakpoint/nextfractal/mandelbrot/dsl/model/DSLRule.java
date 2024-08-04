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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;
import lombok.Getter;

import java.util.Map;

@Getter
public class DSLRule extends DSLObject {
	private final DSLCondition ruleCondition;
	private final DSLColorExpression colorExp;
	private final double opacity;

	public DSLRule(DSLToken token, DSLCondition ruleCondition, DSLColorExpression colorExp, double opacity) {
		super(token);
        this.ruleCondition = ruleCondition;
        this.colorExp = colorExp;
        this.opacity = opacity;
    }

	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		context.append("if (");
		ruleCondition.compile(context, scope);
		context.append(") {\n");
		context.append("addColor(");
		context.append(opacity);
		context.append(",");
		colorExp.compile(context, scope);
		context.append(");\n}\n");
	}
}
