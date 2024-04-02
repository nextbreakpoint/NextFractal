/*
 * NextFractal 2.1.5
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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import org.antlr.v4.runtime.Token;

public class ASTColorPalette extends ASTColorExpression {
	private String name;
	private ASTExpression exp;
	
	public ASTColorPalette(Token location, String name, ASTExpression exp) {
		super(location);
		this.name = name;
		this.exp = exp;
	}

	public String getName() {
		return name;
	}

	public ASTExpression getExp() {
		return exp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("[");
		builder.append(exp);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public CompiledColorExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}
}
