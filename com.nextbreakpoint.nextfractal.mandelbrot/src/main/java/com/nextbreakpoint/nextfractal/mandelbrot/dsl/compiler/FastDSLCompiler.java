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
import com.nextbreakpoint.nextfractal.mandelbrot.core.Expression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.FastExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResultV2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import lombok.extern.java.Log;

import javax.tools.JavaCompiler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.JAVA_COMPILE;
import static com.nextbreakpoint.nextfractal.core.common.ParserErrorType.PARSE;

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
			final ExpressionCompilerContext context = new ExpressionCompilerContext();
			final StringBuilder builder = new StringBuilder();
			final Map<String, Variable> variables = new HashMap<>();
			if (report.fractal() != null) {
				compileOrbit(context, builder, variables, report.fractal().getOrbit());
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
			final ExpressionCompilerContext context = new ExpressionCompilerContext();
			final StringBuilder builder = new StringBuilder();
			final Map<String, Variable> variables = new HashMap<>();
			if (report.fractal() != null) {
				compileColor(context, builder, variables, report.fractal().getColor());
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


	private void compileOrbit(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledOrbit orbit) {
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n");
		builder.append("import static ");
		if (Boolean.getBoolean("mandelbrot.expression.fastmath")) {
			builder.append(FastExpression.class.getCanonicalName());
		} else {
			builder.append(Expression.class.getCanonicalName());
		}
		builder.append(".*;\n");
		builder.append("import ");
		builder.append(Number.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(MutableNumber.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Trap.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Orbit.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Scope.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(List.class.getCanonicalName());
		builder.append(";\n");
		builder.append("@SuppressWarnings(value=\"unused\")\n");
		builder.append("public class ");
		builder.append(classNamePrefix);
		builder.append("Orbit extends Orbit {\n");
		if (orbit != null) {
			compile(context, builder, variables, orbit);
		}
		builder.append("}\n");
	}

	private void compileColor(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledColor color) {
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n");
		builder.append("import static ");
		if (Boolean.getBoolean("mandelbrot.expression.fastmath")) {
			builder.append(FastExpression.class.getCanonicalName());
		} else {
			builder.append(Expression.class.getCanonicalName());
		}
		builder.append(".*;\n");
		builder.append("import ");
		builder.append(Number.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(MutableNumber.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Palette.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Color.class.getCanonicalName());
		builder.append(";\n");
		builder.append("import ");
		builder.append(Scope.class.getCanonicalName());
		builder.append(";\n");
		builder.append("@SuppressWarnings(value=\"unused\")\n");
		builder.append("public class ");
		builder.append(classNamePrefix);
		builder.append("Color extends Color {\n");
		if (color != null) {
			compile(context, builder, variables, color);
		}
		builder.append("}\n");
	}

	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> scope, CompiledOrbit orbit) {
		if (orbit != null) {
			for (Variable var : orbit.getOrbitVariables()) {
				scope.put(var.getName(),  var);
			}
			for (Variable var : orbit.getStateVariables()) {
				scope.put(var.getName(),  var);
			}
//			compile(context, builder, scope, orbit.getStateVariables(), orbit);
		}
	}

	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> scope, CompiledColor color) {
		if (color != null) {
			for (Variable var : color.getColorVariables()) {
				scope.put(var.getName(),  var);
			}
			for (Variable var : color.getStateVariables()) {
				scope.put(var.getName(),  var);
			}
//			compile(context, builder, scope, color.getStateVariables(), color);
		}
	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> scope, Collection<Variable> stateVariables, CompiledOrbit orbit) {
//		builder.append("public void init() {\n");
//		if (orbit != null) {
//			builder.append("setInitialRegion(");
//			builder.append("number(");
//			builder.append(orbit.getRegion()[0]);
//			builder.append("),number(");
//			builder.append(orbit.getRegion()[1]);
//			builder.append("));\n");
//			for (Variable var : stateVariables) {
//				builder.append("addVariable(");
//				builder.append(var.getName());
//				builder.append(");\n");
//			}
//			builder.append("resetTraps();\n");
//			for (CompiledTrap trap : orbit.getTraps()) {
//				builder.append("addTrap(trap");
//				builder.append(trap.getName().toUpperCase().charAt(0));
//				builder.append(trap.getName().substring(1));
//				builder.append(");\n");
//			}
//		}
//		builder.append("}\n");
//		for (Variable var : scope.values()) {
//			scope.put(var.getName(), var);
//			if (var.isCreate()) {
//				if (var.isReal()) {
//					builder.append("private double ");
//					builder.append(var.getName());
//					builder.append(" = 0.0;\n");
//				} else {
//					builder.append("private final MutableNumber ");
//					builder.append(var.getName());
//					builder.append(" = getNumber(");
//					builder.append(context.newNumberIndex());
//					builder.append(").set(0.0,0.0);\n");
//				}
//			}
//		}
//		if (orbit != null) {
//			for (CompiledTrap trap : orbit.getTraps()) {
//				compile(context, builder, scope, trap);
//			}
//		}
//		builder.append("public void render(List<Number[]> states) {\n");
//		if (orbit != null) {
//			compile(context, builder, scope, orbit.getBegin(), stateVariables);
//			Map<String, Variable> vars = new HashMap<String, Variable>(scope);
//			compile(context, builder, vars, orbit.getLoop(), stateVariables);
//			compile(context, builder, scope, orbit.getEnd(), stateVariables);
//		}
//		int i = 0;
//		for (Variable var : stateVariables) {
//			builder.append("setVariable(");
//			builder.append(i++);
//			builder.append(",");
//			builder.append(var.getName());
//			builder.append(");\n");
//		}
//		builder.append("}\n");
//		builder.append("protected MutableNumber[] createNumbers() {\n");
//		builder.append("return new MutableNumber[");
//		builder.append(context.getNumberCount());
//		builder.append("];\n");
//		builder.append("}\n");
//		builder.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
//		builder.append("public boolean useTime() {\nreturn ");
//		builder.append(context.orbitUseTime());
//		builder.append(";\n}\n");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> scope, Collection<Variable> stateVariables, CompiledColor color) {
//		if (color != null) {
//			for (CompiledPalette palette : color.getPalettes()) {
//				builder.append("private Palette palette");
//				builder.append(palette.getName().toUpperCase().charAt(0));
//				builder.append(palette.getName().substring(1));
//				builder.append(";\n");
//			}
//		}
//		builder.append("public void init() {\n");
//		int i = 0;
//		for (Variable var : stateVariables) {
//			if (var.isReal()) {
//				builder.append("double ");
//				builder.append(var.getName());
//				builder.append(" = getRealVariable(");
//				builder.append(i++);
//				builder.append(");\n");
//			} else {
//				builder.append("final MutableNumber ");
//				builder.append(var.getName());
//				builder.append(" = getVariable(");
//				builder.append(i++);
//				builder.append(");\n");
//			}
//		}
//		i = 0;
//		for (Variable var : scope.values()) {
//			if (!stateVariables.contains(var)) {
//				if (var.isReal()) {
//					builder.append("double ");
//					builder.append(var.getName());
//					builder.append(" = 0;\n");
//				} else {
//					builder.append("final MutableNumber ");
//					builder.append(var.getName());
//					builder.append(" = getNumber(");
//					builder.append(i++);
//					builder.append(");\n");
//				}
//			}
//		}
//		if (color != null) {
//			for (CompiledPalette palette : color.getPalettes()) {
//				compile(context, builder, scope, palette);
//			}
//		}
//		builder.append("}\n");
//		for (Variable var : stateVariables) {
//			scope.put(var.getName(), var);
//		}
//		builder.append("public void render() {\n");
//		i = 0;
//		for (Variable var : stateVariables) {
//			if (var.isReal()) {
//				builder.append("double ");
//				builder.append(var.getName());
//				builder.append(" = getRealVariable(");
//				builder.append(i++);
//				builder.append(");\n");
//			} else {
//				builder.append("final MutableNumber ");
//				builder.append(var.getName());
//				builder.append(" = getVariable(");
//				builder.append(i++);
//				builder.append(");\n");
//			}
//		}
//		i = 0;
//		for (Variable var : scope.values()) {
//			if (!stateVariables.contains(var)) {
//				if (var.isReal()) {
//					builder.append("double ");
//					builder.append(var.getName());
//					builder.append(" = 0;\n");
//				} else {
//					builder.append("final MutableNumber ");
//					builder.append(var.getName());
//					builder.append(" = getNumber(");
//					builder.append(i++);
//					builder.append(");\n");
//				}
//			}
//		}
//		if (color != null) {
//			builder.append("setColor(color(");
//			builder.append(color.getBackgroundColor()[0]);
//			builder.append(",");
//			builder.append(color.getBackgroundColor()[1]);
//			builder.append(",");
//			builder.append(color.getBackgroundColor()[2]);
//			builder.append(",");
//			builder.append(color.getBackgroundColor()[3]);
//			builder.append("));\n");
//			if (color.getInit() != null) {
//				for (CompiledStatement statement : color.getInit().getStatements()) {
//					compile(context, builder, scope, statement, ClassType.COLOR);
//				}
//			}
//			for (CompiledRule rule : color.getRules()) {
//				compile(context, builder, scope, rule);
//			}
//		}
//		builder.append("}\n");
//		builder.append("protected MutableNumber[] createNumbers() {\n");
//		builder.append("return new MutableNumber[");
//		builder.append(context.getNumberCount());
//		builder.append("];\n");
//		builder.append("}\n");
//		builder.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
//		builder.append("public boolean useTime() {\nreturn ");
//		builder.append(context.colorUseTime());
//		builder.append(";\n}\n");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledRule rule) {
//		final CompiledExpressionCompiler expressionCompiler = new CompiledExpressionCompiler(context, variables, builder, ClassType.COLOR);
//		builder.append("if (");
//		expressionCompiler.compile(rule.getRuleCondition());
//		builder.append(") {\n");
//		builder.append("addColor(");
//		builder.append(rule.getOpacity());
//		builder.append(",");
//		expressionCompiler.compile(rule.getColorExp())
//		builder.append(");\n}\n");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledPalette palette) {
//		builder.append("palette");
//		builder.append(palette.getName().toUpperCase().charAt(0));
//		builder.append(palette.getName().substring(1));
//		builder.append(" = palette()");
//		for (CompiledPaletteElement element : palette.getElements()) {
//			builder.append(".add(");
//			compile(context, builder, variables, element);
//			builder.append(")");
//		}
//		builder.append(".build();\n");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledPaletteElement element) {
//		builder.append("element(");
//		builder.append(createArray(element.beginColor()));
//		builder.append(",");
//		builder.append(createArray(element.endColor()));
//		builder.append(",");
//		builder.append(element.steps());
//		builder.append(",s -> { return ");
//		if (element.exp() != null) {
//			if (element.exp().isReal()) {
//				final CompiledExpressionCompiler expressionCompiler = new CompiledExpressionCompiler(context, variables, builder, ClassType.COLOR);
//				expressionCompiler.compile(element.exp());
//			} else {
//				throw new ASTException("Expression type not valid: " + element.location().getText(), element.location());
//			}
//		} else {
//			builder.append("s");
//		}
//		builder.append(";})");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledTrap trap) {
//		builder.append("private Trap trap");
//		builder.append(trap.getName().toUpperCase().charAt(0));
//		builder.append(trap.getName().substring(1));
//		builder.append(" = trap(number(");
//		builder.append(trap.getCenter());
//		builder.append("))");
//		for (CompiledTrapOp operator : trap.getOperators()) {
//			builder.append(".");
//			switch (operator.op()) {
//				case "MOVETO" -> builder.append("moveTo");
//				case "MOVEREL", "MOVETOREL" -> builder.append("moveRel");
//				case "LINETO" -> builder.append("lineTo");
//				case "LINEREL", "LINETOREL" -> builder.append("lineRel");
//				case "ARCTO" -> builder.append("arcTo");
//				case "ARCREL", "ARCTOREL" -> builder.append("arcRel");
//				case "QUADTO" -> builder.append("quadTo");
//				case "QUADREL", "QUADTOREL" -> builder.append("quadRel");
//				case "CURVETO" -> builder.append("curveTo");
//				case "CURVEREL", "CURVETOREL" -> builder.append("curveRel");
//				case "CLOSE" -> builder.append("close");
//				default -> {}
//			}
//			builder.append("(");
//			if (operator.c1() != null) {
//				if (operator.c1().isReal()) {
//					builder.append("number(");
//					operator.c1().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//					builder.append(")");
//				} else {
//					operator.c1().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//				}
//			}
//			if (operator.c2() != null) {
//				builder.append(",");
//				if (operator.c2().isReal()) {
//					builder.append("number(");
//					operator.c2().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//					builder.append(")");
//				} else {
//					operator.c2().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//				}
//			}
//			if (operator.c3() != null) {
//				builder.append(",");
//				if (operator.c3().isReal()) {
//					builder.append("number(");
//					operator.c3().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//					builder.append(")");
//				} else {
//					operator.c3().compile(new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT));
//				}
//			}
//			builder.append(")");
//		}
//		builder.append(";\n");
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledOrbitBegin begin, Collection<Variable> stateVariables) {
//		if (begin != null) {
//			for (CompiledStatement statement : begin.getStatements()) {
//				compile(context, builder, variables, statement, ClassType.ORBIT);
//			}
//		}
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledOrbitEnd end, Collection<Variable> stateVariables) {
//		if (end != null) {
//			for (CompiledStatement statement : end.getStatements()) {
//				compile(context, builder, variables, statement, ClassType.ORBIT);
//			}
//		}
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledOrbitLoop loop, Collection<Variable> stateVariables) {
//		if (loop != null) {
//			builder.append("n = ");
//			builder.append(loop.getBegin());
//			builder.append(";\n");
//			builder.append("if (states != null) {\n");
//			builder.append("states.add(new Number[] { ");
//			int i = 0;
//			for (Variable var : stateVariables) {
//				if (i > 0) {
//					builder.append(", ");
//				}
//				builder.append("number(");
//				builder.append(var.getName());
//				builder.append(")");
//				i += 1;
//			}
//			builder.append(" });\n");
//			builder.append("}\n");
//			builder.append("for (int i = ");
//			builder.append(loop.getBegin());
//			builder.append(" + 1; i <= ");
//			builder.append(loop.getEnd());
//			builder.append("; i++) {\n");
//			for (CompiledStatement statement : loop.getStatements()) {
//				compile(context, builder, variables, statement, ClassType.ORBIT);
//			}
//			builder.append("if (");
//			final CompiledExpressionCompiler expressionCompiler = new CompiledExpressionCompiler(context, variables, builder, ClassType.ORBIT);
//			expressionCompiler.compile(loop.getCondition());
//			builder.append(") { n = i; break; }\n");
//			builder.append("if (states != null) {\n");
//			builder.append("states.add(new Number[] { ");
//			i = 0;
//			for (Variable var : stateVariables) {
//				if (i > 0) {
//					builder.append(", ");
//				}
//				builder.append("number(");
//				builder.append(var.getName());
//				builder.append(")");
//				i += 1;
//			}
//			builder.append(" });\n");
//			builder.append("}\n");
//			builder.append("}\n");
//			builder.append("if (states != null) {\n");
//			builder.append("states.add(new Number[] { ");
//			i = 0;
//			for (Variable var : stateVariables) {
//				if (i > 0) {
//					builder.append(", ");
//				}
//				builder.append("number(");
//				builder.append(var.getName());
//				builder.append(")");
//				i += 1;
//			}
//			builder.append(" });\n");
//			builder.append("}\n");
//		}
//	}
//
//	private void compile(ExpressionCompilerContext context, StringBuilder builder, Map<String, Variable> variables, CompiledStatement statement, ClassType classType) {
//		if (statement != null) {
//			final CompiledExpressionCompiler expressionCompiler = new CompiledExpressionCompiler(context, variables, builder, classType);
//			expressionCompiler.compile(statement);
//		}
//	}
//
//	private String createArray(float[] components) {
//		return "new float[] {" + components[0] + "f," + components[1] + "f," + components[2] + "f," + components[3] + "f}";
//	}
}
