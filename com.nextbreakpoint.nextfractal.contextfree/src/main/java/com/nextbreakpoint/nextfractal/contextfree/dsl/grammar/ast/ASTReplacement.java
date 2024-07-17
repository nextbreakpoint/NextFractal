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
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import org.antlr.v4.runtime.Token;

public class ASTReplacement {
	private ASTRuleSpecifier shapeSpec;
	private ASTModification childChange;
	private RepElemType repType;
	private PathOp pathOp;
	protected Token location;
	protected CFDGDriver driver;

	public ASTReplacement(CFDGDriver driver, ASTRuleSpecifier shapeSpec, ASTModification childChange, RepElemType repType, Token location) {
		this.driver = driver;
		this.repType = repType;
		this.shapeSpec = shapeSpec;
		this.pathOp = PathOp.UNKNOWN;
		this.location = location;
		this.childChange = childChange;
		if (this.childChange == null) {
			this.childChange = new ASTModification(driver, location);
		}
	}

	public ASTReplacement(CFDGDriver driver, ASTModification childChange, RepElemType repType, Token location) {
		this(driver, new ASTRuleSpecifier(driver, location), childChange, repType, location);
	}

//	public ASTReplacement(CFDGDriver driver, ASTModification childChange, Token location) {
//		this(driver, new ASTRuleSpecifier(driver, location), childChange, RepElemType.replacement, location);
//	}

	public ASTReplacement(CFDGDriver driver, String name, Token location) {
		this(driver, new ASTRuleSpecifier(driver, location), new ASTModification(driver, location), RepElemType.op, location);
		this.pathOp = PathOp.byName(name);
		if (this.pathOp == PathOp.UNKNOWN) {
			driver.error("Unknown path operation type", location);
		}
	}

	public ASTReplacement(ASTReplacement replacement) {
		this(replacement.driver, replacement.getShapeSpecifier(), replacement.getChildChange(), replacement.getRepType(), replacement.getLocation());
		this.pathOp = replacement.getPathOp();
	}

	public Token getLocation() {
		return location;
	}

	public ASTRuleSpecifier getShapeSpecifier() {
		return shapeSpec;
	}

	public ASTModification getChildChange() {
		return childChange;
	}

	public RepElemType getRepType() {
		return repType;
	}
	
	public void setRepType(RepElemType repType) {
		this.repType = repType;
	}
	
	public PathOp getPathOp() {
		return pathOp;
	}

	public void setPathOp(PathOp pathOp) {
		this.pathOp = pathOp;
	}

	public void replace(Shape s, CFDGRenderer renderer) {
		if (shapeSpec.getArgSource() == ArgSource.NoArgs) {
			s.setShapeType(shapeSpec.getShapeType());
			s.setParameters(null);
		} else {
			s.setParameters(shapeSpec.evalArgs(renderer, s.getParameters()));
			CFStackRule stackRule = s.getParameters();
			if (shapeSpec.getArgSource() == ArgSource.SimpleParentArgs) {
				s.setShapeType(shapeSpec.getShapeType());
			} else {
				s.setShapeType(stackRule.getRuleName());
			}
			if (stackRule != null && stackRule.getParamCount() == 0) {
				s.setParameters(null);
			}
		}
		renderer.getCurrentSeed().add(childChange.getModData().getRand64Seed());
		childChange.evaluate(s.getWorldState(), true, renderer);
		s.setAreaCache(s.getWorldState().area());
	}

	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		Shape child = (Shape)parent.clone();
		switch (repType) {
			case replacement:
				replace(child, renderer);
				child.getWorldState().setRand64Seed(renderer.getCurrentSeed());
				renderer.processShape(child);
				break;
			case op:
				if (!tr) child.getWorldState().getTransform().setToIdentity();
			case mixed:
			case command:
				replace(child, renderer);
				renderer.processSubpath(child, tr || repType == RepElemType.op, repType);
				break;
			default:
				driver.fail("Subpaths must be all path operation or all path command", location);
		}
	}

	public void compile(CompilePhase ph) {
		ASTExpression r = shapeSpec.compile(ph);
		assert(r == null);
		r = childChange.compile(ph);
		assert(r == null);

		switch (ph) {
			case TypeCheck: 
				childChange.addEntropy(shapeSpec.getEntropy());
				if (getClass() == ASTReplacement.class && driver.isInPathContainer()) {
					// This is a subpath
					if (shapeSpec.getArgSource() == ArgSource.ShapeArgs || shapeSpec.getArgSource() == ArgSource.StackArgs || PrimShape.isPrimShape(shapeSpec.getShapeType())) {
						if (repType != RepElemType.op) {
							driver.error("Error in subpath specification", location);
						}
					} else {
						ASTRule rule = driver.getRule(shapeSpec.getShapeType());
						if (rule == null || rule.isPath()) {
							driver.error("Subpath can only refer to a path", location);
						} else if (rule.getRuleBody().getRepType() != repType.getType()) {
							driver.error("Subpath type mismatch error", location);
						}
					}
				}
				break;
	
			case Simplify: 
				r = shapeSpec.simplify();
				assert(r == shapeSpec);
				r = childChange.simplify();
				assert(r == childChange);
				break;
	
			default:
				break;
		}
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
