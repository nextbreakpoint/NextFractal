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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledAssignStatement;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColorComponent;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledColorPalette;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionCompareOp;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionExpression;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionJulia;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionLogicOp;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionNeg;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionParen;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionTrap;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionalExpression;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledConditionalStatement;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledException;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledExpression;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledExpressionCompiler;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledFunction;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledNumber;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOperator;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitTrap;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitTrapOp;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledPalette;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledPaletteElement;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledParen;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRuleCompareOp;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRuleExpression;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledRuleLogicOp;
//import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledStopStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledVariable;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiledExpressionCompiler {
	private final Map<String, Variable> variables;
	private final ExpressionCompilerContext context;
	private final StringBuilder builder;
	private final ClassType classType;

	public CompiledExpressionCompiler(ExpressionCompilerContext context, Map<String, Variable> variables, StringBuilder builder, ClassType classType) {
		this.variables = variables;
		this.context = context;
		this.builder = builder;
		this.classType = classType;
	}

//	public CompiledExpression compile(CompiledNumber number) {
//		if (number.isReal()) {
//			builder.append(number.getR());
//		} else {
//			builder.append("getNumber(");
//			builder.append(context.newNumberIndex());
//			builder.append(").set(");
//			builder.append(number.getR());
//			builder.append(",");
//			builder.append(number.getI());
//			builder.append(")");
//		}
//		return null;
//	}
//
//	public CompiledExpression compile(CompiledFunction function) {
//		final Token location = function.getLocation();
//		if (function.getName().equals("time")) {
//			if (classType.equals(ClassType.ORBIT)) {
//				context.setOrbitUseTime(true);
//			}
//			if (classType.equals(ClassType.COLOR)) {
//				context.setColorUseTime(true);
//			}
//			if (function.getArguments().length != 0) {
//				throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//			}
//			builder.append("time()");
//		} else {
//			builder.append("func");
//			builder.append(function.getName().toUpperCase().charAt(0));
//			builder.append(function.getName().substring(1));
//			builder.append("(");
//            switch (function.getName()) {
//                case "mod", "mod2", "pha", "re", "im" -> {
//                    if (function.getArguments().length != 1) {
//                        throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//                    }
//                }
//                case "sin", "cos", "tan", "asin", "acos", "atan", "sqrt", "exp" -> {
//                    if (function.getArguments().length != 1) {
//                        throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[0].isReal()) {
//                        builder.append("getNumber(");
//                        builder.append(context.newNumberIndex());
//                        builder.append("),");
//                    }
//                }
//                case "abs", "ceil", "floor", "log", "square", "saw", "ramp" -> {
//                    if (function.getArguments().length != 1) {
//                        throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[0].isReal()) {
//                        throw new CompiledException("Invalid type of arguments: " + location.getText(), location);
//                    }
//                }
//                case "min", "max", "atan2", "hypot", "pulse" -> {
//                    if (function.getArguments().length != 2) {
//                        throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[0].isReal()) {
//                        throw new CompiledException("Invalid type of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[1].isReal()) {
//                        throw new CompiledException("Invalid type of arguments: " + location.getText(), location);
//                    }
//                }
//                case "pow" -> {
//                    if (function.getArguments().length != 2) {
//                        throw new CompiledException("Invalid number of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[1].isReal()) {
//                        throw new CompiledException("Invalid type of arguments: " + location.getText(), location);
//                    }
//                    if (!function.getArguments()[0].isReal()) {
//                        builder.append("getNumber(");
//                        builder.append(context.newNumberIndex());
//                        builder.append("),");
//                    }
//                }
//                default ->
//                        throw new CompiledException("Unsupported function: " + location.getText(), location);
//            }
//			CompiledExpression[] arguments = function.getArguments();
//			for (int i = 0; i < arguments.length; i++) {
//				compile(arguments[i]);
//				if (i < arguments.length - 1) {
//					builder.append(",");
//				}
//			}
//			builder.append(")");
//		}
//		return null;
//	}
//
//	public CompiledExpression compile(CompiledOperator operator) {
//		CompiledExpression exp1 = operator.getExp1();
//		CompiledExpression exp2 = operator.getExp2();
//		final Token location = operator.getLocation();
//		if (exp2 == null) {
//            switch (operator.getOp()) {
//                case "-" -> {
//                    if (exp1.isReal()) {
//                        builder.append("-");
//                        compile(exp1);
//                    } else {
//                        builder.append("opNeg");
//                        builder.append("(");
//                        builder.append("getNumber(");
//                        builder.append(context.newNumberIndex());
//                        builder.append("),");
//                        compile(exp1);
//                        builder.append(")");
//                    }
//                }
//                case "+" -> {
//                    if (exp1.isReal()) {
//                        compile(exp1);
//                    } else {
//                        builder.append("opPos");
//                        builder.append("(");
//                        builder.append("getNumber(");
//                        builder.append(context.newNumberIndex());
//                        builder.append("),");
//                        compile(exp1);
//                        builder.append(")");
//                    }
//                }
//                default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//            }
//		} else {
//			if (exp1.isReal() && exp2.isReal()) {
//				if (operator.getOp().equals("^")) {
//					builder.append("opPow");
//				}
//				if (operator.getOp().equals("<>")) {
//					builder.append("getNumber(");
//					builder.append(context.newNumberIndex());
//					builder.append(").set");
//				}
//				builder.append("(");
//				compile(exp1);
//                switch (operator.getOp()) {
//                    case "+" -> builder.append("+");
//                    case "-" -> builder.append("-");
//                    case "*" -> builder.append("*");
//                    case "/" -> builder.append("/");
//                    case "^", "<>" -> builder.append(",");
//                    default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//                }
//				compile(exp2);
//				builder.append(")");
//			} else if (exp2.isReal()) {
//                switch (operator.getOp()) {
//                    case "+" -> builder.append("opAdd");
//                    case "-" -> builder.append("opSub");
//                    case "*" -> builder.append("opMul");
//                    case "/" -> builder.append("opDiv");
//                    case "^" -> builder.append("opPow");
//                    default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//                }
//				builder.append("(");
//				builder.append("getNumber(");
//				builder.append(context.newNumberIndex());
//				builder.append("),");
//				compile(exp1);
//				builder.append(",");
//				compile(exp2);
//				builder.append(")");
//			} else {
//                switch (operator.getOp()) {
//                    case "+" -> builder.append("opAdd");
//                    case "-" -> builder.append("opSub");
//                    case "*" -> builder.append("opMul");
//                    case "/" -> builder.append("opDiv");
//                    default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//                }
//				builder.append("(");
//				builder.append("getNumber(");
//				builder.append(context.newNumberIndex());
//				builder.append("),");
//				compile(exp1);
//				builder.append(",");
//				compile(exp2);
//				builder.append(")");
//			}
//		}
//		return null;
//	}
//
//	public CompiledExpression compile(CompiledParen paren) {
//		builder.append("(");
//		compile(paren.getExp());
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledExpression compile(CompiledVariable variable) {
//		builder.append(variable.getName());
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionCompareOp compareOp) {
//		CompiledExpression exp1 = compareOp.getExp1();
//		CompiledExpression exp2 = compareOp.getExp2();
//		final Token location = compareOp.getLocation();
//		if (exp1.isReal() && exp2.isReal()) {
//			builder.append("(");
//			compile(exp1);
//            switch (compareOp.getOp()) {
//                case ">" -> builder.append(">");
//                case "<" -> builder.append("<");
//                case ">=" -> builder.append(">=");
//                case "<=" -> builder.append("<=");
//                case "=" -> builder.append("==");
//                case "<>" -> builder.append("!=");
//                default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//            }
//			compile(exp2);
//			builder.append(")");
//		} else {
//			throw new CompiledException("Real expressions required: " + location.getText(), location);
//		}
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionLogicOp logicOp) {
//		CompiledConditionExpression exp1 = logicOp.getExp1();
//		CompiledConditionExpression exp2 = logicOp.getExp2();
//		final Token location = logicOp.getLocation();
//		builder.append("(");
//		compile(exp1);
//		switch (logicOp.getOp()) {
//			case "&":
//				builder.append("&&");
//				break;
//
//			case "|":
//				builder.append("||");
//				break;
//
//			case "^":
//				builder.append("^^");
//				break;
//
//			default:
//				throw new CompiledException("Unsupported operator: " + location.getText(), location);
//		}
//		compile(exp2);
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionTrap trap) {
//		if (!trap.isContains()) {
//			builder.append("!");
//		}
//		builder.append("trap");
//		builder.append(trap.getName().toUpperCase().charAt(0));
//		builder.append(trap.getName().substring(1));
//		builder.append(".contains(");
//		compile(trap.getExp());
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledRuleLogicOp logicOp) {
//		CompiledRuleExpression exp1 = logicOp.getExp1();
//		CompiledRuleExpression exp2 = logicOp.getExp2();
//		final Token location = logicOp.getLocation();
//		builder.append("(");
//		compile(exp1);
//        switch (logicOp.getOp()) {
//            case "&" -> builder.append("&&");
//            case "|" -> builder.append("||");
//            case "^" -> builder.append("^^");
//            default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//        }
//		compile(exp2);
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledRuleCompareOp compareOp) {
//		CompiledExpression exp1 = compareOp.getExp1();
//		CompiledExpression exp2 = compareOp.getExp2();
//		final Token location = compareOp.getLocation();
//		if (exp1.isReal() && exp2.isReal()) {
//			builder.append("(");
//			compile(exp1);
//            switch (compareOp.getOp()) {
//                case ">" -> builder.append(">");
//                case "<" -> builder.append("<");
//                case ">=" -> builder.append(">=");
//                case "<=" -> builder.append("<=");
//                case "=" -> builder.append("==");
//                case "<>" -> builder.append("!=");
//                default ->
//                        throw new CompiledException("Unsupported operator: " + location.getText(), location);
//            }
//			compile(exp2);
//			builder.append(")");
//		} else {
//			throw new CompiledException("Real expressions required: " + location.getText(), location);
//		}
//		return null;
//	}
//
//	public CompiledColorExpression compile(CompiledColorPalette palette) {
//		final Token location = palette.getLocation();
//		builder.append("palette");
//		builder.append(palette.getName().toUpperCase().charAt(0));
//		builder.append(palette.getName().substring(1));
//		builder.append(".get(");
//		if (palette.getExp().isReal()) {
//			compile(palette.getExp());
//		} else {
//			throw new CompiledException("Expression type not valid: " + location.getText(), location);
//		}
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledColorExpression compile(CompiledColorComponent component) {
//		builder.append("color(");
//		compile(component.getExp1());
//		if (component.getExp2() != null) {
//			builder.append(",");
//			compile(component.getExp2());
//		}
//		if (component.getExp3() != null) {
//			builder.append(",");
//			compile(component.getExp3());
//		}
//		if (component.getExp4() != null) {
//			builder.append(",");
//			compile(component.getExp4());
//		}
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledStatement compile(CompiledConditionalStatement statement) {
//		builder.append("if (");
//		compile(statement.getConditionExp());
//		builder.append(") {\n");
//		Map<String, Variable> vars = new HashMap<String, Variable>(variables);
//		for (CompiledStatement innerStatement : statement.getThenStatementList().getStatements()) {
//			compile(innerStatement);
//		}
//		if (statement.getElseStatementList() != null) {
//			builder.append("} else {\n");
//			for (CompiledStatement innerStatement : statement.getElseStatementList().getStatements()) {
//				compile(innerStatement);
//			}
//		}
//		builder.append("}\n");
//		return null;
//	}
//
//	public CompiledStatement compile(CompiledAssignStatement statement) {
//		Variable var = variables.get(statement.getName());
//		if (var != null) {
//			if (var.isReal() && statement.getExp().isReal()) {
//				builder.append(statement.getName());
//				builder.append(" = real(");
//				compile(statement.getExp());
//				builder.append(");\n");
//			} else if (!var.isReal() && !statement.getExp().isReal()) {
//				builder.append(statement.getName());
//				builder.append(".set(");
//				compile(statement.getExp());
//				builder.append(");\n");
//			} else if (!var.isReal() && statement.getExp().isReal()) {
//				builder.append(statement.getName());
//				builder.append(".set(");
//				compile(statement.getExp());
//				builder.append(");\n");
//			} else if (var.isReal() && !statement.getExp().isReal()) {
//				throw new CompiledException("Can't assign expression: " + statement.getLocation().getText(), statement.getLocation());
//			}
//		} else {
//			var = new Variable(statement.getName(), statement.getExp().isReal(), false);
//			variables.put(statement.getName(), var);
//			if (var.isReal()) {
//				builder.append("double ");
//				builder.append(statement.getName());
//				builder.append(" = real(");
//				compile(statement.getExp());
//				builder.append(");\n");
//			} else {
//				builder.append("final MutableNumber ");
//				builder.append(statement.getName());
//				builder.append(" = getNumber(");
//				builder.append(context.newNumberIndex());
//				builder.append(").set(");
//				compile(statement.getExp());
//				builder.append(");\n");
//			}
//		}
//		return null;
//	}
//
//	public CompiledStatement compile(CompiledStopStatement statement) {
//		builder.append("n = i;\nbreak;\n");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionJulia condition) {
//		builder.append("isJulia()");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionParen condition) {
//		builder.append("(");
//		compile(condition.getExp());
//		builder.append(")");
//		return null;
//	}
//
//	public CompiledCondition compile(CompiledConditionNeg condition) {
//		builder.append("!");
//		compile(condition.getExp());
//		return null;
//	}
//
//	public CompiledTrap compile(CompiledTrap orbitTrap) {
//		CompiledTrap trap = new CompiledTrap(orbitTrap.getLocation());
//		trap.setName(orbitTrap.getName());
//		trap.setCenter(new Number(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
//		List<CompiledTrapOp> operators = new ArrayList<>();
//		for (CompiledTrapOp astTrapOp : orbitTrap.getOperators()) {
//			operators.add(compile(astTrapOp));
//		}
//		trap.setOperators(operators);
//		return trap;
//	}
//
//	public CompiledTrapOp compile(CompiledTrapOp orbitTrapOp) {
//		Number c1 = null;
//		Number c2 = null;
//		Number c3 = null;
//		if (orbitTrapOp.c1() != null) {
//			c1 = new Number(orbitTrapOp.c1().r(), orbitTrapOp.c1().i());
//		}
//		if (orbitTrapOp.c2() != null) {
//			c2 = new Number(orbitTrapOp.c2().r(), orbitTrapOp.c2().i());
//		}
//		if (orbitTrapOp.c3() != null) {
//			c3 = new Number(orbitTrapOp.c3().r(), orbitTrapOp.c3().i());
//		}
//		final Token location = orbitTrapOp.getLocation();
//		return switch (orbitTrapOp.op()) {
//            case "MOVETO" -> new CompiledTrapOpMoveTo(c1, location);
//            case "MOVEREL", "MOVETOREL" -> new CompiledTrapOpMoveRel(c1, location);
//            case "LINETO" -> new CompiledTrapOpLineTo(c1, location);
//            case "LINEREL", "LINETOREL" -> new CompiledTrapOpLineRel(c1, location);
//            case "ARCTO" -> new CompiledTrapOpArcTo(c1, c2, location);
//            case "ARCREL", "ARCTOREL" -> new CompiledTrapOpArcRel(c1, c2, location);
//            case "QUADTO" -> new CompiledTrapOpQuadTo(c1, c2, location);
//            case "QUADREL", "QUADTOREL" -> new CompiledTrapOpQuadRel(c1, c2, location);
//            case "CURVETO" -> new CompiledTrapOpCurveTo(c1, c2, c3, location);
//            case "CURVEREL", "CURVETOREL" -> new CompiledTrapOpCurveRel(c1, c2, c3, location);
//            case "CLOSE" -> new CompiledTrapOpClose(location);
//            default -> throw new CompiledException("Unsupported operator: " + location.getText(), location);
//        };
//	}
//
//	public CompiledPalette compile(CompiledPalette astPalette) {
//		CompiledPalette palette = new CompiledPalette(astPalette.getLocation());
//		palette.setName(astPalette.getName());
//		List<CompiledPaletteElement> elements = new ArrayList<>();
//		for (CompiledPaletteElement astElement : astPalette.getElements()) {
//			elements.add(compile(astElement));
//		}
//		palette.setElements(elements);
//		return palette;
//	}
//
//	public CompiledPaletteElement compile(CompiledPaletteElement astElement) {
//		return new CompiledPaletteElement(astElement.beginColor(), astElement.endColor(), astElement.steps(), astElement.exp() != null ? compile(astElement.exp()) : null, astElement.location());
//	}
//
//	public CompiledExpression compile(CompiledConditionalExpression astConditionalExpression) {
//		return new CompiledConditionalExpression(context, compile(astConditionalExpression.getCondition()), compile(astConditionalExpression.getThenExp()), compile(astConditionalExpression.getElseExp()), astConditionalExpression.getLocation());
//	}
}
