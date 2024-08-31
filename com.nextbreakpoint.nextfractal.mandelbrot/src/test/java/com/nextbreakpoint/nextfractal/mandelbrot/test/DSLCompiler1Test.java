package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Scope;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
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
			final DSLParser parser = new DSLParser(DSLParser.getPackageName(), DSLParser.getClassName());
			final DSLParserResult parserResult = parser.parse(getSource("/source1.m"));
			assertThat(parserResult.orbitDSL()).isNotNull();
			System.out.println(parserResult.orbitDSL());
			assertThat(parserResult.colorDSL()).isNotNull();
			System.out.println(parserResult.colorDSL());
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
			System.out.printf("%f,%f,%f,%f%n", c[0], c[1], c[2], c[3]);
			ComplexNumber z = orbit.getVariable(0);
			assertThat(z).isNotNull();
			System.out.printf("%f,%f%n", z.r(), z.i());
		} catch (DSLParserException e) {
			printErrors(e.getErrors());
			e.printStackTrace();
			fail(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
