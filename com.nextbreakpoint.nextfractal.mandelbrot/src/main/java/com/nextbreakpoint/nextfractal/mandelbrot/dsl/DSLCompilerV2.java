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

import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.JavaCompilerProvider;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler.FastDSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler.SimpleDSLCompiler;

import javax.tools.JavaCompiler;
import java.util.Objects;

public class DSLCompilerV2 {
	private final String packageName;
	private final String classNamePrefix;

	public DSLCompilerV2(String packageName, String classNamePrefix) {
		this.packageName = Objects.requireNonNull(packageName);
		this.classNamePrefix = Objects.requireNonNull(classNamePrefix);
	}

	public ClassFactory<Orbit> compileOrbit(DSLParserResultV2 result) throws DSLCompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return new SimpleDSLCompiler().compileOrbit(result);
		} else {
			return new FastDSLCompiler(packageName, classNamePrefix, javaCompiler).compileOrbit(result);
		}
	}

	public ClassFactory<Color> compileColor(DSLParserResultV2 result) throws DSLCompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return new SimpleDSLCompiler().compileColor(result);
		} else {
			return new FastDSLCompiler(packageName, classNamePrefix, javaCompiler).compileColor(result);
		}
	}
}
