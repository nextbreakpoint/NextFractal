/*
 * NextFractal 2.1.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.mandelbrot.compiler;

import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.mandelbrot.grammar.ASTFractal;

import java.util.List;

public class CompilerReport {
	private ASTFractal ast;
	private Type type;
	private String orbitSource;
	private String colorSource;
	private List<SourceError> errors;
	private String source;
	private String packageName;
	private String className;

	public CompilerReport(ASTFractal ast, Type type, String source, String orbitSource, String colorSource, List<SourceError> errors, String packageName, String className) {
		this.ast = ast;
		this.type = type;
		this.source = source;
		this.orbitSource = orbitSource;
		this.colorSource = colorSource;
		this.errors = errors;
		this.packageName = packageName;
		this.className = className;
	}

	public ASTFractal getAST() {
		return ast;
	}

	public String getOrbitSource() {
		return orbitSource;
	}

	public String getColorSource() {
		return colorSource;
	}

	public List<SourceError> getErrors() {
		return errors;
	}

	public Type getType() {
		return type;
	}

	public String getSource() {
		return source;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	public enum Type {
		JAVA, INTERPRETER
	}
}
