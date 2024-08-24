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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;

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

public class ASTParen extends ASTExpression {
	private ASTExpression expression;
	
	public ASTParen(CFDGSystem system, ASTWhere where, ASTExpression expression) {
		super(system, where, expression.isConstant(), expression.isNatural(), expression.getType());
		this.expression = expression;
	}

	@Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
        if (type != ExpType.Numeric) {
			system.error("Non-numeric/flag expression in a numeric/flag context", getWhere());
			return -1;
        }
        return expression.evaluate(builder, renderer, result, length);
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
        if (type != ExpType.Mod) {
			system.error("Expression does not evaluate to an adjustment", getWhere());
			return;
        }
		super.evaluate(builder, renderer, mod, shapeDest);
	}

	@Override
	public void entropy(StringBuilder entropy) {
		if (expression != null) {
			expression.entropy(entropy);
		}
		entropy.append("\u00E8\u00E9\u00F6\u007E\u001A\u00F1");
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		return expression = ASTExpression.simplify(builder, expression);
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		if (expression == null) return null;

		expression = ASTExpression.compile(builder, phase, expression);

        switch (phase) {
            case TypeCheck -> {
                constant = expression.isConstant();
                natural = expression.isNatural();
                locality = expression.getLocality();
                type = expression.getType();
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
	public CFStackRule evalArgs(CFDGBuilder builder, CFDGRenderer renderer, CFStackRule parent) {
		return expression.evalArgs(builder, renderer, parent);
	}
}
