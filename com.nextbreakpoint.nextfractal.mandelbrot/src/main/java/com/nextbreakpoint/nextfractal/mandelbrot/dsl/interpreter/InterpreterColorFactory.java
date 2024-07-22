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

import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InterpreterColorFactory implements ClassFactory<Color> {
	private ASTFractal astFractal;
	private String source;
	private List<SourceError> errors;

	public InterpreterColorFactory(ASTFractal astFractal, String source, List<SourceError> errors) {
		this.astFractal = astFractal;
		this.source = source;
		this.errors = errors;
	}

	public Color create() throws ParserException {
		try {
			ExpressionContext context = new ExpressionContext();
			ASTColor astColor = astFractal.getColor();
			List<CompilerVariable> colorVars = new ArrayList<>();
			for (CompilerVariable var : astFractal.getColorVariables()) {
				colorVars.add(var.copy());
			}
			List<CompilerVariable> stateVars = new ArrayList<>();
			for (CompilerVariable var : astFractal.getStateVariables()) {
				stateVars.add(var.copy());
			}
			Map<String, CompilerVariable> vars = new HashMap<>();

			for (Iterator<CompilerVariable> s = astFractal.getStateVariables().iterator(); s.hasNext();) {
				CompilerVariable var = s.next();
				vars.put(var.getName(), var);
			}
			for (Iterator<CompilerVariable> s = astFractal.getOrbitVariables().iterator(); s.hasNext();) {
				CompilerVariable var = s.next();
				vars.put(var.getName(), var);
			}

			Map<String, CompilerVariable> newScope = new HashMap<>(vars);
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
			List<CompiledPalette> palettes = new ArrayList<>();
			if (astColor.getPalettes() != null) {
				for (ASTPalette astPalette : astColor.getPalettes()) {
					palettes.add(astPalette.compile(compiler));
				}
			}
			List<CompiledStatement> statements = new ArrayList<>();
			if (astColor.getInit() != null) {
				for (ASTStatement statement : astColor.getInit().getStatements()) {
					statements.add(statement.compile(compiler));
				}
			}
			color.setInitStatements(statements);
			color.setPalettes(palettes);
			color.setRules(rules);
			return new InterpreterColor(color, context);
		} catch (ASTException e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			errors.add(new SourceError(type, line, charPositionInLine, index, length, message));
			throw new ParserException("Can't build color", errors);
		} catch (Exception e) {
			SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
			String message = e.getMessage();
			errors.add(new SourceError(type, 0, 0, 0, 0, message));
			throw new ParserException("Can't build color", errors);
		}
	}

	public List<SourceError> getErrors() {
		return errors;
	}
}
