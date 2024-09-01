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

import com.nextbreakpoint.nextfractal.core.common.Time;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class Orbit {
	protected MutableNumber[] region = new MutableNumber[2];
	protected MutableNumber point = new MutableNumber(0, 0);
	protected MutableNumber x = new MutableNumber(0, 0);
	protected MutableNumber w = new MutableNumber(0, 0);
	protected double n = 0.0;
	protected MutableNumber[] numbers;
	@Getter
    protected List<Trap> traps = new ArrayList<>();
	@Setter
    @Getter
    protected Scope scope;
	@Setter
    @Getter
    protected boolean julia;
	@Setter
    @Getter
    protected Time time = new Time(0, 1);

	public Orbit() {
		region[0] = new MutableNumber();
		region[1] = new MutableNumber();
		//TODO invoke init explicitly in generated code
		initializeNumbersStack();
	}

    public void setX(ComplexNumber x) {
		this.x.set(x);
	}

	public void setW(ComplexNumber w) {
		this.w.set(w);
	}

	public ComplexNumber getX() {
		return x;
	}

	public ComplexNumber getW() {
		return w;
	}

	protected Trap trap(ComplexNumber center) {
		return new Trap(center);
	}

	public void getState(MutableNumber[] state) {
		scope.getState(state);
	}

	public int stateSize() {
		return scope.stateSize();
	}

	public MutableNumber getVariable(int index) {
		return scope.getVariable(index);
	}

	public double getRealVariable(int index) {
		return scope.getVariable(index).r();
	}

	public void setVariable(int index, MutableNumber value) {
		scope.setVariable(index, value);
	}

	public void setVariable(int index, double value) {
		scope.setVariable(index, value);
	}

	public void addVariable(MutableNumber value) {
		scope.addVariable(value);
	}

	public void addVariable(double value) {
		scope.addVariable(value);
	}

	public ComplexNumber[] getInitialRegion() {
		return region;
	}

	public ComplexNumber getInitialPoint() {
		return point;
	}

	public void setInitialRegion(ComplexNumber a, ComplexNumber b) {
		this.region[0].set(a);
		this.region[1].set(b);
	}

    public MutableNumber getNumber(int index) {
		return numbers[index];
	}
	
	public void reset() {
	}

	public void resetTraps() {
		traps.clear();
	}

	public void addTrap(Trap trap) {
		traps.add(trap);
	}

    public abstract void init();

	public abstract void render(List<ComplexNumber[]> states);
	
    public abstract boolean useTime();

	protected void initializeNumbersStack() {
		numbers = createNumbers();
		if (numbers != null) {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = new MutableNumber();
			}
		}
	}

	protected abstract MutableNumber[] createNumbers();
}
