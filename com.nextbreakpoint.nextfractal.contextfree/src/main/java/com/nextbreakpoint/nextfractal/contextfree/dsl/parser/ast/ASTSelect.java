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
import lombok.Getter;

import java.util.List;

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

public class ASTSelect extends ASTExpression {
	private static final int NOT_CACHED = -1;

	private final StringBuilder entropy;
	private List<ASTExpression> arguments;
	private ASTExpression selector;
	private int tupleSize;
	private int indexCache;
	@Getter
	private final boolean select;

	public ASTSelect(CFDGSystem system, ASTWhere where, ASTExpression arguments, boolean select) {
		super(system, where);
		tupleSize = -1;
		indexCache = NOT_CACHED;
		selector = arguments;
		constant = false;
		entropy = new StringBuilder();
		this.select = select;
		if (selector == null || selector.size() < 3) {
			system.error("select()/if() function requires arguments", where);
		}
	}

    @Override
	public CFStackRule evalArgs(CFDGBuilder builder, CFDGRenderer renderer, CFStackRule parent) {
		return arguments.get(getIndex(builder, renderer)).evalArgs(builder, renderer, parent);
	}

	@Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if (type != ExpType.Numeric) {
			system.error("Evaluation of a non-shape select() in a numeric context", getWhere());
			return -1;
		}

		if (result == null)
			return tupleSize;

		return arguments.get(getIndex(builder, renderer)).evaluate(builder, renderer, result, length);
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		if (type != ExpType.Mod) {
			system.error("Evaluation of a non-adjustment select() in an adjustment context", getWhere());
			return;
		}

		arguments.get(getIndex(builder, renderer)).evaluate(builder, renderer, mod, shapeDest);
	}

	@Override
	public void entropy(StringBuilder entropy) {
		entropy.append(this.entropy);
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		if (indexCache == NOT_CACHED) {
            arguments.replaceAll(astExpression -> ASTExpression.simplify(builder, astExpression));
			selector = ASTExpression.simplify(builder, selector);
			return null;
		}
		ASTExpression expression = arguments.get(indexCache);
		final ASTExpression ret = expression.simplify(builder);
		arguments.set(indexCache, ret);
		return ret;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		if (selector == null) {
			return null;
		}
        arguments.replaceAll(exp -> ASTExpression.compile(builder, phase, exp));
		selector = ASTExpression.compile(builder, phase, selector);

		if (selector == null) {
			system.error("Missing selector expression", getWhere());
			return null;
		}

		switch (phase) {
            case TypeCheck -> {
                selector.entropy(entropy);
				//TODO define constant
                entropy.append("\u00B5\u00A2\u004A\u0074\u00A9\u00DF");
                locality = selector.getLocality();

                arguments = AST.extract(selector);
                selector = arguments.getFirst();
                arguments.removeFirst();

                if (selector.getType() != ExpType.Numeric || selector.evaluate(builder, null, 0) != 1) {
                    system.error("if()/select() selector must be a numeric scalar", selector.getWhere());
					return null;
                }

                if (arguments.size() < 2) {
                    system.error("if()/select() selector must have at least two arguments", selector.getWhere());
					return null;
                }

                type = arguments.getFirst().getType();
                natural = arguments.getFirst().isNatural();
                tupleSize = type == ExpType.Numeric ? arguments.getFirst().evaluate(builder, null, 0) : 1;
                for (int i = 1; i < arguments.size(); i++) {
                    final ASTExpression argument = arguments.get(i);
                    if (type != argument.getType()) {
                        system.error("select()/if() choices must be of same type", argument.getWhere());
                    } else if (type == ExpType.Numeric && tupleSize != -1 && argument.evaluate(builder, null, 0) != tupleSize) {
                        system.error("select()/if() choices must be of same length", argument.getWhere());
						tupleSize = -1;
                    }
                    natural = natural && argument.isNatural();
                }

				if (select && arguments.size() != 2) {
					system.error("if() function requires two arguments", getWhere());
				}

                if (selector.isConstant()) {
                    indexCache = getIndex(builder, null);
                    constant = arguments.get(indexCache).isConstant();
                    locality = arguments.get(indexCache).getLocality();
                    natural = arguments.get(indexCache).isNatural();
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

	public int getIndex(CFDGBuilder builder, CFDGRenderer renderer) {
		if (indexCache != NOT_CACHED) {
			return indexCache;
		}

		double[] select = new double[] { 0.0 };
		selector.evaluate(builder, renderer, select, 1);

		if (this.select) {
			return select[0] != 0 ? 0 : 1;
		}

		if (select[0] < 0.0)
			return 0;

		int i = (int)select[0];

		if (i >= arguments.size()) {
			return arguments.size() - 1;
		}

		return i;
	}
}
