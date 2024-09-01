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
package com.nextbreakpoint.nextfractal.core.common;

import lombok.Builder;

@Builder(setterPrefix = "with", toBuilder = true)
public record Time(double value, double scale) {
	/**
	 *
	 */
	public Time() {
		this(0, 0);
	}

	/**
	 *
	 * @param array
	 */
	public Time(double[] array) {
		this(array[0], array[1]);
	}

	/**
	 *
	 * @param array
	 */
	public Time(Double[] array) {
		this(array[0], array[1]);
	}

	/**
	 * @return
	 */
	public double[] toArray() {
		return new double[] {value, scale};
	}
}
