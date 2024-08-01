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

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResultV2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedOrbit;

import java.util.List;

import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.COMPILE;

public class SimpleDSLCompiler {
	public ClassFactory<Orbit> compileOrbit(DSLParserResultV2 result) throws DSLCompilerException {
		if (result.fractal() == null) {
			final List<ParserError> errors = List.of(new ParserError(COMPILE, 0, 0, 0, 0, "Can't compile orbit"));
			throw new DSLCompilerException("Can't compile orbit", result.source(), errors);
		}

		return () -> new InterpretedOrbit(result.fractal().getOrbit());
	}

	public ClassFactory<Color> compileColor(DSLParserResultV2 result) throws DSLCompilerException {
		if (result.fractal() == null) {
			final List<ParserError> errors = List.of(new ParserError(COMPILE, 0, 0, 0, 0, "Can't compile color"));
			throw new DSLCompilerException("Can't compile color", result.source(), errors);
		}

		return () -> new InterpretedColor(result.fractal().getColor());
	}
}	
