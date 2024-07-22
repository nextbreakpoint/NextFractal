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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import org.antlr.v4.runtime.Token;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ASTFunction extends ASTExpression {
	private String name;
	private ASTExpression[] arguments;
	private Set<String> realFunctions = new HashSet<>(Arrays.asList("time", "mod", "mod2", "pha", "log", "exp", "atan2", "hypot", "sqrt", "re", "im", "ceil", "floor", "abs", "square", "saw", "ramp", "pulse"));

	public ASTFunction(Token location, String name, ASTExpression[] arguments) {
		super(location);
		this.name = name;
		this.arguments = arguments;
	}

	public ASTFunction(Token location, String name, ASTExpression argument) {
		this(location, name, new ASTExpression[] { argument });
	}

	public String getName() {
		return name;
	}

	public ASTExpression[] getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("(");
		for (int i = 0; i < arguments.length; i++) {
			builder.append(arguments[i]);
			if (i < arguments.length - 1) {
				builder.append(",");
			}
		}
		builder.append(")");
		return builder.toString();
	}

	@Override
	public CompiledExpression compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}

	@Override
	public boolean isReal() {
		if (realFunctions.contains(name)) {
			return true;
		}
		for (ASTExpression argument : arguments) {
			if (!argument.isReal()) {
				return false;
			}
		}
		return true;
	}
}
