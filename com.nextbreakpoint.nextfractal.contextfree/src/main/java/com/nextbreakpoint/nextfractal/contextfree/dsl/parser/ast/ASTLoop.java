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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGStopException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
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
public class ASTLoop extends ASTReplacement {
	private final ASTRepContainer loopBody;
	private final ASTRepContainer finallyBody;
	private final int loopNameIndex;
	private final String loopName;
	private final double[] loopData;
	private ASTExpression loopArgs;
	@Setter
    private ASTModification loopModHolder;

	public ASTLoop(CFDGSystem system, ASTWhere where, int nameIndex, String name, ASTExpression args, ASTModification mods) {
		super(system, where, mods, RepElemType.empty);
		loopBody = new ASTRepContainer(system, where);
		finallyBody = new ASTRepContainer(system, where);
		loopNameIndex = nameIndex;
		loopArgs = args;
		loopModHolder = null;
		loopData = new double[] { 0, 0, 0 };
		loopName = name;
		loopBody.addLoopParameter(loopNameIndex, where);
		finallyBody.addLoopParameter(loopNameIndex, where);
	}

	@Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		super.compile(builder, phase);
		loopArgs = ASTExpression.compile(builder, phase, loopArgs);

        switch (phase) {
            case TypeCheck -> {
                if (loopArgs == null) {
                    system.error("A loop must have one to three index parameters", getWhere());
					return;
                }

                final StringBuilder entropy = new StringBuilder();
                entropy.append(loopName);
                loopArgs.entropy(entropy);
                if (loopModHolder != null) {
                    getChildChange().addEntropy(entropy.toString());
                }

                boolean bodyNatural = false;
                boolean finallyNatural = false;
                final Locality locality = loopArgs.getLocality();

				final int c = loopArgs.evaluate(builder, null, 0);
				if (c < 1 || c > 3) {
					system.error("A loop must have one to three index parameters", getWhere());
					//TODO missing return. is it a bug?
				}

				if (loopArgs.isConstant()) {
					bodyNatural = finallyNatural = loopArgs.isNatural();
                } else {
                    for (int i = 0, count = 0; i < loopArgs.size(); i++) {
                        final ASTExpression loopArg = loopArgs.getChild(i);
                        final int num = loopArg.evaluate(builder, null, 0);
                        switch (count) {
                            case 0:
                                if (loopArg.isNatural()) {
                                    bodyNatural = finallyNatural = true;
                                }
                                break;
                            case 2:
                                // Special case: if 1st & 2nd args are natural and 3rd
                                // is -1 then that is ok
                                final double[] step = new double[1];
                                if (loopArg.isConstant() && loopArg.evaluate(builder, step, 1) == 1 && step[0] == -1.0) {
                                    break;
                                } // else fall through
                            case 1:
                                if (!loopArg.isNatural()) {
                                    bodyNatural = finallyNatural = false;
                                }
                                break;
                            default:
                                break;
                        }
                        count += num;
                    }
                }

                loopBody.getParameters().getFirst().setNatural(bodyNatural);
                loopBody.getParameters().getFirst().setLocality(locality);
                loopBody.compile(builder, phase, this, null);
                finallyBody.getParameters().getFirst().setNatural(finallyNatural);
                finallyBody.getParameters().getFirst().setLocality(locality);
                finallyBody.compile(builder, phase, null, null);

                if (loopModHolder == null) {
                    getChildChange().addEntropy(entropy.toString());
                }
            }
            case Simplify -> {
                loopArgs = ASTExpression.simplify(builder, loopArgs);
				if (loopArgs.isConstant()) {
					boolean bodyNatural = loopBody.getParameters().getFirst().isNatural();
					boolean finallyNatural = loopBody.getParameters().getFirst().isNatural();
					//TODO check arguments are correct
					setupLoop(builder, null, loopData, loopArgs);
					bodyNatural = bodyNatural && loopData[0] == Math.floor(loopData[0]) &&
							loopData[1] == Math.floor(loopData[1]) &&
							loopData[2] == Math.floor(loopData[2]) &&
							loopData[0] >= 0.0 && loopData[1] >= 0.0 &&
							loopData[0] < AST.MAX_NATURAL &&
							loopData[1] < AST.MAX_NATURAL;
					finallyNatural = finallyNatural && bodyNatural &&
							loopData[1] + loopData[2] >= -1.0 &&
							loopData[1] + loopData[2] < AST.MAX_NATURAL;
					loopArgs = null;
					loopBody.getParameters().getFirst().setNatural(bodyNatural);
					finallyBody.getParameters().getFirst().setNatural(finallyNatural);

				}
                loopBody.compile(builder, phase, null, null);
                finallyBody.compile(builder, phase, null, null);
            }
            default -> {
            }
        }
	}

	@Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		final Shape loopChild = (Shape) parent.clone();
		boolean opsOnly = (loopBody.getRepType() | finallyBody.getRepType()) == RepElemType.op.getType();
		if (opsOnly && !tr) {
			loopChild.getWorldState().getTransform().setToIdentity();
		}
		final double[] data = new double[3];
		renderer.getCurrentSeed().add(getChildChange().getModData().getRand64Seed());
		if (loopArgs != null) {
			//TODO check arguments are correct
			setupLoop(builder, renderer, data, loopArgs);
		} else {
			data[0] = loopData[0];
			data[1] = loopData[1];
			data[2] = loopData[2];
		}
		final double start = data[0];
		final double end = data[1];
		final double step = data[2];
		if (renderer.getStackSize() + 1 > renderer.getStack().getMaxStackSize()) {
			system.error("Maximum stack depth exceeded", getWhere());
		}
		//TODO verify stack behaviour
		final CFStackNumber stackNumber = new CFStackNumber(renderer.getStack(), start);
		renderer.addStackItem(stackNumber);
		double index = stackNumber.getNumber();
		for (;;) {
			if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
				throw new CFDGStopException("Stopping", getWhere());
			}
			if (step > 0.0) {
				if (index >= end) {
					break;
				}
			} else {
				if (index <= end) {
					break;
				}
			}
			loopBody.traverse(builder, renderer, loopChild, tr || opsOnly, false);
			getChildChange().evaluate(builder, renderer, loopChild.getWorldState(), true);
			index += step;
			stackNumber.setNumber(index);
		}
		finallyBody.traverse(builder, renderer, loopChild, tr || opsOnly, false);
		renderer.removeStackItem();
	}

	public void compileLoopMod(CFDGBuilder builder) {
		if (loopModHolder != null) {
			loopModHolder.compile(builder, CompilePhase.TypeCheck);
			getChildChange().grab(loopModHolder);
		} else {
			getChildChange().compile(builder, CompilePhase.TypeCheck);
		}
	}

	private void setupLoop(CFDGBuilder builder, CFDGRenderer renderer, double[] data, ASTExpression exp) {
		switch (exp.evaluate(builder, renderer, data, 3)) {
			case 1:
				data[1] = data[0];
				data[0] = 0.0;
			case 2:
				data[2] = 1.0;
			case 3:
				break;
			default:
		}
	}
}
