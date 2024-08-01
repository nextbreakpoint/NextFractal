package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionContext;
import lombok.Getter;

import java.util.Objects;

public class DSLCompilerContext {
    private final ExpressionContext expressionContext;
    private final StringBuilder builder;
    @Getter
    private final ClassType classType;

    public DSLCompilerContext(ExpressionContext expressionContext, StringBuilder builder, ClassType classType) {
        this.expressionContext = Objects.requireNonNull(expressionContext);
        this.builder = Objects.requireNonNull(builder);
        this.classType = Objects.requireNonNull(classType);
    }

    public int newNumberIndex() {
        return expressionContext.newNumberIndex();
    }

    public int getNumberCount() {
        return expressionContext.getNumberCount();
    }

    public boolean orbitUseTime() {
        return expressionContext.orbitUseTime();
    }

    public boolean colorUseTime() {
        return expressionContext.colorUseTime();
    }

    public void append(String string) {
        builder.append(string);
    }

    public void append(boolean bool) {
        builder.append(bool);
    }

    public void append(double number) {
        builder.append(number);
    }

    public void append(int number) {
        builder.append(number);
    }

    public void append(ComplexNumber number) {
        builder.append(number);
    }

    public void setOrbitUseTime(boolean value) {
        expressionContext.setOrbitUseTime(value);
    }

    public void setColorUseTime(boolean value) {
        expressionContext.setColorUseTime(value);
    }
}
