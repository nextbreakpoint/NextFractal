/*
 * NextFractal 2.1.1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2019 Andrea Medeghini
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

import com.nextbreakpoint.nextfractal.mandelbrot.compiler.Compiler;
import com.nextbreakpoint.nextfractal.mandelbrot.compiler.CompilerReport;
import org.junit.Assert;
import org.junit.Test;

public class CompilerTest2 extends BaseTest {
	@Test
	public void Compiler1() {
		try {
			Compiler compiler = new Compiler("test", "Compile");
			CompilerReport report = compiler.compileReport(getSource("/source2.m"));
			printErrors(report.getErrors());
			Assert.assertEquals(1, report.getErrors().size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
