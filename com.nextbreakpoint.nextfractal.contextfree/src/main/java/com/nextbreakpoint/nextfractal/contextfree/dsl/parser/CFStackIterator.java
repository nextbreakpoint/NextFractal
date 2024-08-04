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

import java.util.ArrayList;
import java.util.List;

public class CFStackIterator {
    private int stackPos;
    private int paramPos;
    private CFStack stack;
    private List<ASTParameter> params;

    public CFStackIterator(CFStack stack, List<ASTParameter> parameters) {
        this.stack = stack;
        this.params = new ArrayList<>(parameters);
        this.paramPos = 0;
        this.stackPos = stack.getStackTop();
    }

    public CFStackIterator(CFStack stack) {
        this.stack = stack;
        this.params = new ArrayList<>();
        this.paramPos = 0;
        this.stackPos = stack.getStackTop();
    }

    public CFStackIterator(CFStackIterator iterator) {
        this.stack = iterator.stack;
        this.params = iterator.params;
        this.stackPos = iterator.stackPos;
        this.paramPos = iterator.paramPos;
    }

    public ASTParameter getType() {
        return params.get(paramPos);
    }

    public CFStackItem getItem() {
        return stack.getStackItem(stackPos);
    }

    public void setItem(int index, CFStackItem item) {
        stack.setStackItem(stackPos + index, item);
    }

    public CFStackIterator next() {
        if (paramPos >= params.size()) {
            return null;
        }
        ASTParameter next = params.get(paramPos);
        stackPos += next.getTupleSize();
        paramPos += 1;
        return this;
    }

    public CFStack getStack() {
        return stack;
    }
}
