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
package com.nextbreakpoint.nextfractal.mandelbrot.core;

public class Scope {
	private MutableNumber[] vars = new MutableNumber[0]; 

	/**
	 * @param value
	 */
	public void addVariable(MutableNumber value) {
		MutableNumber[] tmpVars = vars;
		vars = new MutableNumber[vars.length + 1];
		System.arraycopy(tmpVars, 0, vars, 0, tmpVars.length);
		vars[tmpVars.length] = new MutableNumber(value);
	}

	/**
	 * @param value
	 */
	public void addVariable(double value) {
		MutableNumber[] tmpVars = vars;
		vars = new MutableNumber[vars.length + 1];
		System.arraycopy(tmpVars, 0, vars, 0, tmpVars.length);
		vars[tmpVars.length] = new MutableNumber(value);
	}

	/**
	 * @param index
	 * @param value
	 */
	public void setVariable(int index, Number value) {
		vars[index].set(value);
	}

	/**
	 * @param index
	 * @param value
	 */
	public void setVariable(int index, double value) {
		vars[index].set(value);
	}

	/**
	 * @param index
	 * @return
	 */
	public MutableNumber getVariable(int index) {
		return vars[index];
	}
	
	/**
	 * @return
	 */
	public int stateSize() {
		return vars.length;
	}

	/**
	 * @param state
	 */
	public void getState(MutableNumber[] state) {
		for (int i = 0; i < vars.length; i++) {
			state[i].set(vars[i]);
		}
	}

	/**
	 * @param state
	 */
	public void setState(Number[] state) {
		for (int i = 0; i < vars.length; i++) {
			vars[i].set(state[i]);
		}
	}

	/**
	 * 
	 */
	public void clear() {
		for (int i = 0; i < vars.length; i++) {
			vars[i].set(0, 0);
		}
	}

	/**
	 * 
	 */
	public void empty() {
		vars = new MutableNumber[0];
	}
}
