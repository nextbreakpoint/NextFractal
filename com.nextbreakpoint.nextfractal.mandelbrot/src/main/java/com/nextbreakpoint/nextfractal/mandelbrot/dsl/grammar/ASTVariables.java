package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;

import java.util.Collection;

public class ASTVariables {
    private final ASTScope orbitVariables;
    private final ASTScope colorVariables;
    private final ASTScope stateVariables;

    public ASTVariables(ASTScope orbitVariables, ASTScope colorVariables, ASTScope stateVariables) {
        this.orbitVariables = orbitVariables;
        this.colorVariables = colorVariables;
        this.stateVariables = stateVariables;
    }

    public Collection<Variable> getOrbitVariables() {
        return orbitVariables.values();
    }

    public Collection<Variable> getColorVariables() {
        return colorVariables.values();
    }

    public Collection<Variable> getStateVariables() {
        return stateVariables.values();
    }
}
