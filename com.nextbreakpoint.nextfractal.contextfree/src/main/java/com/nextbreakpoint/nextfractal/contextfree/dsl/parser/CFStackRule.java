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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTUtils;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CFStackRule extends CFStackItem implements Cloneable {
    private int ruleName;
    private int paramCount;

    public CFStackRule(CFStack stack, int ruleName, int paramCount) {
        super(stack);
        this.ruleName = ruleName;
        this.paramCount = paramCount;
    }

    public CFStackRule(CFStackRule rule) {
        super(rule.getStack());
        this.ruleName = rule.ruleName;
        this.paramCount = rule.paramCount;
    }

    public Object clone() {
        return new CFStackRule(stack, ruleName, paramCount);
    }

    @Override
    public ExpType getType() {
        return ExpType.Rule;
    }

    @Override
    public int getTupleSize() {
        return 1;
    }

    @Override
    public void evalArgs(CFDGRenderer renderer, ASTExpression arguments, List<ASTParameter> parameters, boolean sequential) {
        ASTUtils.evalArgs(renderer, (CFStackRule)stack.getStackItem(stack.getStackTop()), iterator(), arguments, false);
    }

    @Override
    protected CFStackIterator iterator() {
        if (paramCount > 0) {
            return iterator(((CFStackParams)stack.getStackItem(stack.getStackTop() + 1)).getParams());
        }
        return super.iterator();
    }

    public void copyTo(CFStackItem[] dest, int destOffset) {
        int destIndex = destOffset;
        for (int srcIndex = 0; srcIndex < paramCount; srcIndex++) {
            switch (stack.getStackItem(srcIndex).getType()) {
                case Numeric, Flag, Mod ->
                        System.arraycopy(stack.getStackItems(), srcIndex, dest, destIndex, stack.getStackItem(srcIndex).getTupleSize());
                case Rule -> dest[destIndex] = stack.getStackItem(srcIndex);
                default -> {
                }
            }
            destIndex += stack.getStackItem(srcIndex).getTupleSize();
        }
    }
}
