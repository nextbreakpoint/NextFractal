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
package com.nextbreakpoint.nextfractal.core.common;

import lombok.Builder;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * @author Andrea Medeghini
 */
@Builder(setterPrefix = "with", toBuilder = true)
public class Double2D implements Serializable, Cloneable {
	private final double x;
	private final double y;

	/**
	 *
	 */
	public Double2D() {
		this(0, 0);
	}

	/**
	 * @param x
	 * @param y
	 */
	public Double2D(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	public Double2D(double[] v) {
		this.x = v[0];
		this.y = v[1];
	}

	public Double2D(Double[] v) {
		this.x = v[0];
		this.y = v[1];
	}

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(x);
		builder.append(", ");
		builder.append(y);
		return builder.toString();
	}

	/**
	 * @param value
	 * @return
	 */
	public static Double2D valueOf(final String value) {
		final StringTokenizer tkn = new StringTokenizer(value, ",");
		final String x = tkn.nextToken().trim();
		final String y = tkn.nextToken().trim();
		return new Double2D(Double.valueOf(x), Double.valueOf(y));
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		final Double2D other = (Double2D) obj;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x)) {
			return false;
		}
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y)) {
			return false;
		}
		return true;
	}

	/**
	 * @see Object#clone()
	 */
	@Override
	public Double2D clone() {
		return new Double2D(x, y);
	}
	
	/**
	 * @return
	 */
	public double[] toArray() {
		return new double[] { x, y };
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
