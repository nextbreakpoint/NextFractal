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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import org.antlr.v4.runtime.Token;

public class ASTExpression {
	protected CFDGDriver driver;
	protected boolean isConstant;
	protected boolean isNatural;
	protected Locality locality;
	protected ExpType type;
	protected Token location;

	public ASTExpression(CFDGDriver driver, Token location) {
		this(driver, false, false, Locality.UnknownLocal, ExpType.NoType, location);
	}

	public ASTExpression(CFDGDriver driver, boolean isConstant, boolean isNatural, Token location) {
		this(driver, isConstant, isNatural, Locality.UnknownLocal, ExpType.NoType, location);
	}
	
	public ASTExpression(CFDGDriver driver, boolean isConstant, boolean isNatural, ExpType type, Token location) {
		this(driver, isConstant, isNatural, Locality.UnknownLocal, type, location);
	}

	public ASTExpression(CFDGDriver driver, boolean isConstant, boolean isNatural, Locality locality, Token location) {
		this(driver, isConstant, isNatural, locality, ExpType.NoType, location);
	}

	public ASTExpression(CFDGDriver driver, boolean isConstant, boolean isNatural, Locality locality, ExpType type, Token location) {
		this.driver = driver;
		this.isConstant = isConstant;
		this.isNatural = isNatural;
		this.locality = locality;
		this.type = type;
		this.location = location;
	}

	public boolean isConstant() {
		return isConstant;
	}

	public void setIsConstant(boolean isConstant) {
		this.isConstant = isConstant;
	}

	public boolean isNatural() {
		return isNatural;
	}

	public void setIsNatural(boolean isNatural) {
		this.isNatural = isNatural;
	}
	
	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality locality) {
		this.locality = locality;
	}

	public ExpType getType() {
		return type;
	}

	public void setType(ExpType type) {
		this.type = type;
	}

	public Token getLocation() {
		return location;
	}

	public void setLocation(Token location) {
		this.location = location;
	}

	public ASTExpression simplify() {
		return this;
	}

	public ASTExpression compile(CompilePhase ph) {
		return null;
	}

	public int evaluate(double[] result, int length) {
		return evaluate(result, length, null);
	}

	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		return 0;
	}

	public void evaluate(Modification result, boolean shapeDest) {
		evaluate(result, shapeDest, null);
	}

	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		throw new RuntimeException("Can't convert this expression into an adjustment"); 
	}

	public CFStackRule evalArgs(CFDGRenderer renderer, CFStackRule parent) {
		throw new RuntimeException("Can't convert this expression into a shape"); 
	}
	
	public void entropy(StringBuilder e) {
	}
	
	public ASTExpression getChild(int i) {
		if (i > 0) {
			driver.error("Expression list bounds exceeded", location);
		}
		return this;
	}

	public int size() {
		return 1;
	}

	public static ASTExpression append(ASTExpression le, ASTExpression re) {
		return (le != null && re != null) ? le.append(re) : (le != null) ? le : re;
	}

	public ASTExpression append(ASTExpression sib) {
		return sib != null ? new ASTCons(driver, location, this, sib) : this;
	}

	public int getTupleSize() {
		return 1;
	}

	protected ASTExpression compile(ASTExpression exp, CompilePhase ph) {
		if (exp == null) {
			return null;
		}
		ASTExpression tmpExp = exp.compile(ph);
		if (tmpExp != null) {
			return tmpExp;
		}
		return exp;
	}

	protected ASTExpression simplify(ASTExpression exp) {
		if (exp == null) {
			return null;
		}
		return exp.simplify();
	}
}
