/*
 * NextFractal 2.3.0
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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import org.antlr.v4.runtime.Token;

public class CompiledRule {
	private CompiledCondition ruleCondition;
	private CompiledColorExpression colorExp;
	private double opacity;
	private Token location;

	public CompiledRule(Token location) {
		this.location = location;
	}

	public CompiledCondition getRuleCondition() {
		return ruleCondition;
	}

	public void setRuleCondition(CompiledCondition ruleCondition) {
		this.ruleCondition = ruleCondition;
	}

	public CompiledColorExpression getColorExp() {
		return colorExp;
	}

	public void setColorExp(CompiledColorExpression colorExp) {
		this.colorExp = colorExp;
	}

	public double getOpacity() {
		return opacity;
	}

	public void setOpacity(double opacity) {
		this.opacity = opacity;
	}

	public Token getLocation() {
		return location;
	}
}
