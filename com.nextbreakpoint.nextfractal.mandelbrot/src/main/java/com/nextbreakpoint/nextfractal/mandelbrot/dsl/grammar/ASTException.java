/*
 * NextFractal 2.1.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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

import org.antlr.v4.runtime.Token;

public class ASTException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private Token location;

	public ASTException(String message, Token location, Throwable cause) {
		super(message, cause);
		this.location = location;
	}

	public ASTException(String message, Token location) {
		super(message);
		this.location = location;
	}

	public Token getLocation() {
		return location;
	}
	
	@Override
	public String toString() {
		if (location != null) {
			return "[" + location.getLine() + ":" + location.getCharPositionInLine() + "] " + super.toString();
		}
		return super.toString();
	}
}
