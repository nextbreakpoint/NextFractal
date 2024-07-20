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
public class Integer4D implements Serializable, Cloneable {
	private final int x;
	private final int y;
	private final int z;
	private final int w;
	
	/**
	 * 
	 */
	public Integer4D() {
		this(0, 0, 0, 0);
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param w
	 */
	public Integer4D(final int x, final int y, final int z, final int w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Integer4D(int[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
		this.w = v[3];
	}

	public Integer4D(Integer[] v) {
		this.x = v[0];
		this.y = v[1];
		this.z = v[2];
		this.w = v[3];
	}

	/**
	 * @return the w
	 */
	public int getW() {
		return w;
	}

	/**
	 * @return the x
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return the z
	 */
	public int getZ() {
		return z;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(x);
		builder.append(", ");
		builder.append(y);
		builder.append(", ");
		builder.append(z);
		builder.append(", ");
		builder.append(w);
		return builder.toString();
	}

	/**
	 * @param value
	 * @return
	 */
	public static Integer4D valueOf(final String value) {
		final StringTokenizer tkn = new StringTokenizer(value, ",");
		final String x = tkn.nextToken().trim();
		final String y = tkn.nextToken().trim();
		final String z = tkn.nextToken().trim();
		final String w = tkn.nextToken().trim();
		return new Integer4D(Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(z), Integer.valueOf(w));
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		final Integer4D other = (Integer4D) obj;
		if (w != other.w) {
			return false;
		}
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		if (z != other.z) {
			return false;
		}
		return true;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Integer4D clone() {
		return new Integer4D(x, y, z, w);
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		result = 31 * result + z;
		result = 31 * result + w;
		return result;
	}
}
