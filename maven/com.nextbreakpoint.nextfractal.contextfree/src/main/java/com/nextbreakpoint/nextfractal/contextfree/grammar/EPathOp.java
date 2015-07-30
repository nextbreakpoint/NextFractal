/*
 * NextFractal 1.1.4
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.grammar;

enum EPathOp { 
	UNKNOWN, MOVETO, MOVEREL, LINETO, LINEREL, ARCTO, ARCREL, CURVETO, CURVEREL, CLOSEPOLY;

	public static EPathOp pathOpTypeByName(String name) {
		for (EPathOp type : EPathOp.values()) {
			if (type.name().equals(name)) {
				return type;
			}
		}
		return UNKNOWN;
	}
}
