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
import lombok.Getter;

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

@Getter
public class ASTStartSpecifier extends ASTRuleSpecifier {
	private final ASTModification modification;

	public ASTStartSpecifier(CFDGSystem system, ASTWhere where, int nameIndex, String name, ASTExpression args, ASTModification mod) {
		super(system, where, nameIndex, name, args, null);
		this.modification = mod;
	}
	
	public ASTStartSpecifier(CFDGSystem system, ASTWhere where, int nameIndex, String name, ASTModification mod) {
		super(system, where, nameIndex, name, null, null);
		this.modification = mod;
	}
	
	public ASTStartSpecifier(CFDGSystem system, ASTWhere where, ASTExpression args, ASTModification mod) {
		super(system, where, args);
		this.modification = mod;
	}

    @Override
	public void entropy(StringBuilder entropy) {
		entropy.append(getEntropy());
		if (modification != null) {
			modification.entropy(entropy);
		}
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		super.simplify(builder);
		if (modification != null) {
			modification.simplify(builder);
		}
		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		final String name = getEntropy();
		super.compile(builder, phase);
		setEntropy(name);
		if (modification != null) {
			modification.compile(builder, phase);
		}
		return null;
	}
}
