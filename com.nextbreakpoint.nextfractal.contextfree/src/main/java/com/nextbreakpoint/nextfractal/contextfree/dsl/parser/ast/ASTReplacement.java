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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGStopException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;

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
public class ASTReplacement extends ASTObject {
	private final ASTRuleSpecifier shapeSpecifier;
	private final ASTModification childChange;
	@Setter
    private RepElemType repType;
	@Setter
    private PathOp pathOp;

	public ASTReplacement(CFDGSystem system, ASTWhere where, ASTRuleSpecifier shapeSpecifier, ASTModification mods, RepElemType repType) {
		super(system, where);
		this.repType = repType;
		this.shapeSpecifier = shapeSpecifier;
		this.pathOp = PathOp.UNKNOWN;
		if (mods != null) {
			childChange = mods;
		} else {
			childChange = new ASTModification(system, getWhere());
		}
	}

	public ASTReplacement(CFDGSystem system, ASTWhere where, ASTModification mod, RepElemType repType) {
		this(system, where, new ASTRuleSpecifier(system, where), mod, repType);
	}

	public ASTReplacement(CFDGSystem system, ASTWhere where, String name) {
		this(system, where, new ASTRuleSpecifier(system, where), new ASTModification(system, where), RepElemType.op);
		this.pathOp = PathOp.byName(name);
		if (this.pathOp == PathOp.UNKNOWN) {
			system.error("Unknown path operation type", where);
		}
	}

    public void replace(CFDGBuilder builder, CFDGRenderer renderer, Shape shape) {
		if (shapeSpecifier.getArgSource() == ArgSource.NoArgs) {
			shape.setShapeType(shapeSpecifier.getShapeType());
			shape.setParameters(null);
		} else {
			shape.setParameters(shapeSpecifier.evalArgs(builder, renderer, shape.getParameters()));
			final CFStackRule stackRule = shape.getParameters();
			if (shapeSpecifier.getArgSource() == ArgSource.SimpleParentArgs) {
				shape.setShapeType(shapeSpecifier.getShapeType());
			} else {
				shape.setShapeType(stackRule.getRuleName());
			}
			if (stackRule != null && stackRule.getParamCount() == 0) {
				shape.setParameters(null);
			}
		}
		renderer.getCurrentSeed().add(childChange.getModData().getRand64Seed());
		//TODO do we need to call getCurrentSeed?
		renderer.getCurrentSeed();
		childChange.evaluate(builder, renderer, shape.getWorldState(), true);
		shape.setAreaCache(shape.getWorldState().area());
	}

	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		Shape child = (Shape)parent.clone();
		switch (repType) {
			case replacement:
				replace(builder, renderer, child);
				child.getWorldState().setRand64Seed(renderer.getCurrentSeed());
				renderer.processShape(child);
				break;
			case op:
				if (!tr) child.getWorldState().getTransform().setToIdentity();
				// fall through
			case mixed:
			case command:
				replace(builder, renderer, child);
				renderer.processSubpath(child, tr || repType == RepElemType.op, repType);
				break;
            default:
				throw new CFDGStopException("Subpaths must be all path operation or all path command", getWhere());
		}
	}

	public void compile(CFDGBuilder builder, CompilePhase phase) {
		ASTExpression r = shapeSpecifier.compile(builder, phase);
		assert(r == null);
		r = childChange.compile(builder, phase);
		assert(r == null);

        switch (phase) {
            case TypeCheck -> {
                childChange.addEntropy(shapeSpecifier.getEntropy());
                if (getClass() == ASTReplacement.class && builder.isInPathContainer()) {
                    // This is a subpath
                    if (shapeSpecifier.getArgSource() == ArgSource.ShapeArgs || shapeSpecifier.getArgSource() == ArgSource.StackArgs || PrimShape.isPrimShape(shapeSpecifier.getShapeType())) {
                        if (repType != RepElemType.op) {
                            system.error("Error in subpath specification", getWhere());
                        }
						if (shapeSpecifier.getShapeType() == PrimShapeType.fillType.getType()) {
							system.error("FILL cannot be a subpath", getWhere());
						}
                    } else {
                        ASTRule rule = builder.getRule(shapeSpecifier.getShapeType());
                        if (rule == null || !rule.isPath()) {
                            system.error("Subpath can only refer to a path", getWhere());
                        } else if (rule.getRuleBody().getRepType() != repType.getType()) {
                            system.error("Subpath type mismatch error", getWhere());
                        }
                    }
                }
            }
            case Simplify -> {
                r = shapeSpecifier.simplify(builder);
                assert (r == null);
                r = childChange.simplify(builder);
                assert (r == null);
            }
            default -> {
            }
        }
	}
}
