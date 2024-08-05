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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.JavaCompilerProvider;

import javax.tools.JavaCompiler;
import java.util.Objects;

public class DSLCompiler {
	private final String packageName;
	private final String className;

	public DSLCompiler(String packageName, String className) {
		this.packageName = Objects.requireNonNull(packageName);
		this.className = Objects.requireNonNull(className);
	}

	public DSLParserResult compile(DSLExpressionContext context, DSLParserResult result) throws DSLCompilerException {
		CompilerResult<Orbit> orbitResult = compileOrbit(context, result);
		CompilerResult<Color> colorResult = compileColor(context, result);
		return result.toBuilder()
				.withOrbitClassFactory(orbitResult.classFactory())
				.withColorClassFactory(colorResult.classFactory())
				.build();
	}

	private CompilerResult<Orbit> compileOrbit(DSLExpressionContext context, DSLParserResult result) throws DSLCompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return new CompilerResult<>(() -> new InterpretedOrbit(context, result.fractal().getOrbit()), null);
		} else {
			return new FractalCompiler(packageName, className, javaCompiler).compileOrbit(context, result);
		}
	}

	private CompilerResult<Color> compileColor(DSLExpressionContext context, DSLParserResult result) throws DSLCompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return new CompilerResult<>(() -> new InterpretedColor(context, result.fractal().getColor()), null);
		} else {
			return new FractalCompiler(packageName, className, javaCompiler).compileColor(context, result);
		}
	}
}
