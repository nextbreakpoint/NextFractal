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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColorInt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.ExpressionCompiler;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTColor extends ASTObject {
	private ASTColorInit colorInit;
	@Getter
    private final List<ASTPalette> palettes = new ArrayList<>();
	@Getter
    private final List<ASTRule> rules = new ArrayList<>();
	@Getter
    private final ASTColorARGB argb;

	public ASTColor(Token location, ASTColorARGB argb) {
		super(location);
		this.argb = argb;
	}

    public void addPalette(ASTPalette palette) {
		palettes.add(palette);
	}

    public void addRule(ASTRule rule) {
		rules.add(rule);
	}

	public void setInit(ASTColorInit colorInit) {
		this.colorInit = colorInit;
	}

	public ASTColorInit getInit() {
		return colorInit;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("argb = ");
		builder.append(argb);
		builder.append(",palettes = [");
		for (int i = 0; i < palettes.size(); i++) {
			ASTPalette palette = palettes.get(i);
			builder.append("{");
			builder.append(palette);
			builder.append("}");
			if (i < palettes.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		builder.append(",init = {");
		if (colorInit != null) {
			builder.append(colorInit);
		}
		builder.append("}");
		builder.append(",rules = [");
		for (int i = 0; i < rules.size(); i++) {
			ASTRule rule = rules.get(i);
			builder.append("{");
			builder.append(rule);
			builder.append("}");
			if (i < rules.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public CompiledColor compile(ASTVariables variables) {
		try {
			ExpressionCompilerContext context = new ExpressionCompilerContext();
			List<Variable> colorVars = new ArrayList<>();
			for (Variable var : variables.getColorVariables()) {
				colorVars.add(var.copy());
			}
			List<Variable> stateVars = new ArrayList<>();
			for (Variable var : variables.getStateVariables()) {
				stateVars.add(var.copy());
			}
			Map<String, Variable> vars = new HashMap<>();
			for (Variable var : variables.getStateVariables()) {
				vars.put(var.getName(), var);
			}
			for (Variable var : variables.getColorVariables()) {
				vars.put(var.getName(), var);
			}
			Map<String, Variable> newScope = new HashMap<>(vars);
			ExpressionCompiler compiler = new ExpressionCompiler(context, newScope);
			CompiledColor color = new CompiledColor(colorVars, stateVars, getLocation());
			color.setBackgroundColor(getArgb().getComponents());
			List<CompiledRule> rules = new ArrayList<>();
			for (ASTRule astRule : getRules()) {
				CompiledRule rule = new CompiledRule(astRule.getLocation());
				rule.setRuleCondition(astRule.getRuleExp().compile(compiler));
				rule.setColorExp(astRule.getColorExp().compile(compiler));
				rule.setOpacity(astRule.getOpacity());
				rules.add(rule);
			}
			color.setRules(rules);
			List<CompiledPalette> palettes = new ArrayList<>();
			if (getPalettes() != null) {
				for (ASTPalette astPalette : getPalettes()) {
					palettes.add(astPalette.compile(compiler));
				}
			}
			color.setPalettes(palettes);
			List<CompiledStatement> statements = new ArrayList<>();
			if (getInit() != null) {
				for (ASTStatement statement : getInit().getStatements()) {
					statements.add(statement.compile(compiler));
				}
			}
			color.setInit(new CompiledColorInt(statements));
			return color;
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(type, line, charPositionInLine, index, length, message));
//			throw new DSLParserException("Can't build color", errors);
		} catch (Exception e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
//			throw new DSLParserException("Can't build color", errors);
		}
		return null;
	}
}
