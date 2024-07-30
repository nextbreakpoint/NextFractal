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

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Variable {
	private final String name;
	private final boolean real;
	private final boolean create;
	private final MutableNumber value = new MutableNumber();

	public Variable(String name, boolean real, boolean create) {
		this.name = name;
		this.real = real;
		this.create = create;
	}

	public Variable(String name, boolean real, boolean create, Number value) {
		this(name, real, create);
		setValue(value);
	}

	public Variable(String name, boolean real, boolean create, double value) {
		this(name, real, create);
		setValue(value);
	}

    public void setValue(MutableNumber value) {
		this.value.set(value);
	}

	public void setValue(Number value) {
		this.value.set(value);
	}

	public void setValue(double value) {
		this.value.set(value);
	}

	public double getRealValue() {
		return value.r();
	}

	public Variable copy() {
		return new Variable(name, real, create, value);
	}
}
