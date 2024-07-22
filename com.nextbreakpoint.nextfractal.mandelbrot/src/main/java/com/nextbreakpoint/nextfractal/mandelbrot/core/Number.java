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

public class Number {
	protected double r;
	protected double i;

	public Number() {
		this(0, 0);
	}

	public Number(int n) {
		this(n, 0);
	}

	public Number(double r) {
		this(r, 0);
	}

	public Number(double[] v) {
		this(v[0], v[1]);
	}

	public Number(Number number) {
		this(number.r(), number.i());
	}
	
	public Number(double r, double i) {
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(i);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(r);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Number other = (Number) obj;
		if (Double.doubleToLongBits(i) != Double.doubleToLongBits(other.i))
			return false;
		if (Double.doubleToLongBits(r) != Double.doubleToLongBits(other.r))
			return false;
		return true;
	}
}
