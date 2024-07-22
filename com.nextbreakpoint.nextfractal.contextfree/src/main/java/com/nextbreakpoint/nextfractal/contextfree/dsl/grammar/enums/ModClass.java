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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums;

public enum ModClass {
    InvalidClass(-1), 
    NotAClass(0), 
    GeomClass(1), 
    ZClass(2), 
    TimeClass(4),
    HueClass(8), 
    SatClass(16), 
    BrightClass(32), 
    AlphaClass(64),
    HueTargetClass(128), 
    SatTargetClass(256), 
    BrightTargetClass(512), 
    AlphaTargetClass(1024),
    StrokeClass(2048), 
    ParamClass(4096), 
    PathOpClass(8192);
    
	private int type;
	
	private ModClass(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static ModClass fromType(int type) {
		for (ModClass value : ModClass.values()) {
			if (value.getType() == type) {
				return value;
			}
		}
		return NotAClass;
	}
}
