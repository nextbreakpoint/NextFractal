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

import java.util.List;

public class CFStack {
	private CFStackItem[] stackItems;
	private int stackSize;
	private int stackTop;

	public CFStack(CFStackItem[] stackItems) {
		this.stackItems = stackItems;
		this.stackSize = 0;
		this.stackTop = 0;
	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}

	public int getStackTop() {
		return stackTop;
	}

	public void setStackTop(int stackTop) {
		this.stackTop = stackTop;
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

	public CFStackItem[] getStackItems() {
		return stackItems;
	}

	public void addStackItem(CFStackItem stackType) {
		//TODO rivedere
		setStackItem(stackSize, stackType);
		setStackTop(stackSize + 1);
		setStackSize(stackSize + 1);
	}

	public void removeStackItem() {
		//TODO rivedere
		setStackTop(stackSize - 1);
		setStackSize(stackSize - 1);
	}

	public static CFStackRule createStackRule(CFStackRule rule) {
		//TODO rivedere
		CFStackItem srcItem = rule.getStack().getStackItem(0);
		if (srcItem == null) {
			return null;
		}
		CFStackRule stackRule = (CFStackRule) rule.getStack().getStackItem(0);
		CFStackParams typeInfo = stackRule.getParamCount() > 0 ? (CFStackParams) rule.getStack().getStackItem(1) : null;
		CFStackItem[] newItems = new CFStackItem[2 + stackRule.getParamCount()];
		CFStackRule newRule = new CFStackRule(stackRule);
		newItems[0] = newRule;
		if (stackRule.getParamCount() > 0) {
			newItems[1] = typeInfo;
			stackRule.copyTo(newItems, 2);
		}
		return newRule;
	}

	public static CFStackRule createStackRule(int nameIndex, int paramCount, List<ASTParameter> params) {
		//TODO rivedere
		CFStackItem[] newItems = new CFStackItem[paramCount > 0 ? paramCount + 2 : 1];
		CFStack newStack = new CFStack(newItems);
		CFStackRule stackRule = new CFStackRule(newStack, nameIndex, paramCount);
		newItems[0] = stackRule;
		newStack.setStackSize(1);
		if (paramCount > 0) {
			newItems[1] = new CFStackParams(newStack, params);
			newStack.setStackSize(2);
		}
		return stackRule;
	}
}
