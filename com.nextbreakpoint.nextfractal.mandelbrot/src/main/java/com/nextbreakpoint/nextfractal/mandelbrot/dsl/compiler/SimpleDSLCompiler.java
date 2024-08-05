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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;

public class SimpleDSLCompiler {
	public CompilerResult<Orbit> compileOrbit(DSLExpressionContext context, DSLParserResult result) {
		return new CompilerResult<>(() -> new InterpretedOrbit(context, result.fractal().getOrbit()), null);
	}

	public CompilerResult<Color> compileColor(DSLExpressionContext context, DSLParserResult result) {
		return new CompilerResult<>(() -> new InterpretedColor(context, result.fractal().getColor()), null);
	}
}	
