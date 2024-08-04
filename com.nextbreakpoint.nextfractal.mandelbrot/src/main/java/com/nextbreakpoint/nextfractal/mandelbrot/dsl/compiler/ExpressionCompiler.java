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

import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLConditionalExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTAssignStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColorComponent;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTColorPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTCompiler;
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

@Deprecated
public class ExpressionCompiler implements ASTCompiler {
	private final Map<String, VariableDeclaration> variables;
	private final DSLExpressionContext context;
	private final StringBuilder builder;
	private final ClassType classType;

	public ExpressionCompiler(DSLExpressionContext context, Map<String, VariableDeclaration> variables, StringBuilder builder, ClassType classType) {
		this.variables = variables;
		this.context = context;
		this.builder = builder;
		this.classType = classType;
	}

	@Override
	public DSLExpression compile(ASTNumber number) {
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
	public DSLExpression compile(ASTFunction function) {
		final DSLToken location = function.getLocation();
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
			builder.append(function.getName().toUpperCase().substring(0, 1));
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
	public DSLExpression compile(ASTOperator operator) {
		ASTExpression exp1 = operator.getExp1();
		ASTExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
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
	public DSLExpression compile(ASTParen paren) {
		builder.append("(");
		paren.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public DSLExpression compile(ASTVariable variable) {
		builder.append(variable.getName());
		return null;
	}

	@Override
	public DSLCondition compile(ASTConditionCompareOp condition) {
		ASTExpression exp1 = condition.getExp1();
		ASTExpression exp2 = condition.getExp2();
		final DSLToken location = condition.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			builder.append("(");
			exp1.compile(this);
            switch (condition.getOp()) {
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
	public DSLCondition compile(ASTConditionLogicOp condition) {
		ASTConditionExpression exp1 = condition.getExp1();
		ASTConditionExpression exp2 = condition.getExp2();
		final DSLToken location = condition.getLocation();
		builder.append("(");
		exp1.compile(this);
		switch (condition.getOp()) {
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
	public DSLCondition compile(ASTConditionTrap condition) {
		if (!condition.isContains()) {
			builder.append("!");
		}
		builder.append("trap");
		builder.append(condition.getName().toUpperCase().substring(0, 1));
		builder.append(condition.getName().substring(1));
		builder.append(".contains(");
		condition.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public DSLCondition compile(ASTRuleLogicOp operator) {
		ASTRuleExpression exp1 = operator.getExp1();
		ASTRuleExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
		builder.append("(");
		exp1.compile(this);
        switch (operator.getOp()) {
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
	public DSLCondition compile(ASTRuleCompareOp operator) {
		ASTExpression exp1 = operator.getExp1();
		ASTExpression exp2 = operator.getExp2();
		final DSLToken location = operator.getLocation();
		if (exp1.isReal() && exp2.isReal()) {
			builder.append("(");
			exp1.compile(this);
            switch (operator.getOp()) {
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
	public DSLColorExpression compile(ASTColorPalette palette) {
		final DSLToken location = palette.getLocation();
		builder.append("palette");
		builder.append(palette.getName().toUpperCase().substring(0, 1));
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
	public DSLColorExpression compile(ASTColorComponent component) {
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
	public DSLStatement compile(ASTConditionalStatement statement) {
		builder.append("if (");
		statement.getConditionExp().compile(this);
		builder.append(") {\n");
		Map<String, VariableDeclaration> vars = new HashMap<>(variables);
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
	public DSLStatement compile(ASTAssignStatement statement) {
		VariableDeclaration var = variables.get(statement.getName());
		if (var != null) {
			if (var.real() && statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(" = real(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (!var.real() && !statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(".set(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (!var.real() && statement.getExp().isReal()) {
				builder.append(statement.getName());
				builder.append(".set(");
				statement.getExp().compile(this);
				builder.append(");\n");
			} else if (var.real() && !statement.getExp().isReal()) {
				throw new ASTException("Can't assign expression: " + statement.getLocation().getText(), statement.getLocation());
			}
		} else {
			var = new VariableDeclaration(statement.getName(), statement.getExp().isReal(), false);
			variables.put(statement.getName(), var);
			if (var.real()) {
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
	public DSLStatement compile(ASTStopStatement statement) {
		builder.append("n = i;\nbreak;\n");
		return null;
	}

	@Override
	public DSLCondition compile(ASTConditionJulia condition) {
		builder.append("isJulia()");
		return null;
	}

	@Override
	public DSLCondition compile(ASTConditionParen condition) {
		builder.append("(");
		condition.getExp().compile(this);
		builder.append(")");
		return null;
	}

	@Override
	public DSLCondition compile(ASTConditionNeg condition) {
		builder.append("!");
		condition.getExp().compile(this);
		return null;
	}

	@Override
	public DSLTrap compile(ASTOrbitTrap orbitTrap) {
		DSLTrap trap = new DSLTrap(orbitTrap.getLocation());
		trap.setName(orbitTrap.getName());
		trap.setCenter(new ComplexNumber(orbitTrap.getCenter().r(), orbitTrap.getCenter().i()));
		List<DSLTrapOp> operators = new ArrayList<>();
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
	public DSLPalette compile(ASTPalette palette) {
		List<DSLPaletteElement> elements = new ArrayList<>();
		for (ASTPaletteElement astElement : palette.getElements()) {
			elements.add(astElement.compile(this));
		}
        return new DSLPalette(palette.getLocation(), palette.getName(), elements);
	}

	@Override
	public DSLPaletteElement compile(ASTPaletteElement paletteElement) {
		return new DSLPaletteElement(paletteElement.getLocation(), paletteElement.getBeginColor().getComponents(), paletteElement.getEndColor().getComponents(), paletteElement.getSteps(), paletteElement.getExp() != null ? paletteElement.getExp().compile(this) : null);
	}

	@Override
	public DSLExpression compile(ASTConditionalExpression conditional) {
		return new DSLConditionalExpression(conditional.getLocation(), conditional.getConditionExp().compile(this), conditional.getThenExp().compile(this), conditional.getElseExp().compile(this), context.newNumberIndex());
	}
}
