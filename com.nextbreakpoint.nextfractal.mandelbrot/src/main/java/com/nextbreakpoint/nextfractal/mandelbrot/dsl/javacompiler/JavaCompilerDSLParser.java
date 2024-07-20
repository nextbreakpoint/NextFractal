/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.javacompiler;

import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.*;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ErrorStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult.Type;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.MandelbrotLexer;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.MandelbrotParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaCompilerDSLParser {
	private static final Logger logger = Logger.getLogger(JavaCompilerDSLParser.class.getName());
	private final String packageName;
	private final String className;
	
	public JavaCompilerDSLParser(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
	}
	
	public DSLParserResult parse(String source) throws ParserException {
		List<SourceError> errors = new ArrayList<>();
		ASTFractal ast = parse(source, errors);
		ExpressionContext orbitContext = new ExpressionContext();
		String orbitSource = buildOrbit(orbitContext, ast, errors);
		ExpressionContext colorContext = new ExpressionContext();
		String colorSource = buildColor(colorContext, ast, errors);
		if (logger.isLoggable(Level.FINE)) {
			logger.fine(orbitSource);
			logger.fine(colorSource);
		}
		return new DSLParserResult(ast, Type.JAVA, source, orbitSource, colorSource, errors, packageName, className);
	}
	
	private ASTFractal parse(String source, List<SourceError> errors) throws ParserException {
		try {
			CharStream is = CharStreams.fromReader(new StringReader(source));
			MandelbrotLexer lexer = new MandelbrotLexer(is);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			MandelbrotParser parser = new MandelbrotParser(tokens);
			parser.setErrorHandler(new ErrorStrategy(errors));
			ParseTree fractalTree = parser.fractal();
            if (fractalTree != null) {
            	ASTBuilder builder = parser.getBuilder();
            	ASTFractal fractal = builder.getFractal();
            	return fractal;
            }
		} catch (ASTException e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			SourceError error = new SourceError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		} catch (Exception e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			String message = e.getMessage();
			SourceError error = new SourceError(type, 0L, 0L, 0L, 0L, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		}
		return null;
	}

	private String buildOrbit(ExpressionContext context, ASTFractal fractal, List<SourceError> errors) throws ParserException {
		try {
			StringBuilder builder = new StringBuilder();
			Map<String, CompilerVariable> variables = new HashMap<>();
			compileOrbit(context, builder, variables, fractal);
			return builder.toString();
		} catch (ASTException e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			SourceError error = new SourceError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		}
	}
	
	private String buildColor(ExpressionContext context, ASTFractal fractal, List<SourceError> errors) throws ParserException {
		try {
			StringBuilder builder = new StringBuilder();
			Map<String, CompilerVariable> variables = new HashMap<>();
			compileColor(context, builder, variables, fractal);
			return builder.toString();
		} catch (ASTException e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			SourceError error = new SourceError(type, line, charPositionInLine, index, length, message);
			logger.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		}
	}
	
	private String compileOrbit(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTFractal fractal) {
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
		builder.append(className);
		builder.append("Orbit extends Orbit {\n");
		buildOrbit(context, builder, variables, fractal);
		builder.append("}\n");
		return builder.toString();
	}

	private String compileColor(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTFractal fractal) {
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
		builder.append(className);
		builder.append("Color extends Color {\n");
		buildColor(context, builder, variables, fractal);
		builder.append("}\n");
		return builder.toString();
	}

	private void buildOrbit(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> scope, ASTFractal fractal) {
		if (fractal != null) {
			for (CompilerVariable var : fractal.getOrbitVariables()) {
				scope.put(var.getName(),  var);
			}
			for (CompilerVariable var : fractal.getStateVariables()) {
				scope.put(var.getName(),  var);
			}
			compile(context, builder, scope, fractal.getStateVariables(), fractal.getOrbit());
		}
	}

	private void buildColor(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> scope, ASTFractal fractal) {
		if (fractal != null) {
			for (CompilerVariable var : fractal.getColorVariables()) {
				scope.put(var.getName(),  var);
			}
			for (CompilerVariable var : fractal.getStateVariables()) {
				scope.put(var.getName(),  var);
			}
			compile(context, builder, scope, fractal.getStateVariables(), fractal.getColor());
		}
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> scope, Collection<CompilerVariable> stateVariables, ASTOrbit orbit) {
		builder.append("public void init() {\n");
		if (orbit != null) {
			builder.append("setInitialRegion(");
			builder.append("number(");
			builder.append(orbit.getRegion().getA());
			builder.append("),number(");
			builder.append(orbit.getRegion().getB());
			builder.append("));\n");
			for (CompilerVariable var : stateVariables) {
				builder.append("addVariable(");
				builder.append(var.getName());
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
		for (CompilerVariable var : scope.values()) {
			scope.put(var.getName(), var);
			if (var.isCreate()) {
				if (var.isReal()) {
					builder.append("private double ");
					builder.append(var.getName());
					builder.append(" = 0.0;\n");
				} else {
					builder.append("private final MutableNumber ");
					builder.append(var.getName());
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
		builder.append("public void render(List<Number[]> states) {\n");
		if (orbit != null) {
			compile(context, builder, scope, orbit.getBegin(), stateVariables);
			Map<String, CompilerVariable> vars = new HashMap<String, CompilerVariable>(scope);
			compile(context, builder, vars, orbit.getLoop(), stateVariables);
			compile(context, builder, scope, orbit.getEnd(), stateVariables);
		}
		int i = 0;
		for (CompilerVariable var : stateVariables) {
			builder.append("setVariable(");
			builder.append(i++);
			builder.append(",");
			builder.append(var.getName());
			builder.append(");\n");
		}
		builder.append("}\n");
		builder.append("protected MutableNumber[] createNumbers() {\n");
		builder.append("return new MutableNumber[");
		builder.append(context.getNumberCount());
		builder.append("];\n");
		builder.append("}\n");
		builder.append("public double time() {\nreturn getTime().getValue() * getTime().getScale();\n}\n");
		builder.append("public boolean useTime() {\nreturn ");
		builder.append(context.orbitUseTime());
		builder.append(";\n}\n");
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> scope, Collection<CompilerVariable> stateVariables, ASTColor color) {
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
		for (CompilerVariable var : stateVariables) {
			if (var.isReal()) {
				builder.append("double ");
				builder.append(var.getName());
				builder.append(" = getRealVariable(");
				builder.append(i++);
				builder.append(");\n");
			} else {
				builder.append("final MutableNumber ");
				builder.append(var.getName());
				builder.append(" = getVariable(");
				builder.append(i++);
				builder.append(");\n");
			}
		}
		i = 0;
		for (CompilerVariable var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.isReal()) {
					builder.append("double ");
					builder.append(var.getName());
					builder.append(" = 0;\n");
				} else {
					builder.append("final MutableNumber ");
					builder.append(var.getName());
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
		for (CompilerVariable var : stateVariables) {
			scope.put(var.getName(), var);
		}
		builder.append("public void render() {\n");
		i = 0;
		for (CompilerVariable var : stateVariables) {
			if (var.isReal()) {
				builder.append("double ");
				builder.append(var.getName());
				builder.append(" = getRealVariable(");
				builder.append(i++);
				builder.append(");\n");
			} else {
				builder.append("final MutableNumber ");
				builder.append(var.getName());
				builder.append(" = getVariable(");
				builder.append(i++);
				builder.append(");\n");
			}
		}
		i = 0;
		for (CompilerVariable var : scope.values()) {
			if (!stateVariables.contains(var)) {
				if (var.isReal()) {
					builder.append("double ");
					builder.append(var.getName());
					builder.append(" = 0;\n");
				} else {
					builder.append("final MutableNumber ");
					builder.append(var.getName());
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
		builder.append("public double time() {\nreturn getTime().getValue() * getTime().getScale();\n}\n");
		builder.append("public boolean useTime() {\nreturn ");
		builder.append(context.colorUseTime());
		builder.append(";\n}\n");
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTRule rule) {
		builder.append("if (");
		rule.getRuleExp().compile(new ExpressionCompiler(context, variables, builder, ClassType.COLOR));
		builder.append(") {\n");
		builder.append("addColor(");
		builder.append(rule.getOpacity());
		builder.append(",");
		rule.getColorExp().compile(new ExpressionCompiler(context, variables, builder, ClassType.COLOR));
		builder.append(");\n}\n");
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTPalette palette) {
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

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTPaletteElement element) {
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

	private void compile(ExpressionContext context, StringBuilder builder,	Map<String, CompilerVariable> variables, ASTOrbitTrap trap) {
		builder.append("private Trap trap");
		builder.append(trap.getName().toUpperCase().substring(0, 1));
		builder.append(trap.getName().substring(1));
		builder.append(" = trap(number(");
		builder.append(trap.getCenter());
		builder.append("))");
		for (ASTOrbitTrapOp operator : trap.getOperators()) {
			builder.append(".");
			switch (operator.getOp()) {
				case "MOVETO":
					builder.append("moveTo");
					break;

				case "MOVEREL":
				case "MOVETOREL":
					builder.append("moveRel");
					break;

				case "LINETO":
					builder.append("lineTo");
					break;

				case "LINEREL":
				case "LINETOREL":
					builder.append("lineRel");
					break;

				case "ARCTO":
					builder.append("arcTo");
					break;

				case "ARCREL":
				case "ARCTOREL":
					builder.append("arcRel");
					break;

				case "QUADTO":
					builder.append("quadTo");
					break;

				case "QUADREL":
				case "QUADTOREL":
					builder.append("quadRel");
					break;

				case "CURVETO":
					builder.append("curveTo");
					break;

				case "CURVEREL":
				case "CURVETOREL":
					builder.append("curveRel");
					break;

				case "CLOSE":
					builder.append("close");
					break;

				default:
					break;
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

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTOrbitBegin begin, Collection<CompilerVariable> stateVariables) {
		if (begin != null) {
			for (ASTStatement statement : begin.getStatements()) {
				compile(context, builder, variables, statement, ClassType.ORBIT);
			}
		}
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTOrbitEnd end, Collection<CompilerVariable> stateVariables) {
		if (end != null) {
			for (ASTStatement statement : end.getStatements()) {
				compile(context, builder, variables, statement, ClassType.ORBIT);
			}
		}
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTOrbitLoop loop, Collection<CompilerVariable> stateVariables) {
		if (loop != null) {
			builder.append("n = ");
			builder.append(loop.getBegin());
			builder.append(";\n");
			builder.append("if (states != null) {\n");
			builder.append("states.add(new Number[] { ");
			int i = 0;
			for (CompilerVariable var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.getName());
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
			builder.append("states.add(new Number[] { ");
			i = 0;
			for (CompilerVariable var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.getName());
				builder.append(")");
				i += 1;
			}
			builder.append(" });\n");
			builder.append("}\n");
			builder.append("}\n");
			builder.append("if (states != null) {\n");
			builder.append("states.add(new Number[] { ");
			i = 0;
			for (CompilerVariable var : stateVariables) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append("number(");
				builder.append(var.getName());
				builder.append(")");
				i += 1;
			}
			builder.append(" });\n");
			builder.append("}\n");
		}
	}

	private void compile(ExpressionContext context, StringBuilder builder, Map<String, CompilerVariable> variables, ASTStatement statement, ClassType classType) {
		if (statement != null) {
			statement.compile(new ExpressionCompiler(context, variables, builder, classType));
		}		
	}
	
	private String createArray(float[] components) {
		return "new float[] {" + components[0] + "f," + components[1] + "f," + components[2] + "f," + components[3] + "f}";
	}
}	
