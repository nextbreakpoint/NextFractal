/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import org.antlr.v4.runtime.Token;

public class ASTIf extends ASTReplacement {
	private ASTExpression condition;
	private ASTRepContainer thenBody;
	private ASTRepContainer elseBody;
	
	public ASTIf(CFDGDriver driver, ASTExpression condition, Token location) {
		super(driver, null, RepElemType.empty, location);
		thenBody = new ASTRepContainer(driver);
		elseBody = new ASTRepContainer(driver);
		this.condition = condition;
	}
	
	public ASTRepContainer getThenBody() {
		return thenBody;
	}
	
	public ASTRepContainer getElseBody() {
		return elseBody;
	}

	@Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		condition = compile(condition, ph);
		thenBody.compile(ph, null, null);
		elseBody.compile(ph, null, null);
		
		switch (ph) {
			case TypeCheck:
				if (condition.getType() != ExpType.NumericType || condition.evaluate(null, 0) != 1) {
					driver.error("If condition must be a numeric scalar", condition.getLocation());
				}
				break;
	
			case Simplify:
				condition = simplify(condition);
				break;
	
			default:
				break;
		}
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		double[] cond = new double[1];
		if (condition.evaluate(cond, 1, renderer) != 1) {
			driver.error("Error evaluating if condition", location);
			return;
		}
		if (cond[0] != 0) {
			thenBody.traverse(parent, tr, renderer, false);
		} else {
			elseBody.traverse(parent, tr, renderer, false);
		}
	}
}
