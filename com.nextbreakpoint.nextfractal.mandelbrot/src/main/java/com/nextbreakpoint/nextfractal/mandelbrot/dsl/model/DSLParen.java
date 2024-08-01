package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import org.antlr.v4.runtime.Token;

import java.util.Map;

public class DSLParen extends DSLExpression {
    private final DSLExpression exp;

    public DSLParen(Token location, DSLExpression exp, int numberIndex) {
        super(numberIndex, location);
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
