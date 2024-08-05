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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorInt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.DefaultResolver;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;

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

	public DSLColor resolve(ASTVariables variables, DSLExpressionContext expressionContext) throws DSLParserException {
		try {
            final List<VariableDeclaration> colorVars = new ArrayList<>(variables.getColorVariables());
			final List<VariableDeclaration> stateVars = new ArrayList<>(variables.getStateVariables());
			final Map<String, VariableDeclaration> vars = new HashMap<>();
			for (VariableDeclaration var : variables.getStateVariables()) {
				vars.put(var.name(), var);
			}
			for (VariableDeclaration var : variables.getColorVariables()) {
				vars.put(var.name(), var);
			}
			final DefaultResolver resolver = new DefaultResolver(expressionContext, vars);
            final List<DSLRule> rules = new ArrayList<>();
			if (getRules() != null) {
				for (ASTRule astRule : getRules()) {
					DSLRule rule = new DSLRule(
							astRule.getLocation(),
							astRule.getRuleExp().resolve(resolver),
							astRule.getColorExp().resolve(resolver),
							astRule.getOpacity()
					);
					rules.add(rule);
				}
			}
			final List<DSLPalette> palettes = new ArrayList<>();
			if (getPalettes() != null) {
				for (ASTPalette astPalette : getPalettes()) {
					palettes.add(astPalette.resolve(resolver));
				}
			}
			final List<DSLStatement> initStatements = new ArrayList<>();
			if (getInit() != null) {
				for (ASTStatement statement : getInit().getStatements()) {
					initStatements.add(statement.resolve(resolver));
				}
			}
			final DSLColorInt colorInt = new DSLColorInt(getLocation(), initStatements);
			return DSLColor.builder()
					.withToken(getLocation())
					.withBackgroundColor(getArgb().getComponents())
					.withPalettes(palettes)
					.withInit(colorInt)
					.withRules(rules)
					.withColorVariables(colorVars)
					.withStateVariables(stateVars)
					.build();
		} catch (ASTException e) {
            long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
            final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, line, charPositionInLine, index, length, e.getMessage()));
			throw new DSLParserException("Can't build color", errors);
		} catch (Exception e) {
            final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage()));
			throw new DSLParserException("Can't build color", errors);
		}
	}
}
