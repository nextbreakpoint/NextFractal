/*
 * NextFractal 2.2.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2022 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

public class ASTRegion {
	private ASTNumber a; 
	private ASTNumber b; 

	public ASTRegion(ASTNumber a, ASTNumber b) {
		this.a = a;
		this.b = b;
	}

	public ASTNumber getA() {
		return a;
	}

	public ASTNumber getB() {
		return b;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(a);
		builder.append(",");
		builder.append(b);
		builder.append("]");
		return builder.toString();
	}
}
