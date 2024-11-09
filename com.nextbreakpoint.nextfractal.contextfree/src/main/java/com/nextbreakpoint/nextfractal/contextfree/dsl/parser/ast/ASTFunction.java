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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.HSBColor;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST.MAX_VECTOR_SIZE;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Abs;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitAnd;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitLeft;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitNot;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitOr;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitRight;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.BitXOR;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Div;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Divides;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Factorial;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.IsNatural;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Max;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Min;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Mod;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.RandInt;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType.Sg;

// astexpression.cpp
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

public class ASTFunction extends ASTExpression {
    //TODO move to constants class
    private static final boolean RandStaticIsConst = true;

    private static final FuncType[] MUST_BE_NATURAL = new FuncType[] {
            Factorial, Sg, IsNatural, Div, Divides
    };

    private static final FuncType[] MIGHT_BE_NATURAL = new FuncType[] {
            Mod, Abs, Min, Max, BitNot, BitOr, BitAnd, BitXOR, BitLeft, BitRight, RandInt
    };

	private double random;

    @Getter
    private ASTExpression arguments;
	@Getter
    private FuncType funcType;

    public ASTFunction(CFDGSystem system, ASTWhere where, String name, ASTExpression arguments, Rand64 seed) {
		super(system, where, true, false, ExpType.Numeric);
		this.funcType = FuncType.NotAFunction;
		this.arguments = arguments;

		if (name.isEmpty()) {
			system.error("Bad function call", where);
            return;
		}

		funcType = FuncType.byName(name);

		if (funcType == FuncType.NotAFunction) {
            system.error("Unknown function", where);
            return;
		}

		if (funcType == FuncType.RandStatic) {
			random = seed.getDouble();
		}
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if (type != ExpType.Numeric) {
		   system.error("Non-numeric expression in a numeric context", getWhere());
           return -1;
		}

		final int destLength = (funcType.getType() >= FuncType.Cross.getType() && funcType.getType() <= FuncType.Rgb2Hsb.getType()) ? 3 : funcType == FuncType.Vec ? (int) Math.floor(random) : 1;

		if (result == null)
			return destLength;

		if (length < destLength)
			return -1;

        switch (funcType) {
            case Min, Max -> {
                result[0] = minMax(builder, renderer, arguments, funcType == FuncType.Min);
                return 1;
            }
            case Dot -> {
                final double[] l = new double[MAX_VECTOR_SIZE];
                final double[] r = new double[MAX_VECTOR_SIZE];
                final int lc = arguments.getChild(0).evaluate(builder, renderer, l, MAX_VECTOR_SIZE);
                final int rc = arguments.getChild(1).evaluate(builder, renderer, r, MAX_VECTOR_SIZE);
                if (lc == rc && lc > 1) {
                    result[0] = 0.0;
                    for (int i = 0; i < lc; i++) {
                        result[0] += l[i] * r[i];
                    }
                }
                return 1;
            }
            case Cross -> {
                final double[] l = new double[3];
                final double[] r = new double[3];
                final int lc = arguments.getChild(0).evaluate(builder, renderer, l, 3);
                final int rc = arguments.getChild(1).evaluate(builder, renderer, r, 3);
                if (lc == rc && lc == 3) {
                    result[0] = l[1] * r[2] - l[2] * r[1];
                    result[1] = l[2] * r[0] - l[0] * r[2];
                    result[2] = l[0] * r[1] - l[1] * r[0];
                }
                return 3;
            }
            case Vec -> {
                final double[] v = new double[MAX_VECTOR_SIZE];
                final int lc = arguments.getChild(0).evaluate(builder, renderer, v, MAX_VECTOR_SIZE);
                if (lc >= 1) {
                    for (int i = 0; i < destLength; i++) {
                        result[i] = v[i % lc];
                    }
                }
                return destLength;
            }
            case Hsb2Rgb -> {
                final double[] c = new double[3];
                final int lc = arguments.evaluate(builder, renderer, c, 3);
                if (lc == 3) {
                    HSBColor color = new HSBColor(c[0], c[1], c[2], 1.0);
                    double[] rgb = color.getRGBA();
                    result[0] = rgb[0];
                    result[1] = rgb[2];
                    result[2] = rgb[3];
                }
                return 3;
            }
            case Rgb2Hsb -> {
                final double[] c = new double[3];
                final int lc = arguments.evaluate(builder, renderer, c, 3);
                if (lc == 3) {
                    double[] rgb = new double[]{c[0], c[1], c[2], 1.0};
                    HSBColor color = new HSBColor(rgb);
                    result[0] = color.hue();
                    result[1] = color.bright();
                    result[2] = color.sat();
                }
                return 3;
            }
            case RandDiscrete -> {
                final double[] w = new double[MAX_VECTOR_SIZE];
                final int lc = arguments.evaluate(builder, renderer, w, MAX_VECTOR_SIZE);
                if (lc >= 1) {
                    result[0] = renderer.getCurrentSeed().getDiscrete(lc, w);
                }
                return 1;
            }
            default -> {
            }
        }

        final double[] a = new double[2];
        final int count = arguments.evaluate(builder, renderer, a, 2);
		// no need to checkParam the argument count, the constructor already checked it

		// But checkParam it anyway to make valgrind happy
		if (count < 0) return 1;

        switch (funcType) {
            case Cos -> result[0] = Math.cos(a[0] * 0.0174532925199);
            case Sin -> result[0] = Math.sin(a[0] * 0.0174532925199);
            case Tan -> result[0] = Math.tan(a[0] * 0.0174532925199);
            case Cot -> result[0] = 1.0 / Math.tan(a[0] * 0.0174532925199);
            case Acos -> result[0] = Math.acos(a[0]) * 57.29577951308;
            case Asin -> result[0] = Math.asin(a[0]) * 57.29577951308;
            case Atan -> result[0] = Math.atan(a[0]) * 57.29577951308;
            case Acot -> result[0] = Math.atan(1.0 / a[0]) * 57.29577951308;
            case Cosh -> result[0] = Math.cosh(a[0]);
            case Sinh -> result[0] = Math.sinh(a[0]);
            case Tanh -> result[0] = Math.tanh(a[0]);
            case Acosh -> result[0] = Math.log(a[0] + Math.sqrt(a[0] * a[0] - 1));
            case Asinh -> result[0] = Math.log(a[0] + Math.sqrt(a[0] * a[0] + 1));
            case Atanh -> result[0] = Math.log((1 / a[0] + 1) / (1 / a[0] - 1)) / 2;
            case Log -> result[0] = Math.log(a[0]);
            case Log10 -> result[0] = Math.log10(a[0]);
            case Sqrt -> result[0] = Math.sqrt(a[0]);
            case Exp -> result[0] = Math.exp(a[0]);
            case Abs -> {
                if (count == 1) {
                    result[0] = Math.abs(a[0]);
                } else {
                    result[0] = Math.abs(a[0] - a[1]);
                }
            }
            case Infinity -> result[0] = a[0] < 0.0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            case Factorial -> {
                if (a[0] < 0.0 || a[0] > 18.0 || a[0] != Math.floor(a[0])) {
                    system.error("Illegal argument for factorial", getWhere());
                }
                result[0] = 1.0;
                for (double v = 1.0; v <= a[0]; v += 1.0) {
                    result[0] *= v;
                }
            }
            case Sg -> result[0] = a[0] == 0.0 ? 0.0 : 1.0;
            case IsNatural -> result[0] = isNatural(renderer, a[0]) ? 1 : 0;
            case BitNot -> result[0] = ~((long) a[0]);
            case BitOr -> result[0] = ((long) a[0]) | ((long) a[1]);
            case BitAnd -> result[0] = ((long) a[0]) & ((long) a[1]);
            case BitXOR -> result[0] = ((long) a[0]) ^ ((long) a[1]);
            case BitLeft -> result[0] = ((long) a[0]) << ((long) a[1]);
            case BitRight -> result[0] = ((long) a[0]) >> ((long) a[1]);
            case Atan2 -> result[0] = Math.atan2(a[0], a[1]) * 57.29577951308;
            case Mod -> {
                if (arguments.isNatural()) {
                    result[0] = ((long) a[0]) % ((long) a[1]);
                } else {
                    result[0] = a[0] % a[1];
                }
            }
            case Divides -> result[0] = (((long) a[0]) % ((long) a[1])) == 0 ? 1.0 : 0.0;
            case Div -> result[0] = a[0] / a[1];
            case Floor -> result[0] = Math.floor(a[0]);
            case Ceiling -> result[0] = Math.ceil(a[0]);
            case Ftime -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                result[0] = renderer.getCurrentTime();
            }
            case Frame -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                result[0] = renderer.getCurrentFrame();
            }
            case RandStatic -> result[0] = random * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]);
            case Rand, RandOp -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getDouble() * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]);
            }
            case Rand2 -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = (renderer.getCurrentSeed().getDouble() * 2.0 - 1.0) * a[1] + a[0];
            }
            case RandExponential -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getExponential(a[0]);
            }
            case RandGamma -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getGamma(a[0], a[1]);
            }
            case RandWeibull -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getWeibull(a[0], a[1]);
            }
            case RandExtremeValue -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getExtremeValue(a[0], a[1]);
            }
            case RandNormal -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getNormal(a[0], a[1]);
            }
            case RandLogNormal -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getLogNormal(a[0], a[1]);
            }
            case RandChiSquared -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getChiSquared(a[0]);
            }
            case RandCauchy -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getCauchy(a[0], a[1]);
            }
            case RandFisherF -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getFisherF(a[0], a[1]);
            }
            case RandStudentT -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getStudentT(a[0]);
            }
            case RandInt -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = Math.floor(renderer.getCurrentSeed().getDouble() * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]));
            }
            case RandBernoulli -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = renderer.getCurrentSeed().getBernoulli(a[0]) ? 1.0 : 0.0;
            }
            case RandBinomial -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = Math.floor(renderer.getCurrentSeed().getBinomial((long) a[0], a[1]));
            }
            case RandNegBinomial -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = Math.floor(renderer.getCurrentSeed().getNegativeBinomial((long) a[0], a[1]));
            }
            case RandPoisson -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = Math.floor(renderer.getCurrentSeed().getPoisson(a[0]));
            }
            case RandGeometric -> {
                if (renderer == null) throw new CFDGDeferUntilRuntimeException(getWhere());
                renderer.setRandUsed(true);
                result[0] = Math.floor(renderer.getCurrentSeed().getGeometric(a[0]));
            }
            default -> {
                return -1;
            }
        }

		return 1;
	}

	@Override
	public void entropy(StringBuilder entropy) {
		if (arguments != null) {
			arguments.entropy(entropy);
		}
		entropy.append(funcType.getEntropy());
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
        ASTWhere where = getWhere();
		arguments = ASTExpression.compile(builder, phase, arguments);
        int argCount = 0;
        int argNum = 0;
        switch (phase) {
            case TypeCheck -> {
                constant = true;
                locality = Locality.PureLocal;
                if (arguments != null) {
                    argNum = arguments.size();
                    where = arguments.getWhere();
                    constant = arguments.isConstant();
                    locality = arguments.getLocality();
                    if (locality == Locality.PureNonLocal) {
                        locality = Locality.ImpureNonLocal;
                    }
                    if (arguments.getType() == ExpType.Numeric) {
                        argCount = arguments.evaluate(builder, null, 0);
                    } else {
                        system.error("Function arguments must be numeric", where);
                    }
                }
                switch (funcType) {
                    case Abs:
                        if (argCount < 1 || argCount > 2) {
                            system.error("Function takes one or two arguments", where);
                        }
                        break;
                    case Infinity:
                        if (argCount == 0) {
                            arguments = new ASTReal(system, this.where, 1.0);
                            argCount = 1;
                        }
                        // fall through
                    case Cos:
                    case Sin:
                    case Tan:
                    case Cot:
                    case Acos:
                    case Atan:
                    case Acot:
                    case Cosh:
                    case Sinh:
                    case Tanh:
                    case Acosh:
                    case Asinh:
                    case Atanh:
                    case Log:
                    case Log10:
                    case Sqrt:
                    case Exp:
                    case Floor:
                    case Ceiling:
                    case BitNot:
                    case Factorial:
                    case Sg:
                    case IsNatural:
                        if (argCount != 1) {
                            system.error("Function takes one argument", where);
                        }
                        break;
                    case BitOr:
                    case BitAnd:
                    case BitXOR:
                    case BitLeft:
                    case BitRight:
                    case Atan2:
                    case Mod:
                    case Divides:
                    case Div:
                        if (argCount != 2) {
                            system.error("Function takes two arguments", where);
                        }
                        break;
                    case Dot:
                    case Cross:
                        if (argNum != 2) {
                            system.error("Dot/cross product takes two vectors", where);
                        } else {
                            int l = arguments.getChild(0).evaluate(builder, null, 0);
                            int r = arguments.getChild(0).evaluate(builder, null, 0);
                            if (funcType == FuncType.Dot && (l != r || l < 2)) {
                                system.error("Dot product takes two vectors of the same length", where);
                            }
                            if (funcType == FuncType.Cross && (l != 3 || r != 3)) {
                                system.error("Cross product takes two vector3s", where);
                            }
                        }
                        break;
                    case Hsb2Rgb:
                    case Rgb2Hsb:
                        if (argCount != 3) {
                            system.error("RGB/HSB conversion function takes 3 arguments", where);
                        }
                        break;
                    case Vec:
                        final double[] value = new double[1];
                        if (argNum != 2) {
                            system.error("Vec function takes two arguments", where);
                        } else if (!arguments.getChild(1).isConstant() || !arguments.getChild(1).isNatural() || arguments.getChild(1).evaluate(builder, value, 1) != 1) {
                            system.error("Vec function length argument must be a scalar constant", where);
                        } else if ((int) Math.floor(value[0]) < 2 || (int) Math.floor(value[0]) > AST.MAX_VECTOR_SIZE) {
                            system.error("Vec function length argument must be >= 2 and <= 99", where);
                        }
                        break;
                    case Ftime:
                    case Frame:
                        if (arguments != null) {
                            system.error("ftime/frame functions takes no arguments", where);
                        }
                        constant = false;
                        arguments = new ASTReal(system, where, 1.0);
                        break;
                    case Rand:
                    case Rand2:
                    case RandInt:
                        constant = false;
                        // fall through
                    case RandStatic:
                        switch (argCount) {
                            case 0:
                                arguments = new ASTCons(system, where, new ASTReal(system, where, 0.0), new ASTReal(system, where, funcType == FuncType.RandInt ? 2.0 : 1.0));
                                break;
                            case 1:
                                arguments = new ASTCons(system, where, new ASTReal(system, where, 0.0));
                                break;
                            case 2:
                                break;
                            default:
                                system.error("Illegal argument(s) for random function", where);
                                break;
                        }
                        if (funcType == FuncType.RandStatic) {
                            if (!constant) {
                                system.error("Argument(s) for rand_static() must be constant", where);
                            }
                            constant = RandStaticIsConst; // terrible, but works for JSON
                        }
                        break;
                    case RandDiscrete:
                        constant = false;
                        natural = isNatural(null, argCount);
                        if (argCount < 1) {
                            system.error("Function takes at least one argument", where);
                        }
                        break;
                    case RandBernoulli:
                    case RandGeometric:
                    case RandPoisson:
                    case RandExponential:
                    case RandChiSquared:
                    case RandStudentT:
                        constant = false;
                        if (argCount != 1) {
                            system.error("Function takes one argument", where);
                        }
                        break;
                    case RandBinomial:
                    case RandNegBinomial:
                        natural = arguments != null && arguments.size() == 2 && arguments.getChild(0).isNatural();
                        //fall through
                    case RandCauchy:
                    case RandExtremeValue:
                    case RandFisherF:
                    case RandGamma:
                    case RandLogNormal:
                    case RandNormal:
                    case RandWeibull:
                        constant = false;
                        if (argCount != 2) {
                            system.error("Function takes two arguments", where);
                        }
                        break;
                    case Min:
                    case Max:
                        if (argCount != 2) {
                            system.error("Function takes at least two arguments", where);
                        }
                        break;
                    case NotAFunction:
                        system.error("Unknown function", where);
                }

                for (FuncType t : MIGHT_BE_NATURAL) {
                    if (t == funcType) {
                        natural = arguments == null || arguments.isNatural();
                        break;
                    }
                }

                for (FuncType t : MUST_BE_NATURAL) {
                    if (t == funcType) {
                        if (arguments != null && !arguments.isNatural() && !ASTParameter.Impure) {
                            system.error("Function is defined over natural numbers only", null);
                        }
                        natural = true;
                        break;
                    }
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

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		arguments = ASTExpression.simplify(builder, arguments);
		if (constant) {
			final double[] result = new double[MAX_VECTOR_SIZE];
			int len = evaluate(builder, null, result, MAX_VECTOR_SIZE);
			if (len < 0) {
				return this;
			}
            return AST.makeResult(result, len, this);
		}
		return null;
	}

    private double minMax(CFDGBuilder builder, CFDGRenderer renderer, ASTExpression e, boolean isMin) {
        final double[] result = new double[] { 0.0 };
        if (e.getChild(0).evaluate(builder, renderer, result, 1) != 1) {
            system.error("Error computing min/max here", e.getChild(0).getWhere());
        }
        for (int i = 1; i < e.size(); ++i) {
            final double[] value = new double[] { 0.0 };
            if (e.getChild(i).evaluate(builder, renderer, value, 1) != 1) {
                system.error("Error computing min/max here", e.getChild(i).getWhere());
            }
            boolean leftMin = result[0] < value[0];
            result[0] = ((isMin && leftMin) || (!isMin && !leftMin)) ? result[0] : value[0];
        }
        return result[0];
    }

    private boolean isNatural(CFDGRenderer renderer, double value) {
        return value >= 0 && value <= (renderer != null ? renderer.getMaxNatural() : Integer.MAX_VALUE) && value == Math.floor(value);
    }
}
