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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLToken;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColorComponent;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColorPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionCompareOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionJulia;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionLogicOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionParen;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTConditionalStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFunction;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOperator;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTParen;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTResolver;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTRuleCompareOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTRuleExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTRuleLogicOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTStopStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultResolver implements ASTResolver {
	private final Map<String, VariableDeclaration> variables;
	private final DSLExpressionContext context;
	
	public DefaultResolver(DSLExpressionContext context, Map<String, VariableDeclaration> variables) {
		this.context = context;
		this.variables = variables;
	}

	@Override
	public DSLExpression resolve(ASTNumber number) {
		return new DSLNumber(number.getLocation(), number.r(), number.i(), context.newNumberIndex());
	}

	@Override
	public DSLExpression resolve(ASTFunction function) {
		final DSLToken location = function.getLocation();
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
                    throw new ASTException("Invalid type of arguments: " + function.getArguments()[0].getLocation().getText(), function.getArguments()[0].getLocation());
                }
            }
            case "min", "max", "atan2", "hypot", "pulse" -> {
                if (function.getArguments().length != 2) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[0].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + function.getArguments()[0].getLocation().getText(), function.getArguments()[0].getLocation());
                }
                if (!function.getArguments()[1].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + function.getArguments()[1].getLocation().getText(), function.getArguments()[1].getLocation());
                }
            }
            case "pow" -> {
                if (function.getArguments().length != 2) {
                    throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                }
                if (!function.getArguments()[1].isReal()) {
                    throw new ASTException("Invalid type of arguments: " + function.getArguments()[1].getLocation().getText(), function.getArguments()[1].getLocation());
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
                    return new DSLFunctionMod(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionModZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "mod2" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionMod2(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionModZ2(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "pha" -> {
                return new DSLFunctionPhaZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "re" -> {
                return new DSLFunctionReZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "im" -> {
                return new DSLFunctionImZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "sin" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionSin(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionSinZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "cos" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionCos(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionCosZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "tan" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionTan(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionTanZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "asin" -> {
                return new DSLFunctionAsin(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "acos" -> {
                return new DSLFunctionAcos(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "atan" -> {
                return new DSLFunctionAtan(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "abs" -> {
                return new DSLFunctionAbs(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "ceil" -> {
                return new DSLFunctionCeil(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "floor" -> {
                return new DSLFunctionFloor(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "log" -> {
                return new DSLFunctionLog(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "square" -> {
                return new DSLFunctionSquare(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "saw" -> {
                return new DSLFunctionSaw(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "ramp" -> {
                return new DSLFunctionRamp(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "min" -> {
                return new DSLFunctionMin(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "max" -> {
                return new DSLFunctionMax(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "atan2" -> {
                return new DSLFunctionAtan2(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "hypot" -> {
                return new DSLFunctionHypot(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "pulse" -> {
                return new DSLFunctionPulse(location, resolveArguments(function.getArguments()), context.newNumberIndex());
            }
            case "pow" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionPow(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionPowZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "sqrt" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionSqrt(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionSqrtZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            case "exp" -> {
                if (function.getArguments()[0].isReal()) {
                    return new DSLFunctionExp(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                } else {
                    return new DSLFunctionExpZ(location, resolveArguments(function.getArguments()), context.newNumberIndex());
                }
            }
            default -> {
            }
        }
		throw new ASTException("Unsupported function: " + location.getText(), location);
	}

	@Override
	public DSLExpression resolve(ASTOperator operator) {
		final ASTExpression exp1 = operator.getExp1();
		final ASTExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
		if (exp2 == null) {
            return switch (operator.getOp()) {
                case "-" -> new DSLOperatorNeg(location, exp1.resolve(this), context.newNumberIndex());
                case "+" -> new DSLOperatorPos(location, exp1.resolve(this), context.newNumberIndex());
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			if (exp1.isReal() && exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAdd(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSub(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMul(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDiv(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPow(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "<>" -> new DSLOperatorNumber(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp1.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPowZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "^" -> new DSLOperatorPowZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else {
                return switch (operator.getOp()) {
                    case "+" -> new DSLOperatorAddZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "-" -> new DSLOperatorSubZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "*" -> new DSLOperatorMulZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    case "/" -> new DSLOperatorDivZ(location, exp1.resolve(this), exp2.resolve(this), context.newNumberIndex());
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			}
		}
	}

	@Override
	public DSLExpression resolve(ASTParen paren) {
		return new DSLParen(paren.getLocation(), paren.getExp().resolve(this));
	}

	@Override
	public DSLExpression resolve(ASTVariable variable) {
		return new DSLVariable(variable.getLocation(), variable.getName(), variable.isReal());
	}

    @Override
    public DSLExpression resolve(ASTConditionalExpression conditional) {
        return new DSLConditionalExpression(conditional.getLocation(), conditional.getConditionExp().resolve(this), conditional.getThenExp().resolve(this), conditional.getElseExp().resolve(this));
    }

	@Override
	public DSLCondition resolve(ASTConditionCompareOp condition) {
		final ASTExpression exp1 = condition.getExp1();
		final ASTExpression exp2 = condition.getExp2();
		final DSLToken location = condition.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			exp1.resolve(this);
            return switch (condition.getOp()) {
                case ">" -> new DSLCompareOperatorGreater(location, resolveOperands(exp1, exp2));
                case "<" -> new DSLCompareOperatorLesser(location, resolveOperands(exp1, exp2));
                case ">=" -> new DSLCompareOperatorGreaterOrEquals(location, resolveOperands(exp1, exp2));
                case "<=" -> new DSLCompareOperatorLesserOrEquals(location, resolveOperands(exp1, exp2));
                case "=" -> new DSLCompareOperatorEquals(location, resolveOperands(exp1, exp2));
                case "<>" -> new DSLCompareOperatorNotEquals(location, resolveOperands(exp1, exp2));
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
	}

	@Override
	public DSLCondition resolve(ASTConditionLogicOp condition) {
		final ASTConditionExpression exp1 = condition.getExp1();
		final ASTConditionExpression exp2 = condition.getExp2();
		final DSLToken location = condition.getLocation();
		return switch (condition.getOp()) {
            case "&" -> new DSLLogicOperatorAnd(location, resolveLogicOperands(exp1, exp2));
            case "|" -> new DSLLogicOperatorOr(location, resolveLogicOperands(exp1, exp2));
            case "^" -> new DSLLogicOperatorXor(location, resolveLogicOperands(exp1, exp2));
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public DSLCondition resolve(ASTConditionTrap condition) {
		if (condition.isContains()) {
			return new DSLTrapCondition(condition.getLocation(), condition.getName(), condition.getExp().resolve(this));
		} else {
			return new DSLTrapConditionNeg(condition.getName(), condition.getExp().resolve(this), condition.getLocation());
		}
	}

	@Override
	public DSLCondition resolve(ASTRuleLogicOp operator) {
		final ASTRuleExpression exp1 = operator.getExp1();
		final ASTRuleExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
		return switch (operator.getOp()) {
            case "&" -> new DSLLogicOperatorAnd(location, resolveLogicOperands(exp1, exp2));
            case "|" -> new DSLLogicOperatorOr(location, resolveLogicOperands(exp1, exp2));
            case "^" -> new DSLLogicOperatorXor(location, resolveLogicOperands(exp1, exp2));
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public DSLCondition resolve(ASTRuleCompareOp operator) {
		final ASTExpression exp1 = operator.getExp1();
		final ASTExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
            return switch (operator.getOp()) {
                case ">" -> new DSLCompareOperatorGreater(location, resolveOperands(exp1, exp2));
                case "<" -> new DSLCompareOperatorLesser(location, resolveOperands(exp1, exp2));
                case ">=" -> new DSLCompareOperatorGreaterOrEquals(location, resolveOperands(exp1, exp2));
                case "<=" -> new DSLCompareOperatorLesserOrEquals(location, resolveOperands(exp1, exp2));
                case "=" -> new DSLCompareOperatorEquals(location, resolveOperands(exp1, exp2));
                case "<>" -> new DSLCompareOperatorNotEquals(location, resolveOperands(exp1, exp2));
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
	}

	@Override
	public DSLColorExpression resolve(ASTColorPalette palette) {
		if (palette.getExp().isReal()) {
			return new DSLColorExpressionPalette(palette.getLocation(), palette.getName(), palette.getExp().resolve(this));
		} else {
			throw new ASTException("Expression type not valid: " + palette.getLocation().getText(), palette.getLocation());
		}
	}

	@Override
	public DSLColorExpression resolve(ASTColorComponent component) {
		final DSLExpression exp1 = component.getExp1().resolve(this);
		DSLExpression exp2 = null;
		DSLExpression exp3 = null;
		DSLExpression exp4 = null;
		if (component.getExp2() != null) {
			exp2 = component.getExp2().resolve(this);
		}
		if (component.getExp3() != null) {
			exp3 = component.getExp3().resolve(this);
		}
		if (component.getExp4() != null) {
			exp4 = component.getExp4().resolve(this);
		}
		return new DSLColorExpressionScalar(component.getLocation(), exp1, exp2, exp3, exp4);
	}

	@Override
	public DSLStatement resolve(ASTConditionalStatement statement) {
		final List<DSLStatement> thenStatements = new ArrayList<>();
		final List<DSLStatement> elseStatements = new ArrayList<>();
		final HashMap<String, VariableDeclaration> newThenScope = new HashMap<>(variables);
		final DefaultResolver thenCompiler = new DefaultResolver(context, newThenScope);
		for (ASTStatement innerStatement : statement.getThenStatementList().getStatements()) {
			thenStatements.add(innerStatement.resolve(thenCompiler));
		}
		if (statement.getElseStatementList() != null) {
			final HashMap<String, VariableDeclaration> newElseScope = new HashMap<>(variables);
			final DefaultResolver elseCompiler = new DefaultResolver(context, newElseScope);
			for (ASTStatement innerStatement : statement.getElseStatementList().getStatements()) {
				elseStatements.add(innerStatement.resolve(elseCompiler));
			}
		}
		return new DSLConditionalStatement(statement.getLocation(), statement.getConditionExp().resolve(this), thenStatements, elseStatements);
	}

	@Override
	public DSLStatement resolve(ASTAssignStatement statement) {
        final VariableDeclaration var = variables.get(statement.getName());
        if (var != null && var.real() && !statement.getExp().isReal()) {
            throw new ASTException("Can't assign expression", statement.getLocation());
        }
		return new DSLAssignStatement(statement.getLocation(), statement.getName(), statement.getExp().resolve(this), context.newNumberIndex());
	}

	@Override
	public DSLStatement resolve(ASTStopStatement statement) {
		return new DSLStatementStop(statement.getLocation());
	}

	@Override
	public DSLCondition resolve(ASTConditionJulia condition) {
		return new DSLConditionJulia(condition.getLocation());
	}

	@Override
	public DSLCondition resolve(ASTConditionParen condition) {
		return condition.getExp().resolve(this);
	}

	@Override
	public DSLCondition resolve(ASTConditionNeg condition) {
		return new DSLConditionNeg(condition.getLocation(), condition.getExp().resolve(this));
	}

	@Override
	public DSLTrap resolve(ASTOrbitTrap orbitTrap) {
		final DSLTrap trap = new DSLTrap(orbitTrap.getLocation());
		trap.setName(orbitTrap.getName());
		trap.setCenter(new ComplexNumber(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
		final List<DSLTrapOp> operators = new ArrayList<>();
		for (ASTOrbitTrapOp astTrapOp : orbitTrap.getOperators()) {
			operators.add(astTrapOp.resolve(this));
		}
		trap.setOperators(operators);
		return trap;
	}

	@Override
	public DSLTrapOp resolve(ASTOrbitTrapOp orbitTrapOp) {
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
		final DSLToken location = orbitTrapOp.getLocation();
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
	public DSLPalette resolve(ASTPalette palette) {
		final List<DSLPaletteElement> elements = new ArrayList<>();
		for (ASTPaletteElement astElement : palette.getElements()) {
			elements.add(astElement.resolve(this));
		}
        return new DSLPalette(palette.getLocation(), palette.getName(), elements);
	}

	@Override
	public DSLPaletteElement resolve(ASTPaletteElement paletteElement) {
		return new DSLPaletteElement(paletteElement.getLocation(), paletteElement.getBeginColor().getComponents(), paletteElement.getEndColor().getComponents(), paletteElement.getSteps(), paletteElement.getExp() != null ? paletteElement.getExp().resolve(this) : null);
	}

	private DSLExpression[] resolveArguments(ASTExpression[] arguments) {
		final DSLExpression[] args = new DSLExpression[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			args[i] = arguments[i].resolve(this);
		}
		return args;
	}

	private DSLExpression[] resolveOperands(ASTExpression exp1, ASTExpression exp2) {
		final DSLExpression[] operands = new DSLExpression[2];
		operands[0] = exp1.resolve(this);
		operands[1] = exp2.resolve(this);
		return operands;
	}

	private DSLCondition[] resolveLogicOperands(ASTConditionExpression exp1, ASTConditionExpression exp2) {
		final DSLCondition[] operands = new DSLCondition[2];
		operands[0] = exp1.resolve(this);
		operands[1] = exp2.resolve(this);
		return operands;
	}

	private DSLCondition[] resolveLogicOperands(ASTRuleExpression exp1, ASTRuleExpression exp2) {
		final DSLCondition[] operands = new DSLCondition[2];
		operands[0] = exp1.resolve(this);
		operands[1] = exp2.resolve(this);
		return operands;
	}
}
