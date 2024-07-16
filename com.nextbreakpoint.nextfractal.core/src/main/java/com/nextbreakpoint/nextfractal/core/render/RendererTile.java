/*
 * NextFractal 2.3.0
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

/**
 * @author Andrea Medeghini
 */
public class RendererTile {
	private final RendererSize imageSize;
	private final RendererSize tileSize;
	private final RendererPoint tileOffset;
	private final RendererSize borderSize;

	/**
	 * @param imageSize
	 * @param tileSize
	 * @param tileOffset
	 * @param borderSize
	 */
	public RendererTile(final RendererSize imageSize, final RendererSize tileSize, final RendererPoint tileOffset, final RendererSize borderSize) {
		this.imageSize = imageSize;
		this.tileSize = tileSize;
		this.tileOffset = tileOffset;
		this.borderSize = borderSize;
	}

	/**
	 * @return the imageSize
	 */
	public RendererSize getImageSize() {
		return imageSize;
	}

	/**
	 * @return the borderSize
	 */
	public RendererSize getBorderSize() {
		return borderSize;
	}

	/**
	 * @return the tileOffset
	 */
	public RendererPoint getTileOffset() {
		return tileOffset;
	}

	/**
	 * @return the tileSize
	 */
	public RendererSize getTileSize() {
		return tileSize;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(imageSize.toString());
		builder.append(", ");
		builder.append(tileSize.toString());
		builder.append(", ");
		builder.append(tileOffset.toString());
		builder.append(", ");
		builder.append(borderSize.toString());
		return builder.toString();
	}
}
