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
import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class ASTStartSpecifier extends ASTRuleSpecifier {
	private ASTModification modification;
	
	public ASTStartSpecifier(Token token, CFDGDriver driver, int nameIndex, String name, ASTExpression args, ASTModification mod) {
		super(token, driver, nameIndex, name, args, null);
		this.modification = mod;
	}
	
	public ASTStartSpecifier(Token token, CFDGDriver driver, int nameIndex, String name, ASTModification mod) {
		super(token, driver, nameIndex, name, null, null);
		this.modification = mod;
	}
	
	public ASTStartSpecifier(Token token, CFDGDriver driver, ASTExpression exp, ASTModification mod) {
		super(token, driver, exp);
		this.modification = mod;
	}

	public ASTStartSpecifier(ASTRuleSpecifier rule, ASTModification mod) {
		super(rule.getToken(), rule.driver);
		this.modification = mod;
	}

    @Override
	public void entropy(StringBuilder e) {
		e.append(getEntropy());
		if (modification != null) {
			modification.entropy(e);
		}
	}

	@Override
	public ASTExpression simplify() {
		super.simplify();
		modification = (ASTModification) simplify(modification);
		return this;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		//TODO controllare
		String name = getEntropy();
		super.compile(ph);
		setEntropy(name);
		modification = (ASTModification) compile(modification, ph);
		return null;
	}
}
