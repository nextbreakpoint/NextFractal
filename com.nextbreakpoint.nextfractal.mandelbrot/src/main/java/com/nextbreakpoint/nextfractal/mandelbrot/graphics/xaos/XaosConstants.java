/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics.xaos;

/*
 *     XaoS, a fast portable realtime fractal zoomer
 *                  Copyright (C) 1996,1997 by
 *
 *      Jan Hubicka          (hubicka@paru.cas.cz)
 *      Thomas Marsh         (tmarsh@austin.ibm.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

class XaosConstants {
	public static final boolean DUMP = false;
	public static final boolean DUMP_XAOS = false;
	public static final boolean SHOW_FILL = false;
	public static final boolean SHOW_UNCACHED = false;
	public static final boolean SHOW_REFRESH = false;
	public static final boolean SHOW_SYMETRY = false;
	public static final boolean SHOW_CALCULATE = false;
	public static final boolean SHOW_SOLIDGUESS = false;
	public static final boolean PRINT_REALLOCTABLE = false;
	public static final boolean PRINT_CALCULATE = false;
	public static final boolean PRINT_POSITIONS = false;
	public static final boolean PRINT_MOVETABLE = false;
	public static final boolean PRINT_FILLTABLE = false;
	public static final boolean PRINT_MULTABLE = false;
	public static final boolean PRINT_REGION = false;
	public static final boolean PRINT_ONLYNEW = true;
	public static final boolean USE_XAOS = true;
	public static final boolean USE_SYMETRY = true;
	public static final boolean USE_SOLIDGUESS = true;
	public static final boolean USE_MULTITHREAD = true;
	public static final int GUESS_RANGE = 4;
	public static final int RANGES = 2;
	public static final int RANGE = 4;
	public static final int STEPS = 32;
	public static final int MASK = 0x7;
	public static final int DSIZE = (RANGES + 1);
	public static final int FPMUL = 64;
	public static final int FPRANGE = FPMUL * RANGE;
	public static final int MAX_PRICE = Integer.MAX_VALUE;
	public static final int NEW_PRICE = FPRANGE * FPRANGE;
	public static final int[] MULTABLE = new int[FPRANGE * 2];
	static {
		for (int i = -FPRANGE; i < FPRANGE; i++) {
			MULTABLE[FPRANGE + i] = i * i;
		}
	}
}

