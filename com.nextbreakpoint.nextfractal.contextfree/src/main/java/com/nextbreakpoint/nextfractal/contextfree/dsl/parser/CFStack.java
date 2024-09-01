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
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// stacktype.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA

@Getter
public class CFStack {
	private final CFStackItem[] stackItems;
	@Setter
    private int stackSize;
	@Setter
    private int stackTop;

	public CFStack(CFStackItem[] stackItems) {
		this.stackItems = stackItems;
		this.stackSize = 0;
		this.stackTop = 0;
	}

	public int getMaxStackSize() {
		return stackItems.length;
	}

	public CFStackItem getStackItem(int index) {
		return stackItems[index < 0 ? stackTop + index : index];
	}

	public void setStackItem(int index, CFStackItem item) {
		stackItems[index < 0 ? stackTop + index : index] = item;
	}

    public void addStackItem(CFStackItem item) {
		setStackItem(stackTop, item);
		setStackSize(stackSize + 1);
		setStackTop(stackTop + 1);
	}

	public void removeStackItem() {
		setStackItem(stackTop, null);
		setStackSize(stackSize - 1);
		setStackTop(stackTop - 1);
	}

	public static CFStackRule createStackRule(CFStackRule parent, int shapeType) {
		if (parent == null) {
			return null;
		}
		final List<ASTParameter> params = parent.getParamCount() > 0 ? parent.getParams() : null;
		final CFStackRule newRule = createStackRule(shapeType > 0 ? shapeType : parent.getRuleName(), parent.getParamCount(), params);
		if (newRule.getParamCount() > 0) {
			parent.copyParams(newRule.getStack().getStackItems(), newRule.getParamCount());
		}
		return newRule;
	}

	public static CFStackRule createStackRule(int nameIndex, int paramCount, List<ASTParameter> params) {
		final CFStackRule stackRule = new CFStackRule(new CFStack(new CFStackItem[paramCount]), nameIndex, paramCount);
		if (paramCount > 0) {
			stackRule.setParams(params);
		}
		return stackRule;
	}

	public static void evalArgs(CFDGBuilder builder, CFDGRenderer renderer, CFStackRule parent, CFStackIterator dest, ASTExpression arguments, boolean onStack) {
		for (int i = 0; i < arguments.size(); i++) {
			if (onStack) {
				//TODO can renderer be null?
				renderer.setLogicalStack(dest.getStack());
				renderer.setLogicalStackTop(dest.getStack().getStackTop());
			}
			final ASTExpression arg = arguments.getChild(i);
			switch (arg.getType()) {
				case Numeric -> {
					final double[] value = new double[dest.getType().getTupleSize()];
					final int num = arg.evaluate(builder, renderer, value, dest.getType().getTupleSize());
					if (!ASTParameter.Impure && dest.getType().isNatural() && !CFDGRenderer.isNatural(builder, renderer, value[0])) {
						builder.getSystem().error("Expression does not evaluate to a legal natural number", arg.getWhere());
					}
					if (num != dest.getType().getTupleSize()) {
						builder.getSystem().error("Expression does not evaluate to the correct size", arg.getWhere());
					}
					for (int j = 0; j < dest.getType().getTupleSize(); j++) {
						final CFStackNumber item = new CFStackNumber(dest.getStack(), value[j]);
						dest.getStack().setStackItem(dest.getStack().getStackTop() + j, item);
					}
				}
				case Mod -> {
					final Modification modification = new Modification();
					arg.evaluate(builder, renderer, modification, false);
					final CFStackModification item = new CFStackModification(dest.getStack(), modification);
					dest.getStack().setStackItem(dest.getStack().getStackTop(), item);
				}
				case Rule -> {
					//TODO can renderer be null?
					final CFStackRule item = arg.evalArgs(builder, renderer, parent);
					dest.getStack().setStackItem(dest.getStack().getStackTop(), item);
				}
				default -> {
				}
			}
			dest.next();
		}
	}
}
