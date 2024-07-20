/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.ImageGenerator;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.Renderer;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererView;

import java.nio.IntBuffer;
import java.util.concurrent.ThreadFactory;

public class MandelbrotImageGenerator implements ImageGenerator {
	private boolean aborted;
	private boolean opaque;
	private RendererTile tile;
	private ThreadFactory threadFactory;
	private RendererFactory renderFactory;

	public MandelbrotImageGenerator(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile, boolean opaque) {
		this.tile = tile;
		this.opaque = opaque;
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
	}

	@Override
	public IntBuffer renderImage(String script, Metadata data) {
		MandelbrotMetadata metadata = (MandelbrotMetadata) data;
		RendererSize suggestedSize = tile.getTileSize();
		int[] pixels = new int[suggestedSize.getWidth() * suggestedSize.getHeight()];
		IntBuffer buffer = IntBuffer.wrap(pixels);
		try {
			DSLParser parser = new DSLParser(DSLParser.class.getPackage().getName() + ".generated", "Compile" + System.nanoTime());
			DSLParserResult result = parser.parse(script);
			DSLCompiler compiler = new DSLCompiler();
			Orbit orbit = compiler.compileOrbit(result).create();
			Color color = compiler.compileColor(result).create();
			Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
			renderer.setOpaque(opaque);
			Double4D translation = metadata.getTranslation();
			Double4D rotation = metadata.getRotation();
			Double4D scale = metadata.getScale();
			Double2D constant = metadata.getPoint();
			Time time = metadata.getTime();
			boolean julia = metadata.isJulia();
			renderer.setOrbit(orbit);
			renderer.setColor(color);
			renderer.init();
			RendererView view = new RendererView();
			view .setTraslation(translation);
			view.setRotation(rotation);
			view.setScale(scale);
			view.setState(new Integer4D(0, 0, 0, 0));
			view.setJulia(julia);
			view.setPoint(new Number(constant.getX(), constant.getY()));
			renderer.setView(view);
			renderer.setTime(time);
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
