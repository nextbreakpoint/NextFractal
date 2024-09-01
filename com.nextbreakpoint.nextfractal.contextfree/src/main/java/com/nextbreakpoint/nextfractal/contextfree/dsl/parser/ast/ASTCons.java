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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST.combineLocality;

// astexpression.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2009-2014 John Horigan - john@glyphic.com
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
public class ASTCons extends ASTExpression {
	private final List<ASTExpression> children = new ArrayList<>();

	public ASTCons(CFDGSystem system, ASTWhere where, List<ASTExpression> kids) {
		// Must have at least one kid or else undefined behavior
		super(system, where, true, true, ExpType.None);
		locality = Locality.PureLocal;
		for (ASTExpression kid : kids) {
			append(kid);
		}
	}

	public ASTCons(CFDGSystem system, ASTWhere where, ASTExpression... args) {
		// Must have at least one kid or else undefined behavior
		super(system, where, true, true, ExpType.None);
		locality = Locality.PureLocal;
		for (ASTExpression arg : args) {
			append(arg);
		}
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
        children.replaceAll(exp -> ASTExpression.simplify(builder, exp));
		if (children.size() == 1) {
			return children.getFirst();
		}
        return null;
	}

	@Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if ((type.getType() & (ExpType.Numeric.getType() | ExpType.Flag.getType())) == 0 || (type.getType() & (ExpType.Mod.getType() | ExpType.Rule.getType())) != 0) {
			system.error("Non-numeric expression in a numeric context", null);
			return -1;
		}
		int count = 0;
		for (ASTExpression child : children) {
			final double[] value = result != null ? new double[length] : null;
			final int num = child.evaluate(builder, renderer, value, length);
			if (num <= 0) {
				return -1;
			}
			if (result != null) {
				System.arraycopy(value, 0, result, count, num);
				length -= num;
			}
			count += num;
		}
		return count;
	}		

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		for (ASTExpression child : children) {
			child.evaluate(builder, renderer, mod, shapeDest);
		}
	}

	@Override
	public void entropy(StringBuilder entropy) {
		for (ASTExpression child : children) {
			child.entropy(entropy);
		}
		entropy.append("\u00C5\u0060\u00A5\u00C5\u00C8\u0074");
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
        switch (phase) {
            case TypeCheck -> {
                constant = natural = true;
                locality = Locality.PureLocal;
                type = ExpType.None;
                for (int i = 0; i < children.size(); i++) {
                    children.set(i, ASTExpression.compile(builder, phase, children.get(i)));
                    ASTExpression child = children.get(i);
                    constant = constant && child.isConstant();
                    natural = natural && child.isNatural();
                    locality = combineLocality(locality, child.getLocality());
                    type = ExpType.fromType(type.getType() | child.getType().getType());
                }
            }
			case Simplify -> {
				// do nothing
			}
            default -> {
            }
        }
		return null;
	}

	@Override
	public ASTExpression append(ASTExpression sibling) {
		if (sibling == null) return this;
		constant = constant && sibling.isConstant();
		natural = natural && sibling.isNatural();
		locality = combineLocality(locality, sibling.getLocality());
		type = ExpType.fromType(type.getType() | sibling.getType().getType());
		// Cannot insert an ASTcons into children, it will be flattened away.
		// You must wrap the ASTcons in an ASTparen in order to insert it whole.
		if (sibling instanceof ASTCons c) {
			children.addAll(c.getChildren());
		} else {
			children.add(sibling);
		}
		return this;
	}

	public void setChild(int index, ASTExpression expression) {
		if (index >= children.size()) {
			system.error("Expression list bounds exceeded", getWhere());
		}
		children.set(index, expression);
	}

	@Override
	public ASTExpression getChild(int index) {
		if (index >= children.size()) {
			system.error("Expression list bounds exceeded", getWhere());
		}
		return children.get(index);
	}

	@Override
	public int size() {
		return children.size();
	}
}
