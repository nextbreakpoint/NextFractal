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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.ImageGenerator;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Renderer;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.View;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class MandelbrotImageGenerator implements ImageGenerator {
	private boolean aborted;
	private final boolean opaque;
	private final Tile tile;
	private final ThreadFactory threadFactory;
	private final GraphicsFactory renderFactory;

	public MandelbrotImageGenerator(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile, boolean opaque) {
		this.tile = tile;
		this.opaque = opaque;
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
	}

	@Override
	public IntBuffer renderImage(String script, Metadata data) {
		MandelbrotMetadata metadata = (MandelbrotMetadata) data;
		Size suggestedSize = tile.tileSize();
		int[] pixels = new int[suggestedSize.width() * suggestedSize.height()];
		Arrays.fill(pixels, 0xFF000000);
		IntBuffer buffer = IntBuffer.wrap(pixels);
		try {
			final DSLParser parser = new DSLParser(DSLParser.getPackageName(), DSLParser.getClassName());
			final DSLParserResult parserResult = parser.parse(script);
			Orbit orbit = parserResult.orbitClassFactory().create();
			Color color = parserResult.colorClassFactory().create();
			Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
			renderer.setOpaque(opaque);
			Double4D translation = metadata.getTranslation();
			Double4D rotation = metadata.getRotation();
			Double4D scale = metadata.getScale();
			Double2D constant = metadata.getPoint();
			Time time = metadata.time();
			boolean julia = metadata.isJulia();
			renderer.setOrbit(orbit);
			renderer.setColor(color);
			renderer.init();
			View view = new View();
			view .setTranslation(translation);
			view.setRotation(rotation);
			view.setScale(scale);
			view.setState(new Integer4D(0, 0, 0, 0));
			view.setJulia(julia);
			view.setPoint(new ComplexNumber(constant.x(), constant.y()));
			renderer.setView(view);
			renderer.setTime(time);
			renderer.runTask();
			renderer.waitForTask();
			if (renderer.getProgress() != 1) {
				aborted = true;
				return buffer;
			}
			renderer.getPixels(pixels);
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
