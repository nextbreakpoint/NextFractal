/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledColorComponentExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledConditionalStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncAbs;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncAcos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncAsin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncAtan;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncAtan2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncCeil;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncCos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncCosZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncExp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncExpZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncFloor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncHypot;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncImZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncLog;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncMax;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncMin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncMod;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncMod2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncModZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncModZ2;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncPhaZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncPow;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncPowZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncPulse;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncRamp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncReZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSaw;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSinZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSqrt;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSqrtZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncSquare;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncTan;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncTanZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledFuncTime;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledInvertedCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledJuliaCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorAnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorGreather;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorGreatherOrEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorLesser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorLesserOrEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorNotEquals;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorOr;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledLogicOperatorXor;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorAdd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorAddZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorDiv;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorDivZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorMul;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorMulZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorNeg;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorPos;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorPow;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorPowZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorSub;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOperatorSubZ;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledPaletteExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledStopStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapInvertedCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpArcRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpArcTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpClose;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpCurveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpCurveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpLineRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpLineTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpMoveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpMoveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpQuadRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledTrapOpQuadTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColorComponent;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTColorPalette;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTExpressionCompiler;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionCompiler implements ASTExpressionCompiler {
	private final Map<String, CompilerVariable> variables;
	private final ExpressionContext context;
	
	public ExpressionCompiler(ExpressionContext context, Map<String, CompilerVariable> variables) {
		this.context = context;
		this.variables = variables;
	}

	@Override
	public CompiledExpression compile(ASTNumber number) {
		return new CompiledNumber(context, number.r(), number.i(), number.getLocation());
	}

	@Override
	public CompiledExpression compile(ASTFunction function) {
		switch (function.getName()) {
			case "time":
				if (function.getArguments().length != 0) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}
				break;
			case "mod":
			case "mod2":
			case "pha":
			case "re":
			case "im":
				if (function.getArguments().length != 1) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				break;
				
			case "sin":
			case "cos":
			case "tan":
			case "asin":
			case "acos":
			case "atan":
				if (function.getArguments().length != 1) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}	
				break;

			case "abs":
			case "ceil":
			case "floor":
			case "log":
			case "square":
			case "saw":
			case "ramp":
				if (function.getArguments().length != 1) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				if (!function.getArguments()[0].isReal()) {
					throw new ASTException("Invalid type of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				break;
				
			case "min":
			case "max":
			case "atan2":
			case "hypot":
			case "pulse":
				if (function.getArguments().length != 2) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				if (!function.getArguments()[0].isReal()) {
					throw new ASTException("Invalid type of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				if (!function.getArguments()[1].isReal()) {
					throw new ASTException("Invalid type of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				break;
				
			case "pow":
				if (function.getArguments().length != 2) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				if (!function.getArguments()[1].isReal()) {
					throw new ASTException("Invalid type of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				break;

			case "sqrt":
			case "exp":
				if (function.getArguments().length != 1) {
					throw new ASTException("Invalid number of arguments: " + function.getLocation().getText(), function.getLocation());
				}				
				break;
				
			default:
				throw new ASTException("Unsupported function: " + function.getLocation().getText(), function.getLocation());
		}
		switch (function.getName()) {
			case "time":
				return new CompiledFuncTime(context, function.getLocation());
			case "mod":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncMod(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncModZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "mod2":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncMod2(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncModZ2(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "pha":
				return new CompiledFuncPhaZ(context, compileArguments(function.getArguments()), function.getLocation());
			case "re":
				return new CompiledFuncReZ(context, compileArguments(function.getArguments()), function.getLocation());
			case "im":
				return new CompiledFuncImZ(context, compileArguments(function.getArguments()), function.getLocation());
				
			case "sin":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncSin(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncSinZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "cos":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncCos(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncCosZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "tan":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncTan(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncTanZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "asin":
				return new CompiledFuncAsin(context, compileArguments(function.getArguments()), function.getLocation());
			case "acos":
				return new CompiledFuncAcos(context, compileArguments(function.getArguments()), function.getLocation());
			case "atan":
				return new CompiledFuncAtan(context, compileArguments(function.getArguments()), function.getLocation());
	
			case "abs":
				return new CompiledFuncAbs(context, compileArguments(function.getArguments()), function.getLocation());
			case "ceil":
				return new CompiledFuncCeil(context, compileArguments(function.getArguments()), function.getLocation());
			case "floor":
				return new CompiledFuncFloor(context, compileArguments(function.getArguments()), function.getLocation());
			case "log":
				return new CompiledFuncLog(context, compileArguments(function.getArguments()), function.getLocation());
			case "square":
				return new CompiledFuncSquare(context, compileArguments(function.getArguments()), function.getLocation());
			case "saw":
				return new CompiledFuncSaw(context, compileArguments(function.getArguments()), function.getLocation());
			case "ramp":
				return new CompiledFuncRamp(context, compileArguments(function.getArguments()), function.getLocation());

			case "min":
				return new CompiledFuncMin(context, compileArguments(function.getArguments()), function.getLocation());
			case "max":
				return new CompiledFuncMax(context, compileArguments(function.getArguments()), function.getLocation());
			case "atan2":
				return new CompiledFuncAtan2(context, compileArguments(function.getArguments()), function.getLocation());
			case "hypot":
				return new CompiledFuncHypot(context, compileArguments(function.getArguments()), function.getLocation());
			case "pulse":
				return new CompiledFuncPulse(context, compileArguments(function.getArguments()), function.getLocation());

			case "pow":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncPow(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncPowZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
	
			case "sqrt":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncSqrt(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncSqrtZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
			case "exp":
				if (function.getArguments()[0].isReal()) {
					return new CompiledFuncExp(context, compileArguments(function.getArguments()), function.getLocation());
				} else {
					return new CompiledFuncExpZ(context, compileArguments(function.getArguments()), function.getLocation());
				}
				
			default:
		}
		throw new ASTException("Unsupported function: " + function.getLocation().getText(), function.getLocation());
	}

	@Override
	public CompiledExpression compile(ASTOperator operator) {
		ASTExpression exp1 = operator.getExp1();
		ASTExpression exp2 = operator.getExp2();
		if (exp2 == null) {
			switch (operator.getOp()) {
				case "-":
					return new CompiledOperatorNeg(context, exp1.compile(this), operator.getLocation());
				
				case "+":
					return new CompiledOperatorPos(context, exp1.compile(this), operator.getLocation());
				
				default:
					throw new ASTException("Unsupported operator: " + operator.getLocation().getText(), operator.getLocation());
			}
		} else {
			if (exp1.isReal() && exp2.isReal()) {
				switch (operator.getOp()) {
					case "+":
						return new CompiledOperatorAdd(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					case "-":
						return new CompiledOperatorSub(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "*":
						return new CompiledOperatorMul(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "/":
						return new CompiledOperatorDiv(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "^":
						return new CompiledOperatorPow(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					case "<>":
						return new CompiledOperatorNumber(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					default:
						throw new ASTException("Unsupported operator: " + operator.getLocation().getText(), operator.getLocation());
				}
			} else if (exp1.isReal()) {
				switch (operator.getOp()) {
					case "+":
						return new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					case "-":
						return new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "*":
						return new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "/":
						return new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "^":
						return new CompiledOperatorPowZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					default:
						throw new ASTException("Unsupported operator: " + operator.getLocation().getText(), operator.getLocation());
				}
			} else if (exp2.isReal()) {
				switch (operator.getOp()) {
					case "+":
						return new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					case "-":
						return new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "*":
						return new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "/":
						return new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "^":
						return new CompiledOperatorPowZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					default:
						throw new ASTException("Unsupported operator: " + operator.getLocation().getText(), operator.getLocation());
				}
			} else {
				switch (operator.getOp()) {
					case "+":
						return new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
					
					case "-":
						return new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "*":
						return new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					case "/":
						return new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), operator.getLocation());
						
					default:
						throw new ASTException("Unsupported operator: " + operator.getLocation().getText(), operator.getLocation());
				}
			}
		}
	}

	@Override
	public CompiledExpression compile(ASTParen paren) {
		return paren.getExp().compile(this);
	}

	@Override
	public CompiledExpression compile(ASTVariable variable) {
		return new CompiledVariable(context, variable.getName(), variable.isReal(), variable.getLocation());
	}

	@Override
	public CompiledCondition compile(ASTConditionCompareOp compareOp) {
		ASTExpression exp1 = compareOp.getExp1();
		ASTExpression exp2 = compareOp.getExp2();
		if (exp1.isReal() && exp2.isReal()) {
			exp1.compile(this);
			switch (compareOp.getOp()) {
				case ">":
					return new CompiledLogicOperatorGreather(context, compileOperands(exp1, exp2), compareOp.getLocation());
				
				case "<":
					return new CompiledLogicOperatorLesser(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case ">=":
					return new CompiledLogicOperatorGreatherOrEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "<=":
					return new CompiledLogicOperatorLesserOrEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "=":
					return new CompiledLogicOperatorEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "<>":
					return new CompiledLogicOperatorNotEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
				
				default:
					throw new ASTException("Unsupported operator: " + compareOp.getLocation().getText(), compareOp.getLocation());
			}
		} else {
			throw new ASTException("Real expressions required: " + compareOp.getLocation().getText(), compareOp.getLocation());
		}
	}

	@Override
	public CompiledCondition compile(ASTConditionLogicOp logicOp) {
		ASTConditionExpression exp1 = logicOp.getExp1();
		ASTConditionExpression exp2 = logicOp.getExp2();
		switch (logicOp.getOp()) {
			case "&":
				return new CompiledLogicOperatorAnd(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
			
			case "|":
				return new CompiledLogicOperatorOr(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
				
			case "^":
				return new CompiledLogicOperatorXor(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
				
			default:
				throw new ASTException("Unsupported operator: " + logicOp.getLocation().getText(), logicOp.getLocation());
		}
	}

	@Override
	public CompiledCondition compile(ASTConditionTrap trap) {
		if (trap.isContains()) {
			return new CompiledTrapCondition(trap.getName(), trap.getExp().compile(this), trap.getLocation());
		} else {
			return new CompiledTrapInvertedCondition(trap.getName(), trap.getExp().compile(this), trap.getLocation());
		}
	}

	@Override
	public CompiledCondition compile(ASTRuleLogicOp logicOp) {
		ASTRuleExpression exp1 = logicOp.getExp1();
		ASTRuleExpression exp2 = logicOp.getExp2();
		switch (logicOp.getOp()) {
			case "&":
				return new CompiledLogicOperatorAnd(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
			
			case "|":
				return new CompiledLogicOperatorOr(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
				
			case "^":
				return new CompiledLogicOperatorXor(context, compileLogicOperands(exp1, exp2), logicOp.getLocation());
				
			default:
				throw new ASTException("Unsupported operator: " + logicOp.getLocation().getText(), logicOp.getLocation());
		}
	}

	@Override
	public CompiledCondition compile(ASTRuleCompareOp compareOp) {
		ASTExpression exp1 = compareOp.getExp1();
		ASTExpression exp2 = compareOp.getExp2();
		if (exp1.isReal() && exp2.isReal()) {
			switch (compareOp.getOp()) {
				case ">":
					return new CompiledLogicOperatorGreather(context, compileOperands(exp1, exp2), compareOp.getLocation());
				
				case "<":
					return new CompiledLogicOperatorLesser(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case ">=":
					return new CompiledLogicOperatorGreatherOrEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "<=":
					return new CompiledLogicOperatorLesserOrEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "=":
					return new CompiledLogicOperatorEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
					
				case "<>":
					return new CompiledLogicOperatorNotEquals(context, compileOperands(exp1, exp2), compareOp.getLocation());
				
				default:
					throw new ASTException("Unsupported operator: " + compareOp.getLocation().getText(), compareOp.getLocation());
			}
		} else {
			throw new ASTException("Real expressions required: " + compareOp.getLocation().getText(), compareOp.getLocation());
		}
	}

	@Override
	public CompiledColorExpression compile(ASTColorPalette palette) {
		if (palette.getExp().isReal()) {
			return new CompiledPaletteExpression(palette.getName(), palette.getExp().compile(this), palette.getLocation());
		} else {
			throw new ASTException("Expression type not valid: " + palette.getLocation().getText(), palette.getLocation());
		}
	}

	@Override
	public CompiledColorExpression compile(ASTColorComponent component) {
		CompiledExpression exp1 = component.getExp1().compile(this);
		CompiledExpression exp2 = null;
		CompiledExpression exp3 = null;
		CompiledExpression exp4 = null;
		if (component.getExp2() != null) {
			exp2 = component.getExp2().compile(this);
		}
		if (component.getExp3() != null) {
			exp3 = component.getExp3().compile(this);
		}
		if (component.getExp4() != null) {
			exp4 = component.getExp4().compile(this);
		}
		return new CompiledColorComponentExpression(exp1, exp2, exp3, exp4, component.getLocation());
	}

	@Override
	public CompiledStatement compile(ASTConditionalStatement statement) {
		List<CompiledStatement> thenStatements = new ArrayList<>();
		List<CompiledStatement> elseStatements = new ArrayList<>();
		HashMap<String, CompilerVariable> newThenScope = new HashMap<String, CompilerVariable>(variables);
		ExpressionCompiler thenCompiler = new ExpressionCompiler(context, newThenScope);
		for (ASTStatement innerStatement : statement.getThenStatementList().getStatements()) {
			thenStatements.add(innerStatement.compile(thenCompiler));
		}
		if (statement.getElseStatementList() != null) {
			HashMap<String, CompilerVariable> newElseScope = new HashMap<String, CompilerVariable>(variables);
			ExpressionCompiler elseCompiler = new ExpressionCompiler(context, newElseScope);
			for (ASTStatement innerStatement : statement.getElseStatementList().getStatements()) {
				elseStatements.add(innerStatement.compile(elseCompiler));
			}
		}
		return new CompiledConditionalStatement(statement.getConditionExp().compile(this), thenStatements, elseStatements, statement.getLocation());
	}

	@Override
	public CompiledStatement compile(ASTAssignStatement statement) {
		return new CompiledAssignStatement(statement.getName(), statement.getExp().compile(this), variables, statement.getLocation());
	}

	@Override
	public CompiledStatement compile(ASTStopStatement statement) {
		return new CompiledStopStatement(statement.getLocation());
	}

	@Override
	public CompiledCondition compile(ASTConditionJulia condition) {
		return new CompiledJuliaCondition(condition.getLocation());
	}

	@Override
	public CompiledCondition compile(ASTConditionParen condition) {
		return condition.getExp().compile(this);
	}

	@Override
	public CompiledCondition compile(ASTConditionNeg condition) {
		return new CompiledInvertedCondition(condition.getExp().compile(this), condition.getLocation());
	}

	@Override
	public CompiledTrap compile(ASTOrbitTrap orbitTrap) {
		CompiledTrap trap = new CompiledTrap(orbitTrap.getLocation());
		trap.setName(orbitTrap.getName());
		trap.setCenter(new Number(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
		List<CompiledTrapOp> operators = new ArrayList<>();
		for (ASTOrbitTrapOp astTrapOp : orbitTrap.getOperators()) {
			operators.add(astTrapOp.compile(this));
		}
		trap.setOperators(operators);
		return trap;
	}

	@Override
	public CompiledTrapOp compile(ASTOrbitTrapOp orbitTrapOp) {
		Number c1 = null;
		Number c2 = null;
		Number c3 = null;
		if (orbitTrapOp.getC1() != null) {
			c1 = new Number(orbitTrapOp.getC1().r(), orbitTrapOp.getC1().i());
		}
		if (orbitTrapOp.getC2() != null) {
			c2 = new Number(orbitTrapOp.getC2().r(), orbitTrapOp.getC2().i());
		}
		if (orbitTrapOp.getC3() != null) {
			c3 = new Number(orbitTrapOp.getC3().r(), orbitTrapOp.getC3().i());
		}
		switch (orbitTrapOp.getOp()) {
			case "MOVETO":
				return new CompiledTrapOpMoveTo(c1, orbitTrapOp.getLocation());

			case "MOVEREL":
			case "MOVETOREL":
				return new CompiledTrapOpMoveRel(c1, orbitTrapOp.getLocation());
	
			case "LINETO":
				return new CompiledTrapOpLineTo(c1, orbitTrapOp.getLocation());

			case "LINEREL":
			case "LINETOREL":
				return new CompiledTrapOpLineRel(c1, orbitTrapOp.getLocation());
	
			case "ARCTO":
				return new CompiledTrapOpArcTo(c1, c2, orbitTrapOp.getLocation());

			case "ARCREL":
			case "ARCTOREL":
				return new CompiledTrapOpArcRel(c1, c2, orbitTrapOp.getLocation());
	
			case "QUADTO":
				return new CompiledTrapOpQuadTo(c1, c2, orbitTrapOp.getLocation());

			case "QUADREL":
			case "QUADTOREL":
				return new CompiledTrapOpQuadRel(c1, c2, orbitTrapOp.getLocation());
	
			case "CURVETO":
				return new CompiledTrapOpCurveTo(c1, c2, c3, orbitTrapOp.getLocation());

			case "CURVEREL":
			case "CURVETOREL":
				return new CompiledTrapOpCurveRel(c1, c2, c3, orbitTrapOp.getLocation());
	
			case "CLOSE":
				return new CompiledTrapOpClose(orbitTrapOp.getLocation());
	
			default:
				throw new ASTException("Unsupported operator: " + orbitTrapOp.getLocation().getText(), orbitTrapOp.getLocation());
		}		
	}

	@Override
	public CompiledPalette compile(ASTPalette astPalette) {
		CompiledPalette palette = new CompiledPalette(astPalette.getLocation());
		palette.setName(astPalette.getName());
		List<CompiledPaletteElement> elements = new ArrayList<>();
		for (ASTPaletteElement astElement : astPalette.getElements()) {
			elements.add(astElement.compile(this));
		}
		palette.setElements(elements);
		return palette;
	}

	@Override
	public CompiledPaletteElement compile(ASTPaletteElement astElement) {
		return new CompiledPaletteElement(astElement.getBeginColor().getComponents(), astElement.getEndColor().getComponents(), astElement.getSteps(), astElement.getExp() != null ? astElement.getExp().compile(this) : null, astElement.getLocation());
	}

	private CompiledExpression[] compileArguments(ASTExpression[] arguments) {
		CompiledExpression[] args = new CompiledExpression[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			args[i] = arguments[i].compile(this);
		}
		return args;
	}

	private CompiledExpression[] compileOperands(ASTExpression exp1, ASTExpression exp2) {
		CompiledExpression[] operands = new CompiledExpression[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private CompiledCondition[] compileLogicOperands(ASTConditionExpression exp1, ASTConditionExpression exp2) {
		CompiledCondition[] operands = new CompiledCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private CompiledCondition[] compileLogicOperands(ASTRuleExpression exp1, ASTRuleExpression exp2) {
		CompiledCondition[] operands = new CompiledCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	@Override
	public CompiledExpression compile(ASTConditionalExpression astConditionalExpression) {
		return new CompiledConditionalExpression(context, astConditionalExpression.getConditionExp().compile(this), astConditionalExpression.getThenExp().compile(this), astConditionalExpression.getElseExp().compile(this), astConditionalExpression.getLocation());
	}
}
