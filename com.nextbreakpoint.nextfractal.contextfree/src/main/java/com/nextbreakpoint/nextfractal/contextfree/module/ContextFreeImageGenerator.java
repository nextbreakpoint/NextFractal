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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.contextfree.graphics.Renderer;
import com.nextbreakpoint.nextfractal.core.common.ImageGenerator;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class ContextFreeImageGenerator implements ImageGenerator {
	private boolean aborted;
	private final boolean opaque;
	private final Tile tile;
	private final ThreadFactory threadFactory;
	private final GraphicsFactory renderFactory;

	public ContextFreeImageGenerator(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile, boolean opaque) {
		this.tile = tile;
		this.opaque = opaque;
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
	}

	@Override
	public IntBuffer renderImage(String script, Metadata data) {
		final ContextFreeMetadata metadata = (ContextFreeMetadata)data;
		final Size suggestedSize = tile.tileSize();
		final int[] pixels = new int[suggestedSize.width() * suggestedSize.height()];
        Arrays.fill(pixels, 0xFF000000);
		final IntBuffer buffer = IntBuffer.wrap(pixels);
		try {
			final CFParser parser = new CFParser();
			final CFParserResult parserResult = parser.parse(script);
			final CFDGImage cfdgImage = parserResult.classFactory().create();
			final Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
			renderer.setImage(cfdgImage, metadata.getSeed());
			renderer.setOpaque(opaque);
			renderer.init();
			renderer.runTask();
			renderer.waitForTask();
			renderer.getPixels(pixels);
			if (renderer.getProgress() != 1) {
				aborted = true;
				return buffer;
			}
		} catch (Throwable e) {
			log.log(Level.WARNING, "Can't render image", e);
			aborted = true;
		}
		return buffer;
	}

	@Override
	public Size getSize() {
		return tile.tileSize();
	}
	
	@Override
	public boolean isAborted() {
		return aborted;
	}
}
