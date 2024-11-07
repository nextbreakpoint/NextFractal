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

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLInterpreterContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterpretedOrbit extends Orbit implements DSLInterpreterContext {
	private final DSLOrbit orbit;
	private final DSLExpressionContext context;
	private final Map<String, Trap> traps = new HashMap<>();
	private final Map<String, Variable> variables = new HashMap<>();

	public InterpretedOrbit(DSLExpressionContext context, DSLOrbit orbit) {
        this.context = context;
        this.orbit = orbit;
		initializeNumbersStack();
	}

	public void init() {
        for (VariableDeclaration var : orbit.getStateVariables()) {
            variables.put(var.name(), var.asVariable());
        }
        for (VariableDeclaration var : orbit.getOrbitVariables()) {
            variables.put(var.name(), var.asVariable());
        }
		setInitialRegion(orbit.getRegion()[0], orbit.getRegion()[1]);
        for (VariableDeclaration var : orbit.getStateVariables()) {
			final Variable variable = variables.get(var.name());
            if (var.real()) {
                addVariable(variable.getRealValue());
            } else {
                addVariable(variable.getValue());
            }
        }
		resetTraps();
		traps.clear();
		for (DSLTrap trap : orbit.getTraps()) {
			Trap newTrap = new Trap(trap.getCenter());
			addTrap(newTrap);
			for (DSLTrapOp trapOp : trap.getOperators()) {
				trapOp.evaluate(newTrap);
			}
			traps.put(trap.getName(), newTrap);
		}
	}

	public void render(List<ComplexNumber[]> states) {
		n = orbit.getLoop().getBegin();
		ensureVariable(variables, "n", n);
		ensureVariable(variables, "x", x);
		ensureVariable(variables, "w", w);
		if (states != null) {
			updateState();
			saveState(states);
		}
		if (orbit.getBegin() != null) {
			orbit.getBegin().evaluate(this, variables);
		}
		if (orbit.getLoop() != null) {
			for (int i = orbit.getLoop().getBegin() + 1; i <= orbit.getLoop().getEnd(); i++) {
				if (orbit.getLoop().evaluate(this, variables) || orbit.getLoop().getCondition().evaluate(this, variables)) {
					n = i;
					break;
				}
				if (states != null) {
					updateState();
					saveState(states);
				}
			}
		}
		ensureVariable(variables, "n", n);
		if (orbit.getEnd() != null) {
            orbit.getEnd().evaluate(this, variables);
		}
		updateState();
		if (states != null) {
			saveState(states);
		}
	}

	private void ensureVariable(Map<String, Variable> scope, String name, double value) {
		Variable var = scope.get(name);
		if (var == null) {
			var = new Variable(name, true);
			scope.put(name, var);
		}
		var.setValue(value);
	}

	private void ensureVariable(Map<String, Variable> scope, String name, ComplexNumber value) {
		Variable var = scope.get(name);
		if (var == null) {
			var = new Variable(name, true);
			scope.put(name, var);
		}
		var.setValue(value);
	}

	private void saveState(List<ComplexNumber[]> states) {
		MutableNumber[] state = new MutableNumber[scope.stateSize()];
		for (int k = 0; k < state.length; k++) {
			state[k] = new MutableNumber();
		}
		scope.getState(state);
		states.add(state);
	}

	private void updateState() {
		int i = 0;
        for (VariableDeclaration var : orbit.getStateVariables()) {
            if (var.real()) {
                setVariable(i, variables.get(var.name()).getRealValue());
            } else {
                setVariable(i, variables.get(var.name()).getValue());
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
		return null;
	}

	@Override
	public Trap getTrap(String name) {
		return traps.get(name);
	}
}
