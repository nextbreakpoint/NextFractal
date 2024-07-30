package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import lombok.Getter;

import java.util.Collection;

@Getter
public class CompiledOrbitBegin {
    private final Collection<CompiledStatement> statements;

    public CompiledOrbitBegin(Collection<CompiledStatement> statements) {
        this.statements = statements;
    }
}
