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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import lombok.Getter;

import java.util.Objects;

public class DSLCompilerContext {
    private final DSLExpressionContext expressionContext;
    private final StringBuilder builder;
    @Getter
    private final ClassType classType;

    public DSLCompilerContext(DSLExpressionContext expressionContext, StringBuilder builder, ClassType classType) {
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
