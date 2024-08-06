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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ASTDefine extends ASTReplacement {
	private final List<ASTParameter> parameters = new ArrayList<>();
	private final String name;
	private int configDepth;
	private DefineType defineType;
	private ExpType expType;
	private ASTExpression exp;
	private int tupleSize;
	private int stackCount;
    private boolean natural;

	public ASTDefine(CFDGDriver driver, String name, Token location) {
		super(driver, new ASTModification(driver, location), RepElemType.empty, location);
		this.name = name;
		this.configDepth = -1;
		this.defineType = DefineType.Stack;
		this.expType = ExpType.None;
		this.stackCount = 0;
		this.natural = false;
		int[] i = new int[1];
		getChildChange().getModData().getRand64Seed().xorString(name, i);
	}

    public void incStackCount(int value) {
		stackCount += value;
	}
	
	public void decStackCount(int value) {
		stackCount -= value;
	}
	
	@Override
	public void compile(CompilePhase ph) {
		if (defineType == DefineType.Function || defineType == DefineType.Let) {
			ASTRepContainer tempCont = new ASTRepContainer(driver);
			tempCont.setParameters(parameters);
			tempCont.setStackCount(stackCount);
			driver.pushRepContainer(tempCont);
			super.compile(ph);
			exp = compile(exp, ph);
			if (ph == CompilePhase.Simplify) {
				exp = simplify(exp);
			}
			driver.popRepContainer(null);
		} else {
			super.compile(ph);
			exp = compile(exp, ph);
			if (ph == CompilePhase.Simplify) {
				exp = simplify(exp);
			}
		}

        switch (ph) {
            case TypeCheck -> {
                if (defineType == DefineType.Config) {
                    driver.makeConfig(this);
                    return;
                }

                getChildChange().setEntropyIndex(0);
                getChildChange().addEntropy(name);

                ExpType t = exp != null ? exp.getType() : ExpType.Mod;
                int sz = 1;
                if (t == ExpType.Numeric) {
                    sz = exp.evaluate(null, 0);
                }
                if (t == ExpType.Mod) {
                    sz = 6;
                }
                if (defineType == DefineType.Function) {
                    if (t != getExpType()) {
                        driver.error("Mismatch between declared and defined type of user function", getToken());
                    }
                    if (getExpType() == ExpType.Numeric && t == ExpType.Numeric && sz != tupleSize) {
                        driver.error("Mismatch between declared and defined vector length of user function", getToken());
                    }
                    if (isNatural() && (exp == null || exp.isNatural())) {
                        driver.error("Mismatch between declared natural and defined not-natural type of user function", getToken());
                    }
                } else {
                    if (getShapeSpecifier().getShapeType() >= 0) {
                        ASTDefine[] func = new ASTDefine[1];
                        @SuppressWarnings("unchecked")
                        List<ASTParameter>[] shapeParams = new List[1];
                        driver.getTypeInfo(getShapeSpecifier().getShapeType(), func, shapeParams);
                        if (func[0] != null) {
                            driver.error("Variable name is also the name of a function", getToken());
                            driver.error("function definition is here", func[0].getToken());
                        }
                        if (shapeParams[0] != null) {
                            driver.error("Variable name is also the name of a shape", getToken());
                        }
                    }

                    tupleSize = sz;
                    expType = t;
                    //TODO controllare
                    if (t.getType() != (t.getType() & (-t.getType())) || t.getType() == 0) {
                        driver.error("Expression can only have one type", getToken());
                    }
                    if (defineType == DefineType.Stack && (exp != null ? exp.isConstant() : getChildChange().getModExp().isEmpty())) {
                        defineType = DefineType.Const;
                    }
                    natural = exp != null && exp.isNatural() && expType == ExpType.Numeric;
                    ASTParameter param = driver.getContainerStack().peek().addDefParameter(getShapeSpecifier().getShapeType(), this, getToken());
                    if (param.isParameter() || param.getDefinition() == null) {
                        param.setStackIndex(driver.getLocalStackDepth());
                        driver.getContainerStack().peek().setStackCount(driver.getContainerStack().peek().getStackCount() + param.getTupleSize());
                        driver.setLocalStackDepth(driver.getLocalStackDepth() + param.getTupleSize());
                    }
                }
            }
            case Simplify -> {
                // do nothing
            }
            default -> {
            }
        }
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		if (defineType != DefineType.Stack) {
			return;
		}
		if (renderer.getStackSize() + tupleSize > renderer.getMaxStackSize()) {
			driver.error("Maximum stack depth exceeded", getToken());
		}

		renderer.setStackSize(renderer.getStackSize() + tupleSize);
		renderer.getCurrentSeed().add(getChildChange().getModData().getRand64Seed());

        switch (expType) {
            case Numeric -> {
                double[] result = new double[1];
                if (exp.evaluate(result, tupleSize, renderer) != tupleSize) {
                    driver.error("Error evaluating parameters (too many or not enough)", null);
                }
                renderer.setStackItem(renderer.getStackSize() - 1, new CFStackNumber(renderer.getStack(), result[0]));
            }
            case Mod -> {
                Modification[] mod = new Modification[1];
                getChildChange().setVal(mod, renderer);
                renderer.setStackItem(renderer.getStackSize() - 1, new CFStackModification(renderer.getStack(), mod[0]));
            }
            case Rule ->
                    renderer.setStackItem(renderer.getStackSize() - 1, exp.evalArgs(renderer, parent.getParameters()));
            default -> driver.error("Unimplemented parameter type", null);
        }

		renderer.setLogicalStackTop(renderer.getStackSize());
	}
}
