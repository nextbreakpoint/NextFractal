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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser;

import com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

@Log
public class JavaCompilerProvider {
    @Getter
    private static final JavaCompiler javaCompiler = JavaCompilerHolder.getInstance();

    private static class JavaCompilerHolder {
        public static JavaCompiler getInstance() {
            if (Boolean.getBoolean(SystemProperties.PROPERTY_MANDELBROT_COMPILER_DISABLED)) {
                log.warning("Mandelbrot DSL compiler disabled");

                return null;
            }

            final JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();

            if (javaCompiler == null) {
                log.warning("Java compiler not found. Disabling Mandelbrot DSL compiler");
            }

            return javaCompiler;
        }
    }
}
