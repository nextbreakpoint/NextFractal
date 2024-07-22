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
package com.nextbreakpoint.nextfractal.core.render;

//TODO should it be immutable?
public class RendererSurface {
	private RendererBuffer buffer;
	private RendererAffine affine;
	private RendererSize size;
	private RendererTile tile;

	public RendererBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(RendererBuffer buffer) {
		this.buffer = buffer;
	}

	public RendererAffine getAffine() {
		return affine;
	}

	public void setAffine(RendererAffine affine) {
		this.affine = affine;
	}

	public RendererSize getSize() {
		return size;
	}

	public void setSize(RendererSize size) {
		this.size = size;
	}

	public RendererTile getTile() {
		return tile;
	}

	public void setTile(RendererTile tile) {
		this.tile = tile;
	}

	public void dispose() {
		if (buffer != null) {
			buffer.dispose();
			buffer = null;
		}
	}
}
