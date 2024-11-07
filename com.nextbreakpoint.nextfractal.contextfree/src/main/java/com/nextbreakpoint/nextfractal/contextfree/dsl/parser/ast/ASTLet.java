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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;

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

public class ASTLet extends ASTUserFunction {
	private ASTRepContainer definitions;

	public ASTLet(CFDGSystem system, ASTWhere where, ASTRepContainer definitions, ASTDefine func) {
		super(system, where, -1, null, func);
		this.definitions = definitions;
		let = true;
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
        final ASTDefine definition = getDefinition();
        if (definition == null) {
            system.error("Error in let expression", getWhere());
            return null;
        }
		definition.compile(builder, CompilePhase.Simplify);
		if (isConstant()) {
			final StringBuilder entropy = new StringBuilder();
			entropy(entropy);
            final ASTParameter p = new ASTParameter(system, getWhere(), -1, definition);
            p.setDefinition(definition); // constructor won't do this
            final ASTExpression ret = p.constCopy(builder, entropy.toString());
			if (ret != null) {
				return ret;
			}
		} else if (getArguments() == null) {
            return definition.getExp();
		}
        builder.pushRepContainer(definitions);
		super.simplify(builder);
        builder.popRepContainer(null);
        return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
        switch (phase) {
            case TypeCheck -> {
                if (definitions == null) {
                    system.error("Error in let expression", getWhere());
                    return null;
                }
                definitions.compile(builder, phase, null, getDefinition());
                ASTRepContainer constDefs = new ASTRepContainer(system, getWhere());
                // transfer non-const definitions to arguments
                ASTExpression args = null;
                for (ASTReplacement rep : definitions.getBody()) {
                    //TODO verify behaviour
                    if (!(rep instanceof ASTDefine def)) {
                        system.error("Error in let expression", getWhere());
                    } else {
                        if (def.getDefineType() == DefineType.Stack) {
                            getDefinition().incParamSize(def.getTupleSize());
                            args = ASTExpression.append(args, def.getExp());
                        } else {
                            constDefs.getBody().add(rep);
                        }
                    }
                }
                definitions.getParameters().removeLast();  // remove the definition parameter
                final List<ASTParameter> tmpParameters = definitions.getParameters();
                definitions.setParameters(getDefinition().getParameters());
                getDefinition().setParameters(tmpParameters);
                final ASTRepContainer tmpDefs = definitions;
                definitions = constDefs;
                constDefs = tmpDefs;
                //TODO is it the same or a clone?
                definitions.setParameters(getDefinition().getParameters()); // copy
                setArguments(args);
                constant = getArguments() == null && getDefinition().getExp().isConstant();
                natural = getDefinition().isNatural();
                locality = getDefinition().getExp() != null ? getDefinition().getExp().getLocality() : getDefinition().getChildChange().getLocality();
                type = getDefinition().getExpType();
            }
            case Simplify -> {
                // do nothing
            }
            default -> {
            }
        }
		return null;
	}
}
