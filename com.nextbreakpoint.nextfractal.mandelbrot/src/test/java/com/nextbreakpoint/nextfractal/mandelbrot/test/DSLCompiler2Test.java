package com.nextbreakpoint.nextfractal.mandelbrot.test;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DSLCompiler2Test extends BaseTest {
	@Test
	public void Compiler1() {
		try {
			final DSLParser parser = new DSLParser(DSLParser.getPackageName(), DSLParser.getClassName());
			DSLParserResult result = parser.parse(getSource("/source2.m"));
			assertThat(result).isNotNull();
		} catch (DSLParserException e) {
			printErrors(e.getErrors());
			assertThat(e.getErrors()).hasSize(1);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
