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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import lombok.Getter;

// astexpression.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2014 John Horigan - john@glyphic.com
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
public class ASTVariable extends ASTExpression {
    private static final int IllegalStackIndex = Integer.MAX_VALUE;

	private final String text;
	private final int stringIndex;
	private int stackIndex;
	private int count;
	private boolean parameter;
    private ASTParameter bound;

    public ASTVariable(CFDGSystem system, ASTWhere where, int stringIndex, String text) {
		super(system, where);
		this.stringIndex = stringIndex;
		this.parameter = false;
		this.stackIndex = 0;
		this.text = text;
		this.count = 0;
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if (type != ExpType.Numeric) {
            system.error("Non-numeric variable in a numeric context", getWhere());
            return -1;
        }
		if (result != null && length < count) {
			return -1;
		}
        if (result != null) {
            if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
            if (stackIndex == IllegalStackIndex) {
                system.error("Non-stack variable accessed through stack.", getWhere());
            }
            for (int i = 0; i < count; ++i) {
				result[i] = ((CFStackNumber)renderer.getStackItem(stackIndex + i)).getNumber();
            }
        }
        return count;
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification result, boolean shapeDest) {
		if (type != ExpType.Mod) {
            system.error("Non-adjustment variable referenced in an adjustment context", getWhere());
        }
        if (renderer == null) {
            return;
        }
        if (stackIndex == IllegalStackIndex) {
            system.error("Non-stack variable accessed through stack.", getWhere());
        }
        final Modification mod = ((CFStackModification)renderer.getStackItem(stackIndex)).getModification();
        if (shapeDest) {
        	result.concat(mod);
        } else {
        	if (result.merge(mod)) {
    			renderer.colorConflict(getWhere());
        	}
        }
	}

	@Override
	public void entropy(StringBuilder entropy) {
		entropy.append(text);
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
        switch (phase) {
            case TypeCheck -> {
                final boolean[] isGlobal = new boolean[] { false };
                final ASTParameter tempBound = builder.findExpression(stringIndex, isGlobal);
                if (tempBound == null) {
                    system.error("internal error", getWhere());
                    return null;
                }
                bound = tempBound;

                final String name = builder.shapeToString(stringIndex);

                count = bound.getType() == ExpType.Numeric ? bound.getTupleSize() : 1;
                type = bound.getType();
                natural = bound.isNatural();
                locality = bound.getLocality();
                parameter = bound.isParameter();

                if (bound.getStackIndex() == -1) {
                    constant = true;
                    stackIndex = IllegalStackIndex;
                } else {
                    if (bound.getType() == ExpType.Rule) {
                        ASTRuleSpecifier ret = new ASTRuleSpecifier(system, getWhere(), stringIndex, name);
                        ret.compile(builder, phase);
                        return ret;
                    }
                    stackIndex = bound.getStackIndex() - (isGlobal[0] ? 0 : builder.getLocalStackDepth());
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
    public ASTExpression simplify(CFDGBuilder builder) {
        if (bound.getStackIndex() == -1) {
            if (bound.getDefinition() == null) {
                system.error("internal error", getWhere());
                return null;
            }
            final String name = builder.shapeToString(stringIndex);
            final ASTExpression exp = bound.constCopy(builder, name);
            if (exp == null) {
                system.error("internal error", getWhere());
            }
            return exp;
        }
        return null;
    }
}
