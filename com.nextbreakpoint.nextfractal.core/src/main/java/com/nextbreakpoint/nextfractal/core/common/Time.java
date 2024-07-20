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
public class Time implements Serializable, Cloneable {
	private final double value;
	private final double scale;

	/**
	 *
	 */
	public Time() {
		this(0, 0);
	}

	/**
	 * @param value
	 * @param scale
	 */
	public Time(final double value, final double scale) {
		this.value = value;
		this.scale = scale;
	}

	public Time(double[] v) {
		this.value = v[0];
		this.scale = v[1];
	}

	public Time(Double[] v) {
		this.value = v[0];
		this.scale = v[1];
	}

	/**
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(value);
		builder.append(", ");
		builder.append(scale);
		return builder.toString();
	}

	/**
	 * @param text
	 * @return
	 */
	public static Time valueOf(final String text) {
		final StringTokenizer tkn = new StringTokenizer(text, ",");
		final String value = tkn.nextToken().trim();
		final String scale = tkn.nextToken().trim();
		return new Time(Double.valueOf(value), Double.valueOf(scale));
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
		final Time other = (Time) obj;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
			return false;
		}
		if (Double.doubleToLongBits(scale) != Double.doubleToLongBits(other.scale)) {
			return false;
		}
		return true;
	}

	/**
	 * @see Object#clone()
	 */
	@Override
	public Time clone() {
		return new Time(value, scale);
	}
	
	/**
	 * @return
	 */
	public double[] toArray() {
		return new double[] {value, scale};
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(value);
		result = (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(scale);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
