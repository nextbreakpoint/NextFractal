/*
 * NextFractal 2.3.1
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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterContext;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class CompiledColorComponentExpression extends CompiledColorExpression {
	private CompiledExpression exp1;
	private CompiledExpression exp2;
	private CompiledExpression exp3;
	private CompiledExpression exp4;
	
	public CompiledColorComponentExpression(CompiledExpression exp1, CompiledExpression exp2, CompiledExpression exp3, CompiledExpression exp4, Token location) {
		super(location);
		this.exp1 = exp1;
		this.exp2 = exp2;
		this.exp3 = exp3;
		this.exp4 = exp4;
	}

	public float[] evaluate(InterpreterContext context, Map<String, CompilerVariable> scope) {
		if (exp1 != null && exp2 != null && exp2 != null && exp4 != null) {
			return new float[] { (float)exp1.evaluateReal(context, scope), (float)exp2.evaluateReal(context, scope), (float)exp3.evaluateReal(context, scope), (float)exp4.evaluateReal(context, scope) }; 
		} else if (exp1 != null && exp2 != null && exp3 != null) {
			return new float[] { 1, (float)exp1.evaluateReal(context, scope), (float)exp2.evaluateReal(context, scope), (float)exp3.evaluateReal(context, scope) }; 
		} else if (exp1 != null) {
			return new float[] { 1, (float)exp1.evaluateReal(context, scope), (float)exp1.evaluateReal(context, scope), (float)exp1.evaluateReal(context, scope) }; 
		}
		return new float[] { 1, 0, 0, 0 };
	}
}
