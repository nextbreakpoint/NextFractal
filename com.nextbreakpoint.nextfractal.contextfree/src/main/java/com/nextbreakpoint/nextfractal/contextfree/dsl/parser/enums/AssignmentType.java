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

import lombok.Getter;

@Getter
public enum AssignmentType {
	None(0), ColorTarget(1), Color2Value(3), ColorMask(3), HueTarget(1), Hue2Value(3), HueMask(3), SaturationTarget(1 << 2), Saturation2Value(3 << 2), SaturationMask(3 << 2),
	BrightnessTarget(1 << 4), Brightness2Value(3 << 4), BrightnessMask(3 << 4), AlphaTarget(1 << 6), Alpha2Value(3 << 6), AlphaMask(3 << 6), 
	HSBA2Value(3 | 3 << 2 | 3 << 4 | 3 << 6), HSBATarget(1 | 1 << 2 | 1 << 4 | 1 << 6);
	
	private final int type;

	AssignmentType(int type) {
		this.type = type;
	}
}
