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

import com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererData;

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

class XaosRendererData extends RendererData {
	private final int[] position = new int[XaosConstants.STEPS];
	private final int[] offset = new int[XaosConstants.STEPS];
	private XaosRealloc[] reallocX;
	private XaosRealloc[] reallocY;
	private XaosDynamic dynamicX;
	private XaosDynamic dynamicY;
	private XaosChunkTable moveTable;
	private XaosChunkTable fillTable;
	private XaosRealloc[] queue;

	@Override
	public void free() {
		reallocX = null;
		reallocY = null;
		dynamicX = null;
		dynamicY = null;
		moveTable = null;
		fillTable = null;
		queue = null;
		super.free();
	}

	@Override
	protected void realloc(final int width, final int height) {
		super.realloc(width, height);
		reallocX = new XaosRealloc[width];
		reallocY = new XaosRealloc[height];
		dynamicX = new XaosDynamic(width);
		dynamicY = new XaosDynamic(height);
		moveTable = new XaosChunkTable(width);
		fillTable = new XaosChunkTable(width);
		queue = new XaosRealloc[reallocX.length + reallocY.length];
		for (int i = 0; i < width; i++) {
			reallocX[i] = new XaosRealloc(false);
			reallocX[i].pos = i;
		}
		for (int i = 0; i < height; i++) {
			reallocY[i] = new XaosRealloc(true);
			reallocY[i].pos = i;
		}
	}

	public XaosRealloc[] reallocX() {
		return reallocX;
	}

	public XaosRealloc[] reallocY() {
		return reallocY;
	}

	public XaosDynamic dynamicX() {
		return dynamicX;
	}

	public XaosDynamic dynamicY() {
		return dynamicY;
	}

	public XaosChunkTable moveTable() {
		return moveTable;
	}

	public XaosChunkTable fillTable() {
		return fillTable;
	}

	public XaosRealloc[] queue() {
		return queue;
	}

	public int[] position() {
		return position;
	}

	public int[] offset() {
		return offset;
	}
}
