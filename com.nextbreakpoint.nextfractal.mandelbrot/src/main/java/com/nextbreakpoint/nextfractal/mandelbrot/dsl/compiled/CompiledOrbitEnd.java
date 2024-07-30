package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import lombok.Getter;

import java.util.Collection;

@Getter
public class CompiledOrbitEnd {
    private final Collection<CompiledStatement> statements;

    public CompiledOrbitEnd(Collection<CompiledStatement> statements) {
        this.statements = statements;
    }
}
