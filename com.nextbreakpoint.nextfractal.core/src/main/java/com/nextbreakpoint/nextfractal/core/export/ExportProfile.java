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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import lombok.Builder;

@Builder(setterPrefix = "with", toBuilder = true)
public record ExportProfile(
	int frameWidth,
	int frameHeight,
	int tileWidth,
	int tileHeight,
	int tileOffsetX,
	int tileOffsetY,
	int borderWidth,
	int borderHeight
) {
	public RendererTile createRenderTile() {
		final RendererSize imageSize = new RendererSize(frameWidth, frameHeight);
		final RendererSize tileSize = new RendererSize(tileWidth, tileHeight);
		final RendererSize tileBorder = new RendererSize(borderWidth, borderHeight);
		final RendererPoint tileOffset = new RendererPoint(tileOffsetX, tileOffsetY);

		return new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
	}
}
