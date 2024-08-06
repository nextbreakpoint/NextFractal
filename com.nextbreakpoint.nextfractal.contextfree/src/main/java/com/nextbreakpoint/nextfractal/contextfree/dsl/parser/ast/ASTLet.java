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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import org.antlr.v4.runtime.Token;

public class ASTLet extends ASTUserFunction {
	private ASTRepContainer definitions;
	
	public ASTLet(Token token, CFDGDriver driver, ASTRepContainer definitions, ASTDefine func) {
		super(token, driver, -1, null, func);
		this.definitions = definitions;
		let = true;
	}

	@Override
	public ASTExpression simplify() {
		getDefinition().compile(CompilePhase.Simplify);
		if (isConstant()) {
			StringBuilder e = new StringBuilder();
			entropy(e);
			ASTParameter p = new ASTParameter(getToken(), driver, -1, getDefinition());
			ASTExpression ret = p.constCopy(e.toString());
			if (ret != null) {
				return ret;
			}
		} else if (getArguments() == null) {
            return getDefinition().getExp();
		}
		return super.simplify();
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
        switch (ph) {
            case TypeCheck -> {
                definitions.compile(ph, null, getDefinition());
                ASTExpression args = null;
                for (ASTReplacement rep : definitions.getBody()) {
                    if (rep instanceof ASTDefine def) {
                        if (def.getDefineType() == DefineType.Stack) {
                            getDefinition().incStackCount(def.getTupleSize());
                            args = ASTExpression.append(args, def.getExp());
                        }
                    }
                }
                definitions.getParameters().removeLast();
                for (ASTParameter param : definitions.getParameters()) {
                    if (param.getDefinition() != null) {
                        getDefinition().getParameters().add(param);
                    }
                }
                definitions = null;
                //TODO controllare
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
