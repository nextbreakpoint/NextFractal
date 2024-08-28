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

import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.core.PaletteExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DSLPaletteElement extends DSLObject {
    private final float[] beginColor;
    private final float[] endColor;
    private final int steps;
    private final DSLExpression exp;

    public DSLPaletteElement(
            DSLToken token,
            float[] beginColor,
            float[] endColor,
            int steps,
            DSLExpression exp
    ) {
        super(token);
        this.beginColor = beginColor;
        this.endColor = endColor;
        this.steps = steps;
        this.exp = exp;
    }

    public PaletteElement evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
        return new PaletteElement(beginColor, endColor, steps, getPaletteExpression(context, new HashMap<>(scope)));
    }

    private PaletteExpression getPaletteExpression(DSLInterpreterContext context, Map<String, Variable> scope) {
        if (exp != null) {
            return step -> {
                final Variable variable = new Variable("s", true);
                variable.setValue(step);
                scope.put(variable.getName(), variable);
                return exp.evaluateReal(context, scope);
            };
        } else {
            return step -> step;
        }
    }

    public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
        context.append("element(");
        context.append(createArray(beginColor));
        context.append(",");
        context.append(createArray(endColor));
        context.append(",");
        context.append(steps);
        context.append(",s -> { return ");
        if (exp != null) {
            if (exp.isReal()) {
                exp.compile(context, scope);
            } else {
                throw new DSLException("Invalid expression type: " + exp.token.getText(), exp.token);
            }
        } else {
            context.append("s");
        }
        context.append(";})");
    }

    private String createArray(float[] components) {
        return "new float[] {" + components[0] + "f," + components[1] + "f," + components[2] + "f," + components[3] + "f}";
    }
}
