package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.Map;

public class DSLColorInt {
    private final Token location;
    private final List<DSLStatement> statements;

    public DSLColorInt(Token location, List<DSLStatement> statements) {
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
