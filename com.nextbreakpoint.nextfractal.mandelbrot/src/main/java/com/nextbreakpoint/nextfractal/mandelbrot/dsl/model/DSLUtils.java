package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.Token;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DSLUtils {
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
            throw new DSLException("Invalid expression type: " + exp1.location.getText(), exp1.location);
        }
        if (!exp2.isReal()) {
            throw new DSLException("Invalid expression type: " + exp2.location.getText(), exp2.location);
        }
        context.append("(");
        exp1.compile(context, scope);
        context.append(op);
        exp2.compile(context, scope);
        context.append(")");
    }

    public static void compileRealMathOperator(DSLCompilerContext context, Map<String, VariableDeclaration> scope, String op, DSLExpression exp1, DSLExpression exp2) {
        if (!exp1.isReal()) {
            throw new DSLException("Invalid expression type: " + exp1.location.getText(), exp1.location);
        }
        if (!exp2.isReal()) {
            throw new DSLException("Invalid expression type: " + exp2.location.getText(), exp2.location);
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

    public static void compileRealFunctionOneArgument(Token location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 1) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        if (!arguments[0].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[0].location.getText(), arguments[0].location);
        }
        arguments[0].compile(context, scope);
        context.append(")");
    }

    public static void compileRealFunctionTwoArguments(Token location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 2) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        if (!arguments[0].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[0].location.getText(), arguments[0].location);
        }
        if (!arguments[1].isReal()) {
            throw new DSLException("Invalid type of arguments: " + arguments[1].location.getText(), arguments[1].location);
        }
        arguments[0].compile(context, scope);
        context.append(",");
        arguments[1].compile(context, scope);
        context.append(")");
    }

    public static void compileComplexFunctionOneArgument(Token location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments, int index) {
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

    public static void compileComplexFunctionOneComplexArgument(Token location, DSLCompilerContext context, Map<String, VariableDeclaration> scope, String func, DSLExpression[] arguments) {
        context.append(func);
        context.append("(");
        if (arguments.length != 1) {
            throw new DSLException("Invalid number of arguments: " + location.getText(), location);
        }
        arguments[0].compile(context, scope);
        context.append(")");
    }
}
