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
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColorInt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.ExpressionCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			ExpressionCompilerContext context = new ExpressionCompilerContext();
			ASTColor astColor = fractal.getColor();
			List<Variable> colorVars = new ArrayList<>();
			for (Variable var : fractal.getColorVariables()) {
				colorVars.add(var.copy());
			}
			List<Variable> stateVars = new ArrayList<>();
			for (Variable var : fractal.getStateVariables()) {
				stateVars.add(var.copy());
			}
			Map<String, Variable> vars = new HashMap<>();
            for (Variable var : fractal.getStateVariables()) {
                vars.put(var.getName(), var);
            }
            for (Variable var : fractal.getOrbitVariables()) {
                vars.put(var.getName(), var);
            }
			Map<String, Variable> newScope = new HashMap<>(vars);
			ExpressionCompiler compiler = new ExpressionCompiler(context, newScope);
			CompiledColor color = new CompiledColor(colorVars, stateVars, astColor.getLocation());
			color.setBackgroundColor(astColor.getArgb().getComponents());
			List<CompiledRule> rules = new ArrayList<>();
			for (ASTRule astRule : astColor.getRules()) {
				CompiledRule rule = new CompiledRule(astRule.getLocation());
				rule.setRuleCondition(astRule.getRuleExp().compile(compiler));
				rule.setColorExp(astRule.getColorExp().compile(compiler));
				rule.setOpacity(astRule.getOpacity());
				rules.add(rule);
			}
			color.setRules(rules);
			List<CompiledPalette> palettes = new ArrayList<>();
			if (astColor.getPalettes() != null) {
				for (ASTPalette astPalette : astColor.getPalettes()) {
					palettes.add(astPalette.compile(compiler));
				}
			}
			color.setPalettes(palettes);
			List<CompiledStatement> statements = new ArrayList<>();
			if (astColor.getInit() != null) {
				for (ASTStatement statement : astColor.getInit().getStatements()) {
					statements.add(statement.compile(compiler));
				}
			}
			color.setInit(new CompiledColorInt(statements));
			return new InterpretedColor(color, context);
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
