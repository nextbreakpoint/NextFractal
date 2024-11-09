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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;

// astexpression.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2009-2014 John Horigan - john@glyphic.com
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

@Setter
@Getter
public class ASTExpression extends ASTObject {
	protected boolean constant;
	protected boolean natural;
	protected Locality locality;
	protected ExpType type;

	public ASTExpression(CFDGSystem system, ASTWhere where) {
		this(system, where, false, false, Locality.UnknownLocal, ExpType.None);
	}

	public ASTExpression(CFDGSystem system, ASTWhere where, boolean constant, boolean natural) {
		this(system, where, constant, natural, Locality.UnknownLocal, ExpType.None);
	}
	
	public ASTExpression(CFDGSystem system, ASTWhere where, boolean constant, boolean natural, ExpType type) {
		this(system, where, constant, natural, Locality.UnknownLocal, type);
	}

	public ASTExpression(CFDGSystem system, ASTWhere where, boolean constant, boolean natural, Locality locality) {
		this(system, where, constant, natural, locality, ExpType.None);
	}

	public ASTExpression(CFDGSystem system, ASTWhere where, boolean constant, boolean natural, Locality locality, ExpType type) {
		super(system, where);
		this.constant = constant;
		this.natural = natural;
		this.locality = locality;
		this.type = type;
	}

    public ASTExpression simplify(CFDGBuilder builder) {
		return this;
	}

	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		return null;
	}

	public int evaluate(CFDGBuilder builder, double[] result, int length) {
		return evaluate(builder, null, result, length);
	}

	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		return 0;
	}

	public void evaluate(CFDGBuilder builder, Modification mod, boolean shapeDest) {
		evaluate(builder, null, mod, shapeDest);
	}

	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		system.error("Cannot convert this expression into an adjustment", getWhere());
	}

	public CFStackRule evalArgs(CFDGBuilder builder, CFDGRenderer renderer, CFStackRule parent) {
		system.error("Cannot convert this expression into a shape", getWhere());
		return parent;
	}
	
	public void entropy(StringBuilder entropy) {
	}
	
	public ASTExpression getChild(int index) {
		if (index != 0) {
			system.error("Expression list bounds exceeded", getWhere());
		}
		return this;
	}

	public int size() {
		return 1;
	}

	// Always returns nullptr except during type check in the following cases:
	// * An ASTvariable bound to a constant returns a copy of the constant
	// * An ASTvariable bound to a rule spec returns an ASTruleSpec that
	//   acts as a stack variable
	// * A shape spec that was parsed as an ASTuserFunc because of grammar
	//   ambiguity will return the correct ASTruleSpec
	//
	// It is safe to ignore the return value if you can guarantee that none
	// of these conditions is possible. Otherwise you must replace the object
	// with the returned object. Using the original object after type check
	// will fail.
	public static ASTExpression append(ASTExpression left, ASTExpression right) {
		return (left != null && right != null) ? left.append(right) : (left != null) ? left : right;
	}

	public ASTExpression append(ASTExpression sibling) {
		return sibling != null ? new ASTCons(system, getWhere(), this, sibling) : this;
	}

	protected static ASTExpression compile(CFDGBuilder builder, CompilePhase phase, ASTExpression exp) {
		if (exp == null) {
			return null;
		}
		ASTExpression tmpExp = exp.compile(builder, phase);
		if (tmpExp != null) {
			return tmpExp;
		}
		return exp;
	}

	protected static ASTExpression simplify(CFDGBuilder builder, ASTExpression exp) {
		if (exp == null) {
			return null;
		}
		ASTExpression tmpExp = exp.simplify(builder);
		if (tmpExp != null) {
			return tmpExp;
		}
		return exp;
	}
}
