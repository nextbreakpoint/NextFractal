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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;

import java.util.Map;

public abstract class DSLTrapOp {
	protected final DSLToken token;
	//TODO replace with array of complex numbers
	protected final ComplexNumber c1;
	protected final ComplexNumber c2;
	protected final ComplexNumber c3;

	protected DSLTrapOp(DSLToken token, ComplexNumber c1, ComplexNumber c2, ComplexNumber c3) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.token = token;
	}

	public abstract void evaluate(Trap trap);

	public abstract void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope);
}
