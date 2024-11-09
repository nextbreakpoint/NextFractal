/*
 * NextFractal 2.4.0
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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLInterpreterContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLRule;

import java.util.HashMap;
import java.util.Map;

public class InterpretedColor extends Color implements DSLInterpreterContext {
	private final DSLColor color;
	private final DSLExpressionContext context;
	private final Map<String, Palette> palettes = new HashMap<>();
	private final Map<String, Variable> variables = new HashMap<>();

	public InterpretedColor(DSLExpressionContext context, DSLColor color) {
        this.context = context;
        this.color = color;
		initializeNumbersStack();
	}

	public void init() {
		variables.clear();
        for (VariableDeclaration var : color.getStateVariables()) {
            variables.put(var.name(), var.asVariable());
        }
        for (VariableDeclaration var : color.getColorVariables()) {
            variables.put(var.name(), var.asVariable());
        }
		for (DSLPalette palette : color.getPalettes()) {
            palettes.put(palette.getName(), palette.evaluate(this, variables).build());
		}
	}

	public void render() {
		updateStateVars(variables);
		setColor(color.getBackgroundColor());
		color.getInit().evaluate(this, variables);
		for (DSLRule rule : color.getRules()) {
			if ((rule.getRuleCondition().evaluate(this, variables))) {
				addColor(rule.getOpacity(), rule.getColorExp().evaluate(this, variables));
			}
		}
	}

	private void updateStateVars(Map<String, Variable> vars) {
		int i = 0;
        for (VariableDeclaration var : color.getStateVariables()) {
            vars.get(var.name()).setValue(scope.getVariable(i));
            i++;
        }
	}

	protected MutableNumber[] createNumbers() {
		if (context == null) {
			return null;
		}
		return new MutableNumber[context.getNumberCount()];
	}

	@Override
	public boolean useTime() {
		return context.colorUseTime();
	}

	@Override
	public Trap getTrap(String name) {
		return null;
	}

	@Override
	public Palette getPalette(String name) {
		return palettes.get(name);
	}
}
