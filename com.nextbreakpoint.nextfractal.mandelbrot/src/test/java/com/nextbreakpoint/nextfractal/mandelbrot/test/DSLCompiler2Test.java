package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class DSLCompiler2Test extends BaseTest {
	@Test
	public void Compiler1() throws IOException {
		try {
			final DSLParser parser = new DSLParser(DSLParser.getPackageName(), DSLParser.getClassName());
			DSLParserResult result = parser.parse(getSource("/source2.m"));
			assertThat(result).isNotNull();
		} catch (DSLParserException e) {
			printErrors(e.getErrors());
			assertThat(e.getErrors()).hasSize(1);
		}
	}
}
