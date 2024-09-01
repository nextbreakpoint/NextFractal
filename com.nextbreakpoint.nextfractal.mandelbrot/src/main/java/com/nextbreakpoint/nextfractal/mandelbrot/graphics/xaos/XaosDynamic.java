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

class XaosDynamic {
	int[] delta;
	XaosPrice[] oldBest;
	XaosPrice[] newBest;
	XaosPrice[] calData;
	XaosPrice[] conData;

	public XaosDynamic(final int size) {
		delta = new int[size + 1];
		oldBest = new XaosPrice[size];
		newBest = new XaosPrice[size];
		calData = new XaosPrice[size];
		conData = new XaosPrice[size << XaosConstants.DSIZE];
		for (int i = 0; i < size; i++) {
			calData[i] = new XaosPrice();
		}
		for (int i = 0; i < (size << XaosConstants.DSIZE); i++) {
			conData[i] = new XaosPrice();
		}
	}

	public void dispose() {
		oldBest = null;
		newBest = null;
		calData = null;
		conData = null;
	}

	public void swap() {
		final XaosPrice[] tmp_best = newBest;
		newBest = oldBest;
		oldBest = tmp_best;
	}
}
