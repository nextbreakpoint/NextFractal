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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CFStackIterator {
    private final List<ASTParameter> params;
    private int paramPos;

    @Getter
    private CFStack stack;

    public CFStackIterator(CFStack stack, List<ASTParameter> parameters) {
        this.params = parameters;
        this.paramPos = 0;
        this.stack = parameters != null ? stack : null;
    }

    public CFStackIterator() {
        this.params = new ArrayList<>();
        this.paramPos = 0;
        this.stack = null;
    }

    public CFStackIterator(CFStackIterator iterator) {
        this.params = iterator.params;
        this.paramPos = iterator.paramPos;
        this.stack = iterator.stack;
    }

    public ASTParameter getType() {
        return params.get(paramPos);
    }

    public CFStackIterator next() {
        if (stack == null) {
            return this;
        }
        final ASTParameter param = params.get(paramPos);
        stack.setStackTop(stack.getStackTop() + param.getTupleSize());
        paramPos += 1;
        if (paramPos >= params.size()) {
            stack = null;
        }
        return this;
    }

    public void setStack(CFStack stack) {

    }

    public void setLogicalStackTop(int stackTop) {

    }
}
