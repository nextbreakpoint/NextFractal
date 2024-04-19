/*
 * NextFractal 2.1.5
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

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.contextfree.renderer.Renderer;
import com.nextbreakpoint.nextfractal.core.common.ImageGenerator;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;

public class ContextFreeImageGenerator implements ImageGenerator {
	private boolean aborted;
	private boolean opaque;
	private RendererTile tile;
	private ThreadFactory threadFactory;
	private RendererFactory renderFactory;

	public ContextFreeImageGenerator(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile, boolean opaque) {
		this.tile = tile;
		this.opaque = opaque;
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
	}

	@Override
	public IntBuffer renderImage(String script, Metadata data) {
		ContextFreeMetadata metadata = (ContextFreeMetadata)data;
		RendererSize suggestedSize = tile.getTileSize();
		int[] pixels = new int[suggestedSize.getWidth() * suggestedSize.getHeight()];
		for (int i = 0; i < pixels.length; i++) pixels[i] = 0xFF000000;
		IntBuffer buffer = IntBuffer.wrap(pixels);
		try {
			DSLParser parser = new DSLParser();
			DSLParserResult report = parser.parse(script);
			DSLCompiler compiler = new DSLCompiler();
			CFDGInterpreter interpreter = compiler.compile(report);
			Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
			renderer.setInterpreter(interpreter);
			renderer.setSeed(metadata.getSeed());
			renderer.setOpaque(opaque);
			renderer.init();
			renderer.runTask();
			renderer.waitForTasks();
			renderer.getPixels(pixels);
			aborted = renderer.isInterrupted();
		} catch (Exception e) {
			//TODO display errors
		}
		return buffer;
	}

	@Override
	public RendererSize getSize() {
		return tile.getTileSize();
	}
	
	@Override
	public boolean isInterrupted() {
		return aborted;
	}
}
