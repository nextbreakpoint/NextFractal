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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums;

public enum Locality {
	UnknownLocal(0), ImpureNonlocal(1), PureNonlocal(3), PureLocal(7);
	
	private int type;

	private Locality(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static Locality fromType(int type) {
		for (Locality value : Locality.values()) {
			if (value.getType() == type) {
				return value;
			}
		}
		return UnknownLocal;
	}
}
