/*
 * NextFractal 2.1.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterContext;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class CompiledConditionalExpression extends CompiledExpression {
	private CompiledCondition condition;
	private CompiledExpression thenExp;
	private CompiledExpression elseExp;
	
	public CompiledConditionalExpression(ExpressionContext context, CompiledCondition condition, CompiledExpression thenExp, CompiledExpression elseExp, Token location) {
		super(context.newNumberIndex(), location);
		this.condition = condition;
		this.thenExp = thenExp;
		this.elseExp = elseExp;
	}

	@Override
	public double evaluateReal(InterpreterContext context, Map<String, CompilerVariable> scope) {
		return condition.evaluate(context, scope) ? thenExp.evaluateReal(context, scope) : elseExp.evaluateReal(context, scope);
	}
	
	@Override
	public Number evaluate(InterpreterContext context, Map<String, CompilerVariable> scope) {
		return condition.evaluate(context, scope) ? thenExp.evaluate(context, scope) : elseExp.evaluate(context, scope);
	}

	@Override
	public boolean isReal() {
		return thenExp.isReal() && elseExp.isReal();
	}
}
