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
package com.nextbreakpoint.nextfractal.mandelbrot.renderer.strategy;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererState;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy;

public class MandelbrotRendererStrategy implements RendererStrategy {
		private RendererFractal rendererFractal;

		public MandelbrotRendererStrategy(RendererFractal rendererFractal) {
			this.rendererFractal = rendererFractal;
		}

		@Override
		public void prepare() {
			rendererFractal.setPoint(rendererFractal.getOrbit().getInitialPoint());
			rendererFractal.getOrbit().setJulia(false);
			rendererFractal.getColor().setJulia(false);
		}

		@Override
		public int renderPoint(RendererState p, Number x, Number w) {
			rendererFractal.renderOrbit(p.vars(), x, w);
			return renderColor(p);
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#renderColor(com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererState)
		 */
		@Override
		public int renderColor(RendererState p) {
			return Colors.color(rendererFractal.renderColor(p.vars()));
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#isSolidGuessSupported()
		 */
		@Override
		public boolean isSolidGuessSupported() {
			return rendererFractal.isSolidGuessSupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#isVerticalSymetrySupported()
		 */
		@Override
		public boolean isVerticalSymetrySupported() {
			return rendererFractal.isVerticalSymetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#isHorizontalSymetrySupported()
		 */
		@Override
		public boolean isHorizontalSymetrySupported() {
			return rendererFractal.isHorizontalSymetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#getVerticalSymetryPoint()
		 */
		@Override
		public double getVerticalSymetryPoint() {
			return rendererFractal.getVerticalSymetryPoint();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererStrategy#getHorizontalSymetryPoint()
		 */
		@Override
		public double getHorizontalSymetryPoint() {
			return rendererFractal.getHorizontalSymetryPoint();
		}
	}
