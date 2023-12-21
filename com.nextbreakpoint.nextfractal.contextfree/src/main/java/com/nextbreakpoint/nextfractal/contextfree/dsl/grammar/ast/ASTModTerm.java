/*
 * NextFractal 2.1.5
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2022 Andrea Medeghini
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

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransform1D;
import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.HSBColor;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.AssignmentType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ModClass;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ModType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.DeferUntilRuntimeException;
import org.antlr.v4.runtime.Token;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Map;

public class ASTModTerm extends ASTExpression {
	private int argCount;
	private ModType modType;
	private ASTExpression args;

	private static Map<String, Long> paramMap = new HashMap<>();

	static {
		paramMap.put("evenodd", FlagType.CF_EVEN_ODD.getMask());
		paramMap.put("iso", FlagType.CF_ISO_WIDTH.getMask());
		paramMap.put("miterjoin", FlagType.CF_MITER_JOIN.getMask());
		paramMap.put("roundjoin", FlagType.CF_ROUND_JOIN.getMask());
		paramMap.put("beveljoin", FlagType.CF_BEVEL_JOIN.getMask());
		paramMap.put("buttcap", FlagType.CF_BUTT_CAP.getMask());
		paramMap.put("squarecap", FlagType.CF_SQUARE_CAP.getMask());
		paramMap.put("roundcap", FlagType.CF_ROUND_CAP.getMask());
		paramMap.put("large", FlagType.CF_ARC_LARGE.getMask());
		paramMap.put("cw", FlagType.CF_ARC_CW.getMask());
		paramMap.put("align", FlagType.CF_ALIGN.getMask());
	}

	public ASTModTerm(CFDGDriver driver, ModType modType, String paramStrings, Token location) {
		super(driver, true, false, ExpType.ModType, location);
		this.modType = modType;
		this.args = null;
		this.argCount = 0;

		//TODO controllare

		for (String paramString : paramStrings.split(" ")) {
			Long count = paramMap.get(paramString);
			if (count != null) {
				argCount |= count;
			}
		}
	}

	public ASTModTerm(CFDGDriver driver, ModType modType, ASTExpression args, Token location) {
		super(driver, args.isConstant(), false, ExpType.ModType, location);
		this.modType = modType;
		this.args = args;
		this.argCount = 0;
	}

	public ASTModTerm(CFDGDriver driver, ModType modType, Token location) {
		super(driver, true, false, ExpType.ModType, location);
		this.modType = modType;
		this.args = null;
		this.argCount = 0;
	}

	public ModType getModType() {
		return modType;
	}

	public void setModType(ModType modType) {
		this.modType = modType;
	}
	
	public ASTExpression getArguments() {
		return args;
	}

	public void setArguments(ASTExpression arguments) {
		this.args = arguments;
	}
	
	public int getArgumentsCount() {
		return argCount;
	}

	public void setArgumentsCount(int argCount) {
		this.argCount = argCount;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
        driver.error("Improper evaluation of an adjustment expression", location);
        return -1;
	}

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		double[] modArgs = new double[6];
		int argcount = 0;

		if (args != null) {
			if (modType != ModType.modification && args.type == ExpType.NumericType) {
				argcount = args.evaluate(modArgs, 6, renderer);
			} else if (modType == ModType.modification && args.type != ExpType.ModType) {
                driver.error("Adjustments require numeric arguments", location);
                return;
			}
		}

		if (argcount != argCount) {
            driver.error("Error evaluating arguments", location);
            return;
		}
		
		double[] color = result.color().values();
		double[] target = result.colorTarget().values();
		int colorComp = 0;
		int targetComp = 0;
		boolean hue = true;
		int mask = AssignmentType.HueMask.getType();

		switch (modType) {
			case x: {
				if (argcount == 1) {
					modArgs[1] = 0.0;
				}
				AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
				result.getTransform().concatenate(t2d);
				break;
			}
			case y: {
				AffineTransform t2d = AffineTransform.getTranslateInstance(0.0, modArgs[0]);
				result.getTransform().concatenate(t2d);
				break;
			}
			case z: {
				AffineTransform1D t1d = AffineTransform1D.getTranslateInstance(modArgs[0]);
				result.getTransformZ().concatenate(t1d);
				break;
			}
			case xyz: {
				AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
				AffineTransform1D t1d = AffineTransform1D.getTranslateInstance(modArgs[2]);
				result.getTransform().concatenate(t2d);
				result.getTransformZ().concatenate(t1d);
				break;
			}
			case time: {
				AffineTransformTime tTime = AffineTransformTime.getTranslateInstance(modArgs[0], modArgs[1]);
				result.getTransformTime().concatenate(tTime);
				break;
			}
			case timescale: {
				AffineTransformTime tTime = AffineTransformTime.getScaleInstance(modArgs[0]);
				result.getTransformTime().concatenate(tTime);
				break;
			}
			case transform: {
				switch (argcount) {
					case 2:
					case 1: {
						if (argcount == 1) {
							modArgs[1] = 0.0;
						}
						AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
						result.getTransform().concatenate(t2d);
						break;
					}
					case 4: {
						AffineTransform t2d = new AffineTransform();
						double dx = modArgs[2] - modArgs[0];
						double dy = modArgs[3] - modArgs[1];
						double s = Math.hypot(dx, dy);
						t2d.rotate(Math.atan2(dx, dy));
						t2d.scale(s, s);
						t2d.translate(modArgs[0], modArgs[1]);
						result.getTransform().concatenate(t2d);
						break;
					}
					case 6: {
						try {
							AffineTransform t2d = new AffineTransform(modArgs[2] - modArgs[0], modArgs[3] - modArgs[1], modArgs[4] - modArgs[0], modArgs[5] - modArgs[1], modArgs[0], modArgs[1]);
							AffineTransform par = new AffineTransform();
							par.shear(1, 0);
							par.invert();
							par.concatenate(t2d);
							result.getTransform().concatenate(par);
						} catch (NoninvertibleTransformException e) {
							driver.error(e.getMessage(), null);
						}
						break;
					}
					default:
						break;
				}
				break;
			}
			case size: {
				if (argcount == 1) {
					modArgs[1] =  modArgs[0];
				}
				AffineTransform t2d = AffineTransform.getScaleInstance(modArgs[0], modArgs[1]);
				result.getTransform().concatenate(t2d);
				break;
			}
			case sizexyz: {
				AffineTransform t2d = AffineTransform.getScaleInstance(modArgs[0], modArgs[1]);
				AffineTransform1D t1d = AffineTransform1D.getScaleInstance(modArgs[2]);
				result.getTransform().concatenate(t2d);
				result.getTransformZ().concatenate(t1d);
				break;
			}
			case zsize: {
				AffineTransform1D t1d = AffineTransform1D.getScaleInstance(modArgs[0]);
				result.getTransformZ().concatenate(t1d);
				break;
			}
			case rotate: {
				AffineTransform t2d = AffineTransform.getRotateInstance(modArgs[0] * Math.PI / 180.0);
				result.getTransform().concatenate(t2d);
				break;
			}
			case skew: {
				AffineTransform t2d = AffineTransform.getShearInstance(modArgs[0] * Math.PI / 180.0, modArgs[1] * Math.PI / 180.0);
				result.getTransform().concatenate(t2d);
				break;
			}
			case flip: {
				double a = modArgs[0] * Math.PI / 180.0;
				double ux = Math.cos(a);
				double uy = Math.sin(a);
				AffineTransform t2d = new AffineTransform(2.0 * ux * ux - 1.0, 2.0 * ux * uy, 2.0 * ux * uy, 2.0 * uy * uy - 1.0, 0.0, 0.0);
				result.getTransform().concatenate(t2d);
				break;
			}
			case alpha:
			case bright:
			case sat: {
				colorComp += modType.getType() - ModType.hue.getType();
				targetComp += modType.getType() - ModType.hue.getType();
				mask <<= 2 * (modType.getType() - ModType.hue.getType());
				hue = false;
			}
			case hue: {
				 if (argcount != 2) {
				 	 for (int i = 0; i < argcount; i++) {
						 if ((result.colorAssignment() & mask) != 0 || (!hue && color[colorComp] != 0.0)) {
							 if (renderer == null) throw new DeferUntilRuntimeException(location);
							 if (!shapeDest) {
								 renderer.colorConflict(getLocation());
							 }
						 }
						 if (shapeDest) {
							 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], modArgs[i]) : HSBColor.adjust(color[colorComp], limitValue(modArgs[i]));
						 } else {
							 color[colorComp] = hue ? color[colorComp] + modArgs[0] : limitValue(modArgs[0]);
						 }
						 mask <<= 2;
						 hue = false;
						 colorComp += 1;
					 }
				 } else {
					 if ((result.colorAssignment() & mask) != 0 || (color[colorComp] != 0.0) || (!hue && target[targetComp] != 0.0)) {
						 if (renderer == null) throw new DeferUntilRuntimeException(location);
						 if (!shapeDest) {
							 renderer.colorConflict(getLocation());
						 }
					 }
					 if (shapeDest) {
						 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], limitValue(modArgs[0]), 1, modArgs[1]) : HSBColor.adjust(color[colorComp], limitValue(modArgs[0]), 1, limitValue(modArgs[1]));
					 } else {
						 color[colorComp] = limitValue(modArgs[0]);
						 target[targetComp] = hue ? modArgs[1] : limitValue(modArgs[1]);
						 result.setColorAssignment(result.colorAssignment() | AssignmentType.HSBA2Value.getType() & mask);
					 }
				 }
				 break;
			}
			case alphaTarg:
			case brightTarg:
			case satTarg: {
				colorComp += modType.getType() - ModType.hueTarg.getType();
				targetComp += modType.getType() - ModType.hueTarg.getType();
				mask <<= 2 * (modType.getType() - ModType.hueTarg.getType());
				hue = false;
			}
			case hueTarg: {
				 if ((result.colorAssignment() & mask) != 0 || (color[colorComp] != 0.0)) {
					 if (renderer == null) throw new DeferUntilRuntimeException(location);
					 if (!shapeDest) {
						 renderer.colorConflict(getLocation());
					 }
				 }
				 if (shapeDest) {
					 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], limitValue(modArgs[0]), 1, target[targetComp]) : HSBColor.adjust(color[colorComp], limitValue(modArgs[0]), 1, target[targetComp]);
				 } else {
					 color[colorComp] = limitValue(modArgs[0]);
					 result.setColorAssignment(result.colorAssignment() | AssignmentType.HSBATarget.getType() & mask);
				 }
				break;
			}
			case targAlpha:
			case targBright:
			case targSat: {
				targetComp += modType.getType() - ModType.targHue.getType();
				if (target[targetComp] != 0.0) {
					 if (renderer == null) throw new DeferUntilRuntimeException(location);
					 if (!shapeDest) {
						 renderer.colorConflict(getLocation());
					 }
				}
				 if (shapeDest) {
					 target[targetComp] = HSBColor.adjust(target[targetComp], limitValue(modArgs[0]));
				 } else {
					 target[targetComp] = limitValue(modArgs[0]);
				 }
				break;
			}
			case targHue:
				 target[0] += modArgs[0];
				break;
			case stroke: {
				driver.error("Cannot provide a stroke width in this context", location);
				break;
			}
			case x1:
			case y1:
			case x2:
			case y2:
			case xrad:
			case yrad: {
				driver.error("Cannot provide a path operation term in this context", location);
				break;
			}
			case param: {
				driver.error("Cannot provide a parameter in this context", location);
				break;
			}
			case unknown: {
				driver.error("Unrecognized adjustment type", location);
				break;
			}
			case modification: {
				if (renderer == null) {
					if (args != null && getArguments() instanceof ASTModification) {
						ASTModification mod = (ASTModification)getArguments();
						if (mod == null || (mod.getModClass().getType() & (ModClass.HueClass.getType() | ModClass.HueTargetClass.getType() | ModClass.BrightClass.getType() | ModClass.BrightTargetClass.getType() | ModClass.SatClass.getType() | ModClass.SatTargetClass.getType() | ModClass.AlphaClass.getType() | ModClass.AlphaTargetClass.getType())) != 0) {
							throw new DeferUntilRuntimeException(location);
						}
					}
				}
				getArguments().evaluate(result, shapeDest, renderer);
				break;
			}
			default:
				break;
		}
		result.color().setValues(color);
		result.colorTarget().setValues(target);
	}

	private double limitValue(double value) {
		return Math.max(-1.0, Math.min(1.0, value));
	}

	@Override
	public void entropy(StringBuilder e) {
		if (args != null) {
			args.entropy(e);
		}
		e.append(modType.getEntropy());
	}

	@Override
	public ASTExpression simplify() {
		args = simplify(args);
		return this;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		args = compile(args, ph);

		if (args == null) {
			if (modType != ModType.param) {
                driver.error("Illegal expression in shape adjustment", location);
			}
			return null;
		}
		
		switch (ph) {
			case TypeCheck: {
				isConstant = args.isConstant();
				locality = args.getLocality();
				switch (args.getType()) {
					case NumericType: {
						argCount = args.evaluate(null, 0);
						int minCount = 1;
						int maxCount = 1;

						if (argCount == 3 && modType == ModType.x) {
							modType = ModType.xyz;
						}
						if (argCount == 3 && modType == ModType.size) {
							modType = ModType.sizexyz;
						}

						switch (modType) {
							case hue:
								maxCount = 4;
								break;
							case x:
							case size:
							case sat:
							case bright:
							case alpha:
								maxCount = 2;
								break;
							case y:
							case z:
							case timescale:
							case zsize:
							case rotate:
							case flip:
							case hueTarg:
							case satTarg:
							case brightTarg:
							case alphaTarg:
							case targHue:
							case targSat:
							case targBright:
							case targAlpha:
							case stroke:
								break;
							case xyz:
							case sizexyz:
								minCount = maxCount = 3;
								break;
							case time:
							case skew:
								minCount = maxCount = 2;
								break;
							case transform:
								maxCount = 6;
								if (argCount != 1 && argCount != 2 && argCount != 4 && argCount != 6) {
									driver.error("transform adjustment takes 1, 2, 4, or 6 parameters", location);
								}
								break;
							case param:
								minCount = maxCount = 0;
								break;
							case modification:
								break;
							default:
								break;
						}

						if (argCount < minCount) {
							driver.error("Not enough adjustment parameters", location);
						}
						if (argCount > maxCount) {
							driver.error("Too many adjustment parameters", location);
						}
						break;
					}

					case ModType: {
						if (modType != ModType.transform) {
							driver.error("Cannot accept a transform expression here", location);
						} else {
							modType = ModType.modification;
						}
						break;
					}

					default:
						driver.error("Illegal expression in shape adjustment", location);
						break;
				}
				break;
			}

			case Simplify:
				break;

			default:
				break;
		}
		return null;
	}
}
