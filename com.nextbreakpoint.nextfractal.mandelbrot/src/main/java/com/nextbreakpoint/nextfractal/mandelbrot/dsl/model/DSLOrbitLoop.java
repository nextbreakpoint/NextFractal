/*
 * NextFractal 2.3.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
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
