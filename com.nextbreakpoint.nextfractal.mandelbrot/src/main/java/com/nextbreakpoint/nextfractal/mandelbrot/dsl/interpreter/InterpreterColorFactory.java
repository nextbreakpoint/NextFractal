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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorInt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.SimpleASTCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTStatement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class InterpreterColorFactory implements ClassFactory<Color> {
	private final ASTFractal fractal;
	private final String source;
	@Getter
    private final List<ParserError> errors;

	public InterpreterColorFactory(ASTFractal fractal, String source, List<ParserError> errors) {
		this.fractal = fractal;
		this.source = source;
		this.errors = errors;
	}

	public Color create() throws DSLCompilerException {
		try {
			DSLExpressionContext context = new DSLExpressionContext();
			ASTColor astColor = fractal.getColor();
            List<VariableDeclaration> colorVars = new ArrayList<>(fractal.getColorVariables());
            List<VariableDeclaration> stateVars = new ArrayList<>(fractal.getStateVariables());
			Map<String, VariableDeclaration> vars = new HashMap<>();
            for (VariableDeclaration var : fractal.getStateVariables()) {
                vars.put(var.name(), var);
            }
            for (VariableDeclaration var : fractal.getOrbitVariables()) {
                vars.put(var.name(), var);
            }
            SimpleASTCompiler compiler = new SimpleASTCompiler(context, new HashMap<>(vars));
			final List<DSLRule> rules = new ArrayList<>();
			if (astColor.getRules() != null) {
				for (ASTRule astRule : astColor.getRules()) {
					DSLRule rule = new DSLRule(
							astRule.getLocation(),
							astRule.getRuleExp().compile(compiler),
							astRule.getColorExp().compile(compiler),
							astRule.getOpacity()
					);
					rules.add(rule);
				}
			}
			final List<DSLPalette> palettes = new ArrayList<>();
			if (astColor.getPalettes() != null) {
				for (ASTPalette astPalette : astColor.getPalettes()) {
					palettes.add(astPalette.compile(compiler));
				}
			}
			final List<DSLStatement> initStatements = new ArrayList<>();
			if (astColor.getInit() != null) {
				for (ASTStatement statement : astColor.getInit().getStatements()) {
					initStatements.add(statement.compile(compiler));
				}
			}
			final DSLColorInt colorInt = new DSLColorInt(astColor.getLocation(), initStatements);
			final DSLColor color = DSLColor.builder()
					.withToken(astColor.getLocation())
					.withColorVariables(colorVars)
					.withStateVariables(stateVars)
					.withExpressionContext(context)
					.withBackgroundColor(astColor.getArgb().getComponents())
					.withRules(rules)
					.withPalettes(palettes)
					.withInit(colorInt)
					.build();
			return new InterpretedColor(color);
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			errors.add(new ParserError(type, line, charPositionInLine, index, length, message));
			throw new DSLCompilerException("Can't build color", source, errors);
		} catch (Exception e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			String message = e.getMessage();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
			throw new DSLCompilerException("Can't build color", source, errors);
		}
	}
}
