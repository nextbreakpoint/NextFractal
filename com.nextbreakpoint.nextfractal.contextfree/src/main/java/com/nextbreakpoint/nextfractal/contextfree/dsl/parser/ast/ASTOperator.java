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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;

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

@Getter
public class ASTOperator extends ASTExpression {
    private final char op;
	private int tupleSize;
	private ASTExpression left;
	private ASTExpression right;

	public ASTOperator(CFDGSystem system, ASTWhere where, char op, ASTExpression left, ASTExpression right) {
		super(system, where);
		this.op = op;
		this.left = left;
		this.right = right;
		this.tupleSize = 1;

        //TODO move to operator enum
		final int index = "NP!+-*/^_<>LG=n&|X".indexOf(String.valueOf(op));

		if (index == -1) {
            system.error("Unknown operator", where);
        }
        if (index < 3) {
            if (right != null) {
                system.error("Operator takes only one operand", where);
            }
        } else {
            if (right == null) {
                system.error("Operator takes two operands", where);
            }
        }
	}

	public ASTOperator(CFDGSystem system, ASTWhere where, char op, ASTExpression left) {
		this(system, where, op, left, null);
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
        final double[] l = new double[AST.MAX_VECTOR_SIZE];
        final double[] r = new double[AST.MAX_VECTOR_SIZE];

		if (result != null && length < 1)
			return -1;

		if (type == ExpType.Flag && op == '+') {
			if (left.evaluate(builder, renderer, result != null ? l : null, 1) != 1)
				return -1;
			if (right == null || right.evaluate(builder, renderer, result != null ? r : null, 1) != 1)
				return -1;
			if (result != null) {
                int f = (int) l[0] | (int) r[0];
                result[0] = f;
            }
			return 1;
		}

		if (type != ExpType.Numeric) {
            system.error("Non-numeric expression in a numeric context", getWhere());
            return -1;
		}

        final int leftNum = left.evaluate(builder, renderer, result != null ? l : null, result != null ? 1 : 0);
        if (leftNum == -1) {
            system.error("illegal operand", null);
            return -1;
		}

		// short-circuit evaluate && and ||
        if (result != null && (op == '&' || op == '|')) {
            if (l[0] != 0.0 && op == '|') {
                result[0] = l[0];
                return 1;
            }
            if (l[0] == 0.0 && op == '&') {
                result[0] = 0.0;
                return 1;
            }
        }
        
		final int rightNum = right != null ? right.evaluate(builder, renderer, result != null ? r : null, result != null ? 1 : 0) : 0;
        if (right != null && rightNum != 1) {
            system.error("illegal operand", getWhere());
            return -1;
        }

		if (rightNum == 0 && (op == 'N' || op == 'P' || op == '!')) {
			if (result != null) {
				switch (op) {
				case 'P':
					result[0] = +l[0];
					break;
				case 'N':
					result[0] = -l[0];
					break;
				case '!':
					result[0] = l[0] == 0.0 ? 1.0 : 0.0;
					break;
				default:
					return -1;
				}
			}
			return 1;
		}

		if (result != null) {
            switch (op) {
                case '+' -> {
                    for (int i = 0; i < tupleSize; i++) {
                        result[i] = l[i] + r[i];
                    }
                }
                case '-' -> {
                    for (int i = 0; i < tupleSize; i++) {
                        result[i] = l[i] - r[i];
                    }
                }
                case '_' -> {
                    for (int i = 0; i < tupleSize; i++) {
                        result[i] = Math.max(l[i] - r[i], 0.0);
                    }
                }
                case '*' -> {
                    if (leftNum == rightNum) {
                        for (int i = 0; i < tupleSize; i++) {
                            result[i] = l[i] * r[i];
                        }
                    } else {
                        for (int i = 0; i < tupleSize; i++) {
                            result[i] = leftNum == 1 ? l[0] * r[i] : l[i] * r[0];
                        }
                    }
                }
                case '/' -> {
                    if (leftNum == rightNum) {
                        for (int i = 0; i < tupleSize; i++) {
                            result[i] = l[i] / r[i];
                        }
                    } else {
                        for (int i = 0; i < tupleSize; i++) {
                            result[i] = leftNum == 1 ? l[0] / r[i] : l[i] / r[0];
                        }
                    }
                }
                case '<' -> result[0] = (l[0] < r[0]) ? 1.0 : 0.0;
                case 'L' -> result[0] = (l[0] <= r[0]) ? 1.0 : 0.0;
                case '>' -> result[0] = (l[0] > r[0]) ? 1.0 : 0.0;
                case 'G' -> result[0] = (l[0] >= r[0]) ? 1.0 : 0.0;

                case '=' -> {
                    result[0] = 0.0;
                    for (int i = 0; i < tupleSize; i++) {
                        if (l[i] != r[i]) {
                            return 1;
                        }
                    }
                    result[0] = 1.0;
                }
                case 'n' -> {
                    result[0] = 1.0;
                    for (int i = 0; i < tupleSize; i++) {
                        if (l[i] != r[i]) {
                            return 1;
                        }
                    }
                    result[0] = 0.0;
                }
                case '&', '|' -> result[0] = r[0];
                case 'X' -> result[0] = (l[0] != 0 && r[0] == 0 || l[0] == 0 && r[0] != 0) ? 1.0 : 0.0;
                case '^' -> {
                    result[0] = Math.pow(l[0], r[0]);
                    if (natural && result[0] < AST.MAX_NATURAL) {
                        long pow = 1;
                        long il = (long) l[0];
                        long ir = (long) r[0];
                        while (ir != 0) {
                            if ((ir & 1) != 0) pow *= il;
                            il *= il;
                            ir >>= 1;
                        }
                        result[0] = pow;
                    }
                }
                default -> {
                    return -1;
                }
            }
		} else {
			if ("+-*/^_<>LG=n&|X".indexOf(op) == -1)
				return -1;
		}

		return tupleSize;
	}

