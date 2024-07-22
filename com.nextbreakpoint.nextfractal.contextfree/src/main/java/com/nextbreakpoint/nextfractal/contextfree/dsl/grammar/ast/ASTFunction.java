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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.core.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.HSBColor;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.DeferUntilRuntimeException;
import org.antlr.v4.runtime.Token;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.AST.MAX_VECTOR_SIZE;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Abs;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitAnd;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitLeft;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitNot;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitOr;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitRight;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.BitXOR;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Div;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Divides;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Factorial;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.IsNatural;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Max;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Min;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Mod;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.RandInt;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FuncType.Sg;

public class ASTFunction extends ASTExpression {
	private ASTExpression arguments;
	private FuncType funcType;
	private double random;

	public static final FuncType[] MUST_BE_NATURAL = new FuncType[]{
            Factorial, Sg, IsNatural, Div, Divides
    };
	public static final FuncType[] MIGHT_BE_NATURAL = new FuncType[]{
            Mod, Abs, Min, Max, BitNot, BitOr, BitAnd, BitXOR, BitLeft, BitRight, RandInt
    };

	public ASTFunction(CFDGDriver driver, String name, ASTExpression arguments, Rand64 seed, Token location) {
		super(driver, true, false, ExpType.NumericType, location);
		this.funcType = FuncType.NotAFunction;
		this.arguments = arguments;

		if (name.isEmpty()) {
			driver.error("Bad function call", location);
			return;
		}

		funcType = FuncType.byName(name);

		if (funcType == FuncType.NotAFunction) {
			driver.error("Unknown function", location);
			return;
		}

		if (funcType == FuncType.RandStatic) {
			random = seed.getDouble();
		}
	}

	public ASTExpression getArguments() {
		return arguments;
	}

