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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics.strategy;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Fractal;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.State;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy;

public class MandelbrotStrategy implements RendererStrategy {
		private final Fractal rendererFractal;

		public MandelbrotStrategy(Fractal rendererFractal) {
			this.rendererFractal = rendererFractal;
		}

		@Override
		public void prepare() {
			rendererFractal.setPoint(rendererFractal.getOrbit().getInitialPoint());
			rendererFractal.getOrbit().setJulia(false);
			rendererFractal.getColor().setJulia(false);
		}

		@Override
		public int renderPoint(State p, Number x, Number w) {
			rendererFractal.renderOrbit(p.vars(), x, w);
			return renderColor(p);
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#renderColor(State)
		 */
		@Override
		public int renderColor(State p) {
			return Colors.makeColor(rendererFractal.renderColor(p.vars()));
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#isSolidGuessSupported()
		 */
		@Override
		public boolean isSolidGuessSupported() {
			return rendererFractal.isSolidGuessSupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#isVerticalSymmetrySupported()
		 */
		@Override
		public boolean isVerticalSymmetrySupported() {
			return rendererFractal.isVerticalSymmetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#isHorizontalSymmetrySupported()
		 */
		@Override
		public boolean isHorizontalSymmetrySupported() {
			return rendererFractal.isHorizontalSymmetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#getVerticalSymmetryPoint()
		 */
		@Override
		public double getVerticalSymmetryPoint() {
			return rendererFractal.getVerticalSymmetryPoint();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererStrategy#getHorizontalSymmetryPoint()
		 */
		@Override
		public double getHorizontalSymmetryPoint() {
			return rendererFractal.getHorizontalSymmetryPoint();
		}
	}
