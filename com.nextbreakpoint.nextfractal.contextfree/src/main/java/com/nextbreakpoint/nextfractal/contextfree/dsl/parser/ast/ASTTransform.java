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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

// astreplacement.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
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
public class ASTTransform extends ASTReplacement {
	private final ASTRepContainer body;
	@Setter
    private ASTExpression expHolder;
	@Setter
    private boolean clone;
	
	public ASTTransform(CFDGSystem system, ASTWhere where, ASTExpression exp) {
		super(system, where, null, RepElemType.empty);
		body = new ASTRepContainer(this.system, where);
		this.expHolder = exp;
		this.clone = false;
	}

    @Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		super.compile(builder, phase);
		ASTExpression ret = null;
		if (expHolder != null) {
			ret = expHolder.compile(builder, phase);
		}
		if (ret != null) {
			system.error("Error analyzing transform list", getWhere());
		}
		body.compile(builder, phase, null, null);

        switch (phase) {
            case TypeCheck -> {
                if (clone && !ASTParameter.Impure) {
                    system.error("Shape cloning only permitted in impure mode", getWhere());
                }
            }
            case Simplify -> {
				expHolder = ASTExpression.simplify(builder, expHolder);
            }
            default -> {
            }
        }
	}

	@Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		final AffineTransform dummy = new AffineTransform();
		final List<AffineTransform> transforms = new ArrayList<>();
		final List<ASTModification> mods = AST.getTransforms(builder, expHolder, transforms, renderer, false, dummy);

		final Rand64 cloneSeed = renderer.getCurrentSeed();
		final Shape transChild = (Shape)parent.clone();
		final boolean opsOnly = body.getRepType() == RepElemType.op.getType();
		if (opsOnly && !tr) {
			transChild.getWorldState().getTransform().setToIdentity();
		}

		final int modsLength = mods.size();
		final int totalLength = modsLength + transforms.size();
		for (int i = 0; i < totalLength; i++) {
			final Shape child = (Shape)transChild.clone();
            if (i < modsLength) {
				mods.get(i).evaluate(builder, renderer, child.getWorldState(), true);
			} else {
				child.getWorldState().getTransform().concatenate(transforms.get(i - modsLength));
			}
			renderer.getCurrentSeed();
			final int size = renderer.getStackSize();
			for (ASTReplacement rep : body.getBody()) {
				if (clone) {
					renderer.setCurrentSeed(cloneSeed);
				}
				rep.traverse(builder, renderer, child, tr || opsOnly);
			}
			renderer.unwindStack(size, body.getParameters());
		}
	}
}
