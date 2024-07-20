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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import org.antlr.v4.runtime.Token;

public class CompiledTrapOpCurveRel extends CompiledTrapOp {
	private Number c1;
	private Number c2;
	private Number c3;
	
	public CompiledTrapOpCurveRel(Number c1, Number c2, Number c3, Token location) {
		super(location);
		this.c1 = c1;
		this.c2 = c2;
		this.c3 = c3;
	}

	@Override
	public void evaluate(Trap trap) {
		trap.curveRel(c1, c2, c3);
	}
}
