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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import org.antlr.v4.runtime.Token;

import java.util.Map;

import static com.nextbreakpoint.nextfractal.mandelbrot.core.Expression.opDiv;

public class CompiledOperatorDivZ extends CompiledExpression {
	private final CompiledExpression exp1;
	private final CompiledExpression exp2;
	
	public CompiledOperatorDivZ(ExpressionCompilerContext context, CompiledExpression exp1, CompiledExpression exp2, Token location) {
		super(context.newNumberIndex(), location);
		this.exp1 = exp1;
		this.exp2 = exp2;
	}

	@Override
	public double evaluateReal(ExpressionContext context, Map<String, Variable> scope) {
		throw new ASTException("Can't assign operator result to real number", getLocation());
	}

	@Override
	public Number evaluate(ExpressionContext context, Map<String, Variable> scope) {
		return opDiv(context.getNumber(index), exp1.evaluate(context, scope), exp2.evaluate(context, scope));
	}

	@Override
	public boolean isReal() {
		return false;
	}
}
