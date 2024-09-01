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

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransform1D;
import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.HSBColor;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.AssignmentType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModClass;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModType;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;
import java.util.Map;

// astexpression.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2014 John Horigan - john@glyphic.com
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

@Setter
@Getter
public class ASTModTerm extends ASTExpression {
	private static final Map<String, Long> paramMap = new HashMap<>();

	//TODO extract code
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

	private ModType modType;
	private int argCountOrFlags;
	private ASTExpression arguments;

	public ASTModTerm(CFDGSystem system, ASTWhere where, ModType modType, String paramStrings) {
		super(system, where, true, false, ExpType.Mod);
		this.modType = modType;
		this.arguments = null;
		this.argCountOrFlags = 0;

		for (String paramString : paramStrings.split(" ")) {
			final Long flags = paramMap.get(paramString);
			if (flags != null) {
				argCountOrFlags |= flags;
			}
		}
	}

	public ASTModTerm(CFDGSystem system, ASTWhere where, ModType modType, ASTExpression arguments) {
		super(system, where, arguments.isConstant(), false, ExpType.Mod);
		this.modType = modType;
		this.arguments = arguments;
		this.argCountOrFlags = 0;
	}

	public ASTModTerm(CFDGSystem system, ASTWhere where, ModType modType) {
		super(system, where, true, false, ExpType.Mod);
		this.modType = modType;
		this.arguments = null;
		this.argCountOrFlags = 0;
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
        system.error("Improper evaluation of an adjustment expression", getWhere());
		return -1;
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		if (modType == ModType.modification) {
			if (arguments == null || arguments.getType() != ExpType.Mod) {
				system.error("transform adjustments require an adjustment argument", getWhere());
				return;
			}
			if (renderer == null) {
				if (arguments instanceof ASTModification arg) {
					// Color adjustments are not associative like geometry adjustments,
					// so they must be done in order at run-time
					if ((arg.getModClass().getType() & (
							ModClass.HueClass.getType() |
							ModClass.HueTargetClass.getType() |
							ModClass.BrightClass.getType() |
							ModClass.BrightTargetClass.getType() |
							ModClass.SatClass.getType() |
							ModClass.SatTargetClass.getType() |
							ModClass.AlphaClass.getType() |
							ModClass.AlphaTargetClass.getType())) != 0)
					{
						throw new CFDGDeferUntilRuntimeException(getWhere());
					}
				}
			}
			arguments.evaluate(builder, renderer, mod, shapeDest);
			return;
		}

		final double[] modArgs = new double[6];
		int argCount = 0;

		if (arguments != null) {
			switch (arguments.getType()) {
				case ExpType.Numeric -> {
					if (modType == ModType.blend) {
						system.error("Blend adjustments require flag arguments", getWhere());
						return;
					}
					argCount = arguments.evaluate(builder, renderer, modArgs, 6);
				}
				case ExpType.Flag -> {
					if (modType != ModType.blend) {
						system.error("Only blend adjustments accept flag arguments", getWhere());
						return;
					}
					argCount = arguments.evaluate(builder, renderer, modArgs, 1);
				}
				default -> {
					system.error("Adjustments require numeric arguments", getWhere());
					return;
				}
			}
		}

		if (argCount != argCountOrFlags) {
            system.error("Error evaluating arguments", getWhere());
			return;
		}

		final double[] arg = new double[6];
		for (int i = 0; i < argCount; i++) {
			arg[i] = limitValue(modArgs[i]);
		}
		final double[] color = mod.color().values();
		final double[] target = mod.colorTarget().values();
		int colorComp = 0;
		int targetComp = 0;
		boolean hue = true;
		int mask = AssignmentType.HueMask.getType();

		switch (modType) {
			case x: {
				if (argCount == 1) {
					modArgs[1] = 0.0;
				}
				AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
				mod.getTransform().concatenate(t2d);
				break;
			}
			case y: {
				AffineTransform t2d = AffineTransform.getTranslateInstance(0.0, modArgs[0]);
				mod.getTransform().concatenate(t2d);
				break;
			}
			case z: {
				AffineTransform1D t1d = AffineTransform1D.getTranslateInstance(modArgs[0]);
				mod.getTransformZ().concatenate(t1d);
				break;
			}
			case xyz: {
				AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
				AffineTransform1D t1d = AffineTransform1D.getTranslateInstance(modArgs[2]);
				mod.getTransform().concatenate(t2d);
				mod.getTransformZ().concatenate(t1d);
				break;
			}
			case time: {
				AffineTransformTime tTime = AffineTransformTime.getTranslateInstance(modArgs[0], modArgs[1]);
				mod.getTransformTime().concatenate(tTime);
				break;
			}
			case timescale: {
				AffineTransformTime tTime = AffineTransformTime.getScaleInstance(modArgs[0]);
				mod.getTransformTime().concatenate(tTime);
				break;
			}
			case transform: {
				switch (argCount) {
					case 2:
					case 1: {
						if (argCount == 1) {
							modArgs[1] = 0.0;
						}
						AffineTransform t2d = AffineTransform.getTranslateInstance(modArgs[0], modArgs[1]);
						mod.getTransform().concatenate(t2d);
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
						mod.getTransform().concatenate(t2d);
						break;
					}
					case 6: {
						try {
							//TODO is implementation equivalent of original CF code?
							AffineTransform t2d = new AffineTransform(modArgs[2] - modArgs[0], modArgs[3] - modArgs[1], modArgs[4] - modArgs[0], modArgs[5] - modArgs[1], modArgs[0], modArgs[1]);
							AffineTransform par = new AffineTransform();
							par.shear(1, 0);
							par.invert();
							par.concatenate(t2d);
							mod.getTransform().concatenate(par);
						} catch (NoninvertibleTransformException e) {
							system.error(e.getMessage(), null);
						}
						break;
					}
					default:
						break;
				}
				break;
			}
			case size: {
				if (argCount == 1) {
					modArgs[1] =  modArgs[0];
				}
				AffineTransform t2d = AffineTransform.getScaleInstance(modArgs[0], modArgs[1]);
				mod.getTransform().concatenate(t2d);
				break;
			}
			case sizexyz: {
				AffineTransform t2d = AffineTransform.getScaleInstance(modArgs[0], modArgs[1]);
				AffineTransform1D t1d = AffineTransform1D.getScaleInstance(modArgs[2]);
				mod.getTransform().concatenate(t2d);
				mod.getTransformZ().concatenate(t1d);
				break;
			}
			case zsize: {
				AffineTransform1D t1d = AffineTransform1D.getScaleInstance(modArgs[0]);
				mod.getTransformZ().concatenate(t1d);
				break;
			}
			case rotate: {
				AffineTransform t2d = AffineTransform.getRotateInstance(modArgs[0] * Math.PI / 180.0);
				mod.getTransform().concatenate(t2d);
				break;
			}
			case skew: {
				AffineTransform t2d = AffineTransform.getShearInstance(modArgs[0] * Math.PI / 180.0, modArgs[1] * Math.PI / 180.0);
				mod.getTransform().concatenate(t2d);
				break;
			}
			case flip: {
				//TODO extract code to method getFlipInstance of AffineTransform subclass
				double a = modArgs[0] * Math.PI / 180.0;
				double ux = Math.cos(a);
				double uy = Math.sin(a);
				AffineTransform t2d = new AffineTransform(2.0 * ux * ux - 1.0, 2.0 * ux * uy, 2.0 * ux * uy, 2.0 * uy * uy - 1.0, 0.0, 0.0);
				mod.getTransform().concatenate(t2d);
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
				 if (argCount != 2) {
				 	 for (int i = 0; i < argCount; i++) {
						 if ((mod.colorAssignment() & mask) != 0 || (!hue && color[colorComp] != 0.0)) {
							 if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
							 if (!shapeDest) {
								 renderer.colorConflict(getWhere());
							 }
						 }
						 if (shapeDest) {
							 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], modArgs[i]) : HSBColor.adjust(color[colorComp], arg[i]);
						 } else {
							 color[colorComp] = hue ? color[colorComp] + modArgs[0] : arg[0];
						 }
						 mask <<= 2;
						 hue = false;
						 colorComp += 1;
					 }
				 } else {
					 if ((mod.colorAssignment() & mask) != 0 || (color[colorComp] != 0.0) || (!hue && target[targetComp] != 0.0)) {
						 if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
						 if (!shapeDest) {
							 renderer.colorConflict(getWhere());
						 }
					 }
					 if (shapeDest) {
						 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], arg[0], 1, modArgs[1]) : HSBColor.adjust(color[colorComp], arg[0], 1, arg[1]);
					 } else {
						 color[colorComp] = arg[0];
						 target[targetComp] = hue ? modArgs[1] : arg[1];
						 mod.setColorAssignment(mod.colorAssignment() | AssignmentType.HSBA2Value.getType() & mask);
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
				 if ((mod.colorAssignment() & mask) != 0 || (color[colorComp] != 0.0)) {
					 if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
					 if (!shapeDest) {
						 renderer.colorConflict(getWhere());
					 }
				 }
				 if (shapeDest) {
					 color[colorComp] = hue ? HSBColor.adjustHue(color[colorComp], arg[0], 1, target[targetComp]) : HSBColor.adjust(color[colorComp], arg[0], 1, target[targetComp]);
				 } else {
					 color[colorComp] = arg[0];
					 mod.setColorAssignment(mod.colorAssignment() | AssignmentType.HSBATarget.getType() & mask);
				 }
				break;
			}
			case targAlpha:
			case targBright:
			case targSat: {
				targetComp += modType.getType() - ModType.targHue.getType();
				if (target[targetComp] != 0.0) {
					 if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
					 if (!shapeDest) {
						 renderer.colorConflict(getWhere());
					 }
				}
				 if (shapeDest) {
					 target[targetComp] = HSBColor.adjust(target[targetComp], arg[0]);
				 } else {
					 target[targetComp] = arg[0];
				 }
				break;
			}
			case targHue:
				target[0] += modArgs[0];
				break;
			case stroke: {
				system.error("Can't provide a stroke width in this context", getWhere());
			}
			case x1:
			case y1:
			case x2:
			case y2:
			case xrad:
			case yrad: {
				system.error("Can't provide a path operation term in this context", getWhere());
			}
			case param: {
				system.error("Can't provide a parameter in this context", getWhere());
			}
			case unknown: {
				system.error("Unrecognized adjustment type", getWhere());
			}
			case modification: {
				break;  // supress warning, never happens
			}
			default:
				break;
		}
		mod.color().setValues(color);
		mod.colorTarget().setValues(target);
	}

	@Override
	public void entropy(StringBuilder entropy) {
		if (arguments != null) {
			arguments.entropy(entropy);
		}
		entropy.append(modType.getEntropy());
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		arguments = ASTExpression.simplify(builder, arguments);
		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		arguments = ASTExpression.compile(builder, phase, arguments);

		if (arguments == null) {
			if (modType != ModType.param) {
                system.error("Illegal expression in shape adjustment", getWhere());
			}
			return null;
		}

        switch (phase) {
            case TypeCheck -> {
                constant = arguments.isConstant();
                locality = arguments.getLocality();
                switch (arguments.getType()) {
                    case Numeric -> {
                        argCountOrFlags = arguments.evaluate(builder, null, 0);
                        int minCount = 1;
                        int maxCount = 1;

                        if (argCountOrFlags == 3 && modType == ModType.x) {
                            modType = ModType.xyz;
                        }
                        if (argCountOrFlags == 3 && modType == ModType.size) {
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
                                if (argCountOrFlags != 1 && argCountOrFlags != 2 && argCountOrFlags != 4 && argCountOrFlags != 6) {
                                    system.error("transform adjustment takes 1, 2, 4, or 6 parameters", getWhere());
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

                        if (argCountOrFlags < minCount) {
                            system.error("Not enough adjustment parameters", getWhere());
                        }
                        if (argCountOrFlags > maxCount) {
                            system.error("Too many adjustment parameters", getWhere());
                        }
                    }
                    case Mod -> {
                        if (modType != ModType.transform) {
                            system.error("Cannot accept a transform expression here", getWhere());
                        } else {
                            modType = ModType.modification;
                        }
                    }
					case Flag -> {
						if (modType != ModType.blend) {
							system.error("Cannot accept a flag expression here", getWhere());
						} else {
							argCountOrFlags = arguments.evaluate(builder, null, 0);
							if (argCountOrFlags != 1) {
								system.error("Error evaluating flag expression", getWhere());
							}
						}
					}
                    default -> system.error("Illegal expression in shape adjustment", getWhere());
                }
            }
			case Simplify -> {
				// do nothing
			}
            default -> {
            }
        }
		return null;
	}

	private double limitValue(double value) {
		return Math.max(-1.0, Math.min(1.0, value));
	}
}
