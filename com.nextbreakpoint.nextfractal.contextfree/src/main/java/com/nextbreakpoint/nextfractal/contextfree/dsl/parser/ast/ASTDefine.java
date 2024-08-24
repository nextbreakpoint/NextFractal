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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// astreplacement.cpp
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

@Getter
@Setter
public class ASTDefine extends ASTReplacement {
	private List<ASTParameter> parameters = new ArrayList<>();
	private DefineType defineType;
	private ASTExpression exp;
	private ExpType expType;
	private final String name;
	private int tupleSize;
	private int configDepth;
	private int paramSize;
    private boolean natural;

	public ASTDefine(CFDGSystem system, ASTWhere where, String name) {
		super(system, where, null, RepElemType.empty);
		this.defineType = DefineType.Stack;
		this.expType = ExpType.None;
		this.name = name;
		this.configDepth = -1;
		this.paramSize = 0;
		this.natural = false;
        final int[] i = new int[1];
        getChildChange().getModData().getRand64Seed().getSeed();
		getChildChange().getModData().getRand64Seed().xorString(name, i);
	}

	@Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		if (defineType == DefineType.Function || defineType == DefineType.Let) {
            final ASTRepContainer tempCont = new ASTRepContainer(system, getWhere());
			tempCont.setParameters(parameters);
            builder.pushRepContainer(tempCont);
			super.compile(builder, phase);
			exp = ASTExpression.compile(builder, phase, exp);
			if (phase == CompilePhase.Simplify) {
				exp = ASTExpression.simplify(builder, exp);
			}
            builder.popRepContainer(null);
		} else {
			super.compile(builder, phase);
			exp = ASTExpression.compile(builder, phase, exp);
			if (phase == CompilePhase.Simplify) {
				exp = ASTExpression.simplify(builder, exp);
			}
		}

        switch (phase) {
            case TypeCheck -> {
                if (defineType == DefineType.Config) {
                    builder.typeCheckConfig(this);
                    return;
                }

                getChildChange().getModData().getRand64Seed().getSeed();
                getChildChange().setEntropyIndex(0);
                getChildChange().addEntropy(name);

                final ExpType t = exp != null ? exp.getType() : ExpType.Mod;
                int sz = 1;
                if (t == ExpType.Numeric) {
                    sz = exp.evaluate(builder, null, 0);
                }
                if (t == ExpType.Mod) {
                    sz = 6;
                }
                if (defineType == DefineType.Function) {
                    if (t != getExpType()) {
                        system.error("Mismatch between declared and defined type of user function", getWhere());
                    }
                    if (getExpType() == ExpType.Numeric && t == ExpType.Numeric && sz != tupleSize) {
                        system.error("Mismatch between declared and defined vector length of user function", getWhere());
                    }
                    if (isNatural() && (exp == null || exp.isNatural())) {
                        system.error("Mismatch between declared natural and defined not-natural type of user function", getWhere());
                    }
                } else {
                    if (getShapeSpecifier().getShapeType() >= 0) {
                        final ASTDefine[] func = new ASTDefine[1];
                        @SuppressWarnings("unchecked")
                        final List<ASTParameter>[] shapeParams = new List[1];
                        builder.getTypeInfo(getShapeSpecifier().getShapeType(), func, shapeParams);
                        if (func[0] != null) {
                            system.error("Variable name is also the name of a function", getWhere());
                            system.error("function definition is here", func[0].getWhere());
                        }
                        if (shapeParams[0] != null) {
                            system.error("Variable name is also the name of a shape", getWhere());
                        }
                    }

                    tupleSize = sz;
                    expType = t;
                    if (t.getType() != (t.getType() & (-t.getType())) || t.getType() == 0) {
                        system.error("Expression can only have one type", getWhere());
                    }
                    if (defineType == DefineType.Stack && (exp != null ? exp.isConstant() : getChildChange().getModExp().isEmpty())) {
                        defineType = DefineType.Const;
                    }
                    natural = exp != null && exp.isNatural() && expType == ExpType.Numeric;
                    final ASTParameter param = builder.getContainerStack()
                            .peek()
                            .addDefParameter(getShapeSpecifier().getShapeType(), this, getWhere());
                    if (defineType == DefineType.Stack) {
                        param.setStackIndex(builder.getLocalStackDepth());
                        builder.setLocalStackDepth(builder.getLocalStackDepth() + param.getTupleSize());
                    }
                }
            }
            case Simplify -> {
                if (defineType == DefineType.Config) {
                    builder.makeConfig(this);
                }
            }
            default -> {
            }
        }
	}

	@Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		if (defineType != DefineType.Stack) {
			return;
		}
		if (renderer.getStackSize() + tupleSize > renderer.getMaxStackSize()) {
			system.error("Maximum stack depth exceeded", getWhere());
		}

        //TODO verify stack behaviour
        final int stackIndex = renderer.getStackSize();
		renderer.setStackSize(renderer.getStackSize() + tupleSize);
		renderer.getCurrentSeed().add(getChildChange().getModData().getRand64Seed());

        switch (expType) {
            case Numeric -> {
                final double[] value = new double[1];
                if (exp.evaluate(builder, renderer, value, tupleSize) != tupleSize) {
                    system.error("Error evaluating parameters (too many or not enough)", getWhere());
                }
                final CFStackNumber item = new CFStackNumber(renderer.getStack(), value[0]);
                renderer.setStackItem(stackIndex, item);
            }
            case Mod -> {
                final Modification[] mod = new Modification[1];
                getChildChange().setVal(builder, renderer, mod);
                final CFStackModification item = new CFStackModification(renderer.getStack(), mod[0]);
                renderer.setStackItem(stackIndex, item);
            }
            case Rule -> {
                final CFStackRule item = exp.evalArgs(builder, renderer, parent.getParameters());
                renderer.setStackItem(stackIndex, item);
            }
            default -> system.error("Unimplemented parameter type", getWhere());
        }

		renderer.setLogicalStackTop(renderer.getStackSize());
	}

    public void incParamSize(int value) {
        paramSize += value;
    }

    public void decParamSize(int value) {
        paramSize -= value;
    }
}
