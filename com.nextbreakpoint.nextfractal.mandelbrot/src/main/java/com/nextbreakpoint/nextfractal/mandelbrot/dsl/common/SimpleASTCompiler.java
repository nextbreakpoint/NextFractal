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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.common;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColorComponent;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColorPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionCompareOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionJulia;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionLogicOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionParen;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTConditionalStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFunction;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOperator;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTParen;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRuleCompareOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRuleExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTRuleLogicOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStopStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpressionPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpressionScalar;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorGreater;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorGreaterOrEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorLesser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorLesserOrEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCompareOperatorNotEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLConditionJulia;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLConditionNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLConditionalStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionAbs;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionAcos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionAsin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionAtan;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionAtan2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionCeil;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionCos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionCosZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionExp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionExpZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionFloor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionHypot;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionImZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionLog;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionMax;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionMin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionMod;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionMod2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionModZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionModZ2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionPhaZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionPow;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionPowZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionPulse;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionRamp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionReZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSaw;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSinZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSqrt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSqrtZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionSquare;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionTan;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionTanZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFunctionTime;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLLogicOperatorAnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLLogicOperatorOr;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLLogicOperatorXor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorAdd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorAddZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorDiv;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorDivZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorMul;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorMulZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorPos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorPow;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorPowZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorSub;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOperatorSubZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLParen;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatementStop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapConditionNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpArcRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpArcTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpClose;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpCurveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpCurveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpLineRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpLineTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpMoveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpMoveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpQuadRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOpQuadTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLVariable;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleASTCompiler implements ASTCompiler {
	private final Map<String, VariableDeclaration> variables;
	private final ExpressionContext context;
	
	public SimpleASTCompiler(ExpressionContext context, Map<String, VariableDeclaration> variables) {
		this.context = context;
		this.variables = variables;
	}

	@Override
	public DSLExpression compile(ASTNumber number) {
		return new DSLNumber(number.getLocation(), number.r(), number.i(), context.newNumberIndex());
	}

	@Override
	public DSLExpression compile(ASTFunction function) {
		final Token location = function.getLocation();
        switch (function.getName()) {
            case "time" -> {
                if (function.getArguments().length != 0) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
            }
            case "mod", "mod2", "pha", "re", "im", "sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "exp" -> {
                if (function.getArguments().length != 1) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
            }
            case "abs", "ceil", "floor", "log", "square", "saw", "ramp" -> {
                if (function.getArguments().length != 1) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[0].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + location.getText(), location);
                }
            }
            case "min", "max", "atan2", "hypot", "pulse" -> {
                if (function.getArguments().length != 2) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[0].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[1].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + location.getText(), location);
                }
            }
            case "pow" -> {
                if (function.getArguments().length != 2) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[1].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + location.getText(), location);
                }
            }
            default -> throw new ASTException("Unsupported function: " + location.getText(), location);
        }
        switch (function.getName()) {
            case "time" -> {
                return new DSLFunctionTime(location, context.newNumberIndex());
            }
            case "mod" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionMod(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionModZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "mod2" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionMod2(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionModZ2(location, context, compileArguments(function.getArguments()));
                }
            }
            case "pha" -> {
                return new DSLFunctionPhaZ(location, context, compileArguments(function.getArguments()));
            }
            case "re" -> {
                return new DSLFunctionReZ(location, context, compileArguments(function.getArguments()));
            }
            case "im" -> {
                return new DSLFunctionImZ(location, context, compileArguments(function.getArguments()));
            }
            case "sin" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionSin(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionSinZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "cos" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionCos(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionCosZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "tan" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionTan(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionTanZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "asin" -> {
                return new DSLFunctionAsin(location, context, compileArguments(function.getArguments()));
            }
            case "acos" -> {
                return new DSLFunctionAcos(location, context, compileArguments(function.getArguments()));
            }
            case "atan" -> {
                return new DSLFunctionAtan(location, context, compileArguments(function.getArguments()));
            }
            case "abs" -> {
                return new DSLFunctionAbs(location, compileArguments(function.getArguments()), context.newNumberIndex());
            }
            case "ceil" -> {
                return new DSLFunctionCeil(location, context, compileArguments(function.getArguments()));
            }
            case "floor" -> {
                return new DSLFunctionFloor(location, context, compileArguments(function.getArguments()));
            }
            case "log" -> {
                return new DSLFunctionLog(location, context, compileArguments(function.getArguments()));
            }
            case "square" -> {
                return new DSLFunctionSquare(location, context, compileArguments(function.getArguments()));
            }
            case "saw" -> {
                return new DSLFunctionSaw(location, context, compileArguments(function.getArguments()));
            }
            case "ramp" -> {
                return new DSLFunctionRamp(location, context, compileArguments(function.getArguments()));
            }
            case "min" -> {
                return new DSLFunctionMin(location, context, compileArguments(function.getArguments()));
            }
            case "max" -> {
                return new DSLFunctionMax(location, context, compileArguments(function.getArguments()));
            }
            case "atan2" -> {
                return new DSLFunctionAtan2(location, context, compileArguments(function.getArguments()));
            }
            case "hypot" -> {
                return new DSLFunctionHypot(location, context, compileArguments(function.getArguments()));
            }
            case "pulse" -> {
                return new DSLFunctionPulse(location, context, compileArguments(function.getArguments()));
            }
            case "pow" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionPow(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionPowZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "sqrt" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionSqrt(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionSqrtZ(location, context, compileArguments(function.getArguments()));
                }
            }
            case "exp" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionExp(location, context, compileArguments(function.getArguments()));
                } else {
                    return new DSLFunctionExpZ(location, context, compileArguments(function.getArguments()));
                }
            }
            default -> {
            }
        }
		throw new ASTException("Unsupported function: " + location.getText(), location);
	}

	@Override
	public DSLExpression compile(ASTOperator operator) {
		final ASTExpression exp1 = operator.getExp1();
		final ASTExpression exp2 = operator.getExp2();
		final Token location = operator.getLocation();
		if (exp2 == null) {
            return switch (operator.getOp()) {
                case "-" -> new DSLOperatorNeg(location, exp1.compile(this), context.newNumberIndex());
                case "+" -> new DSLOperatorPos(location, exp1.compile(this), context.newNumberIndex());
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			if (exp1.isReal() && exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAdd(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSub(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMul(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDiv(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPow(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "<>" -> new DSLOperatorNumber(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp1.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPowZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPowZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.compile(this), exp2.compile(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			}
		}
	}

	@Override
	public DSLExpression compile(ASTParen paren) {
		return new DSLParen(paren.getLocation(), paren.getExp().compile(this), context.newNumberIndex());
	}

	@Override
	public DSLExpression compile(ASTVariable variable) {
		return new DSLVariable(context, variable.getName(), variable.isReal(), variable.getLocation());
	}

	@Override
	public DSLCondition compile(ASTConditionCompareOp condition) {
		final ASTExpression exp1 = condition.getExp1();
		final ASTExpression exp2 = condition.getExp2();
		final Token location = condition.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			exp1.compile(this);
            return switch (condition.getOp()) {
                case ">" -> new DSLCompareOperatorGreater(location, compileOperands(exp1, exp2));
                case "<" -> new DSLCompareOperatorLesser(location, compileOperands(exp1, exp2));
                case ">=" -> new DSLCompareOperatorGreaterOrEquals(location, compileOperands(exp1, exp2));
                case "<=" -> new DSLCompareOperatorLesserOrEquals(location, compileOperands(exp1, exp2));
                case "=" -> new DSLCompareOperatorEquals(location, compileOperands(exp1, exp2));
                case "<>" -> new DSLCompareOperatorNotEquals(location, compileOperands(exp1, exp2));
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
	}

	@Override
	public DSLCondition compile(ASTConditionLogicOp condition) {
		final ASTConditionExpression exp1 = condition.getExp1();
		final ASTConditionExpression exp2 = condition.getExp2();
		final Token location = condition.getLocation();
		return switch (condition.getOp()) {
            case "&" -> new DSLLogicOperatorAnd(location, compileLogicOperands(exp1, exp2));
            case "|" -> new DSLLogicOperatorOr(location, compileLogicOperands(exp1, exp2));
            case "^" -> new DSLLogicOperatorXor(location, compileLogicOperands(exp1, exp2));
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public DSLCondition compile(ASTConditionTrap condition) {
		if (condition.isContains()) {
			return new DSLTrapCondition(condition.getLocation(), condition.getName(), condition.getExp().compile(this));
		} else {
			return new DSLTrapConditionNeg(condition.getName(), condition.getExp().compile(this), condition.getLocation());
		}
	}

	@Override
	public DSLCondition compile(ASTRuleLogicOp operator) {
		final ASTRuleExpression exp1 = operator.getExp1();
		final ASTRuleExpression exp2 = operator.getExp2();
		final Token location = operator.getLocation();
		return switch (operator.getOp()) {
            case "&" -> new DSLLogicOperatorAnd(location, compileLogicOperands(exp1, exp2));
            case "|" -> new DSLLogicOperatorOr(location, compileLogicOperands(exp1, exp2));
            case "^" -> new DSLLogicOperatorXor(location, compileLogicOperands(exp1, exp2));
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public DSLCondition compile(ASTRuleCompareOp operator) {
		final ASTExpression exp1 = operator.getExp1();
		final ASTExpression exp2 = operator.getExp2();
		final Token location = operator.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
            return switch (operator.getOp()) {
                case ">" -> new DSLCompareOperatorGreater(location, compileOperands(exp1, exp2));
                case "<" -> new DSLCompareOperatorLesser(location, compileOperands(exp1, exp2));
                case ">=" -> new DSLCompareOperatorGreaterOrEquals(location, compileOperands(exp1, exp2));
                case "<=" -> new DSLCompareOperatorLesserOrEquals(location, compileOperands(exp1, exp2));
                case "=" -> new DSLCompareOperatorEquals(location, compileOperands(exp1, exp2));
                case "<>" -> new DSLCompareOperatorNotEquals(location, compileOperands(exp1, exp2));
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
	}

	@Override
	public DSLColorExpression compile(ASTColorPalette palette) {
		if (palette.getExp().isReal()) {
			return new DSLColorExpressionPalette(palette.getLocation(), palette.getName(), palette.getExp().compile(this));
		} else {
			throw new ASTException("Expression type not valid: " + palette.getLocation().getText(), palette.getLocation());
		}
	}

	@Override
	public DSLColorExpression compile(ASTColorComponent component) {
		final DSLExpression exp1 = component.getExp1().compile(this);
		DSLExpression exp2 = null;
		DSLExpression exp3 = null;
		DSLExpression exp4 = null;
		if (component.getExp2() != null) {
			exp2 = component.getExp2().compile(this);
		}
		if (component.getExp3() != null) {
			exp3 = component.getExp3().compile(this);
		}
		if (component.getExp4() != null) {
			exp4 = component.getExp4().compile(this);
		}
		return new DSLColorExpressionScalar(component.getLocation(), exp1, exp2, exp3, exp4);
	}

	@Override
	public DSLStatement compile(ASTConditionalStatement statement) {
		final List<DSLStatement> thenStatements = new ArrayList<>();
		final List<DSLStatement> elseStatements = new ArrayList<>();
		final HashMap<String, VariableDeclaration> newThenScope = new HashMap<>(variables);
		final SimpleASTCompiler thenCompiler = new SimpleASTCompiler(context, newThenScope);
		for (ASTStatement innerStatement : statement.getThenStatementList().getStatements()) {
			thenStatements.add(innerStatement.compile(thenCompiler));
		}
		if (statement.getElseStatementList() != null) {
			final HashMap<String, VariableDeclaration> newElseScope = new HashMap<>(variables);
			final SimpleASTCompiler elseCompiler = new SimpleASTCompiler(context, newElseScope);
			for (ASTStatement innerStatement : statement.getElseStatementList().getStatements()) {
				elseStatements.add(innerStatement.compile(elseCompiler));
			}
		}
		return new DSLConditionalStatement(statement.getLocation(), statement.getConditionExp().compile(this), thenStatements, elseStatements);
	}

	@Override
	public DSLStatement compile(ASTAssignStatement statement) {
        final VariableDeclaration var = variables.get(statement.getName());
        if (var != null && var.isReal() && !statement.getExp().isReal()) {
            throw new ASTException("Can't assign expression", statement.getLocation());
        }
		return new DSLAssignStatement(statement.getLocation(), statement.getName(), statement.getExp().compile(this), context.newNumberIndex());
	}

	@Override
	public DSLStatement compile(ASTStopStatement statement) {
		return new DSLStatementStop(statement.getLocation());
	}

	@Override
	public DSLCondition compile(ASTConditionJulia condition) {
		return new DSLConditionJulia(condition.getLocation());
	}

	@Override
	public DSLCondition compile(ASTConditionParen condition) {
		return condition.getExp().compile(this);
	}

	@Override
	public DSLCondition compile(ASTConditionNeg condition) {
		return new DSLConditionNeg(condition.getLocation(), condition.getExp().compile(this));
	}

	@Override
	public DSLTrap compile(ASTOrbitTrap orbitTrap) {
		final DSLTrap trap = new DSLTrap(orbitTrap.getLocation());
		trap.setName(orbitTrap.getName());
		trap.setCenter(new ComplexNumber(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
		final List<DSLTrapOp> operators = new ArrayList<>();
		for (ASTOrbitTrapOp astTrapOp : orbitTrap.getOperators()) {
			operators.add(astTrapOp.compile(this));
		}
		trap.setOperators(operators);
		return trap;
	}

	@Override
	public DSLTrapOp compile(ASTOrbitTrapOp orbitTrapOp) {
		ComplexNumber c1 = null;
		ComplexNumber c2 = null;
		ComplexNumber c3 = null;
		if (orbitTrapOp.getC1() != null) {
			c1 = new ComplexNumber(orbitTrapOp.getC1().r(), orbitTrapOp.getC1().i());
		}
		if (orbitTrapOp.getC2() != null) {
			c2 = new ComplexNumber(orbitTrapOp.getC2().r(), orbitTrapOp.getC2().i());
		}
		if (orbitTrapOp.getC3() != null) {
			c3 = new ComplexNumber(orbitTrapOp.getC3().r(), orbitTrapOp.getC3().i());
		}
		final Token location = orbitTrapOp.getLocation();
		return switch (orbitTrapOp.getOp()) {
            case "MOVETO" -> new DSLTrapOpMoveTo(location, c1);
            case "MOVEREL", "MOVETOREL" -> new DSLTrapOpMoveRel(location, c1);
            case "LINETO" -> new DSLTrapOpLineTo(location, c1);
            case "LINEREL", "LINETOREL" -> new DSLTrapOpLineRel(location, c1);
            case "ARCTO" -> new DSLTrapOpArcTo(location, c1, c2);
            case "ARCREL", "ARCTOREL" -> new DSLTrapOpArcRel(location, c1, c2);
            case "QUADTO" -> new DSLTrapOpQuadTo(location, c1, c2);
            case "QUADREL", "QUADTOREL" -> new DSLTrapOpQuadRel(location, c1, c2);
            case "CURVETO" -> new DSLTrapOpCurveTo(location, c1, c2, c3);
            case "CURVEREL", "CURVETOREL" -> new DSLTrapOpCurveRel(location, c1, c2, c3);
            case "CLOSE" -> new DSLTrapOpClose(location);
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public DSLPalette compile(ASTPalette palette) {
		final List<DSLPaletteElement> elements = new ArrayList<>();
		for (ASTPaletteElement astElement : palette.getElements()) {
			elements.add(astElement.compile(this));
		}
        return new DSLPalette(palette.getLocation(), palette.getName(), elements);
	}

	@Override
	public DSLPaletteElement compile(ASTPaletteElement paletteElement) {
		return new DSLPaletteElement(paletteElement.getLocation(), paletteElement.getBeginColor().getComponents(), paletteElement.getEndColor().getComponents(), paletteElement.getSteps(), paletteElement.getExp() != null ? paletteElement.getExp().compile(this) : null);
	}

	private DSLExpression[] compileArguments(ASTExpression[] arguments) {
		final DSLExpression[] args = new DSLExpression[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			args[i] = arguments[i].compile(this);
		}
		return args;
	}

	private DSLExpression[] compileOperands(ASTExpression exp1, ASTExpression exp2) {
		final DSLExpression[] operands = new DSLExpression[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private DSLCondition[] compileLogicOperands(ASTConditionExpression exp1, ASTConditionExpression exp2) {
		final DSLCondition[] operands = new DSLCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private DSLCondition[] compileLogicOperands(ASTRuleExpression exp1, ASTRuleExpression exp2) {
		final DSLCondition[] operands = new DSLCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	@Override
	public DSLExpression compile(ASTConditionalExpression conditional) {
		return new DSLConditionalExpression(conditional.getLocation(), conditional.getConditionExp().compile(this), conditional.getThenExp().compile(this), conditional.getElseExp().compile(this), context.newNumberIndex());
	}
}
