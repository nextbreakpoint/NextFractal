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
package com.nextbreakpoint.nextfractal.contextfree.renderer;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class RendererCoordinator implements RendererDelegate {
	private final HashMap<String, Integer> hints = new HashMap<>();
	private final ThreadFactory threadFactory;
	private final RendererFactory renderFactory;
	private volatile boolean pixelsChanged;
	private volatile float progress;
	private Renderer renderer;
	
	/**
	 * @param threadFactory
	 * @param renderFactory
	 * @param tile
	 * @param hints
	 */
	public RendererCoordinator(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile, Map<String, Integer> hints) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.hints.putAll(hints);
		renderer = createRenderer(tile);
		renderer.setRendererDelegate(this);
	}

	/**
	 * 
	 */
	public final void dispose() {
		free();
	}
	
	/**
	 * 
	 */
	public void abort() {
		renderer.abortTasks();
	}
	
	/**
	 * 
	 */
	public void waitFor() {
		renderer.waitForTasks();
	}
	
	/**
	 * 
	 */
	public void run() {
		renderer.runTask();
	}

	/**
	 * @see com.nextbreakpoint.nextfractal.contextfree.renderer.RendererDelegate#updateImageInBackground(float)
	 */
	@Override
	public void updateImageInBackground(float progress) {
		this.progress = progress;
		this.pixelsChanged = true;
	}

	/**
	 * @return
	 */
	public boolean isPixelsChanged() {
		boolean result = pixelsChanged;
		pixelsChanged = false;
		return result;
	}

	/**
	 * @return
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * @return
	 */
	public RendererSize getSize() {
		return renderer.getSize();
	}

	/**
	 * @param cfdgInterpreter
	 */
	public void setInterpreter(CFDGInterpreter cfdgInterpreter) {
		renderer.setInterpreter(cfdgInterpreter);
	}

	/**
	 * @param cfdgSeed
	 */
	public void setSeed(String cfdgSeed) {
		renderer.setSeed(cfdgSeed);
	}

	/**
	 * 
	 */
	public void init() {
		renderer.init();
	}

	/**
	 * @return
	 */
	public boolean isTileSupported() {
		return true;
	}

	/**
	 * @param gc
	 * @param x
	 * @param y
	 */
	public void drawImage(final RendererGraphicsContext gc, final int x, final int y) {
		renderer.drawImage(gc, x, y);
	}

//	/**
//	 * @param gc
//	 * @param x
//	 * @param y
//	 * @param w
//	 * @param h
//	 */
//	public void drawImage(final RendererGraphicsContext gc, final int x, final int y, final int w, final int h) {
//		renderer.drawImage(gc, x, y, w, h);
//	}

	/**
	 * 
	 */
	protected void free() {
		if (renderer != null) {
			renderer.dispose();
			renderer = null;
		}
	}

	/**
	 * @param tile 
	 * @return
	 */
	protected Renderer createRenderer(RendererTile tile) {
		return new Renderer(threadFactory, renderFactory, tile);
	}

	/**
	 * @param pixels
	 */
	public void getPixels(int[] pixels) {
		renderer.getPixels(pixels);
	}

	/**
	 * @return
	 */
	public List<SourceError> getErrors() {
		return renderer.getErrors();
	}

    public boolean isInitialized() {
		return renderer.isInitialized();
    }
}
