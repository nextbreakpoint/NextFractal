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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.Map;

@Getter
public class CompiledNumber extends CompiledExpression {
	private final double r;
	private final double i;

	public CompiledNumber(ExpressionCompilerContext context, double r, double i, Token location) {
		super(context.newNumberIndex(), location);
		this.r = r;
		this.i = i;
	}

	@Override
	public double evaluateReal(ExpressionContext context, Map<String, Variable> scope) {
		return r;
	}

	@Override
	public Number evaluate(ExpressionContext context, Map<String, Variable> scope) {
		MutableNumber number = context.getNumber(index);
		number.set(r, i);
		return number;
	}

	@Override
	public boolean isReal() {
		return i == 0;
	}
}
