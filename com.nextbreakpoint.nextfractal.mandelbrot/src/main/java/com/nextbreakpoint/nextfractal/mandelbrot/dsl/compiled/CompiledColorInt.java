package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import lombok.Getter;

import java.util.List;

@Getter
public class CompiledColorInt {
    private final List<CompiledStatement> statements;

    public CompiledColorInt(List<CompiledStatement> statements) {
        this.statements = statements;
    }
}
