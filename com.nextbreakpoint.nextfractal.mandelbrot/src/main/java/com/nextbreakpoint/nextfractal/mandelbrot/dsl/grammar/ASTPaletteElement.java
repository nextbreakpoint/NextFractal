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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import org.antlr.v4.runtime.Token;

public class ASTPaletteElement extends ASTObject {
	private ASTColorARGB beginColor; 
	private ASTColorARGB endColor; 
	private int steps; 
	private ASTExpression exp;
	
	public ASTPaletteElement(Token location, ASTColorARGB beginColor, ASTColorARGB endColor, int steps, ASTExpression exp) {
		super(location);
		this.beginColor = beginColor;
		this.endColor = endColor;
		this.steps = steps;
		this.exp = exp;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("beginColor = ");
		builder.append(beginColor);
		builder.append(",endColor = ");
		builder.append(endColor);
		builder.append(",steps = ");
		builder.append(steps);
		builder.append(",exp = {");
		builder.append(exp);
		builder.append("}");
		return builder.toString();
	}

	public int getSteps() {
		return steps;
	}

	public ASTColorARGB getBeginColor() {
		return beginColor;
	}

	public ASTColorARGB getEndColor() {
		return endColor;
	}

	public ASTExpression getExp() {
		return exp;
	}

	public CompiledPaletteElement compile(ASTExpressionCompiler compiler) {
		return compiler.compile(this);
	}
}
