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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledColor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledRule;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InterpreterColor extends Color implements InterpreterContext {
	private CompiledColor color;
	private ExpressionContext context;
	private Map<String, Trap> traps = new HashMap<>();
	private Map<String, Palette> palettes = new HashMap<>();
	private Map<String, CompilerVariable> vars = new HashMap<>();

	public InterpreterColor(CompiledColor color, ExpressionContext context) {
		this.color = color;
		this.context = context;
		initializeStack();
	}

	public void init() {
		for (Iterator<CompilerVariable> s = color.getStateVariables().iterator(); s.hasNext();) {
			CompilerVariable var = s.next();
			vars.put(var.getName(), var);
		}
		for (Iterator<CompilerVariable> s = color.getColorVariables().iterator(); s.hasNext();) {
			CompilerVariable var = s.next();
			vars.put(var.getName(), var);
		}
		for (CompiledPalette cPalette : color.getPalettes()) {
			Palette palette = new Palette();
			for (CompiledPaletteElement cElement : cPalette.getElements()) {
				PaletteExpression expression = null;
				if (cElement.getExp() != null) {
					expression = step -> {
						vars.put("step", new CompilerVariable("step", true, false, step));
						return cElement.getExp().evaluateReal(InterpreterColor.this, vars);
					};
				} else {
					expression = step -> step;
				}
				PaletteElement element = new PaletteElement(cElement.getBeginColor(), cElement.getEndColor(), cElement.getSteps(), expression);
				palette.add(element);
			}
			palettes.put(cPalette.getName(), palette.build());
		}
	}

	public void render() {
		updateStateVars(vars);
		setColor(color.getBackgroundColor());
		for (CompiledStatement statement : color.getInitStatements()) {
			statement.evaluate(this, vars);
		} 
		for (CompiledRule rule : color.getRules()) {
			if ((rule.getRuleCondition().evaluate(this, vars))) {
				addColor(rule.getOpacity(), rule.getColorExp().evaluate(this, vars));
			}
		}
	}

	private void updateStateVars(Map<String, CompilerVariable> vars) {
		int i = 0;
		for (Iterator<CompilerVariable> s = color.getStateVariables().iterator(); s.hasNext();) {
			CompilerVariable var = s.next();
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
