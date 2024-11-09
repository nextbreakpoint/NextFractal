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
package com.nextbreakpoint.nextfractal.mandelbrot.core;

import java.util.Objects;

public class ComplexNumber {
	protected double r;
	protected double i;

	public ComplexNumber() {
		this(0, 0);
	}

	public ComplexNumber(int n) {
		this(n, 0);
	}

	public ComplexNumber(double r) {
		this(r, 0);
	}

	public ComplexNumber(double[] v) {
		this(v[0], v[1]);
	}

	public ComplexNumber(ComplexNumber number) {
		this(number.r(), number.i());
	}
	
	public ComplexNumber(double r, double i) {
		this.r = r;
		this.i = i;
	}

	public double r() {
		return r;
	}

	public double i() {
		return i;
	}

	public int n() {
		return (int)r;
	}

	public boolean isReal() {
		return i == 0;
	}

	public boolean isInteger() {
		return i == 0 && r == (int)r;
	}

	@Override
	public String toString() {
		return r + ", " + i;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComplexNumber number = (ComplexNumber) o;
		return Double.compare(r, number.r) == 0 && Double.compare(i, number.i) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, i);
	}
}
