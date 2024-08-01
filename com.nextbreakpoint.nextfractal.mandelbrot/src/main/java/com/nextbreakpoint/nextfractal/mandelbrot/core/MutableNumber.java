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

public class MutableNumber extends ComplexNumber {
	public MutableNumber(int n) {
		super(n, 0);
	}

	public MutableNumber(double r) {
		super(r, 0);
	}

	public MutableNumber(double r, double i) {
		super(r, i);
	}

	public MutableNumber() {
		super(0, 0);
	}
	
	public MutableNumber(ComplexNumber value) {
		super(value.r, value.i);
	}

	public MutableNumber set(ComplexNumber x) {
		this.r = x.r();
		this.i = x.i();
		return this;
	}

	public MutableNumber set(double r, double i) {
		this.r = r;
		this.i = i;
		return this;
	}
	
	public MutableNumber set(double r) {
		this.r = r;
		this.i = 0;
		return this;
	}
	
	public MutableNumber set(int n) {
		this.r = n;
		this.i = 0;
		return this;
	}
}
