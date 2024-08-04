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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.exceptions.StopException;
import org.antlr.v4.runtime.Token;

public class ASTLoop extends ASTReplacement {
	private ASTExpression loopArgs;
	private ASTModification loopModHolder;
	private double[] loopData;
	private ASTRepContainer loopBody;
	private ASTRepContainer finallyBody;
	private int loopIndexName;
	private String loopName;

	public ASTLoop(CFDGDriver driver, int nameIndex, String name, ASTExpression args, ASTModification mods, Token location) {
		super(driver, mods, RepElemType.empty, location);
		loopBody = new ASTRepContainer(driver);
		finallyBody = new ASTRepContainer(driver);
		this.loopArgs = args;
		this.loopModHolder = null;
		this.loopIndexName = nameIndex;
		this.loopName = name;
		loopBody.addLoopParameter(loopIndexName, false, false, location);
		finallyBody.addLoopParameter(loopIndexName, false, false, location);
	}

	public int getLoopIndexName() {
		return loopIndexName;
	}

	public String getLoopName() {
		return loopName;
	}

	public double[] getLoopData() {
		return loopData;
	}

	public ASTExpression getLoopArgs() {
		return loopArgs;
	}

	public ASTRepContainer getLoopBody() {
		return loopBody;
	}
	
	public ASTRepContainer getFinallyBody() {
		return finallyBody;
	}

	public ASTModification getLoopModHolder() {
		return loopModHolder;
	}

	public void setLoopHolder(ASTModification loopModHolder) {
		this.loopModHolder = loopModHolder;
	}

	public void compileLoopMod() {
       if (loopModHolder != null) {
            loopModHolder.compile(CompilePhase.TypeCheck);
            getChildChange().grab(loopModHolder);
        } else {
        	getChildChange().compile(CompilePhase.TypeCheck);
        }
 	}

	@Override
	public void compile(CompilePhase ph) {
		loopArgs = compile(loopArgs, ph);
		loopData = new double[3];

		switch (ph) {
			case TypeCheck: {
				if (loopArgs == null) {
					driver.error("A loop must have one to three index parameters", location);
					return;
				}
				StringBuilder ent = new StringBuilder();
				ent.append(loopName);
				loopArgs.entropy(ent);
				if (loopModHolder != null) {
					getChildChange().addEntropy(ent.toString());
				}

				boolean bodyNatural = false;
				boolean finallyNatural = false;
				Locality locality = loopArgs.getLocality();

				if (loopArgs.isConstant()) {
					setupLoop(loopData, loopArgs, null);
					bodyNatural = loopData[0] == Math.floor(loopData[0]) && loopData[1] == Math.floor(loopData[1]) && loopData[2] == Math.floor(loopData[2]) &&	loopData[0] >= 0 && loopData[1] >= 0 && loopData[0] < 9007199254740992.0 && loopData[1] < 9007199254740992.0;
					finallyNatural = bodyNatural && loopData[1] + loopData[2] >= -1.0 && loopData[1] + loopData[2] < 9007199254740992.0;
//					loopArgs = null;
				} else {
					int c = loopArgs.evaluate(null, 0);
					if (c < 1 || c > 3) {
						driver.error("A loop must have one to three index parameters", location);
					}

					for (int i = 0, count = 0; i < loopArgs.size(); i++) {
						ASTExpression loopArg = loopArgs.getChild(i);
						int num = loopArg.evaluate(null, 0);
						switch (count) {
							case 0:
								if (loopArg.isNatural()) {
									bodyNatural = finallyNatural = true;
								}
								break;

							case 2:
								// Special case: if 1st & 2nd args are natural and 3rd
								// is -1 then that is ok
								double[] step = new double[1];
								if (loopArg.isConstant() && loopArg.evaluate(step, 1) == 1 && step[0] == -1.0) {
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
				loopBody.getParameters().get(loopBody.getParameters().size() - 1).setIsNatural(bodyNatural);
				loopBody.getParameters().get(loopBody.getParameters().size() - 1).setLocality(locality);
				loopBody.compile(ph, this, null);
				finallyBody.getParameters().get(finallyBody.getParameters().size() - 1).setIsNatural(finallyNatural);
				finallyBody.getParameters().get(finallyBody.getParameters().size() - 1).setLocality(locality);
				finallyBody.compile(ph, null, null);

				if (loopModHolder == null) {
					getChildChange().addEntropy(ent.toString());
				}
				break;
			}

			case Simplify:
				loopArgs = simplify(loopArgs);
				loopBody.compile(ph, null, null);
				finallyBody.compile(ph, null, null);
				break;
	
			default:
				break;
		}
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		Shape loopChild = (Shape) parent.clone();
		boolean opsOnly = (loopBody.getRepType() | finallyBody.getRepType()) == RepElemType.op.getType();
		if (opsOnly && !tr) {
			loopChild.getWorldState().getTransform().setToIdentity();
		}
		double[] data = new double[3];
		renderer.getCurrentSeed().add(getChildChange().getModData().getRand64Seed());
		if (!loopArgs.isConstant()) {
			setupLoop(data, loopArgs, renderer);
		} else {
			data[0] = loopData[0];
			data[1] = loopData[1];
			data[2] = loopData[2];
		}
		//TODO controllare
		renderer.addStackItem(new CFStackNumber(renderer.getStack(), data[0]));
		int index = (int)((CFStackNumber)renderer.getStackItem(-1)).getNumber();
		for (;;) {
			if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
				throw new StopException();
			}
			if (data[2] > 0.0) {
				if (index >= data[1]) {
					break;
				}
			} else {
				if (index <= data[1]) {
					break;
				}
			}
			loopBody.traverse(loopChild, tr || opsOnly, renderer, false);
			getChildChange().evaluate(loopChild.getWorldState(), true, renderer);
			index += (int)data[2];
			renderer.setStackItem(-1, new CFStackNumber(renderer.getStack(), index));
		}
		finallyBody.traverse(loopChild, tr || opsOnly, renderer, false);
		renderer.removeStackItem();
	}
	
	private void setupLoop(double[] data, ASTExpression exp, CFDGRenderer renderer) {
		switch (exp.evaluate(data, 3, renderer)) {
			case 1:
				data[1] = data[0];
				data[0] = 0.0;
			case 2:
				data[2] = 1.0;
				break;
			case 3:
				break;
			default:
				driver.error("A loop must have one to three index parameters", location);
				break;
		}
	}
}
