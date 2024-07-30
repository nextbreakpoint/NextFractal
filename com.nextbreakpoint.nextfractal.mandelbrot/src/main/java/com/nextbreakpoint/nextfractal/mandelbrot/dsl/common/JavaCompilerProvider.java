package com.nextbreakpoint.nextfractal.mandelbrot.dsl.common;

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
