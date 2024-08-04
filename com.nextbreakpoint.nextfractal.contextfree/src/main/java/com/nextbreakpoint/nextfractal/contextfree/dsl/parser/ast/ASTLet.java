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
	
	public ASTLet(CFDGDriver driver, ASTRepContainer args, ASTDefine func, Token location) {
		super(driver, -1, null, func, location);
		this.definitions = args;
		isLet = true;
	}

	@Override
	public ASTExpression simplify() {
		getDefinition().compile(CompilePhase.Simplify);
		if (isConstant()) {
			StringBuilder e = new StringBuilder();
			entropy(e);
			ASTParameter p = new ASTParameter(driver, -1, getDefinition(), location);
			ASTExpression ret = p.constCopy(e.toString());
			if (ret != null) {
				return ret;
			}
		} else if (getArguments() == null) {
			ASTExpression ret = getDefinition().getExp();
			return ret;
		}
		return super.simplify();
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		switch (ph) {
			case TypeCheck: {
				definitions.compile(ph, null, getDefinition());
				ASTExpression args = null;
				for (ASTReplacement rep : definitions.getBody()) {
					if (rep instanceof ASTDefine) {
						ASTDefine def = (ASTDefine)rep;
						if (def.getDefineType() == DefineType.StackDefine) {
							getDefinition().incStackCount(def.getTupleSize());
							args = ASTExpression.append(args, def.getExp());
						}
					}
				}
				definitions.getParameters().remove(definitions.getParameters().size() - 1);
				for (ASTParameter param : definitions.getParameters()) {
					if (param.getDefinition() != null) {
						getDefinition().getParameters().add(param);
					}
				}
				definitions = null;
				//TODO controllare
				setArguments(args);
				isConstant = getArguments() == null && getDefinition().getExp().isConstant();
				isNatural = getDefinition().isNatural();
				locality = getDefinition().getExp() != null ? getDefinition().getExp().getLocality() : getDefinition().getChildChange().getLocality();
				type = getDefinition().getExpType();
				break;
			}

			case Simplify: 
				break;
	
			default:
				break;
		}
		return null;
	}
}
