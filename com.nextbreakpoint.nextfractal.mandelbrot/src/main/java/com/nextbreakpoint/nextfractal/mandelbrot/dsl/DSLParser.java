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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.JavaCompilerProvider;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpreterDSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler.JavaCompilerDSLParser;

import javax.tools.JavaCompiler;
import java.util.Objects;

public class DSLParser {
	private final String packageName;
	private final String className;

	public DSLParser(String packageName, String className) {
		this.packageName = Objects.requireNonNull(packageName);
		this.className = Objects.requireNonNull(className);
	}
	
	public DSLParserResult parse(String source) throws DSLParserException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return new InterpreterDSLParser().parse(source);
		} else {
			return new JavaCompilerDSLParser(packageName, className).parse(source);
		}
	}
}
