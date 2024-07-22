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

public class ASTRule extends ASTObject {
	private ASTRuleExpression ruleExp;
	private ASTColorExpression colorExp;
	private float opacity;

	public ASTRule(Token location, float opacity, ASTRuleExpression ruleExp, ASTColorExpression colorExp) {
		super(location);
		this.opacity = opacity;
		this.ruleExp = ruleExp;
		this.colorExp = colorExp;
	}

	public ASTRuleExpression getRuleExp() {
		return ruleExp;
	}

	public ASTColorExpression getColorExp() {
		return colorExp;
	}

	public float getOpacity() {
		return opacity;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("opacity = ");
		builder.append(opacity);
		builder.append(",ruleExp = {");
		builder.append(ruleExp);
		builder.append("},colorExp = {");
		builder.append(colorExp);
		builder.append("}");
		return builder.toString();
	}
}
