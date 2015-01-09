package com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.strategy;

import com.nextbreakpoint.nextfractal.flux.core.Colors;
import com.nextbreakpoint.nextfractal.flux.mandelbrot.MandelbrotFractal;
import com.nextbreakpoint.nextfractal.flux.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererPoint;
import com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy;

public class MandelbrotRendererStrategy implements RendererStrategy {
		private MandelbrotFractal rendererFractal;

		public MandelbrotRendererStrategy(MandelbrotFractal rendererFractal) {
			this.rendererFractal = rendererFractal;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#prepare()
		 */
		@Override
		public void prepare() {
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#renderPoint(com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererPoint.renderer.RenderedPoint)
		 */
		@Override
		public int renderPoint(RendererPoint p, Number x, Number w) {
			Number[] state = rendererFractal.renderOrbit(x, w);
			for (int i = 0; i < p.size(); i++) {
				p.vars()[i].set(state[i]);
			}
			return renderColor(p);
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#renderColor(com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererPoint)
		 */
		@Override
		public int renderColor(RendererPoint p) {
			return Colors.color(rendererFractal.renderColor(p.vars()));
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#isSolidGuessSupported()
		 */
		@Override
		public boolean isSolidGuessSupported() {
			return rendererFractal.isSolidGuessSupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#isVerticalSymetrySupported()
		 */
		@Override
		public boolean isVerticalSymetrySupported() {
			return rendererFractal.isVerticalSymetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#isHorizontalSymetrySupported()
		 */
		@Override
		public boolean isHorizontalSymetrySupported() {
			return rendererFractal.isHorizontalSymetrySupported();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#getVerticalSymetryPoint()
		 */
		@Override
		public double getVerticalSymetryPoint() {
			return rendererFractal.getVerticalSymetryPoint();
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#getHorizontalSymetryPoint()
		 */
		@Override
		public double getHorizontalSymetryPoint() {
			return rendererFractal.getHorizontalSymetryPoint();
		}
	}