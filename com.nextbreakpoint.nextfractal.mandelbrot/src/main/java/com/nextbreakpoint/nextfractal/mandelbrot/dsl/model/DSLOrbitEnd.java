package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.Map;

public class DSLOrbitEnd {
    private final Token location;
    private final Collection<DSLStatement> statements;

    public DSLOrbitEnd(Token location, Collection<DSLStatement> statements) {
        this.location = location;
        this.statements = statements;
    }

    public void evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
        for (DSLStatement statement : statements) {
            statement.evaluate(context, scope);
        }
    }

    public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
        for (DSLStatement statement : statements) {
            statement.compile(context, scope);
        }
    }
}
