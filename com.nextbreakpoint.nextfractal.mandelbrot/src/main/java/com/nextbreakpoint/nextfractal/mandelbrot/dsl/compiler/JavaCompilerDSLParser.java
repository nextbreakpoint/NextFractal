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
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult.Type;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ErrorStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotLexer;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTStatement;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED;

@Deprecated
public class JavaCompilerDSLParser {
	private static final Logger logger = Logger.getLogger(JavaCompilerDSLParser.class.getName());

	private final String packageName;
	private final String className;
	
	public JavaCompilerDSLParser(String packageName, String className) {
		this.packageName = Objects.requireNonNull(packageName);
		this.className = Objects.requireNonNull(className);
	}
	
	public DSLParserResult parse(String source) throws DSLParserException {
		List<ParserError> errors = new ArrayList<>();
		ASTFractal ast = parse(source, errors);
		DSLExpressionContext orbitContext = new DSLExpressionContext();
		String orbitSource = buildOrbit(orbitContext, ast, errors);
		DSLExpressionContext colorContext = new DSLExpressionContext();
		String colorSource = buildColor(colorContext, ast, errors);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(orbitSource);
			logger.fine(colorSource);
		}
		final String orbitScript = ast != null && ast.getOrbit() != null ? ast.getOrbit().toString() : "";
		final String colorScript = ast != null && ast.getColor() != null ? ast.getColor().toString() : "";
		return new DSLParserResult(ast, Type.COMPILED, source, orbitScript, colorScript, orbitSource, colorSource, errors, packageName, className);
	}
	
	private ASTFractal parse(String source, List<ParserError> errors) throws DSLParserException {
		try {
			CharStream is = CharStreams.fromReader(new StringReader(source));
			MandelbrotLexer lexer = new MandelbrotLexer(is);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			MandelbrotParser parser = new MandelbrotParser(tokens);
			parser.setErrorHandler(new ErrorStrategy(errors));
			parser.fractal();
			ASTBuilder builder = parser.getBuilder();
			return builder.getFractal();
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse source", errors);
		} catch (Exception e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			String message = e.getMessage();
			ParserError error = new ParserError(type, 0L, 0L, 0L, 0L, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse source", errors);
		}
	}

	private String buildOrbit(DSLExpressionContext context, ASTFractal fractal, List<ParserError> errors) throws DSLParserException {
		try {
			StringBuilder builder = new StringBuilder();
			Map<String, VariableDeclaration> variables = new HashMap<>();
			compileOrbit(context, builder, variables, fractal);
			return builder.toString();
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse source", errors);
		}
	}
	
	private String buildColor(DSLExpressionContext context, ASTFractal fractal, List<ParserError> errors) throws DSLParserException {
		try {
			StringBuilder builder = new StringBuilder();
			Map<String, VariableDeclaration> variables = new HashMap<>();
			compileColor(context, builder, variables, fractal);
			return builder.toString();
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse source", errors);
		}
	}
	
	private String compileOrbit(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTFractal fractal) {
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n");
		builder.append("import static ");
		if (Boolean.getBoolean(PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED)) {
			builder.append(FastExpression.class.getCanonicalName());
		} else {
			builder.append(Expression.class.getCanonicalName());
		}
		builder.append(".*;\n");
		builder.append("import ");
		builder.append(ComplexNumber.class.getCanonicalName());
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
		builder.append(className);
		builder.append("Orbit extends Orbit {\n");
		buildOrbit(context, builder, variables, fractal);
		builder.append("}\n");
		return builder.toString();
	}

	private String compileColor(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTFractal fractal) {
		builder.append("package ");
		builder.append(packageName);
		builder.append(";\n");
		builder.append("import static ");
		if (Boolean.getBoolean(PROPERTY_MANDELBROT_EXPRESSION_OPTIMISATION_ENABLED)) {
			builder.append(FastExpression.class.getCanonicalName());
		} else {
			builder.append(Expression.class.getCanonicalName());
		}
		builder.append(".*;\n");
		builder.append("import ");
		builder.append(ComplexNumber.class.getCanonicalName());
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
		builder.append(className);
		builder.append("Color extends Color {\n");
		buildColor(context, builder, variables, fractal);
		builder.append("}\n");
		return builder.toString();
	}

	private void buildOrbit(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> scope, ASTFractal fractal) {
		if (fractal != null) {
			for (VariableDeclaration var : fractal.getOrbitVariables()) {
				scope.put(var.name(), var);
			}
			for (VariableDeclaration var : fractal.getStateVariables()) {
				scope.put(var.name(), var);
			}
			compile(context, builder, scope, fractal.getStateVariables(), fractal.getOrbit());
		}
	}

	private void buildColor(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> scope, ASTFractal fractal) {
		if (fractal != null) {
			for (VariableDeclaration var : fractal.getColorVariables()) {
				scope.put(var.name(), var);
			}
			for (VariableDeclaration var : fractal.getStateVariables()) {
				scope.put(var.name(), var);
			}
			compile(context, builder, scope, fractal.getStateVariables(), fractal.getColor());
		}
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> scope, Collection<VariableDeclaration> stateVariables, ASTOrbit orbit) {
		builder.append("public void init() {\n");
		if (orbit != null) {
			builder.append("setInitialRegion(");
			builder.append("number(");
			builder.append(orbit.getRegion().getA());
			builder.append("),number(");
			builder.append(orbit.getRegion().getB());
			builder.append("));\n");
			for (VariableDeclaration var : stateVariables) {
				builder.append("addVariable(");
				builder.append(var.name());
				builder.append(");\n");
			}
			builder.append("resetTraps();\n");
			for (ASTOrbitTrap trap : orbit.getTraps()) {
				builder.append("addTrap(trap");
				builder.append(trap.getName().toUpperCase().substring(0, 1));
				builder.append(trap.getName().substring(1));
				builder.append(");\n");
			}
		}
		builder.append("}\n");
		for (VariableDeclaration var : scope.values()) {
			scope.put(var.name(), var);
			if (var.create()) {
				if (var.real()) {
					builder.append("private double ");
					builder.append(var.name());
					builder.append(" = 0.0;\n");
				} else {
					builder.append("private final MutableNumber ");
					builder.append(var.name());
					builder.append(" = getNumber(");
					builder.append(context.newNumberIndex());
					builder.append(").set(0.0,0.0);\n");
				}
			}
		}
		if (orbit != null) {
			for (ASTOrbitTrap trap : orbit.getTraps()) {
				compile(context, builder, scope, trap);
			}
		}
		builder.append("public void render(List<ComplexNumber[]> states) {\n");
		if (orbit != null) {
			compile(context, builder, scope, orbit.getBegin(), stateVariables);
			Map<String, VariableDeclaration> vars = new HashMap<>(scope);
			compile(context, builder, vars, orbit.getLoop(), stateVariables);
			compile(context, builder, scope, orbit.getEnd(), stateVariables);
		}
		int i = 0;
		for (VariableDeclaration var : stateVariables) {
			builder.append("setVariable(");
			builder.append(i++);
			builder.append(",");
			builder.append(var.name());
			builder.append(");\n");
		}
		builder.append("}\n");
		builder.append("protected MutableNumber[] createNumbers() {\n");
		builder.append("return new MutableNumber[");
		builder.append(context.getNumberCount());
		builder.append("];\n");
		builder.append("}\n");
		builder.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
		builder.append("public boolean useTime() {\nreturn ");
		builder.append(context.orbitUseTime());
		builder.append(";\n}\n");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> scope, Collection<VariableDeclaration> stateVariables, ASTColor color) {
		if (color != null) {
			for (ASTPalette palette : color.getPalettes()) {
				builder.append("private Palette palette");
				builder.append(palette.getName().toUpperCase().substring(0, 1));
				builder.append(palette.getName().substring(1));
				builder.append(";\n");
			}
		}
		builder.append("public void init() {\n");
		int i = 0;
		for (VariableDeclaration var : stateVariables) {
			if (var.real()) {
				builder.append("double ");
				builder.append(var.name());
				builder.append(" = getRealVariable(");
				builder.append(i++);
				builder.append(");\n");
			} else {
				builder.append("final MutableNumber ");
				builder.append(var.name());
				builder.append(" = getVariable(");
				builder.append(i++);
				builder.append(");\n");
			}
		}
		i = 0;
		for (VariableDeclaration var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.real()) {
					builder.append("double ");
					builder.append(var.name());
					builder.append(" = 0;\n");
				} else {
					builder.append("final MutableNumber ");
					builder.append(var.name());
					builder.append(" = getNumber(");
					builder.append(i++);
					builder.append(");\n");
				}
			}
		}
		if (color != null) {
			for (ASTPalette palette : color.getPalettes()) {
				compile(context, builder, scope, palette);
			}
		}
		builder.append("}\n");
		for (VariableDeclaration var : stateVariables) {
			scope.put(var.name(), var);
		}
		builder.append("public void render() {\n");
		i = 0;
		for (VariableDeclaration var : stateVariables) {
			if (var.real()) {
				builder.append("double ");
				builder.append(var.name());
				builder.append(" = getRealVariable(");
				builder.append(i++);
				builder.append(");\n");
			} else {
				builder.append("final MutableNumber ");
				builder.append(var.name());
				builder.append(" = getVariable(");
				builder.append(i++);
				builder.append(");\n");
			}
		}
		i = 0;
		for (VariableDeclaration var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.real()) {
					builder.append("double ");
					builder.append(var.name());
					builder.append(" = 0;\n");
				} else {
					builder.append("final MutableNumber ");
					builder.append(var.name());
					builder.append(" = getNumber(");
					builder.append(i++);
					builder.append(");\n");
				}
			}
		}
		if (color != null) {
			builder.append("setColor(color(");
			builder.append(color.getArgb().getComponents()[0]);
			builder.append(",");
			builder.append(color.getArgb().getComponents()[1]);
			builder.append(",");
			builder.append(color.getArgb().getComponents()[2]);
			builder.append(",");
			builder.append(color.getArgb().getComponents()[3]);
			builder.append("));\n");
			if (color.getInit() != null) {
				for (ASTStatement statement : color.getInit().getStatements()) {
					compile(context, builder, scope, statement, ClassType.COLOR);
				}
			}
			for (ASTRule rule : color.getRules()) {
				compile(context, builder, scope, rule);
			}
		}
		builder.append("}\n");
		builder.append("protected MutableNumber[] createNumbers() {\n");
		builder.append("return new MutableNumber[");
		builder.append(context.getNumberCount());
		builder.append("];\n");
		builder.append("}\n");
		builder.append("public double time() {\nreturn getTime().value() * getTime().scale();\n}\n");
		builder.append("public boolean useTime() {\nreturn ");
		builder.append(context.colorUseTime());
		builder.append(";\n}\n");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTRule rule) {
		builder.append("if (");
		rule.getRuleExp().compile(new ExpressionCompiler(context, variables, builder, ClassType.COLOR));
		builder.append(") {\n");
		builder.append("addColor(");
		builder.append(rule.getOpacity());
		builder.append(",");
		rule.getColorExp().compile(new ExpressionCompiler(context, variables, builder, ClassType.COLOR));
		builder.append(");\n}\n");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTPalette palette) {
		builder.append("palette");
		builder.append(palette.getName().toUpperCase().substring(0, 1));
		builder.append(palette.getName().substring(1));
		builder.append(" = palette()");
		for (ASTPaletteElement element : palette.getElements()) {
			builder.append(".add(");
			compile(context, builder, variables, element);
			builder.append(")");
		}
		builder.append(".build();\n");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTPaletteElement element) {
		builder.append("element(");
		builder.append(createArray(element.getBeginColor().getComponents()));
		builder.append(",");
		builder.append(createArray(element.getEndColor().getComponents()));
		builder.append(",");
		builder.append(element.getSteps());
		builder.append(",s -> { return ");
		if (element.getExp() != null) {
			if (element.getExp().isReal()) {
				element.getExp().compile(new ExpressionCompiler(context, variables, builder, ClassType.COLOR));
			} else {
				throw new ASTException("Expression type not valid: " + element.getLocation().getText(), element.getLocation());
			}
		} else {
			builder.append("s");
		}
		builder.append(";})");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTOrbitTrap trap) {
		builder.append("private Trap trap");
		builder.append(trap.getName().toUpperCase().substring(0, 1));
		builder.append(trap.getName().substring(1));
		builder.append(" = trap(number(");
		builder.append(trap.getCenter());
		builder.append("))");
		for (ASTOrbitTrapOp operator : trap.getOperators()) {
			builder.append(".");
            switch (operator.getOp()) {
                case "MOVETO" -> builder.append("moveTo");
                case "MOVEREL", "MOVETOREL" -> builder.append("moveRel");
                case "LINETO" -> builder.append("lineTo");
                case "LINEREL", "LINETOREL" -> builder.append("lineRel");
                case "ARCTO" -> builder.append("arcTo");
                case "ARCREL", "ARCTOREL" -> builder.append("arcRel");
                case "QUADTO" -> builder.append("quadTo");
                case "QUADREL", "QUADTOREL" -> builder.append("quadRel");
                case "CURVETO" -> builder.append("curveTo");
                case "CURVEREL", "CURVETOREL" -> builder.append("curveRel");
                case "CLOSE" -> builder.append("close");
                default -> {}
            }
			builder.append("(");
			if (operator.getC1() != null) {
				if (operator.getC1().isReal()) {
					builder.append("number(");
					operator.getC1().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
					builder.append(")");
				} else {
					operator.getC1().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
				}
			}
			if (operator.getC2() != null) {
				builder.append(",");
				if (operator.getC2().isReal()) {
					builder.append("number(");
					operator.getC2().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
					builder.append(")");
				} else {
					operator.getC2().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
				}
			}
			if (operator.getC3() != null) {
				builder.append(",");
				if (operator.getC3().isReal()) {
					builder.append("number(");
					operator.getC3().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
					builder.append(")");
				} else {
					operator.getC3().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
				}
			}
			builder.append(")");
		}
		builder.append(";\n");
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTOrbitBegin begin, Collection<VariableDeclaration> stateVariables) {
		if (begin != null) {
			for (ASTStatement statement : begin.getStatements()) {
				compile(context, builder, variables, statement, ClassType.ORBIT);
			}
		}
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTOrbitEnd end, Collection<VariableDeclaration> stateVariables) {
		if (end != null) {
			for (ASTStatement statement : end.getStatements()) {
				compile(context, builder, variables, statement, ClassType.ORBIT);
			}
		}
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTOrbitLoop loop, Collection<VariableDeclaration> stateVariables) {
		if (loop != null) {
			builder.append("n = ");
			builder.append(loop.getBegin());
			builder.append(";\n");
			builder.append("if (states != null) {\n");
			builder.append("states.add(new ComplexNumber[] { ");
			int i = 0;
			for (VariableDeclaration var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.name());
				builder.append(")");
				i += 1;
			}
			builder.append(" });\n");
			builder.append("}\n");
			builder.append("for (int i = ");
			builder.append(loop.getBegin());
			builder.append(" + 1; i <= ");
			builder.append(loop.getEnd());
			builder.append("; i++) {\n");
			for (ASTStatement statement : loop.getStatements()) {
				compile(context, builder, variables, statement, ClassType.ORBIT);
			}
			builder.append("if (");
			loop.getExpression().compile(new ExpressionCompiler(context, variables, builder, ClassType.ORBIT));
			builder.append(") { n = i; break; }\n");
			builder.append("if (states != null) {\n");
			builder.append("states.add(new ComplexNumber[] { ");
			i = 0;
			for (VariableDeclaration var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.name());
				builder.append(")");
				i += 1;
			}
			builder.append(" });\n");
			builder.append("}\n");
			builder.append("}\n");
			builder.append("if (states != null) {\n");
			builder.append("states.add(new ComplexNumber[] { ");
			i = 0;
			for (VariableDeclaration var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.name());
				builder.append(")");
				i += 1;
			}
			builder.append(" });\n");
			builder.append("}\n");
		}
	}

	private void compile(DSLExpressionContext context, StringBuilder builder, Map<String, VariableDeclaration> variables, ASTStatement statement, ClassType classType) {
		if (statement != null) {
			statement.compile(new ExpressionCompiler(context, variables, builder, classType));
		}		
	}
	
	private String createArray(float[] components) {
		return "new float[] {" + components[0] + "f," + components[1] + "f," + components[2] + "f," + components[3] + "f}";
	}
}	
