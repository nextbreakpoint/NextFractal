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

import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.ExpressionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterpretedOrbit extends Orbit implements ExpressionContext {
	private final CompiledOrbit orbit;
	private final ExpressionCompilerContext context;
	private final Map<String, Trap> traps = new HashMap<>();
	private final Map<String, Palette> palettes = new HashMap<>();
	private final Map<String, Variable> vars = new HashMap<>();

	public InterpretedOrbit(CompiledOrbit orbit, ExpressionCompilerContext context) {
		this.orbit = orbit;
		this.context = context;
		initializeNumbersStack();
	}

	public void init() {
        for (Variable var : orbit.getStateVariables()) {
            vars.put(var.getName(), var);
        }
        for (Variable var : orbit.getOrbitVariables()) {
            vars.put(var.getName(), var);
        }
		setInitialRegion(orbit.getRegion()[0], orbit.getRegion()[1]);
        for (Variable var : orbit.getStateVariables()) {
            if (var.isReal()) {
                addVariable(var.getRealValue());
            } else {
                addVariable(var.getValue());
            }
        }
		resetTraps();
		traps.clear();
		for (CompiledTrap cTrap : orbit.getTraps()) {
			Trap trap = new Trap(cTrap.getCenter());
			addTrap(trap);
			for (CompiledTrapOp cTrapOp : cTrap.getOperators()) {
				cTrapOp.evaluate(trap);
			}
			traps.put(cTrap.getName(), trap);
		}
	}

	public void render(List<Number[]> states) {
		n = orbit.getLoop().getBegin();
		ensureVariable(vars, "n", n);
		ensureVariable(vars, "x", x);
		ensureVariable(vars, "w", w);
		if (states != null) {
			updateState();
			saveState(states);
		}
		for (CompiledStatement statement : orbit.getBegin().getStatements()) {
			statement.evaluate(this, vars);
		} 
		boolean stop = false;
		Map<String, Variable> newScope = new HashMap<>(vars);
		for (int i = orbit.getLoop().getBegin() + 1; i <= orbit.getLoop().getEnd(); i++) {
			for (CompiledStatement statement : orbit.getLoop().getStatements()) {
				stop = statement.evaluate(this, vars);
				if (stop) {
					break;
				}
			} 
			if (stop || orbit.getLoop().getCondition().evaluate(this, newScope)) {
				n = i;
				break;
			}
			if (states != null) {
				updateState();
				saveState(states);
			}
		}
		ensureVariable(vars, "n", n);
		newScope = new HashMap<>(vars);
		for (CompiledStatement statement : orbit.getEnd().getStatements()) {
			statement.evaluate(this, newScope);
		} 
		updateState();
		if (states != null) {
			saveState(states);
		}
	}

	private void ensureVariable(Map<String, Variable> scope, String name, double value) {
		Variable var = scope.get(name);
		if (var == null) {
			var = new Variable(name, true, true);
			scope.put(name, var);
		}
		var.setValue(value);
	}

	private void ensureVariable(Map<String, Variable> scope, String name, Number value) {
		Variable var = scope.get(name);
		if (var == null) {
			var = new Variable(name, true, true);
			scope.put(name, var);
		}
		var.setValue(value);
	}

	private void saveState(List<Number[]> states) {
		MutableNumber[] state = new MutableNumber[scope.stateSize()];
		for (int k = 0; k < state.length; k++) {
			state[k] = new MutableNumber();
		}
		scope.getState(state);
		states.add(state);
	}

	private void updateState() {
		int i = 0;
        for (Variable var : orbit.getStateVariables()) {
            if (var.isReal()) {
                setVariable(i, vars.get(var.getName()).getRealValue());
            } else {
                setVariable(i, vars.get(var.getName()).getValue());
            }
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
		return context.orbitUseTime();
	}

	@Override
	public Palette getPalette(String name) {
		return palettes.get(name);
	}

	@Override
	public Trap getTrap(String name) {
		return traps.get(name);
	}
}
