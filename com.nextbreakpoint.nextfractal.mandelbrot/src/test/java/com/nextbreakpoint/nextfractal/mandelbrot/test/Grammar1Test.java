package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class Grammar1Test extends BaseTest {
	@Test
	public void Grammar1() {
		try {
			ASTFractal fractal = parse(getSource("/source1.m"));
			System.out.println(fractal);
			assertThat(fractal).isNotNull();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