	@Override
	public void entropy(StringBuilder entropy) {
		if (left != null) {
			left.entropy(entropy);
		}

		if (right != null) {
			right.entropy(entropy);
		}

        //TODO move to operator enum
        switch (op) {
            case '*' -> entropy.append("\u002E\u0032\u00D9\u002C\u0041\u00FE");
            case '/' -> entropy.append("\u006B\u0015\u0023\u0041\u009E\u00EB");
            case '+' -> entropy.append("\u00D7\u00B1\u00B0\u0039\u0033\u00C8");
            case '-' -> entropy.append("\u005D\u00E7\u00F0\u0094\u00C4\u0013");
            case '^' -> entropy.append("\u0002\u003C\u0068\u0036\u00C5\u00A0");
            case 'N' -> entropy.append("\u0055\u0089\u0051\u0046\u00DB\u0084");
            case 'P' -> entropy.append("\u008E\u00AC\u0029\u004B\u000E\u00DC");
            case '!' -> entropy.append("\u0019\u003A\u003E\u0053\u0014\u00EA");
            case '<' -> entropy.append("\u00BE\u00DB\u00C4\u00A6\u004E\u00AD");
            case '>' -> entropy.append("\u00C7\u00D9\u0057\u0032\u00D6\u0087");
            case 'L' -> entropy.append("\u00E3\u0056\u007E\u0044\u0057\u0080");
            case 'G' -> entropy.append("\u00B1\u002D\u002A\u00CC\u002C\u0040");
            case '=' -> entropy.append("\u0078\u0048\u00C2\u0095\u00A9\u00E2");
            case 'n' -> entropy.append("\u0036\u00CC\u0001\u003B\u002F\u00AD");
            case '&' -> entropy.append("\u0028\u009B\u00FB\u007F\u00DB\u009C");
            case '|' -> entropy.append("\u002E\u0040\u001B\u0044\u0015\u007C");
            case 'X' -> entropy.append("\u00A7\u002B\u0092\u00FA\u00FC\u00F9");
            case '_' -> entropy.append("\u0060\u002F\u0010\u00AD\u0010\u00FF");
            default -> {
            }
        }
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		left = ASTExpression.simplify(builder, left);
		right = ASTExpression.simplify(builder, right);

		if (constant && (type == ExpType.Numeric || type == ExpType.Flag)) {
			final double[] value = new double[AST.MAX_VECTOR_SIZE];
			if (evaluate(builder, null, value, tupleSize) != tupleSize) {
				return null;
			}

            return AST.makeResult(value, tupleSize, this);
		}

		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		left = ASTExpression.compile(builder, phase, left);
		right = ASTExpression.compile(builder, phase, right);

        if (left == null) {
            system.error("Left operand missing", getWhere());
            return null;
        }

        switch (phase) {
            case TypeCheck -> {
                constant = left.isConstant() && (right == null || right.isConstant());
                locality = right != null ? AST.combineLocality(left.getLocality(), right.getLocality()) : left.getLocality();
                if (locality == Locality.PureNonLocal) {
                    locality = Locality.ImpureNonLocal;
                }
                type = right != null ? ExpType.fromType(left.getType().getType() | right.getType().getType()) : left.getType();
                if (type == ExpType.Numeric) {
                    int ls = left != null ? left.evaluate(builder, null, 0) : 0;
                    int rs = right != null ? right.evaluate(builder, null, 0) : 0;
                    switch (op) {
                        case 'N':
                        case 'P':
                            tupleSize = ls;
                            if (rs != 0) {
                                system.error("Unitary operators must have only one operand", getWhere());
                            }
                            break;
                        case '!':
                            if (rs != 0 || ls != 1) {
                                system.error("Unitary operators must have only one scalar operand", getWhere());
                            }
                            break;
                        case '/':
                        case '*':
                            if (ls < 1 || rs < 1) {
                                system.error("Binary operators must have two operands", getWhere());
                            }
                            else if (ls != rs && Math.min(ls, rs) > 1) {
                                system.error("At least one operand must be scalar (or both same size)", getWhere());
                            }
                            tupleSize = Math.max(ls, rs);
                            break;
                        case '+':
                        case '-':
                        case '_':
                            tupleSize = ls;
                        case '=':
                        case 'n':
                            if (ls != rs) {
                                system.error("Operands must have the same length", getWhere());
                            }
                            if (ls < 1 || rs < 1) {
                                system.error("Binary operators must have two operands", getWhere());
                            }
                            break;
                        default:
                            if (ls != 1 || rs != 1) {
                                system.error("Binary operators must have two scalar operands", getWhere());
                            }
                            break;
                    }
                }
                if ("+_*<>LG=n&|X^!".contains(String.valueOf(op))) {
                    natural = (left != null && left.isNatural()) && (right == null || right.isNatural());
                }
                if (op == '+') {
                    if (type == ExpType.Flag || type != ExpType.Numeric) {
                        system.error("Operands must be numeric or flags", getWhere());
                    }
                } else {
                    if (type != ExpType.Numeric) {
                        system.error("Operand(s) must be numeric", getWhere());
                    }
                }
                if (op == '_' && !isNatural() && !ASTParameter.Impure) {
                    system.error("Proper subtraction operands must be natural", getWhere());
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
}
