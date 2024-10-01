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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics;

import com.nextbreakpoint.nextfractal.core.common.RendererDelegate;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.xaos.XaosRenderer;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class Coordinator {
	public static final String KEY_TYPE = "TYPE";
	public static final Integer VALUE_REALTIME = 1;
	public static final String KEY_PROGRESS = "PROGRESS";
	public static final Integer VALUE_SINGLE_PASS = 1;
	public static final String KEY_MULTITHREAD = "MULTITHREAD";
	public static final Integer VALUE_SINGLE_THREAD = 1;

	private final HashMap<String, Integer> hints = new HashMap<>();
	private final ThreadFactory threadFactory;
	private final GraphicsFactory renderFactory;
	@Getter
	private volatile float progress;
	@Getter
	private volatile List<ScriptError> errors;
	private volatile boolean imageChanged;
	private Renderer renderer;
	@Setter
	private volatile RendererDelegate delegate;

	public Coordinator(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile, Map<String, Integer> hints) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.hints.putAll(hints);
		renderer = createRenderer(tile);
		renderer.setDelegate(this::onImageUpdated);
		renderer.setMultiThread(true);
		errors = new ArrayList<>();
		if (hints.get(KEY_PROGRESS) != null && hints.get(KEY_PROGRESS) == VALUE_SINGLE_PASS) {
			renderer.setSinglePass(true);
		}
		if (hints.get(KEY_MULTITHREAD) != null && hints.get(KEY_MULTITHREAD) == VALUE_SINGLE_THREAD) {
			renderer.setMultiThread(false);
		}
	}

	public final void dispose() {
		free();
	}
	
	public void abort() {
		renderer.abortTask();
	}
	
	public void waitFor() {
		renderer.waitForTask();
	}
	
	public void run() {
		renderer.runTask();
	}

	public boolean hasImageChanged() {
		boolean result = imageChanged;
		imageChanged = false;
		return result;
	}

	public Size getSize() {
		return renderer.getSize();
	}

	public void setPoint(ComplexNumber point) {
		renderer.setPoint(point);
	}

	public void setTime(Time time) {
		renderer.setTime(time);
	}

	public void setJulia(final boolean julia) {
		renderer.setJulia(julia);
	}

	public void setOrbitAndColor(Orbit orbit, Color color) {
		renderer.setOrbit(orbit);
		renderer.setColor(color);
	}

	public void setColor(Color color) {
		renderer.setColor(color);
	}

	public void init() {
		renderer.init();
	}

	public boolean isTileSupported() {
		return true;
	}

	public ComplexNumber getInitialCenter() {
		return renderer.getInitialRegion().getCenter();
	}

	public ComplexNumber getInitialSize() {
		return renderer.getInitialRegion().getSize();
	}

	public void setView(View view) {
		renderer.setView(view);
	}

	public void drawImage(final GraphicsContext gc, final int x, final int y) {
		renderer.drawImage(gc, x, y);
	}

//	public void drawImage(final GraphicsContext gc, final int x, final int y, final int w, final int h) {
//		renderer.drawImage(gc, x, y, w, h);
//	}

	public List<Trap> getTraps() {
		return renderer.getTraps();
	}

	public boolean isInitialized() {
		return renderer.isInitialized();
	}

	protected void onImageUpdated(float progress, List<ScriptError> errors) {
		this.progress = progress;
		this.errors = errors;
		this.imageChanged = true;
		if (delegate != null) {
			delegate.onImageUpdated(progress, errors);
		}
	}

	protected void free() {
		if (renderer != null) {
			renderer.dispose();
			renderer = null;
		}
	}

	protected Renderer createRenderer(Tile tile) {
		Integer type = hints.get(KEY_TYPE);
		if (type != null && type.equals(VALUE_REALTIME)) {
			return new XaosRenderer(threadFactory, renderFactory, tile);
		} else {
			return new Renderer(threadFactory, renderFactory, tile);
		}
	}

	public boolean isInterrupted() {
		return renderer.isInterrupted();
	}
}
