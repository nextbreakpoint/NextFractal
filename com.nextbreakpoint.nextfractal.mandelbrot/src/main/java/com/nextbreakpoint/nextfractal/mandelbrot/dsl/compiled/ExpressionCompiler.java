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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
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
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionCompiler implements ASTExpressionCompiler {
	private final Map<String, Variable> variables;
	private final ExpressionCompilerContext context;
	
	public ExpressionCompiler(ExpressionCompilerContext context, Map<String, Variable> variables) {
		this.context = context;
		this.variables = variables;
	}

	@Override
	public CompiledExpression compile(ASTNumber number) {
		return new CompiledNumber(context, number.r(), number.i(), number.getLocation());
	}

	@Override
	public CompiledExpression compile(ASTFunction function) {
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
                return new CompiledFuncTime(context, location);
            }
            case "mod" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncMod(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncModZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "mod2" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncMod2(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncModZ2(context, compileArguments(function.getArguments()), location);
                }
            }
            case "pha" -> {
                return new CompiledFuncPhaZ(context, compileArguments(function.getArguments()), location);
            }
            case "re" -> {
                return new CompiledFuncReZ(context, compileArguments(function.getArguments()), location);
            }
            case "im" -> {
                return new CompiledFuncImZ(context, compileArguments(function.getArguments()), location);
            }
            case "sin" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncSin(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncSinZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "cos" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncCos(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncCosZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "tan" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncTan(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncTanZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "asin" -> {
                return new CompiledFuncAsin(context, compileArguments(function.getArguments()), location);
            }
            case "acos" -> {
                return new CompiledFuncAcos(context, compileArguments(function.getArguments()), location);
            }
            case "atan" -> {
                return new CompiledFuncAtan(context, compileArguments(function.getArguments()), location);
            }
            case "abs" -> {
                return new CompiledFuncAbs(context, compileArguments(function.getArguments()), location);
            }
            case "ceil" -> {
                return new CompiledFuncCeil(context, compileArguments(function.getArguments()), location);
            }
            case "floor" -> {
                return new CompiledFuncFloor(context, compileArguments(function.getArguments()), location);
            }
            case "log" -> {
                return new CompiledFuncLog(context, compileArguments(function.getArguments()), location);
            }
            case "square" -> {
                return new CompiledFuncSquare(context, compileArguments(function.getArguments()), location);
            }
            case "saw" -> {
                return new CompiledFuncSaw(context, compileArguments(function.getArguments()), location);
            }
            case "ramp" -> {
                return new CompiledFuncRamp(context, compileArguments(function.getArguments()), location);
            }
            case "min" -> {
                return new CompiledFuncMin(context, compileArguments(function.getArguments()), location);
            }
            case "max" -> {
                return new CompiledFuncMax(context, compileArguments(function.getArguments()), location);
            }
            case "atan2" -> {
                return new CompiledFuncAtan2(context, compileArguments(function.getArguments()), location);
            }
            case "hypot" -> {
                return new CompiledFuncHypot(context, compileArguments(function.getArguments()), location);
            }
            case "pulse" -> {
                return new CompiledFuncPulse(context, compileArguments(function.getArguments()), location);
            }
            case "pow" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncPow(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncPowZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "sqrt" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncSqrt(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncSqrtZ(context, compileArguments(function.getArguments()), location);
                }
            }
            case "exp" -> {
                if (function.getArguments()[0].isReal()) {
                    return new CompiledFuncExp(context, compileArguments(function.getArguments()), location);
                } else {
                    return new CompiledFuncExpZ(context, compileArguments(function.getArguments()), location);
                }
            }
            default -> {
            }
        }
		throw new ASTException("Unsupported function: " + location.getText(), location);
	}

	@Override
	public CompiledExpression compile(ASTOperator operator) {
		final ASTExpression exp1 = operator.getExp1();
		final ASTExpression exp2 = operator.getExp2();
		final Token location = operator.getLocation();
		if (exp2 == null) {
            return switch (operator.getOp()) {
                case "-" -> new CompiledOperatorNeg(context, exp1.compile(this), location);
                case "+" -> new CompiledOperatorPos(context, exp1.compile(this), location);
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			if (exp1.isReal() && exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new CompiledOperatorAdd(context, exp1.compile(this), exp2.compile(this), location);
                    case "-" -> new CompiledOperatorSub(context, exp1.compile(this), exp2.compile(this), location);
                    case "*" -> new CompiledOperatorMul(context, exp1.compile(this), exp2.compile(this), location);
                    case "/" -> new CompiledOperatorDiv(context, exp1.compile(this), exp2.compile(this), location);
                    case "^" -> new CompiledOperatorPow(context, exp1.compile(this), exp2.compile(this), location);
                    case "<>" -> new CompiledOperatorNumber(context, exp1.compile(this), exp2.compile(this), location);
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp1.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "-" -> new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "*" -> new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "/" -> new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "^" -> new CompiledOperatorPowZ(context, exp1.compile(this), exp2.compile(this), location);
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else if (exp2.isReal()) {
                return switch (operator.getOp()) {
                    case "+" -> new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "-" -> new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "*" -> new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "/" -> new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "^" -> new CompiledOperatorPowZ(context, exp1.compile(this), exp2.compile(this), location);
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
			} else {
                return switch (operator.getOp()) {
                    case "+" -> new CompiledOperatorAddZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "-" -> new CompiledOperatorSubZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "*" -> new CompiledOperatorMulZ(context, exp1.compile(this), exp2.compile(this), location);
                    case "/" -> new CompiledOperatorDivZ(context, exp1.compile(this), exp2.compile(this), location);
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                };
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
		final ASTExpression exp1 = compareOp.getExp1();
		final ASTExpression exp2 = compareOp.getExp2();
		final Token location = compareOp.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			exp1.compile(this);
            return switch (compareOp.getOp()) {
                case ">" -> new CompiledLogicOperatorGreater(context, compileOperands(exp1, exp2), location);
                case "<" -> new CompiledLogicOperatorLesser(context, compileOperands(exp1, exp2), location);
                case ">=" -> new CompiledLogicOperatorGreaterOrEquals(context, compileOperands(exp1, exp2), location);
                case "<=" -> new CompiledLogicOperatorLesserOrEquals(context, compileOperands(exp1, exp2), location);
                case "=" -> new CompiledLogicOperatorEquals(context, compileOperands(exp1, exp2), location);
                case "<>" -> new CompiledLogicOperatorNotEquals(context, compileOperands(exp1, exp2), location);
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
	}

	@Override
	public CompiledCondition compile(ASTConditionLogicOp logicOp) {
		final ASTConditionExpression exp1 = logicOp.getExp1();
		final ASTConditionExpression exp2 = logicOp.getExp2();
		final Token location = logicOp.getLocation();
		return switch (logicOp.getOp()) {
            case "&" -> new CompiledLogicOperatorAnd(context, compileLogicOperands(exp1, exp2), location);
            case "|" -> new CompiledLogicOperatorOr(context, compileLogicOperands(exp1, exp2), location);
            case "^" -> new CompiledLogicOperatorXor(context, compileLogicOperands(exp1, exp2), location);
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
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
		final ASTRuleExpression exp1 = logicOp.getExp1();
		final ASTRuleExpression exp2 = logicOp.getExp2();
		final Token location = logicOp.getLocation();
		return switch (logicOp.getOp()) {
            case "&" -> new CompiledLogicOperatorAnd(context, compileLogicOperands(exp1, exp2), location);
            case "|" -> new CompiledLogicOperatorOr(context, compileLogicOperands(exp1, exp2), location);
            case "^" -> new CompiledLogicOperatorXor(context, compileLogicOperands(exp1, exp2), location);
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public CompiledCondition compile(ASTRuleCompareOp compareOp) {
		final ASTExpression exp1 = compareOp.getExp1();
		final ASTExpression exp2 = compareOp.getExp2();
		final Token location = compareOp.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
            return switch (compareOp.getOp()) {
                case ">" -> new CompiledLogicOperatorGreater(context, compileOperands(exp1, exp2), location);
                case "<" -> new CompiledLogicOperatorLesser(context, compileOperands(exp1, exp2), location);
                case ">=" -> new CompiledLogicOperatorGreaterOrEquals(context, compileOperands(exp1, exp2), location);
                case "<=" -> new CompiledLogicOperatorLesserOrEquals(context, compileOperands(exp1, exp2), location);
                case "=" -> new CompiledLogicOperatorEquals(context, compileOperands(exp1, exp2), location);
                case "<>" -> new CompiledLogicOperatorNotEquals(context, compileOperands(exp1, exp2), location);
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            };
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
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
		final CompiledExpression exp1 = component.getExp1().compile(this);
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
		final List<CompiledStatement> thenStatements = new ArrayList<>();
		final List<CompiledStatement> elseStatements = new ArrayList<>();
		final HashMap<String, Variable> newThenScope = new HashMap<String, Variable>(variables);
		final ExpressionCompiler thenCompiler = new ExpressionCompiler(context, newThenScope);
		for (ASTStatement innerStatement : statement.getThenStatementList().getStatements()) {
			thenStatements.add(innerStatement.compile(thenCompiler));
		}
		if (statement.getElseStatementList() != null) {
			final HashMap<String, Variable> newElseScope = new HashMap<String, Variable>(variables);
			final ExpressionCompiler elseCompiler = new ExpressionCompiler(context, newElseScope);
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
		final CompiledTrap trap = new CompiledTrap(orbitTrap.getLocation());
		trap.setName(orbitTrap.getName());
		trap.setCenter(new Number(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
		final List<CompiledTrapOp> operators = new ArrayList<>();
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
		final Token location = orbitTrapOp.getLocation();
		return switch (orbitTrapOp.getOp()) {
            case "MOVETO" -> new CompiledTrapOpMoveTo(c1, location);
            case "MOVEREL", "MOVETOREL" -> new CompiledTrapOpMoveRel(c1, location);
            case "LINETO" -> new CompiledTrapOpLineTo(c1, location);
            case "LINEREL", "LINETOREL" -> new CompiledTrapOpLineRel(c1, location);
            case "ARCTO" -> new CompiledTrapOpArcTo(c1, c2, location);
            case "ARCREL", "ARCTOREL" -> new CompiledTrapOpArcRel(c1, c2, location);
            case "QUADTO" -> new CompiledTrapOpQuadTo(c1, c2, location);
            case "QUADREL", "QUADTOREL" -> new CompiledTrapOpQuadRel(c1, c2, location);
            case "CURVETO" -> new CompiledTrapOpCurveTo(c1, c2, c3, location);
            case "CURVEREL", "CURVETOREL" -> new CompiledTrapOpCurveRel(c1, c2, c3, location);
            case "CLOSE" -> new CompiledTrapOpClose(location);
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        };
	}

	@Override
	public CompiledPalette compile(ASTPalette astPalette) {
		final CompiledPalette palette = new CompiledPalette(astPalette.getLocation());
		palette.setName(astPalette.getName());
		final List<CompiledPaletteElement> elements = new ArrayList<>();
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
		final CompiledExpression[] args = new CompiledExpression[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			args[i] = arguments[i].compile(this);
		}
		return args;
	}

	private CompiledExpression[] compileOperands(ASTExpression exp1, ASTExpression exp2) {
		final CompiledExpression[] operands = new CompiledExpression[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private CompiledCondition[] compileLogicOperands(ASTConditionExpression exp1, ASTConditionExpression exp2) {
		final CompiledCondition[] operands = new CompiledCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	private CompiledCondition[] compileLogicOperands(ASTRuleExpression exp1, ASTRuleExpression exp2) {
		final CompiledCondition[] operands = new CompiledCondition[2];
		operands[0] = exp1.compile(this);
		operands[1] = exp2.compile(this);
		return operands;
	}

	@Override
	public CompiledExpression compile(ASTConditionalExpression astConditionalExpression) {
		return new CompiledConditionalExpression(context, astConditionalExpression.getConditionExp().compile(this), astConditionalExpression.getThenExp().compile(this), astConditionalExpression.getElseExp().compile(this), astConditionalExpression.getLocation());
	}
}
