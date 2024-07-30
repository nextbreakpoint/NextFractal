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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class CompiledVariable extends CompiledExpression {
	private final String name;
	private final boolean real;

	public CompiledVariable(ExpressionCompilerContext context, String name, boolean real, Token location) {
		super(context.newNumberIndex(), location);
		this.name = name;
		this.real = real;
	}

	@Override
	public double evaluateReal(ExpressionContext context, Map<String, Variable> scope) {
		Variable var = scope.get(name);
		return var.getValue().r();
	}

	@Override
	public Number evaluate(ExpressionContext context, Map<String, Variable> scope) {
		Variable var = scope.get(name);
		return var.getValue();
	}

	@Override
	public boolean isReal() {
		return real;
	}

	@Override
	public String toString() {
		return name + " [real=" + real + "]";
	}
}
