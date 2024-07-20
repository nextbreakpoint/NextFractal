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
package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.renderer.RendererTransform;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RendererTransformTest {
	@Test
	public void given_new_transform_should_be_identity() {
		RendererTransform t = new RendererTransform();
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_translate_should_return_traslated_point() {
		RendererTransform t = new RendererTransform();
		t.traslate(10, 20);
		double[] p = makePoint(0, 0);
		t.transform(p);
		assertThat(p[0]).isEqualTo(10, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(20, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_scale_should_return_scaled_point() {
		RendererTransform t = new RendererTransform();
		t.scale(10, 20);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(10, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(20, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_by_90_degrees_should_return_rotated_point() {
		RendererTransform t = new RendererTransform();
		t.rotate(Math.PI / 2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_by_180_degrees_should_return_rotated_point() {
		RendererTransform t = new RendererTransform();
		t.rotate(Math.PI);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(-1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_should_return_rotated_point() {
		RendererTransform t = new RendererTransform();
		double a = Math.PI / 7;
		t.rotate(a);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(Math.cos(a)-Math.sin(a), Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(Math.sin(a)+Math.cos(a), Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_and_scale_should_return_rotated_and_scaled_point() {
		RendererTransform t = new RendererTransform();
		double a = Math.PI / 7;
		t.rotate(a);
		t.scale(2,2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(2*(Math.cos(a)-Math.sin(a)), Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(2*(Math.sin(a)+Math.cos(a)), Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_and_scale_and_traslate_should_return_rotated_and_scaled_and_traslated_point() {
		RendererTransform t = new RendererTransform();
		double a = Math.PI / 2;
		t.rotate(a);
		t.scale(2,2);
		t.traslate(1,1);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-4, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(4, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_traslate_and_rotate_and_scale_should_return_traslated_and_rotated_and_scaled_point() {
		RendererTransform t = new RendererTransform();
		double a = Math.PI / 2;
		t.traslate(1,1);
		t.rotate(a);
		t.scale(2,2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(3, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_concat_should_return_traslated_and_rotated_and_scaled_point() {
		RendererTransform t = new RendererTransform();
		double a = Math.PI / 2;
		RendererTransform tt = RendererTransform.newTraslate(1, 1);
		RendererTransform rt = RendererTransform.newRotate(a);
		RendererTransform st = RendererTransform.newScale(2, 2);
		t.concat(tt);
		t.concat(rt);
		t.concat(st);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(3, Offset.offset(0.00001));
	}

	private double[] makePoint(int x, int y) {
		return new double[] { x, y };
	}
}
