/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class ASTParen extends ASTExpression {
	private final ASTExpression exp;

	public ASTParen(Token location, ASTExpression exp) {
		super(location);
		this.exp = exp;
	}

    @Override
	public String toString() {
        return "(" + exp + ")";
	}

	@Override
	public DSLExpression resolve(ASTResolver resolver) {
		return resolver.resolve(this);
	}

	public boolean isReal() {
		return exp.isReal();
	}
}
