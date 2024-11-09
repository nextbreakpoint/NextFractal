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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Vertex;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.WeightType;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.PathIterator;
import java.util.Iterator;
import java.util.Objects;

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

public class ASTRule extends ASTReplacement implements Comparable<ASTRule> {
	private ASTCompiledPath cachedPath;
	@Getter
    private final ASTRepContainer ruleBody;
	@Getter
    private final WeightType weightType;
	@Setter
    @Getter
    private double weight;
	@Setter
    @Getter
    private boolean path;
	@Setter
    @Getter
    private int nameIndex;

	public ASTRule(CFDGSystem system, ASTWhere where, int nameIndex, float weight, boolean percent) {
		super(system, where, null, RepElemType.rule);
		this.nameIndex = nameIndex;
		this.weight = weight <= 0.0 ? 1.0f : weight;
		path = false;
		weightType = percent ? WeightType.PercentWeight : WeightType.ExplicitWeight;
		cachedPath = null;
		ruleBody = new ASTRepContainer(system, where);
		if (weight <= 0.0) {
			system.warning("Rule weight coerced to 1.0", where);
		}
	}

	public ASTRule(CFDGSystem system, ASTWhere where, int nameIndex) {
		super(system, where, null, RepElemType.rule);
		this.nameIndex = nameIndex;
		weight = 1.0f;
		path = false;
		weightType = WeightType.NoWeight;
		cachedPath = null;
		ruleBody = new ASTRepContainer(system, where);
	}

	public ASTRule(CFDGSystem system, int nameIndex) {
		super(system, null, null, RepElemType.rule);
		this.nameIndex = nameIndex;
		weight = 1.0f;
		path = true;
		weightType = WeightType.NoWeight;
		cachedPath = null;
		ruleBody = new ASTRepContainer(system, where);

		final PrimShape shape = PrimShape.getShapeMap().get(nameIndex);
		if (shape != null && shape.getTotalVertices() > 0) {
			if (nameIndex != PrimShapeType.circleType.getType()) {
				for (Iterator<Vertex> iterator = shape.getPathIterator(); !iterator.hasNext();) {
					Vertex v = iterator.next();
					if (isVertex(v.command())) {
						ASTExpression a = new ASTCons(this.system, where, new ASTReal(system, where, v.point().x), new ASTReal(system, where, v.point().y));
						ASTPathOp op = new ASTPathOp(system, where, isMoveTo(v.command()) ? PathOp.MOVETO.name() : PathOp.LINETO.name(), a);
						ruleBody.getBody().add(op);
					}
				}
			} else {
				ASTExpression a = new ASTCons(this.system, where, new ASTReal(system, where, 0.5), new ASTReal(system, where, 0.0));
				ASTPathOp op = new ASTPathOp(system, where, PathOp.MOVETO.name(), a);
				ruleBody.getBody().add(op);
				a = new ASTCons(this.system, where, new ASTReal(system, where, -0.5), new ASTReal(system, where, 0.0), new ASTReal(system, where, 0.5));
				op = new ASTPathOp(system, where, PathOp.ARCTO.name(), a);
				ruleBody.getBody().add(op);
				a = new ASTCons(this.system, where, new ASTReal(system, where, 0.5), new ASTReal(system, where, 0.0), new ASTReal(system, where, 0.5));
				op = new ASTPathOp(system, where, PathOp.ARCTO.name(), a);
				ruleBody.getBody().add(op);
			}
			ruleBody.getBody().add(new ASTPathOp(system, where, PathOp.CLOSEPOLY.name(), null));
			ruleBody.setRepType(RepElemType.op.getType());
			ruleBody.setPathOp(PathOp.MOVETO);
		}
	}

	@Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
	}

	public void traverseRule(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		renderer.setCurrentSeed(parent.getWorldState().getRand64Seed());
		if (path) {
			renderer.processPrimShape(parent, this);
		} else {
			ruleBody.traverse(builder, renderer, parent, tr, true);
		}
	}
	
	public void traversePath(CFDGBuilder builder, CFDGRenderer renderer, Shape parent) {
		//TODO check initTraverse
		renderer.init();
		renderer.setCurrentSeed(parent.getWorldState().getRand64Seed());
		renderer.setRandUsed(false);
		
		ASTCompiledPath savedPath = null;

		//TODO verify equals
		if (cachedPath != null && Objects.equals(cachedPath.getParameters(), parent.getParameters())) {
			savedPath = renderer.getCurrentPath();
			renderer.setCurrentPath(cachedPath);
			renderer.setCurrentCommand(renderer.getCurrentPath().getCommandInfo().iterator());
		} else {
			renderer.getCurrentPath().getTerminalCommand().setWhere(getWhere());
		}
		
		ruleBody.traverse(builder, renderer, parent, false, true);
		if (!renderer.getCurrentPath().isCached()) {
			renderer.getCurrentPath().finish(true, renderer);
		}
		if (renderer.getCurrentPath().isUseTerminal()) {
			renderer.getCurrentPath().getTerminalCommand().traverse(builder, renderer, parent, false);
		}

		if (savedPath != null) {
			cachedPath = renderer.getCurrentPath();
			renderer.setCurrentPath(savedPath);
		} else {
			if (!renderer.isRandUsed() && cachedPath == null) {
				cachedPath = renderer.getCurrentPath();
				cachedPath.setCached(true);
				cachedPath.setParameters(parent.getParameters());
				renderer.setCurrentPath(new ASTCompiledPath(system, getWhere()));
			} else {
				final ASTCompiledPath currentPath = renderer.getCurrentPath();
				currentPath.getPath().removeAll();
				currentPath.getCommandInfo().clear();
				currentPath.setUseTerminal(false);
				currentPath.setPathUID(ASTCompiledPath.nextPathUID());
				currentPath.setParameters(null);
			}
		}
	}

	@Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		builder.setInPathContainer(path);
		super.compile(builder, phase);
		ruleBody.compile(builder, phase, null, null);
		builder.setInPathContainer(false);
	}

	@Override
	public int compareTo(ASTRule o) {
		return nameIndex == o.nameIndex ? Double.compare(weight, o.weight) : Integer.compare(nameIndex, o.nameIndex);
	}

	public void resetCachedPath() {
		this.cachedPath = null;
	}

	private boolean isMoveTo(int cmd) {
		return cmd == PathIterator.SEG_MOVETO;
	}

	private boolean isVertex(int cmd) {
		return cmd >= PathIterator.SEG_MOVETO && cmd < PathIterator.SEG_CLOSE;
	}
}
