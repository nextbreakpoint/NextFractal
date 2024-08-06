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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PathStorage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.WeightType;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.awt.geom.PathIterator;

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

	public ASTRule(Token token, CFDGDriver driver, int nameIndex, float weight, boolean percent) {
		super(token, driver, null, RepElemType.rule);
		ruleBody = new ASTRepContainer(token, driver);
		this.nameIndex = nameIndex;
		this.path = false;
		this.weight = weight <= 0.0 ? 1.0f : weight;
		this.weightType = percent ? WeightType.PercentWeight : WeightType.ExplicitWeight;
		this.cachedPath = null;
		if (weight <= 0.0) {
			driver.warning("Rule weight coerced to 1.0", token);
		}
	}

	public ASTRule(Token token, CFDGDriver driver, int nameIndex) {
		super(token, driver, null, RepElemType.rule);
		ruleBody = new ASTRepContainer(token, driver);
		this.nameIndex = nameIndex;
		this.path = false;
		this.weight = 1.0f;
		this.weightType = WeightType.NoWeight;
		this.cachedPath = null;

		PrimShape shape = PrimShape.getShapeMap().get(nameIndex);
		if (shape != null && shape.getTotalVertices() > 0) {
			if (nameIndex != PrimShapeType.circleType.getType()) {
				double[] coords = new double[6];
				int cmd;
				for (PathIterator iterator = shape.getPathIterator(); !iterator.isDone(); iterator.next()) {
					if (isVertex(cmd = iterator.currentSegment(coords))) {
						ASTExpression a = new ASTCons(token, driver, new ASTReal(token, driver, coords[0]), new ASTReal(token, driver, coords[1]));
						ASTPathOp op = new ASTPathOp(token, driver, isMoveTo(cmd) ? PathOp.MOVETO.name() : PathOp.LINETO.name(), a);
						getRuleBody().getBody().add(op);
					}
				}
			} else {
				ASTExpression a = new ASTCons(token, driver, new ASTReal(token, driver, 0.5), new ASTReal(token, driver, 0.0));
				ASTPathOp op = new ASTPathOp(token, driver, PathOp.MOVETO.name(), a);
				getRuleBody().getBody().add(op);
				a = new ASTCons(token, driver, new ASTReal(token, driver, -0.5), new ASTReal(token, driver, 0.0), new ASTReal(token, driver, 0.5));
				op = new ASTPathOp(token, driver, PathOp.ARCTO.name(), a);
				getRuleBody().getBody().add(op);
				a = new ASTCons(token, driver, new ASTReal(token, driver, 0.5), new ASTReal(token, driver, 0.0), new ASTReal(token, driver, 0.5));
				op = new ASTPathOp(token, driver, PathOp.ARCTO.name(), a);
				getRuleBody().getBody().add(op);
			}
			getRuleBody().getBody().add(new ASTPathOp(token, driver, PathOp.CLOSEPOLY.name(), null));
			getRuleBody().setRepType(RepElemType.op.getType());
			getRuleBody().setPathOp(PathOp.MOVETO);
		}
	}

	private boolean isMoveTo(int cmd) {
		return cmd == PathIterator.SEG_MOVETO;
	}

	private boolean isVertex(int cmd) {
		return cmd >= PathIterator.SEG_MOVETO && cmd < PathIterator.SEG_CLOSE;
	}

    public void resetCachedPath() {
		this.cachedPath = null;
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		renderer.setCurrentSeed(parent.getWorldState().getRand64Seed());
		if (path) {
			renderer.processPrimShape(parent, this);
		} else {
			ruleBody.traverse(parent, tr, renderer, true);
		}
	}
	
	public void traversePath(Shape parent, CFDGRenderer renderer) {
		renderer.initTraverse();
		renderer.setCurrentSeed(parent.getWorldState().getRand64Seed());
		renderer.setRandUsed(false);
		
		ASTCompiledPath savedPath = null;
		
		if (cachedPath != null && cachedPath.getParameters() == parent.getParameters()) {
			savedPath = renderer.getCurrentPath();
			renderer.setCurrentPath(cachedPath);
			cachedPath = null;
			renderer.setCurrentCommand(renderer.getCurrentPath().getCommandInfo().iterator());
		}
		
		ruleBody.traverse(parent, false, renderer, true);
		if (!renderer.getCurrentPath().isCached()) {
			renderer.getCurrentPath().finish(true, renderer);
		}
		if (renderer.getCurrentPath().isUseTerminal()) {
			renderer.getCurrentPath().getTerminalCommand().traverse(parent, false, renderer);
		}
		
		if (savedPath != null) {
			cachedPath = renderer.getCurrentPath();
			renderer.setCurrentPath(savedPath);
		} else {
			if (!renderer.isRandUsed() && cachedPath == null) {
				cachedPath = renderer.getCurrentPath();
				cachedPath.setCached(true);
				cachedPath.setParameters(parent.getParameters());
				renderer.setCurrentPath(new ASTCompiledPath(getToken(), driver));
			} else {
				renderer.getCurrentPath().setPathStorage(new PathStorage());
				renderer.getCurrentPath().getCommandInfo().clear();
				renderer.getCurrentPath().setUseTerminal(false);
				renderer.getCurrentPath().setPathUID(ASTCompiledPath.nextPathUID());
				renderer.getCurrentPath().setParameters(null);
			}
		}
	}

	@Override
	public void compile(CompilePhase ph) {
		driver.setInPathContainer(path);
		super.compile(ph);
		ruleBody.compile(ph, null, null);
	}

	@Override
	public int compareTo(ASTRule o) {
		return nameIndex == o.nameIndex ? ((weight < o.weight) ? -1 : 1) : ((nameIndex < o.nameIndex) ? -1 : 1);
	}
}
