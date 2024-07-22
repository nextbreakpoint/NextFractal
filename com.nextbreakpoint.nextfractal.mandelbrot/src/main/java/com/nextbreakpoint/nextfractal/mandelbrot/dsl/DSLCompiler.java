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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl;

import com.nextbreakpoint.nextfractal.mandelbrot.core.CompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.javacompiler.JavaCompilerDSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterDSLCompiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class DSLCompiler {
	private static final String PROPERTY_MANDELBROT_COMPILER_DISABLED = "com.nextbreakpoint.nextfractal.mandelbrot.compiler.disabled";

	public DSLCompiler() {
	}
	
	public ClassFactory<Orbit> compileOrbit(DSLParserResult result) throws CompilerException {
		JavaCompiler javaCompiler = getJavaCompiler();
		if (javaCompiler == null) {
			return new InterpreterDSLCompiler().compileOrbit(result);
		} else {
			return new JavaCompilerDSLCompiler().compileOrbit(result);
		}
	}

	public ClassFactory<Color> compileColor(DSLParserResult result) throws CompilerException {
		JavaCompiler javaCompiler = getJavaCompiler();
		if (javaCompiler == null) {
			return new InterpreterDSLCompiler().compileColor(result);
		} else {
			return new JavaCompilerDSLCompiler().compileColor(result);
		}
	}

	public JavaCompiler getJavaCompiler() {
		return !Boolean.getBoolean(PROPERTY_MANDELBROT_COMPILER_DISABLED) ? ToolProvider.getSystemJavaCompiler() : null;
	}
}	
