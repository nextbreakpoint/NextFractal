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
import com.nextbreakpoint.nextfractal.mandelbrot.core.CompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DSLCompilerTest1 extends BaseTest {
	@Test
	public void Compiler1() {
		try {
//			assertThat(Pattern.matches("([A-Z][a-z]*)-(\\d).(.jpg|.png)", "Andrea-10.png")).isTrue();
			DSLParser parser = new DSLParser("test", "Compile");
			DSLParserResult result = parser.parse(getSource("/source1.m"));
			assertThat(result.getErrors()).isEmpty();
			assertThat(result.getAST()).isNotNull();
			System.out.println(result.getAST());
			assertThat(result.getOrbitSource()).isNotNull();
			System.out.println(result.getOrbitSource());
			assertThat(result.getColorSource()).isNotNull();
			System.out.println(result.getColorSource());
			DSLCompiler compiler = new DSLCompiler();
			Orbit orbit = compiler.compileOrbit(result).create();
			Color color = compiler.compileColor(result).create();
			assertThat(orbit).isNotNull();
			assertThat(color).isNotNull();
			Scope scope = new Scope();
			orbit.setScope(scope);
			color.setScope(scope);
			orbit.init();
			orbit.setX(new Number(0, 0));
			orbit.setW(new Number(0, 0));
			orbit.render(null);
			float[] c = color.getColor();
			assertThat(c).isNotNull();
			System.out.println(String.format("%f,%f,%f,%f", c[0], c[1], c[2], c[3]));
			Number z = orbit.getVariable(0);
			assertThat(z).isNotNull();
			System.out.println(String.format("%f,%f", z.r(), z.i()));
		} catch (CompilerException e) {
			printErrors(e.getErrors());
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ParserException e) {
			printErrors(e.getErrors());
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
