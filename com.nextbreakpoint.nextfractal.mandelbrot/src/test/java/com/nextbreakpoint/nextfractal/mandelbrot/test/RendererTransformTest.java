package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Transform;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RendererTransformTest {
	@Test
	public void given_new_transform_should_be_identity() {
		Transform t = new Transform();
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_translate_should_return_translated_point() {
		Transform t = new Transform();
		t.translate(10, 20);
		double[] p = makePoint(0, 0);
		t.transform(p);
		assertThat(p[0]).isEqualTo(10, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(20, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_scale_should_return_scaled_point() {
		Transform t = new Transform();
		t.scale(10, 20);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(10, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(20, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_by_90_degrees_should_return_rotated_point() {
		Transform t = new Transform();
		t.rotate(Math.PI / 2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_by_180_degrees_should_return_rotated_point() {
		Transform t = new Transform();
		t.rotate(Math.PI);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(-1, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_should_return_rotated_point() {
		Transform t = new Transform();
		double a = Math.PI / 7;
		t.rotate(a);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(Math.cos(a)-Math.sin(a), Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(Math.sin(a)+Math.cos(a), Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_and_scale_should_return_rotated_and_scaled_point() {
		Transform t = new Transform();
		double a = Math.PI / 7;
		t.rotate(a);
		t.scale(2,2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(2*(Math.cos(a)-Math.sin(a)), Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(2*(Math.sin(a)+Math.cos(a)), Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_rotate_and_scale_and_translate_should_return_rotated_and_scaled_and_traslated_point() {
		Transform t = new Transform();
		double a = Math.PI / 2;
		t.rotate(a);
		t.scale(2,2);
		t.translate(1,1);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-4, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(4, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_translate_and_rotate_and_scale_should_return_traslated_and_rotated_and_scaled_point() {
		Transform t = new Transform();
		double a = Math.PI / 2;
		t.translate(1,1);
		t.rotate(a);
		t.scale(2,2);
		double[] p = makePoint(1, 1);
		t.transform(p);
		assertThat(p[0]).isEqualTo(-1, Offset.offset(0.00001));
		assertThat(p[1]).isEqualTo(3, Offset.offset(0.00001));
	}

	@Test
	public void given_new_transform_when_concat_should_return_translated_and_rotated_and_scaled_point() {
		Transform t = new Transform();
		double a = Math.PI / 2;
		Transform tt = Transform.newTranslate(1, 1);
		Transform rt = Transform.newRotate(a);
		Transform st = Transform.newScale(2, 2);
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
