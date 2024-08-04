package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

public class DSLOrbitLoop extends DSLObject {
    private final Collection<DSLStatement> statements;
    private final Collection<VariableDeclaration> stateVariables;
    @Getter
    private final DSLCondition condition;
    @Getter
    private final int begin;
    @Getter
    private final int end;

    public DSLOrbitLoop(DSLToken token, DSLCondition condition, int begin, int end, Collection<DSLStatement> statements, Collection<VariableDeclaration> stateVariables) {
        super(token);
        this.condition = condition;
        this.begin = begin;
        this.end = end;
        this.statements = statements;
        this.stateVariables = stateVariables;
    }

    public boolean evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
        for (DSLStatement statement : statements) {
            if (statement.evaluate(context, scope)) {
                return true;
            }
        }
        return false;
    }

    public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
        context.append("n = ");
        context.append(begin);
        context.append(";\n");
        context.append("if (states != null) {\n");
        context.append("states.add(new ComplexNumber[] { ");
        int i = 0;
        for (VariableDeclaration var : stateVariables) {
            if (i > 0) {
                context.append(", ");
            }
            context.append("number(");
            context.append(var.name());
            context.append(")");
            i += 1;
        }
        context.append(" });\n");
        context.append("}\n");
        context.append("for (int i = ");
        context.append(begin);
        context.append(" + 1; i <= ");
        context.append(end);
        context.append("; i++) {\n");
        for (DSLStatement statement : statements) {
            statement.compile(context, scope);
        }
        context.append("if (");
        condition.compile(context, scope);
        context.append(") { n = i; break; }\n");
        context.append("if (states != null) {\n");
        context.append("states.add(new ComplexNumber[] { ");
        i = 0;
        for (VariableDeclaration var : stateVariables) {
            if (i > 0) {
                context.append(", ");
            }
            context.append("number(");
            context.append(var.name());
            context.append(")");
            i += 1;
        }
        context.append(" });\n");
        context.append("}\n");
        context.append("}\n");
        context.append("if (states != null) {\n");
        context.append("states.add(new ComplexNumber[] { ");
        i = 0;
        for (VariableDeclaration var : stateVariables) {
            if (i > 0) {
                context.append(", ");
            }
            context.append("number(");
            context.append(var.name());
            context.append(")");
            i += 1;
        }
        context.append(" });\n");
        context.append("}\n");
    }
}
