package com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer;

import com.nextbreakpoint.nextfractal.flux.mandelbrot.Number;

class MandelbrotRendererStrategy implements RendererStrategy {
		private RendererFractal rendererFractal;

		public MandelbrotRendererStrategy(RendererFractal rendererFractal) {
			this.rendererFractal = rendererFractal;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#prepare()
		 */
		@Override
		public void prepare() {
//			if (fractalRuntime.getRenderingFormula().getFormulaRuntime() != null) {
//				renderedData.x0 = fractalRuntime.getRenderingFormula().getFormulaRuntime().getInitialPoint().r;
//				renderedData.y0 = fractalRuntime.getRenderingFormula().getFormulaRuntime().getInitialPoint().i;
//			}
//			else {
//				renderedData.x0 = 0;
//				renderedData.y0 = 0;
//			}
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#renderPoint(com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererPoint.renderer.RenderedPoint)
		 */
		@Override
		public int renderPoint(RendererPoint p, Number x, Number w) {
			return 0;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#renderColor(com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererPoint)
		 */
		@Override
		public int renderColor(RendererPoint p) {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#isSolidGuessSupported()
		 */
		@Override
		public boolean isSolidGuessSupported() {
			// TODO Auto-generated method stub
			return false;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#isVerticalSymetrySupported()
		 */
		@Override
		public boolean isVerticalSymetrySupported() {
			return false;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy.renderer.AbstractMandelbrotRenderer.RenderingStrategy#isHorizontalSymetrySupported()
		 */
		@Override
		public boolean isHorizontalSymetrySupported() {
			return false;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#getVerticalSymetryPoint()
		 */
		@Override
		public double getVerticalSymetryPoint() {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 * @see com.nextbreakpoint.nextfractal.flux.mandelbrot.renderer.RendererStrategy#getHorizontalSymetryPoint()
		 */
		@Override
		public double getHorizontalSymetryPoint() {
			// TODO Auto-generated method stub
			return 0;
		}
	}