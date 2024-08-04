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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import org.antlr.v4.runtime.Token;

import java.util.HashMap;
import java.util.Map;

public class ASTSwitch extends ASTReplacement {
	private Map<Integer, ASTRepContainer> caseStatements = new HashMap<>();
	private ASTRepContainer elseBody;
	private ASTExpression switchExp;
	
	public ASTSwitch(CFDGDriver driver, ASTExpression switchExp, Token location) {
		super(driver, null, RepElemType.empty, location);
		elseBody = new ASTRepContainer(driver);
		this.switchExp = switchExp;
	}

	public ASTRepContainer getElseBody() {
		return elseBody;
	}

	public ASTExpression getSwitchExp() {
		return switchExp;
	}

	public Map<Integer, ASTRepContainer> getCaseStatements() {
		return caseStatements;
	}

	@Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		switchExp = compile(switchExp, ph);
		for (ASTRepContainer caseVal : caseStatements.values()) {
			caseVal.compile(ph, null, null);
		}
		elseBody.compile(ph, null, null);
		
		switch (ph) {
			case TypeCheck:
				if (switchExp.getType() != ExpType.NumericType || switchExp.evaluate(null, 0) != 1) {
					driver.error("Switch selector must be a numeric scalar", location);
				}
				break;
	
			case Simplify:
				if (switchExp != null) {
					switchExp = switchExp.simplify();
				}
				break;
	
			default:
				break;
		}
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		double[] caveValue = new double[1];
		if (switchExp.evaluate(caveValue, 1, renderer) != 1) {
			driver.error("Error evaluating switch selector", location);
			return;
		}
		
		ASTRepContainer caseVal = caseStatements.get((int)Math.floor(caveValue[0]));
		if (caseVal != null) {
			caseVal.traverse(parent, tr, renderer, false);
		} else {
			elseBody.traverse(parent, tr, renderer, false);
		}
	}
	
	public void unify() {
		if (elseBody.getPathOp() != getPathOp()) {
			setPathOp(PathOp.UNKNOWN);
		}
		for (ASTRepContainer caseVal : caseStatements.values()) {
			if (caseVal.getPathOp() != getPathOp()) {
				setPathOp(PathOp.UNKNOWN);
			}
		}
	}
}
