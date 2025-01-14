package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.core.common.PlatformThreadFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Renderer;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class RendererTest {
	@Test
	public void testProgress() {
		PlatformThreadFactory threadFactory = new PlatformThreadFactory("Test", false, Thread.MIN_PRIORITY);
		GraphicsFactory renderFactory = GraphicsUtils.findGraphicsFactory("Java2D");
		Point tileOffset = new Point(0, 0);
		Size borderSize = new Size(0, 0);
		Size tileSize = new Size(100, 100);
		Tile tile = new Tile(tileSize, tileSize, tileOffset, borderSize);
		Renderer renderer = new Renderer(threadFactory, renderFactory, tile);
		try {
			TestOrbit orbit = new TestOrbit();
			TestColor color = new TestColor();
			Scope scope = new Scope();
			orbit.setScope(scope);
			color.setScope(scope);
			renderer.setOrbit(orbit);
			renderer.setColor(color);
			renderer.init();
			renderer.setContentRegion(renderer.getInitialRegion());
			List<Float> output = new ArrayList<>(); 
			renderer.setDelegate((progress, errors) -> {
				System.out.println(progress);
				output.add(progress);
			});
			renderer.runTask();
			renderer.waitForTask();
			float[] actual = new float[output.size()];
			for (int i = 0; i < actual.length; i++) {
				actual[i] = output.get(i);
			}
			final float[] expected = {0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f};
			for (int i = 0; i < actual.length; i++) {
				assertThat(actual[i]).isEqualTo(expected[i], Offset.offset(0.01f));
			}
		} finally {
			renderer.dispose();
		}
	}
	
	private static class TestOrbit extends Orbit {
		@Override
		public void init() {
		}

		@Override
		public void render(List<ComplexNumber[]> states) {
		}

		@Override
		protected MutableNumber[] createNumbers() {
			return new MutableNumber[1];
		}

		@Override
		public boolean useTime() {
			return false;
		}
	}
	
	private static class TestColor extends Color {
		@Override
		public void init() {
		}
		
		@Override
		public void render() {
		}

		@Override
		protected MutableNumber[] createNumbers() {
			return new MutableNumber[1];
		}

		@Override
		public boolean useTime() {
			return false;
		}
	}
}
