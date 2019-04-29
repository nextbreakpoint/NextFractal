/*
 * NextFractal 2.1.1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2019 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.mandelbrot.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.compiler.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.compiler.support.CompiledExpression;
import org.antlr.v4.runtime.Token;

public class ASTVariable extends ASTExpression {
	private CompilerVariable variable;

	public ASTVariable(Token location, CompilerVariable variable) {
		super(location);
		this.variable = variable;
	}

	public String getName() {
		return variable.getName();
	}

	@Override
	public String toString() {
		return variable.getName();
	}
	
	@Override
	public boolean isReal() {
		return variable.isReal();
	}

	@Override
	public CompiledExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}
}
