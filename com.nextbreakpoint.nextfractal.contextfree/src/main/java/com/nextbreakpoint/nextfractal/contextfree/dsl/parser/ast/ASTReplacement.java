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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.PrimShape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

public class ASTReplacement extends ASTObject {
	@Getter
    private final ASTRuleSpecifier shapeSpecifier;
	@Getter
    private ASTModification childChange;
	@Setter
    @Getter
    private RepElemType repType;
	@Setter
    @Getter
    private PathOp pathOp;
	protected CFDGDriver driver;

	public ASTReplacement(Token token, CFDGDriver driver, ASTRuleSpecifier shapeSpecifier, ASTModification childChange, RepElemType repType) {
		super(token);
		this.driver = driver;
		this.repType = repType;
		this.shapeSpecifier = shapeSpecifier;
		this.pathOp = PathOp.UNKNOWN;
		this.childChange = childChange;
		if (this.childChange == null) {
			this.childChange = new ASTModification(token, driver);
		}
	}

	public ASTReplacement(Token token, CFDGDriver driver, ASTModification childChange, RepElemType repType) {
		this(token, driver, new ASTRuleSpecifier(token, driver), childChange, repType);
	}

//	public ASTReplacement(Token token, CFDGDriver driver, ASTModification childChange) {
//		this(token, driver, new ASTRuleSpecifier(token, driver), childChange, RepElemType.replacement);
//	}

	public ASTReplacement(Token token, CFDGDriver driver, String name) {
		this(token, driver, new ASTRuleSpecifier(token, driver), new ASTModification(token, driver), RepElemType.op);
		this.pathOp = PathOp.byName(name);
		if (this.pathOp == PathOp.UNKNOWN) {
			driver.error("Unknown path operation type", token);
		}
	}

	public ASTReplacement(ASTReplacement replacement) {
		this(replacement.getToken(), replacement.driver, replacement.getShapeSpecifier(), replacement.getChildChange(), replacement.getRepType());
		this.pathOp = replacement.getPathOp();
	}

    public void replace(Shape s, CFDGRenderer renderer) {
		if (shapeSpecifier.getArgSource() == ArgSource.NoArgs) {
			s.setShapeType(shapeSpecifier.getShapeType());
			s.setParameters(null);
		} else {
			s.setParameters(shapeSpecifier.evalArgs(renderer, s.getParameters()));
			CFStackRule stackRule = s.getParameters();
			if (shapeSpecifier.getArgSource() == ArgSource.SimpleParentArgs) {
				s.setShapeType(shapeSpecifier.getShapeType());
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
				driver.fail("Subpaths must be all path operation or all path command", getToken());
		}
	}

	public void compile(CompilePhase ph) {
		ASTExpression r = shapeSpecifier.compile(ph);
		assert(r == null);
		r = childChange.compile(ph);
		assert(r == null);

        switch (ph) {
            case TypeCheck -> {
                childChange.addEntropy(shapeSpecifier.getEntropy());
                if (getClass() == ASTReplacement.class && driver.isInPathContainer()) {
                    // This is a subpath
                    if (shapeSpecifier.getArgSource() == ArgSource.ShapeArgs || shapeSpecifier.getArgSource() == ArgSource.StackArgs || PrimShape.isPrimShape(shapeSpecifier.getShapeType())) {
                        if (repType != RepElemType.op) {
                            driver.error("Error in subpath specification", getToken());
                        }
                    } else {
                        ASTRule rule = driver.getRule(shapeSpecifier.getShapeType());
                        if (rule == null || rule.isPath()) {
                            driver.error("Subpath can only refer to a path", getToken());
                        } else if (rule.getRuleBody().getRepType() != repType.getType()) {
                            driver.error("Subpath type mismatch error", getToken());
                        }
                    }
                }
            }
            case Simplify -> {
                r = shapeSpecifier.simplify();
                assert (r == shapeSpecifier);
                r = childChange.simplify();
                assert (r == childChange);
            }
            default -> {
            }
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