	public FuncType getFuncType() {
		return funcType;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (type != ExpType.NumericType) {
		   throw new RuntimeException("Non-numeric expression in a numeric context");
		}

		int destLength = (funcType.getType() >= FuncType.Cross.getType() && funcType.getType() <= FuncType.Rgb2Hsb.getType()) ? 3 : funcType == FuncType.Vec ? (int)Math.floor(random) : 1;

		if (result == null)
			return destLength;

		if (length < destLength)
			return -1;

		switch (funcType) {
			case Min:
			case Max: {
				result[0] = minMax(arguments, renderer, funcType == FuncType.Min);
				return 1;
			}
			case Dot: {
				double[] l = new double[MAX_VECTOR_SIZE];
				double[] r = new double[MAX_VECTOR_SIZE];
				int lc = arguments.getChild(0).evaluate(l, MAX_VECTOR_SIZE, renderer);
				int rc = arguments.getChild(1).evaluate(r, MAX_VECTOR_SIZE, renderer);
				if (lc == rc && lc > 1) {
					result[0] = 0.0;
					for (int i = 0; i < lc; i++) {
						result[0] += l[i] * r[i];
					}
				}
				return 1;
			}
			case Cross: {
				double[] l = new double[3];
				double[] r = new double[3];
				int lc = arguments.getChild(0).evaluate(l, 3, renderer);
				int rc = arguments.getChild(1).evaluate(r, 3, renderer);
				if (lc == rc && lc == 3) {
					result[0] = l[1] * r[2] - l[2] * r[1];
					result[1] = l[2] * r[0] - l[0] * r[2];
					result[2] = l[0] * r[1] - l[1] * r[0];
				}
				return 3;
			}
			case Vec: {
				double[] v = new double[MAX_VECTOR_SIZE];
				int lv = arguments.getChild(0).evaluate(v, MAX_VECTOR_SIZE, renderer);
				if (lv >= 1) {
					for (int i = 0; i < destLength; i++) {
						result[i] = v[i % lv];
					}
				}
				return destLength;
			}
			case Hsb2Rgb: {
				double[] c = new double[3];
				int l = arguments.evaluate(c, 3, renderer);
				if (l == 3) {
					HSBColor color = new HSBColor(c[0], c[1], c[2], 1.0);
					double[] rgb = color.getRGBA();
					result[0] = rgb[0];
					result[1] = rgb[2];
					result[2] = rgb[3];
				}
				return 3;
			}
			case Rgb2Hsb: {
				double[] c = new double[3];
				int l = arguments.evaluate(c, 3, renderer);
				if (l == 3) {
					double[] rgb = new double[] { c[0], c[1], c[2], 1.0 };
					HSBColor color = new HSBColor(rgb);
					result[0] = color.hue();
					result[1] = color.bright();
					result[2] = color.sat();
				}
				return 3;
			}
			case RandIntDiscrete: {
				double[] w = new double[MAX_VECTOR_SIZE];
				int lw = arguments.evaluate(w, MAX_VECTOR_SIZE, renderer);
				if (lw  >= 1) {
					result[0] = renderer.getCurrentSeed().getDiscrete(lw, w);
				}
				return 1;
			}
			default: {
				break;
			}
		}

		double[] a = new double[2];
		int count = arguments.evaluate(a, 2, renderer);
		// no need to checkParam the argument count, the constructor already checked it

		// But checkParam it anyway to make valgrind happy
		if (count < 0) return 1;

		switch (funcType) {
			case Cos:
				result[0] = Math.cos(a[0] * 0.0174532925199);
				break;
			case Sin:
				result[0] = Math.sin(a[0] * 0.0174532925199);
				break;
			case Tan:
				result[0] = Math.tan(a[0] * 0.0174532925199);
				break;
			case Cot:
				result[0] = 1.0 / Math.tan(a[0] * 0.0174532925199);
				break;
			case  Acos:
				result[0] = Math.acos(a[0]) * 57.29577951308;
				break;
			case Asin:
				result[0] = Math.asin(a[0]) * 57.29577951308;
				break;
			case Atan:
				result[0] = Math.atan(a[0]) * 57.29577951308;
				break;
			case Acot:
				result[0] = Math.atan(1.0 / a[0]) * 57.29577951308;
				break;
			case Cosh:
				result[0] = Math.cosh(a[0]);
				break;
			case Sinh:
				result[0] = Math.sinh(a[0]);
				break;
			case Tanh:
				result[0] = Math.tanh(a[0]);
				break;
			case Acosh:
				result[0] = Math.log(a[0] + Math.sqrt(a[0] * a[0] - 1));
				break;
			case Asinh:
				result[0] = Math.log(a[0] + Math.sqrt(a[0] * a[0] + 1));
				break;
			case Atanh:
				result[0] = Math.log((1 / a[0] + 1) / (1 / a[0] - 1)) / 2;
				break;
			case Log:
				result[0] = Math.log(a[0]);
				break;
			case Log10:
				result[0] = Math.log10(a[0]);
				break;
			case Sqrt:
				result[0] = Math.sqrt(a[0]);
				break;
			case Exp:
				result[0] = Math.exp(a[0]);
				break;
			case Abs:
				if (count == 1) {
					result[0] = Math.abs(a[0]);
				} else {
					result[0] = Math.abs(a[0] - a[1]);
				}
				break;
			case Infinity:
				result[0] = a[0] < 0.0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
				break;
			case Factorial:
				if (a[0] < 0.0 || a[0] > 18.0 || a[0] != Math.floor(a[0])) {
					driver.fail("Illegal argument for factorial", location);
				}
				result[0] = 1.0;
				for (double v = 1.0; v <= a[0]; v += 1.0) {
					result[0] *= v;
				}
				break;
			case Sg:
				result[0] = a[0] == 0.0 ? 0.0 : 1.0;
				break;
			case IsNatural:
				result[0] = isNatural(renderer, a[0]) ? 1 : 0;
				break;
			case BitNot:
				result[0] = (~((long)a[0])) & 0xFFFFFFFF;
				break;
			case BitOr:
				result[0] = (((long)a[0]) | ((long)a[1])) & 0xFFFFFFFF;
				break;
			case BitAnd:
				result[0] = (((long)a[0]) & ((long)a[1])) & 0xFFFFFFFF;
				break;
			case BitXOR:
				result[0] = (((long)a[0]) ^ ((long)a[1])) & 0xFFFFFFFF;
				break;
			case BitLeft:
				result[0] = (((long)a[0]) << ((long)a[1])) & 0xFFFFFFFF;
				break;
			case BitRight:
				result[0] = (((long)a[0]) >> ((long)a[1])) & 0xFFFFFFFF;
				break;
			case Atan2:
				result[0] = Math.atan2(a[0], a[1]) * 57.29577951308;
				break;
			case Mod:
				if (arguments.isNatural()) {
					result[0] = ((long)a[0]) % ((long)a[1]);
				} else {
					result[0] = a[0] % a[1];
				}
				break;
			case Divides:
				result[0] = (((long)a[0]) % ((long)a[1])) == 0 ? 1.0 : 0.0;
				break;
			case Div:
				result[0] = ((long)a[0]) / ((long)a[1]);
				break;
			case Floor:
				result[0] = Math.floor(a[0]);
				break;
			case Ceiling:
				result[0] = Math.ceil(a[0]);
				break;
			case Ftime:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				result[0] = renderer.getCurrentTime();
				break;
			case Frame:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				result[0] = renderer.getCurrentFrame();
				break;
			case RandStatic:
				result[0] = random * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]);
				break;
			case Rand:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getDouble() * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]);
				break;
			case Rand2:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = (renderer.getCurrentSeed().getDouble() * 2.0 - 1.0) * a[1] + a[0];
				break;
			case RandExponential:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getExponential(a[0]);
				break;
			case RandGamma:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getGamma(a[0], a[1]);
				break;
			case RandWeibull:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getWeibull(a[0], a[1]);
				break;
			case RandExtremeValue:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getExtremeValue(a[0], a[1]);
				break;
			case RandNormal:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getNormal(a[0], a[1]);
				break;
			case RandLogNormal:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getLogNormal(a[0], a[1]);
				break;
			case RandChiSquared:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getChiSquared(a[0]);
				break;
			case RandCauchy:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getCauchy(a[0], a[1]);
				break;
			case RandFisherF:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getFisherF(a[0], a[1]);
				break;
			case RandStudentT:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getStudentT(a[0]);
				break;
			case RandInt:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = Math.floor(renderer.getCurrentSeed().getDouble() * Math.abs(a[1] - a[0]) + Math.min(a[0], a[1]));
				break;
			case RandIntBernoulli:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = renderer.getCurrentSeed().getBernoulli(a[0]) ? 1.0 : 0.0;
				break;
			case RandIntBinomial:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = Math.floor(renderer.getCurrentSeed().getBinomial((long)a[0], a[1]));
				break;
			case RandIntNegBinomial:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = Math.floor(renderer.getCurrentSeed().getNegativeBinomial((long)a[0], a[1]));
				break;
			case RandIntPoisson:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = Math.floor(renderer.getCurrentSeed().getPoisson(a[0]));
				break;
			case RandIntGeometric:
				if (renderer == null) throw new DeferUntilRuntimeException(location);
				renderer.setRandUsed(true);
				result[0] = Math.floor(renderer.getCurrentSeed().getGeometric(a[0]));
				break;

			default:
				return -1;
		}

		return 1;
	}

	private static double minMax(ASTExpression e, CFDGRenderer renderer, boolean isMin) {
		double[] res = new double[] { 0.0 };
		if (e.getChild(0).evaluate(res, 1, renderer) != 1) {
			renderer.getDriver().fail("Error computing min/max here", e.getChild(0).getLocation());
		}
		for (int i = 1; i < e.size(); ++i) {
			double[] val = new double[] { 0.0 };
			if (e.getChild(i).evaluate(val, 1, renderer) != 1) {
				renderer.getDriver().fail("Error computing min/max here", e.getChild(i).getLocation());
			}
			boolean leftMin = res[0] < val[0];
			res[0] = ((isMin && leftMin) || (!isMin && !leftMin)) ? res[0] : val[0];
		}
		return res[0];
	}

	private boolean isNatural(CFDGRenderer renderer, double n) {
		return n >= 0 && n <= (renderer != null ? renderer.getMaxNatural() : Integer.MAX_VALUE) && n == Math.floor(n);
	}

	@Override
	public void entropy(StringBuilder e) {
		if (arguments != null) {
			arguments.entropy(e);
		}
		e.append(funcType.getEntropy());
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		arguments = compile(arguments, ph);
		switch (ph) {
			case TypeCheck:
				{
					isConstant = true;
					locality = Locality.PureLocal;
					int argcount = 0;
					int argnum = 0;
					if (arguments != null) {
						argnum = arguments.size();
						isConstant = arguments.isConstant();
						locality = arguments.getLocality();
						if (locality == Locality.PureNonlocal) {
							locality = Locality.ImpureNonlocal;
						}
						if (arguments.getType() == ExpType.NumericType) {
							argcount = arguments.evaluate(null, 0);
						} else {
							driver.error("Function arguments must be numeric", arguments.getLocation());
						}
					}
					switch (funcType) {
						case Abs:
							if (argcount < 1 || argcount > 2) {
								driver.error("Function takes one or two arguments", arguments.getLocation());
							}
							break;
						case Infinity:
							if (argcount == 0) {
								arguments = new ASTReal(driver, 1.0, arguments.getLocation());
								argcount = 1;
							}
							break;
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
							if (argcount != 1) {
								driver.error("Function takes one argument", arguments.getLocation());
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
							if (argcount != 2) {
								driver.error("Function takes two arguments", arguments.getLocation());
							}
							break;
						case Dot:
						case Cross:
							if (argnum != 2) {
								driver.error("Dot/cross product takes two vectors", arguments.getLocation());
							} else {
								int l = arguments.getChild(0).evaluate(null, 0);
								int r = arguments.getChild(0).evaluate(null, 0);
								if (funcType == FuncType.Dot && (l != r || l < 2)) {
									driver.error("Dot product takes two vectors of the same length", arguments.getLocation());
								}
								if (funcType == FuncType.Cross && (l != 3 || r != 3)) {
									driver.error("Cross product takes two vector3s", arguments.getLocation());
								}
							}
							break;
						case Hsb2Rgb:
						case Rgb2Hsb:
							if (argcount != 3) {
								driver.error("RGB/HSB conversion function takes 3 arguments", arguments.getLocation());
							}
							break;
						case Vec:
							double[] value = new double[1];
							if (argnum != 2) {
								driver.error("Vec function takes two arguments", arguments.getLocation());
							} else if (!arguments.getChild(1).isConstant() || !arguments.getChild(1).isNatural() || arguments.getChild(1).evaluate(value, 1) != 1) {
								driver.error("Vec function length argument must be a scalar constant", arguments.getLocation());
							} else if ((int)Math.floor(value[0]) < 2 || (int)Math.floor(value[0]) > AST.MAX_VECTOR_SIZE) {
								driver.error( "Vec function length argument must be >= 2 and <= 99", arguments.getLocation());
							}
							break;
						case Ftime:
						case Frame:
							if (arguments != null) {
								driver.error("ftime/frame functions takes no arguments", arguments.getLocation());
							}
							isConstant = false;
							arguments = new ASTReal(driver, 1.0, arguments.getLocation());
							break;
						case Rand:
						case Rand2:
						case RandInt:
							isConstant = false;
						case RandStatic:
							switch (argcount) {
								case 0:
									arguments = new ASTCons(driver, arguments.getLocation(), new ASTReal(driver, 0.0, arguments.getLocation()), new ASTReal(driver, funcType == FuncType.RandInt ? 2.0 : 1.0, arguments.getLocation()));
									break;
								case 1:
									arguments = new ASTCons(driver, arguments.getLocation(), new ASTReal(driver, 0.0, arguments.getLocation()));
									break;
								case 2:
									break;
								default:
									driver.error("Illegal argument(s) for random function", arguments.getLocation());
									break;
							}
							if (!isConstant && funcType == FuncType.RandStatic) {
								driver.error("Argument(s) for rand_static() must be constant", arguments.getLocation());
							}
							break;
						case RandIntDiscrete:
							isConstant = false;
							isNatural = isNatural(null, argcount);
							if (argcount < 1) {
								driver.error("Function takes at least one argument", arguments.getLocation());
							}
							break;
						case RandIntBernoulli:
						case RandIntGeometric:
						case RandIntPoisson:
						case RandExponential:
						case RandChiSquared:
						case RandStudentT:
							isConstant = false;
							if (argcount != 1) {
								driver.error("Function takes one argument", arguments.getLocation());
							}
							break;
						case RandIntBinomial:
						case RandIntNegBinomial:
							isNatural = arguments != null && arguments.size() == 2 && arguments.getChild(0).isNatural();
						case RandCauchy:
						case RandExtremeValue:
						case RandFisherF:
						case RandGamma:
						case RandLogNormal:
						case RandNormal:
						case RandWeibull:
							isConstant = false;
							if (argcount != 2) {
								driver.error("Function takes two arguments", arguments.getLocation());
							}
							break;
						case Min:
						case Max:
							if (argcount != 2) {
								driver.error("Function takes at least two arguments", arguments.getLocation());
							}
							break;
						case NotAFunction:
							driver.error("Unknown function", arguments.getLocation());
							break;
					}

					if (funcType == FuncType.Infinity && argcount == 0) {
						arguments = new ASTReal(driver, 1.0, location);
						return null;
					}
					if (funcType == FuncType.Ftime) {
						if (arguments != null) {
							driver.error("ftime() function takes no arguments", null);
						}
						isConstant = false;
						arguments = new ASTReal(driver, 1.0, location);
					}
					if (funcType == FuncType.Frame) {
						if (arguments != null) {
							driver.error("time() function takes no arguments", null);
						}
						isConstant = false;
						arguments = new ASTReal(driver, 1.0, location);
					}
					if (funcType.getType() >= FuncType.RandStatic.getType() && funcType.getType() <= FuncType.RandInt.getType()) {
						if (funcType != FuncType.RandStatic) {
							isConstant = false;
						}
						switch (argcount) {
						case 0:
							arguments = new ASTCons(driver, location, new ASTReal(driver, 0.0, location), new ASTReal(driver, funcType == FuncType.RandInt ? 2.0 : 1.0, location));
							break;

						case 1:
							arguments = new ASTCons(driver, location, new ASTReal(driver, 0.0, location), arguments);
							break;

						case 2:
							break;

						default:
							driver.error("Illegal argument(s) for random function", null);
							break;
						}
						if (!isConstant && funcType == FuncType.RandStatic) {
							driver.error("Argument(s) for rand_static() must be constant", null);
						}
						if (funcType == FuncType.RandInt && arguments != null) {
							isNatural = arguments.isNatural();
						}
						return null;
					}

					for (FuncType t : MIGHT_BE_NATURAL) {
						if (t == funcType) {
							isNatural = arguments != null && arguments.isNatural();
							break;
						}
					}

					for (FuncType t : MUST_BE_NATURAL) {
						if (t == funcType) {
							if (arguments != null && !arguments.isNatural() && !ASTParameter.Impure) {
								driver.error("Function is defined over natural numbers only", null);
							}
							isNatural = true;
							break;
						}
					}
				}
				break;

			case Simplify:
				break;

			default:
				break;
		}
		return null;
	}

	@Override
	public ASTExpression simplify() {
		arguments = simplify(arguments);
		if (isConstant) {
			double[] result = new double[MAX_VECTOR_SIZE];
			int len = evaluate(result, MAX_VECTOR_SIZE, null);
			if (len < 0) {
				return this;
			}
			ASTExpression r = AST.makeResult(driver, result[0], len, this);
			return r;
		}
		return this;
	}
}
