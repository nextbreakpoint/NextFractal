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
import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Expression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.FastExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResultV2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import lombok.extern.java.Log;

import javax.tools.JavaCompiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.JAVA_COMPILE;
import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.PARSE;
import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED;

@Log
public class FastDSLCompiler {
	private final String packageName;
	private final String classNamePrefix;
	private final CompilerAdapter compilerAdapter;

	public FastDSLCompiler(String packageName, String classNamePrefix, JavaCompiler javaCompiler) {
		this.packageName = Objects.requireNonNull(packageName);
		this.classNamePrefix = Objects.requireNonNull(classNamePrefix);
		this.compilerAdapter = new CompilerAdapter(javaCompiler);
	}

	public ClassFactory<Orbit> compileOrbit(DSLParserResultV2 report) throws DSLCompilerException {
		try {
			final StringBuilder builder = new StringBuilder();
            if (report.fractal() != null) {
				final DSLExpressionContext expressionContext = report.fractal().getOrbit().getExpressionContext();
				final HashMap<String, VariableDeclaration> scope = new HashMap<>();
				final DSLCompilerContext context = new DSLCompilerContext(expressionContext, builder, ClassType.ORBIT);
				compileOrbit(context, report.fractal().getOrbit(), scope);
			}
			final String javaSource = builder.toString();
			if (log.isLoggable(Level.FINE)) {
				log.fine(javaSource);
			}
			return compilerAdapter.compile(Orbit.class, javaSource, packageName, classNamePrefix + "Orbit");
		} catch (ASTException e) {
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(PARSE, line, charPositionInLine, index, length, message));
			throw new DSLCompilerException("Can't compile orbit", report.orbitDSL(), errors);
		} catch (Throwable e) {
            final String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(JAVA_COMPILE, 0, 0, 0, 0, message));
			throw new DSLCompilerException("Can't compile orbit", report.orbitDSL(), errors);
		}
	}

	public ClassFactory<Color> compileColor(DSLParserResultV2 report) throws DSLCompilerException {
		try {
			final StringBuilder builder = new StringBuilder();
            if (report.fractal() != null) {
				final DSLExpressionContext expressionContext = report.fractal().getColor().getExpressionContext();
				final HashMap<String, VariableDeclaration> scope = new HashMap<>();
				final DSLCompilerContext context = new DSLCompilerContext(expressionContext, builder, ClassType.COLOR);
				compileColor(context, report.fractal().getColor(), scope);
			}
			final String javaSource = builder.toString();
			if (log.isLoggable(Level.FINE)) {
				log.fine(javaSource);
			}
			return compilerAdapter.compile(Color.class, javaSource, packageName, classNamePrefix + "Color");
		} catch (ASTException e) {
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(PARSE, line, charPositionInLine, index, length, message));
			throw new DSLCompilerException("Can't compile color", report.colorDSL(), errors);
		} catch (Throwable e) {
			final String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(JAVA_COMPILE, 0, 0, 0, 0, message));
			throw new DSLCompilerException("Can't compile color", report.colorDSL(), errors);
		}
	}

	private void compileOrbit(DSLCompilerContext context, DSLOrbit orbit, HashMap<String, VariableDeclaration> scope) {
		context.append("package ");
		context.append(packageName);
		context.append(";\n");
		context.append("import static ");
		if (Boolean.getBoolean(PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED)) {
			context.append(FastExpression.class.getCanonicalName());
		} else {
			context.append(Expression.class.getCanonicalName());
		}
		context.append(".*;\n");
		context.append("import ");
		context.append(ComplexNumber.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(MutableNumber.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Trap.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Orbit.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Scope.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(List.class.getCanonicalName());
		context.append(";\n");
		context.append("@SuppressWarnings(value=\"unused\")\n");
		context.append("public class ");
		context.append(classNamePrefix);
		context.append("Orbit extends Orbit {\n");
		if (orbit != null) {
			for (VariableDeclaration var : orbit.getOrbitVariables()) {
				scope.put(var.name(), var);
			}
			for (VariableDeclaration var : orbit.getStateVariables()) {
				scope.put(var.name(), var);
			}
			orbit.compile(context, scope);
		}
		context.append("}\n");
	}

	private void compileColor(DSLCompilerContext context, DSLColor color, HashMap<String, VariableDeclaration> scope) {
		context.append("package ");
		context.append(packageName);
		context.append(";\n");
		context.append("import static ");
		if (Boolean.getBoolean(PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED)) {
			context.append(FastExpression.class.getCanonicalName());
		} else {
			context.append(Expression.class.getCanonicalName());
		}
		context.append(".*;\n");
		context.append("import ");
		context.append(ComplexNumber.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(MutableNumber.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Palette.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Color.class.getCanonicalName());
		context.append(";\n");
		context.append("import ");
		context.append(Scope.class.getCanonicalName());
		context.append(";\n");
		context.append("@SuppressWarnings(value=\"unused\")\n");
		context.append("public class ");
		context.append(classNamePrefix);
		context.append("Color extends Color {\n");
		if (color != null) {
			for (VariableDeclaration var : color.getColorVariables()) {
				scope.put(var.name(), var);
			}
			for (VariableDeclaration var : color.getStateVariables()) {
				scope.put(var.name(), var);
			}
			color.compile(context, scope);
		}
		context.append("}\n");
	}
}
