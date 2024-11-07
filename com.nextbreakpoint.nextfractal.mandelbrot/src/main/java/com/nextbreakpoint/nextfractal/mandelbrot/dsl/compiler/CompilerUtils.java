/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilerUtils {
    public static void compileTrapOp(DSLCompilerContext context, String op) {
        context.append(op);
        context.append("(");
        context.append(")");
    }

    public static void compileTrapOp(DSLCompilerContext context, String op, ComplexNumber c1) {
        context.append(op);
        context.append("(");
        context.append("number(");
        context.append(c1);
        context.append(")");
        context.append(")");
    }

    public static void compileTrapOp(DSLCompilerContext context, String op, ComplexNumber c1, ComplexNumber c2) {
        context.append(op);
        context.append("(");
        context.append("number(");
        context.append(c1);
        context.append(")");
        context.append(",");
        context.append("number(");
        context.append(c2);
        context.append(")");
        context.append(")");
    }

    public static void compileTrapOp(DSLCompilerContext context, String op, ComplexNumber c1, ComplexNumber c2, ComplexNumber c3) {
        context.append(op);
        context.append("(");
        context.append("number(");
        context.append(c1);
        context.append(")");
        context.append(",");
        context.append("number(");
        context.append(c2);
        context.append(")");
        context.append(",");
        context.append("number(");
        context.append(c3);
        context.append(")");
        context.append(")");
    }

    public static void compileLogicOperator(DSLCompilerContext context, Map<String, VariableDeclaration> scope, String op, DSLCondition condition1, DSLCondition condition2) {
        context.append("(");
        condition1.compile(context, scope);
        context.append(op);
        condition2.compile(context, scope);
        context.append(")");
    }

    public static void compileCompareOperator(DSLCompilerContext context, Map<String, VariableDeclaration> scope, String op, DSLExpression exp1, DSLExpression exp2) {
        if (!exp1.isReal()) {
            throw new DSLException("Invalid expression type: " + exp1.getToken().getText(), exp1.getToken());
        }
        if (!exp2.isReal()) {
            throw new DSLException("Invalid expression type: " + exp2.getToken().getText(), exp2.getToken());
        }
        context.append("(");
        exp1.compile(context, scope);
        context.append(op);
        exp2.compile(context, scope);
        context.append(")");
    }

    public static void compileRealMathOperator(DSLCompilerContext context, Map<String, VariableDeclaration> scope, String op, DSLExpression exp1, DSLExpression exp2) {
        if (!exp1.isReal()) {
            throw new DSLException("Invalid expression type: " + exp1.getToken().getText(), exp1.getToken());
        }
        if (!exp2.isReal()) {
            throw new DSLException("Invalid expression type: " + exp2.getToken().getText(), exp2.getToken());
        }
        exp1.compile(context, scope);
        context.append(op);
        exp2.compile(context, scope);
    }

    public static void compileComplexMathOperator(DSLCompilerContext context, Map<String, VariableDeclaration> scope, String op, DSLExpression exp1, DSLExpression exp2, int index) {
        context.append(op);
        context.append("(");
        context.append("getNumber(");
        context.append(index);
        context.append("),");
        exp1.compile(context, scope);
        context.append(",");
        exp2.compile(context, scope);
        context.append(")");
    }

    public static void compileRealFunctionOneArgument(DSLToken location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 1) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        if (!arguments[0].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[0].getToken().getText(), arguments[0].getToken());
        }
        arguments[0].compile(context, scope);
        context.append(")");
    }

    public static void compileRealFunctionTwoArguments(DSLToken location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 2) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        if (!arguments[0].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[0].getToken().getText(), arguments[0].getToken());
        }
        if (!arguments[1].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[1].getToken().getText(), arguments[1].getToken());
        }
        arguments[0].compile(context, scope);
        context.append(",");
        arguments[1].compile(context, scope);
        context.append(")");
    }

    public static void compileComplexFunctionOneArgument(DSLToken location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments, int index) {
        context.append(func);
        context.append("(");
        if (arguments.length != 1) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        if (!arguments[0].isReal()) {
            context.append("getNumber(");
            context.append(index);
            context.append("),");
        }
        arguments[0].compile(context, scope);
        context.append(")");
    }

    public static void compileComplexFunctionOneComplexArgument(DSLToken location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 1) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        arguments[0].compile(context, scope);
        context.append(")");
    }
}
