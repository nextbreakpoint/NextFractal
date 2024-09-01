package com.nextbreakpoint.nextfractal.contextfree.test;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.SimpleCanvas;
import org.junit.jupiter.api.Test;

import java.awt.geom.AffineTransform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class V3ShapeTest extends BaseTest {
	public static final String RESOURCE_NAME = "/v3-shape-single-rule.cfdg";

	@Test
	public void shouldParseSource() {
		CFDG cfdg = parseSource(RESOURCE_NAME);
		assertThat(cfdg).isNotNull();
	}

	@Test
	public void shouldHaveTwoShapes() {
		CFDG cfdg = parseSource(RESOURCE_NAME);
		assertThat(cfdg.tryEncodeShapeName("Foo")).isEqualTo(14);
		assertThat(cfdg.tryEncodeShapeName("CIRCLE")).isEqualTo(0);
		assertThat(cfdg.tryEncodeShapeName("SQUARE")).isEqualTo(1);
		assertThat(cfdg.tryEncodeShapeName("TRIANGLE")).isEqualTo(2);
	}

	@Test
	public void shouldReloadRules() {
		CFDG cfdg = parseSource(RESOURCE_NAME);
		assertThat(cfdg.getContents().getBody()).hasSize(12);
		assertThat(cfdg.numRules()).isEqualTo(1);
		assertThat(cfdg.getRule(0).getWeight()).isEqualTo(1.0);
		cfdg.rulesLoaded();
		assertThat(cfdg.getRule(0).getWeight()).isEqualTo(1.1);
	}

	@Test
	public void shouldReturnScale() {
		SimpleCanvas canvas = mock(SimpleCanvas.class);
		when(canvas.getWidth()).thenReturn(200);
		when(canvas.getHeight()).thenReturn(200);
		CFDG cfdg = parseSource(RESOURCE_NAME);
		cfdg.rulesLoaded();
		CFDGRenderer renderer = cfdg.createRenderer(200, 200, 1, 0, 0.1);
		assertThat(renderer).isNotNull();
		double scale = renderer.run(canvas, false);
		assertThat(scale).isEqualTo(198.4);
	}

	@Test
	public void shouldCallPrimitive() {
		SimpleCanvas canvas = mock(SimpleCanvas.class);
		when(canvas.getWidth()).thenReturn(200);
		when(canvas.getHeight()).thenReturn(200);
		CFDG cfdg = parseSource(RESOURCE_NAME);
		cfdg.rulesLoaded();
		CFDGRenderer renderer = cfdg.createRenderer(200, 200, 1, 0, 0.1);
		assertThat(renderer).isNotNull();
		renderer.run(canvas, false);
		AffineTransform transform = new AffineTransform();
		transform.translate(100.0, 100.0);
		transform.scale(198.4, 198.4);
		verify(canvas, times(1)).primitive(1, new double[] { 0, 0, 0, 1 }, transform, 0);
	}
}
