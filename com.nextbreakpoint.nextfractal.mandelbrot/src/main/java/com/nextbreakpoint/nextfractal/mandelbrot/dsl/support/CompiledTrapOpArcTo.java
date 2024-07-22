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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.support;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import org.antlr.v4.runtime.Token;

public class CompiledTrapOpArcTo extends CompiledTrapOp {
	private Number c1;
	private Number c2;
	
	public CompiledTrapOpArcTo(Number c1, Number c2, Token location) {
		super(location);
		this.c1 = c1;
		this.c2 = c2;
	}

	@Override
	public void evaluate(Trap trap) {
		trap.arcTo(c1, c2);
	}
}
