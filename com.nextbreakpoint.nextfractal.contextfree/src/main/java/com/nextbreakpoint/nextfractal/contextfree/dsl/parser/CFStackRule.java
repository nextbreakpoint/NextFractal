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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
public class CFStackRule extends CFStackItem implements Cloneable {
    private int ruleName;
    private int paramCount;
    private List<ASTParameter> params;

    public CFStackRule(CFStack stack, int ruleName, int paramCount) {
        super(stack);
        this.ruleName = ruleName;
        this.paramCount = paramCount;
    }

    public CFStackRule(CFStackRule rule) {
        super(rule.getStack());
        this.ruleName = rule.ruleName;
        this.paramCount = rule.paramCount;
        this.params = rule.params;
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
    protected CFStackIterator iterator() {
        if (paramCount > 0) {
            return iterator(((CFStackRule)stack.getStackItem(stack.getStackTop())).getParams());
        }
        return super.iterator();
    }

    @Override
    public void evalArgs(CFDGBuilder builder, CFDGRenderer renderer, ASTExpression arguments, List<ASTParameter> parameters, boolean sequential) {
        CFStack.evalArgs(builder, renderer, (CFStackRule)stack.getStackItem(stack.getStackTop()), iterator(parameters), arguments, sequential);
    }

    public void copyParams(CFStackItem[] items, int destOffset) {
        int destIndex = destOffset;
        for (int srcIndex = 0; srcIndex < paramCount; srcIndex++) {
            final CFStackItem item = stack.getStackItem(srcIndex);
            switch (item.getType()) {
                case Numeric, Flag, Mod ->
                        System.arraycopy(stack.getStackItems(), srcIndex, items, destIndex, item.getTupleSize());
                case Rule -> items[destIndex] = item;
                default -> {
                }
            }
            destIndex += item.getTupleSize();
        }
    }

    public Object clone() {
        final CFStackRule rule = new CFStackRule(stack, ruleName, paramCount);
        rule.setParams(params !=null ? params.stream().map(p -> new ASTParameter(p.getSystem(), p)).toList() : null);
        return rule;
    }
}
