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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler;

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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpArcRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpArcTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpClose;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpCurveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpCurveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpLineRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpLineTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpMoveRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpMoveTo;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpQuadRel;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledTrapOpQuadTo;
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
	private final StringBuilder builder;
	private final ClassType classType;

	public ExpressionCompiler(ExpressionCompilerContext context, Map<String, Variable> variables, StringBuilder builder, ClassType classType) {
		this.variables = variables;
		this.context = context;
		this.builder = builder;
		this.classType = classType;
	}

	@Override
	public CompiledExpression compile(ASTNumber number) {
		if (number.isReal()) {
			builder.append(number.r());
		} else {
			builder.append("getNumber(");
			builder.append(context.newNumberIndex());
			builder.append(").set(");
			builder.append(number.r());
			builder.append(",");
			builder.append(number.i());
			builder.append(")");
		}
		return null;
	}

	@Override
	public CompiledExpression compile(ASTFunction function) {
		final Token location = function.getLocation();
		if (function.getName().equals("time")) {
			if (classType.equals(ClassType.ORBIT)) {
				context.setOrbitUseTime(true);
			}
			if (classType.equals(ClassType.COLOR)) {
				context.setColorUseTime(true);
			}
			if (function.getArguments().length != 0) {
				throw new ASTException("Invalid number of arguments: " + location.getText(), location);
			}
			builder.append("time()");
		} else {
			builder.append("func");
			builder.append(function.getName().toUpperCase().charAt(0));
			builder.append(function.getName().substring(1));
			builder.append("(");
            switch (function.getName()) {
                case "mod", "mod2", "pha", "re", "im" -> {
                    if (function.getArguments().length != 1) {
                        throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                    }
                }
                case "sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "exp" -> {
                    if (function.getArguments().length != 1) {
                        throw new ASTException("Invalid number of arguments: " + location.getText(), location);
                    }
                    if (!function.getArguments()[0].isReal()) {
                        builder.append("getNumber(");
                        builder.append(context.newNumberIndex());
                        builder.append("),");
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
                    if (!function.getArguments()[0].isReal()) {
                        builder.append("getNumber(");
                        builder.append(context.newNumberIndex());
                        builder.append("),");
                    }
                }
                default ->
                        throw new ASTException("Unsupported function: " + location.getText(), location);
            }
			ASTExpression[] arguments = function.getArguments();
			for (int i = 0; i < arguments.length; i++) {
				arguments[i].compile(this);
				if (i < arguments.length - 1) {
					builder.append(",");
				}
			}
			builder.append(")");
		}
		return null;
	}

	@Override
	public CompiledExpression compile(ASTOperator operator) {
		ASTExpression exp1 = operator.getExp1();
		ASTExpression exp2 = operator.getExp2();
		final Token location = operator.getLocation();
		if (exp2 == null) {
            switch (operator.getOp()) {
                case "-" -> {
                    if (exp1.isReal()) {
                        builder.append("-");
                        exp1.compile(this);
                    } else {
                        builder.append("opNeg");
                        builder.append("(");
                        builder.append("getNumber(");
                        builder.append(context.newNumberIndex());
                        builder.append("),");
                        exp1.compile(this);
                        builder.append(")");
                    }
                }
                case "+" -> {
                    if (exp1.isReal()) {
                        exp1.compile(this);
                    } else {
                        builder.append("opPos");
                        builder.append("(");
                        builder.append("getNumber(");
                        builder.append(context.newNumberIndex());
                        builder.append("),");
                        exp1.compile(this);
                        builder.append(")");
                    }
                }
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            }
		} else {
			if (exp1.isReal() && exp2.isReal()) {
				if (operator.getOp().equals("^")) {
					builder.append("opPow");
				}
				if (operator.getOp().equals("<>")) {
					builder.append("getNumber(");
					builder.append(context.newNumberIndex());
					builder.append(").set");
				}
				builder.append("(");
				exp1.compile(this);
                switch (operator.getOp()) {
                    case "+" -> builder.append("+");
                    case "-" -> builder.append("-");
                    case "*" -> builder.append("*");
                    case "/" -> builder.append("/");
                    case "^", "<>" -> builder.append(",");
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                }
				exp2.compile(this);
				builder.append(")");
			} else if (exp2.isReal()) {
                switch (operator.getOp()) {
                    case "+" -> builder.append("opAdd");
                    case "-" -> builder.append("opSub");
                    case "*" -> builder.append("opMul");
                    case "/" -> builder.append("opDiv");
                    case "^" -> builder.append("opPow");
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                }
				builder.append("(");
				builder.append("getNumber(");
				builder.append(context.newNumberIndex());
				builder.append("),");
				exp1.compile(this);
				builder.append(",");
				exp2.compile(this);
				builder.append(")");
			} else {
                switch (operator.getOp()) {
                    case "+" -> builder.append("opAdd");
                    case "-" -> builder.append("opSub");
                    case "*" -> builder.append("opMul");
                    case "/" -> builder.append("opDiv");
                    default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
                }
				builder.append("(");
				builder.append("getNumber(");
				builder.append(context.newNumberIndex());
				builder.append("),");
				exp1.compile(this);
				builder.append(",");
				exp2.compile(this);
				builder.append(")");
			}
		}
		return null;
	}

	@Override
	public CompiledExpression compile(ASTParen paren) {
		builder.append("(");
		paren.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public CompiledExpression compile(ASTVariable variable) {
		builder.append(variable.getName());
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionCompareOp compareOp) {
		ASTExpression exp1 = compareOp.getExp1();
		ASTExpression exp2 = compareOp.getExp2();
		final Token location = compareOp.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			builder.append("(");
			exp1.compile(this);
            switch (compareOp.getOp()) {
                case ">" -> builder.append(">");
                case "<" -> builder.append("<");
                case ">=" -> builder.append(">=");
                case "<=" -> builder.append("<=");
                case "=" -> builder.append("==");
                case "<>" -> builder.append("!=");
                default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
            }
			exp2.compile(this);
			builder.append(")");
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionLogicOp logicOp) {
		ASTConditionExpression exp1 = logicOp.getExp1();
		ASTConditionExpression exp2 = logicOp.getExp2();
		final Token location = logicOp.getLocation();
		builder.append("(");
		exp1.compile(this);
		switch (logicOp.getOp()) {
			case "&":
				builder.append("&&");
				break;
			
			case "|":
				builder.append("||");
				break;
				
			case "^":
				builder.append("^^");
				break;
				
			default:
				throw new ASTException("Unsupported operator: " + location.getText(), location);
		}
		exp2.compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionTrap trap) {
		if (!trap.isContains()) {
			builder.append("!");
		}
		builder.append("trap");
		builder.append(trap.getName().toUpperCase().charAt(0));
		builder.append(trap.getName().substring(1));
		builder.append(".contains(");
		trap.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTRuleLogicOp logicOp) {
		ASTRuleExpression exp1 = logicOp.getExp1();
		ASTRuleExpression exp2 = logicOp.getExp2();
		final Token location = logicOp.getLocation();
		builder.append("(");
		exp1.compile(this);
        switch (logicOp.getOp()) {
            case "&" -> builder.append("&&");
            case "|" -> builder.append("||");
            case "^" -> builder.append("^^");
            default -> throw new ASTException("Unsupported operator: " + location.getText(), location);
        }
		exp2.compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTRuleCompareOp compareOp) {
		ASTExpression exp1 = compareOp.getExp1();
		ASTExpression exp2 = compareOp.getExp2();
		final Token location = compareOp.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			builder.append("(");
			exp1.compile(this);
            switch (compareOp.getOp()) {
                case ">" -> builder.append(">");
                case "<" -> builder.append("<");
                case ">=" -> builder.append(">=");
                case "<=" -> builder.append("<=");
                case "=" -> builder.append("==");
                case "<>" -> builder.append("!=");
                default ->
                        throw new ASTException("Unsupported operator: " + location.getText(), location);
            }
			exp2.compile(this);
			builder.append(")");
		} else {
			throw new ASTException("Real expressions required: " + location.getText(), location);
		}
		return null;
	}

	@Override
	public CompiledColorExpression compile(ASTColorPalette palette) {
		final Token location = palette.getLocation();
		builder.append("palette");
		builder.append(palette.getName().toUpperCase().charAt(0));
		builder.append(palette.getName().substring(1));
		builder.append(".get(");
		if (palette.getExp().isReal()) {
			palette.getExp().compile(this);
		} else {
			throw new ASTException("Expression type not valid: " + location.getText(), location);
		}
		builder.append(")");
		return null;
	}

	@Override
	public CompiledColorExpression compile(ASTColorComponent component) {
		builder.append("color(");
		component.getExp1().compile(this);
		if (component.getExp2() != null) {
			builder.append(",");
			component.getExp2().compile(this);
		}
		if (component.getExp3() != null) {
			builder.append(",");
			component.getExp3().compile(this);
		}
		if (component.getExp4() != null) {
			builder.append(",");
			component.getExp4().compile(this);
		}
		builder.append(")");
		return null;
	}

	@Override
	public CompiledStatement compile(ASTConditionalStatement statement) {
		builder.append("if (");
		statement.getConditionExp().compile(this);
		builder.append(") {\n");
		Map<String, Variable> vars = new HashMap<String, Variable>(variables);
		for (ASTStatement innerStatement : statement.getThenStatementList().getStatements()) {
			innerStatement.compile(new ExpressionCompiler(context, vars, builder, classType));
		}
		if (statement.getElseStatementList() != null) {
			builder.append("} else {\n");
			for (ASTStatement innerStatement : statement.getElseStatementList().getStatements()) {
				innerStatement.compile(new ExpressionCompiler(context, vars, builder, classType));
			}
		}
		builder.append("}\n");
		return null;
	}

	@Override
	public CompiledStatement compile(ASTAssignStatement statement) {
		Variable var = variables.get(statement.getName());
		if (var != null) {
			if (var.isReal() && statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(" = real(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (!var.isReal() && !statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(".set(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (!var.isReal() && statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(".set(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (var.isReal() && !statement.getExp().isReal()) {
				throw new ASTException("Can't assign expression: " + statement.getLocation().getText(), statement.getLocation());
			}
		} else {
			var = new Variable(statement.getName(), statement.getExp().isReal(), false);
			variables.put(statement.getName(), var);
			if (var.isReal()) {
				builder.append("double ");
				builder.append(statement.getName());
				builder.append(" = real(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else {
				builder.append("final MutableNumber ");
				builder.append(statement.getName());
				builder.append(" = getNumber(");
				builder.append(context.newNumberIndex());
				builder.append(").set(");
				statement.getExp().compile(this);
				builder.append(");\n");
			}
		}
		return null;
	}

	@Override
	public CompiledStatement compile(ASTStopStatement statement) {
		builder.append("n = i;\nbreak;\n");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionJulia condition) {
		builder.append("isJulia()");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionParen condition) {
		builder.append("(");
		condition.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public CompiledCondition compile(ASTConditionNeg condition) {
		builder.append("!");
		condition.getExp().compile(this);
		return null;
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

	@Override
	public CompiledExpression compile(ASTConditionalExpression astConditionalExpression) {
		return new CompiledConditionalExpression(context, astConditionalExpression.getConditionExp().compile(this), astConditionalExpression.getThenExp().compile(this), astConditionalExpression.getElseExp().compile(this), astConditionalExpression.getLocation());
	}
}
