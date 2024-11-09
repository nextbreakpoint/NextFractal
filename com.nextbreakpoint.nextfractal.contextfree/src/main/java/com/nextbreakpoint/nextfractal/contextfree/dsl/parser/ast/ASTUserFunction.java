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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGStopException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;

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

public class ASTUserFunction extends ASTExpression {
	@Setter
    @Getter
    private ASTExpression arguments;
	@Getter
    private ASTDefine definition;
	@Getter
    private final int nameIndex;
	@Getter
	protected boolean let;
	private int oldTop;
	private int oldSize;

	public ASTUserFunction(CFDGSystem system, ASTWhere where, int nameIndex, ASTExpression arguments, ASTDefine definition) {
		super(system, where, false, false, ExpType.None);
		this.nameIndex = nameIndex;
		this.definition = definition;
		this.arguments = arguments;
		this.let = false;
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if (type != ExpType.Numeric) {
			system.error("Function does not evaluate to a number", getWhere());
			return -1;
		}
		if (result != null && length < definition.getTupleSize()) {
			return -1;
		}
		if (result == null) {
			return definition.getTupleSize();
		}
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGStopException("Stopping", getWhere());
		}
		setupStack(builder, renderer);
		if (definition.getExp().evaluate(builder, renderer, result, length) != definition.getTupleSize()) {
			system.error("Error evaluating function", getWhere());
		}
		cleanupStack(renderer);
		return definition.getTupleSize();
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		if (type != ExpType.Mod) {
			system.error("Function does not evaluate to an adjustment", getWhere());
			return;
		}
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGStopException("Stopping", getWhere());
		}
		setupStack(builder, renderer);
		definition.getExp().evaluate(builder, renderer, mod, shapeDest);
		cleanupStack(renderer);
	}

	@Override
	public void entropy(StringBuilder entropy) {
		if (arguments != null) {
			arguments.entropy(entropy);
		}
		entropy.append(definition.getName());
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		if (arguments != null) {
			if (arguments instanceof ASTCons carg) {
				// Can't use ASTcons::simplify() because it will collapse the
				// ASTcons if it only has one child and that will break the
				// function arguments.
                for (int i = 0; i < carg.getChildren().size(); i++) {
					carg.setChild(i, ASTExpression.simplify(builder, carg.getChild(i)));
				}
			} else {
				arguments = ASTExpression.simplify(builder, arguments);
			}
		}
		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
        switch (phase) {
            case TypeCheck -> {
                // Function calls and shape specifications are ambiguous at parse
                // time so the parser always chooses a function call. During
                // type checkParam we may need to convert to a shape spec.
                final ASTDefine[] def = new ASTDefine[1];
                @SuppressWarnings("unchecked")
				final List<ASTParameter>[] p = new List[1];
				final String name = builder.getTypeInfo(nameIndex, def, p);
                if (def[0] != null && p[0] != null) {
                    system.error("Name matches both a function and a shape", getWhere());
					return null;
                }
                if (def[0] == null && p[0] == null) {
                    system.error("Name does not match shape name or function name", getWhere());
					return null;
                }
                if (def[0] != null) {
                    arguments = ASTExpression.compile(builder, phase, arguments);
                    definition = def[0];
                    ASTParameter.checkType(builder, getWhere(), def[0].getParameters(), arguments, false);
                    constant = false;
                    natural = definition.isNatural();
                    type = definition.getExpType();
                    locality = arguments != null ? arguments.getLocality() : Locality.PureLocal;
                    if (definition.getExp() != null && definition.getExp().getLocality() == Locality.ImpureNonLocal && locality == Locality.PureNonLocal) {
                        locality = Locality.ImpureNonLocal;
                    }
                    return null;
                }
				final ASTRuleSpecifier r = new ASTRuleSpecifier(system, getWhere(), nameIndex, name, arguments, null);
                r.compile(builder, phase);
                return r;
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
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGStopException("Stopping", getWhere());
		}
		setupStack(builder, renderer);
		CFStackRule ret = definition.getExp().evalArgs(builder, renderer, parent);
		cleanupStack(renderer);
		return ret;
	}
	
	private void setupStack(CFDGBuilder builder, CFDGRenderer renderer) {
		//TODO verify
		oldTop = renderer.getLogicalStackTop();
		oldSize = renderer.getStackSize();
		if (definition.getParamSize() > 0) {
			if (oldSize + definition.getParamSize() > renderer.getMaxStackSize()) {
				system.error("Maximum stack getMaxStackSize exceeded", getWhere());
			}
			renderer.setStackSize(oldSize + definition.getParamSize());
			renderer.setStackItem(oldTop, new CFStackNumber(renderer.getStack(), 0));
			renderer.getStackItem(oldTop).evalArgs(builder, renderer, arguments, definition.getParameters(), let);
			renderer.setLogicalStackTop(oldTop + definition.getParamSize());
		}
	}

	private void cleanupStack(CFDGRenderer renderer) {
		//TODO verify
		if (definition.getParamSize() > 0) {
			renderer.setStackItem(oldSize, null);
			renderer.setLogicalStackTop(oldTop);
			renderer.setStackSize(oldSize);
		}
	}
}
