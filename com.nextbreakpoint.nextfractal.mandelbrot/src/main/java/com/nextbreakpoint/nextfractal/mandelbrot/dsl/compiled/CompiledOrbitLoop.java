package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
public class CompiledOrbitLoop {
    private final Collection<CompiledStatement> statements;
    @Setter
    private CompiledCondition condition;
    @Setter
    private int begin;
    @Setter
    private int end;

    public CompiledOrbitLoop(Collection<CompiledStatement> statements) {
        this.statements = statements;
    }
}
