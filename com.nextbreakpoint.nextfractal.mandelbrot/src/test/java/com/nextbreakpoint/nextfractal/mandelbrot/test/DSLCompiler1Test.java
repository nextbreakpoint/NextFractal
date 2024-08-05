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
package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DSLCompiler1Test extends BaseTest {
	@Test
	public void Compiler1() {
		try {
//			assertThat(Pattern.matches("([A-Z][a-z]*)-(\\d).(.jpg|.png)", "Andrea-10.png")).isTrue();
			final DSLCompiler compiler = new DSLCompiler(DSLParser.getPackageName(), DSLParser.getClassName());
			final DSLParser parser = new DSLParser(compiler);
			final DSLExpressionContext expressionContext = new DSLExpressionContext();
			final DSLParserResult result = parser.parse(expressionContext, getSource("/source1.m"));
			assertThat(result.fractal()).isNotNull();
			System.out.println(result.fractal());
//			assertThat(result.orbitJavaSource()).isNotNull();
//			System.out.println(result.orbitJavaSource());
//			assertThat(result.colorJavaSource()).isNotNull();
//			System.out.println(result.colorJavaSource());
			final DSLParserResult parserResult = compiler.compile(expressionContext, result);
			Orbit orbit = parserResult.orbitClassFactory().create();
			Color color = parserResult.colorClassFactory().create();
			assertThat(orbit).isNotNull();
			assertThat(color).isNotNull();
			Scope scope = new Scope();
			orbit.setScope(scope);
			color.setScope(scope);
			orbit.init();
			orbit.setX(new ComplexNumber(0, 0));
			orbit.setW(new ComplexNumber(0.1, 1.9));
			final List<ComplexNumber[]> states = new ArrayList<>();
			orbit.render(states);
			states.stream().map(Arrays::toString).forEach(System.out::println);
			color.init();
			color.render();
			float[] c = color.getColor();
			assertThat(c).isNotNull();
			System.out.println(String.format("%f,%f,%f,%f", c[0], c[1], c[2], c[3]));
			ComplexNumber z = orbit.getVariable(0);
			assertThat(z).isNotNull();
			System.out.println(String.format("%f,%f", z.r(), z.i()));
		} catch (DSLException e) {
			printErrors(e.getErrors());
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
