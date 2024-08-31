package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Grammar1Test extends BaseTest {
	@Test
	public void Grammar1() throws Exception {
		ASTFractal fractal = parse(getSource("/source1.m"));
		System.out.println(fractal);
		assertThat(fractal).isNotNull();
	}
}
