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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.ExpressionContext;

import java.util.HashMap;
import java.util.Map;

public class InterpretedColor extends Color implements ExpressionContext {
	private final CompiledColor color;
	private final ExpressionCompilerContext context;
	private final Map<String, Trap> traps = new HashMap<>();
	private final Map<String, Palette> palettes = new HashMap<>();
	private final Map<String, Variable> vars = new HashMap<>();

	public InterpretedColor(CompiledColor color, ExpressionCompilerContext context) {
		this.color = color;
		this.context = context;
		initializeNumbersStack();
	}

	public void init() {
        for (Variable var : color.getStateVariables()) {
            vars.put(var.getName(), var);
        }
        for (Variable var : color.getColorVariables()) {
            vars.put(var.getName(), var);
        }
		for (CompiledPalette cPalette : color.getPalettes()) {
			final Palette palette = getPalette(cPalette);
			palettes.put(cPalette.getName(), palette.build());
		}
	}

	private Palette getPalette(CompiledPalette cPalette) {
		Palette palette = new Palette();
		for (CompiledPaletteElement cElement : cPalette.getElements()) {
			final PaletteExpression expression = getPaletteExpression(cElement);
			PaletteElement element = new PaletteElement(cElement.beginColor(), cElement.endColor(), cElement.steps(), expression);
			palette.add(element);
		}
		return palette;
	}

	private PaletteExpression getPaletteExpression(CompiledPaletteElement cElement) {
		if (cElement.exp() != null) {
			return step -> {
				vars.put("step", new Variable("step", true, false, step));
				return cElement.exp().evaluateReal(InterpretedColor.this, vars);
			};
		} else {
			return step -> step;
		}
	}

	public void render() {
		updateStateVars(vars);
		setColor(color.getBackgroundColor());
		for (CompiledStatement statement : color.getInit().getStatements()) {
			statement.evaluate(this, vars);
		} 
		for (CompiledRule rule : color.getRules()) {
			if ((rule.getRuleCondition().evaluate(this, vars))) {
				addColor(rule.getOpacity(), rule.getColorExp().evaluate(this, vars));
			}
		}
	}

	private void updateStateVars(Map<String, Variable> vars) {
		int i = 0;
        for (Variable var : color.getStateVariables()) {
            vars.get(var.getName()).setValue(this.scope.getVariable(i));
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
		return traps.get(name);
	}

	@Override
	public Palette getPalette(String name) {
		return palettes.get(name);
	}
}
