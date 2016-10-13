/*
 * NextFractal 1.3.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2016 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.compiler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;

public class Compiler {
	private final String packageName;
	private final String className;
	
	public Compiler() {
		this(Compiler.class.getPackage().getName(), Compiler.class.getSimpleName());
	}
	
	public Compiler(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
	}
	
	public CompilerReport compileReport(String source) throws IOException {
//		JavaCompiler javaCompiler = getJavaCompiler();
//		if (javaCompiler == null) {
//			InterpreterReportCompiler compiler = new InterpreterReportCompiler();
//			return compiler.generateReport(source);
//		} else {
//			JavaReportCompiler compiler = new JavaReportCompiler(packageName, className);
//			return compiler.generateReport(source);
//		}
		return null;
	}
	
	public JavaCompiler getJavaCompiler() {
		return !Boolean.getBoolean("contextfree.compiler.disabled") ? ToolProvider.getSystemJavaCompiler() : null;
	}
}	
