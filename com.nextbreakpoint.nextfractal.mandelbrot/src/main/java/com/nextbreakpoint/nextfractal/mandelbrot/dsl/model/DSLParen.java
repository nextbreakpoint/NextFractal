package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;

import java.util.Map;

public class DSLParen extends DSLExpression {
    private final DSLExpression exp;

    public DSLParen(DSLToken token, DSLExpression exp) {
        super(token);
        this.exp = exp;
    }

    @Override
    public double evaluateReal(DSLInterpreterContext context, Map<String, Variable> scope) {
        return exp.evaluateReal(context, scope);
    }

    @Override
    public ComplexNumber evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
        return exp.evaluate(context, scope);
    }

    @Override
    public boolean isReal() {
        return exp.isReal();
    }

    public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
        context.append("(");
        exp.compile(context, scope);
        context.append(")");
    }
}
