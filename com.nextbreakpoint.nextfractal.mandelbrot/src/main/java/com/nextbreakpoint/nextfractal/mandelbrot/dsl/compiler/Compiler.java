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

import com.nextbreakpoint.nextfractal.core.common.ClassFactory;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter.InterpretedOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.JavaCompilerProvider;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import lombok.extern.java.Log;

import javax.tools.JavaCompiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;
import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED;

@Log
public class Compiler {
	private final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
	private final String packageName;
	private final String className;

	public Compiler(String packageName, String className) {
		this.packageName = Objects.requireNonNull(packageName);
		this.className = Objects.requireNonNull(className);
	}

	public DSLParserResult compile(DSLExpressionContext context, DSLParserResult result, DSLFractal fractal) throws CompilerException {
		CompilerResult<Orbit> orbitResult = compileOrbit(context, result, fractal);
		CompilerResult<Color> colorResult = compileColor(context, result, fractal);
		return result.toBuilder()
				.withOrbitClassFactory(orbitResult.classFactory())
				.withColorClassFactory(colorResult.classFactory())
				.build();
	}

	private CompilerResult<Orbit> compileOrbit(DSLExpressionContext context, DSLParserResult result, DSLFractal fractal) throws CompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return compileOrbit(context, fractal.getOrbit());
		} else {
			return compileOrbit(context, fractal, result.orbitDSL());
		}
	}

	private CompilerResult<Color> compileColor(DSLExpressionContext context, DSLParserResult result, DSLFractal fractal) throws CompilerException {
		final JavaCompiler javaCompiler = JavaCompilerProvider.getJavaCompiler();
		if (javaCompiler == null) {
			return compileColor(context, fractal.getColor());
		} else {
			return compileColor(context, fractal, result.colorDSL());
		}
	}

	private static CompilerResult<Color> compileColor(DSLExpressionContext context, DSLColor color) {
		return new CompilerResult<>(() -> new InterpretedColor(context, color));
	}

	private static CompilerResult<Orbit> compileOrbit(DSLExpressionContext context, DSLOrbit orbit) {
		return new CompilerResult<>(() -> new InterpretedOrbit(context, orbit));
	}

	private CompilerResult<Orbit> compileOrbit(DSLExpressionContext expressionContext, DSLFractal fractal, String source) throws CompilerException {
		try {
			final StringBuilder builder = new StringBuilder();
			if (fractal != null) {
				final DSLCompilerContext context = new DSLCompilerContext(expressionContext, builder, ClassType.ORBIT);
				compileOrbit(context, fractal.getOrbit(), new HashMap<>());
			}
			final String javaSource = builder.toString();
			if (log.isLoggable(Level.FINE)) {
				log.fine(javaSource);
			}
			final CompilerAdapter compilerAdapter = new CompilerAdapter(javaCompiler);
			final ClassFactory<Orbit> classFactory = compilerAdapter.compile(Orbit.class, javaSource, packageName, className + "Orbit");
			return new CompilerResult<>(classFactory);
		} catch (CompilerException e) {
			throw e;
		} catch (ASTException e) {
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, line, charPositionInLine, index, length, e.getMessage()));
			log.log(Level.INFO, "Can't compile orbit", e);
			throw new CompilerException("Can't compile orbit", source, errors);
		} catch (Throwable e) {
			final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage()));
			log.log(Level.INFO, "Can't compile orbit", e);
			throw new CompilerException("Can't compile orbit", source, errors);
		}
	}

	private CompilerResult<Color> compileColor(DSLExpressionContext expressionContext, DSLFractal fractal, String source) throws CompilerException {
		try {
			final StringBuilder builder = new StringBuilder();
			if (fractal != null) {
				final DSLCompilerContext context = new DSLCompilerContext(expressionContext, builder, ClassType.COLOR);
				compileColor(context, fractal.getColor(), new HashMap<>());
			}
			final String javaSource = builder.toString();
			final CompilerAdapter compilerAdapter = new CompilerAdapter(javaCompiler);
			final ClassFactory<Color> classFactory = compilerAdapter.compile(Color.class, javaSource, packageName, className + "Color");
			return new CompilerResult<>(classFactory);
		} catch (CompilerException e) {
			throw e;
		} catch (ASTException e) {
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, line, charPositionInLine, index, length, e.getMessage()));
			log.log(Level.INFO, "Can't compile color", e);
			throw new CompilerException("Can't compile color", source, errors);
		} catch (Throwable e) {
			final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage()));
			log.log(Level.INFO, "Can't compile color", e);
			throw new CompilerException("Can't compile color", source, errors);
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
		context.append(className);
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
		context.append(className);
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
