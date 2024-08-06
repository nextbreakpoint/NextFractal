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
package com.nextbreakpoint.nextfractal.contextfree.graphics;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class Coordinator implements RendererDelegate {
	private final HashMap<String, Integer> hints = new HashMap<>();
	private final ThreadFactory threadFactory;
	private final GraphicsFactory renderFactory;
	private volatile boolean pixelsChanged;
	private Renderer renderer;
	
	public Coordinator(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile, Map<String, Integer> hints) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.hints.putAll(hints);
		renderer = createRenderer(tile);
		renderer.setRendererDelegate(this);
	}

	public final void dispose() {
		free();
	}
	
	public void abort() {
		renderer.abortTasks();
	}
	
	public void waitFor() {
		renderer.waitForTasks();
	}

	public void run() {
		renderer.runTask();
	}

	@Override
	public void updateImageInBackground(float progress) {
		this.pixelsChanged = true;
	}

	public boolean isPixelsChanged() {
		boolean result = pixelsChanged;
		pixelsChanged = false;
		return result;
	}

	public Size getSize() {
		return renderer.getSize();
	}

	public void setImage(CFDGImage cfdgImage) {
		renderer.setImage(cfdgImage);
	}

	public void setSeed(String cfdgSeed) {
		renderer.setSeed(cfdgSeed);
	}

	public void init() {
		renderer.init();
	}

	public boolean isTileSupported() {
		return true;
	}

	public void drawImage(final GraphicsContext gc, final int x, final int y) {
		renderer.drawImage(gc, x, y);
	}

//	public void drawImage(final RendererGraphicsContext gc, final int x, final int y, final int w, final int h) {
//		renderer.drawImage(gc, x, y, w, h);
//	}

	protected void free() {
		if (renderer != null) {
			renderer.dispose();
			renderer = null;
		}
	}

	protected Renderer createRenderer(Tile tile) {
		return new Renderer(threadFactory, renderFactory, tile);
	}

	public void getPixels(int[] pixels) {
		renderer.getPixels(pixels);
	}

	public List<ScriptError> getErrors() {
		return renderer.getErrors();
	}

    public boolean isInitialized() {
		return renderer.isInitialized();
    }
}
