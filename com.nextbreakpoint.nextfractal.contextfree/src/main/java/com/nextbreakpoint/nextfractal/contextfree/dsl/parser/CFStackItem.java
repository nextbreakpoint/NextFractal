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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;

import java.util.List;

public abstract class CFStackItem {
    protected CFStack stack;

    protected CFStackItem(CFStack stack) {
        this.stack = stack;
    }

    public void evalArgs(CFDGRenderer renderer, ASTExpression arguments, List<ASTParameter> parameters, boolean sequential) {
        AST.evalArgs(renderer, null, iterator(parameters), arguments, sequential);
    }

    public CFStack getStack() {
        return stack;
    }

    public abstract ExpType getType();

    public abstract int getTupleSize();

    protected CFStackIterator iterator(List<ASTParameter> parameters) {
        return new CFStackIterator(stack, parameters);
    }

    protected CFStackIterator iterator() {
        return new CFStackIterator(stack);
    }
}
