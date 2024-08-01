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

import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class DSLFunctionTime extends DSLFunction {
    public DSLFunctionTime(Token location, int numberIndex) {
		super(location, new DSLExpression[0], numberIndex);
    }

	@Override
	public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
		return context.getTime().value() * context.getTime().scale();
	}

	@Override
	public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
		return context.getNumber(numberIndex).set(context.getTime().value() * context.getTime().scale());
	}

	@Override
	public boolean isReal() {
		return true;
	}

	@Override
	public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
		if (context.getClassType().equals(ClassType.ORBIT)) {
			context.setOrbitUseTime(true);
		}
		if (context.getClassType().equals(ClassType.COLOR)) {
			context.setColorUseTime(true);
		}
		if (arguments.length != 0) {
			throw new DSLException("Invalid number of arguments: " + location.getText(), location);
		}
		context.append("time()");
	}
}
