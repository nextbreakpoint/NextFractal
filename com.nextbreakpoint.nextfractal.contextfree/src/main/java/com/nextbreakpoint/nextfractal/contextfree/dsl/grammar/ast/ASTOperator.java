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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.Locality;
import org.antlr.v4.runtime.Token;

public class ASTOperator extends ASTExpression {
	private char op;
	private int tupleSize;
	private ASTExpression left;
	private ASTExpression right;

	public ASTOperator(CFDGDriver driver, char op, ASTExpression left, ASTExpression right, Token location) {
		super(driver, location);
		this.op = op;
		this.left = left;
		this.right = right;
		this.tupleSize = 1;
		int index = "NP!+-*/^_<>LG=n&|X".indexOf(String.valueOf(op));
		if (index == -1) {
            this.driver.error("Unknown operator", location);
        } else if (index < 3) {
			if (right != null) {
                this.driver.error("Operator takes only one operand", location);
            }
		} else {
			if (right == null) {
                this.driver.error("Operator takes two operands", location);
            }
		}
	}

	public ASTOperator(CFDGDriver driver, char op, ASTExpression left, Token location) {
		this(driver, op, left, null, location);
	}

	public char getOp() {
		return op;
	}

	public int getTupleSize() {
		return tupleSize;
	}

	public ASTExpression getLeft() {
		return left;
	}

	public ASTExpression getRight() {
		return right;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		double[] l = new double[] { 0.0 };
		double[] r = new double[] { 0.0 };

		if (result != null && length < 1)
			return -1;

		if (type == ExpType.FlagType && op == '+') {
			if (left.evaluate(result != null ? l : null, 1, renderer) != 1)
				return -1;
			if (right == null || right.evaluate(result != null ? r : null, 1, renderer) != 1)
				return -1;
			int f = (int) l[0] | (int) r[0];
			if (result != null)
				result[0] = f;
			return 1;
		}

		if (type != ExpType.NumericType) {
            driver.error("Non-numeric expression in a numeric context", location);
            return -1;
		}

		if (left.evaluate(result != null ? l : null, result != null ? 1 : 0, renderer) != 1) {
            driver.error("illegal operand", null);
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
        
		int rightnum = right != null ? right.evaluate(result != null ? r : null, result != null ? 1 : 0, renderer) : 0;

		if (rightnum == 0 && (op == 'N' || op == 'P' || op == '!')) {
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

		if (rightnum != 1) {
            driver.error("illegal operand", location);
            return -1;
		}

		if (result != null) {
			switch (op) {
			case '+':
				result[0] = l[0] + r[0];
				break;
			case '-':
				result[0] = l[0] - r[0];
				break;
			case '_':
				result[0] = l[0] - r[0] > 0.0 ? l[0] - r[0] : 0.0;
				break;
			case '*':
				result[0] = l[0] * r[0];
				break;
			case '/':
				result[0] = l[0] / r[0];
				break;
			case '<':
				result[0] = (l[0] < r[0]) ? 1.0 : 0.0;
				break;
			case 'L':
				result[0] = (l[0] <= r[0]) ? 1.0 : 0.0;
				break;
			case '>':
				result[0] = (l[0] > r[0]) ? 1.0 : 0.0;
				break;
			case 'G':
				result[0] = (l[0] >= r[0]) ? 1.0 : 0.0;
				break;
			case '=':
				result[0] = (l[0] == r[0]) ? 1.0 : 0.0;
				break;
			case 'n':
				result[0] = (l[0] != r[0]) ? 1.0 : 0.0;
				break;
			case '&':
				result[0] = r[0];
				break;
			case '|':
				result[0] = r[0];
				break;
			case 'X':
				result[0] = (l[0] != 0 && r[0] == 0 || l[0] == 0 && r[0] != 0) ? 1.0 : 0.0;
				break;
			case '^':
				result[0] = Math.pow(l[0], r[0]);
				if (isNatural && result[0] < 9007199254740992.0) {
					long pow = 1;
					long il = (long)l[0];
					long ir = (long)r[0];
					while (ir != 0) {
						if ((ir & 1) != 0) pow *= il;
						il *= il;
						ir >>= 1;
					}
					result[0] = pow;
				}
				break;
			default:
				return -1;
			}
		} else {
			if ("+-*/^_<>LG=n&|X".indexOf(op) == -1)
				return -1;
		}

		return tupleSize;
	}

	@Override
	public void entropy(StringBuilder e) {
		if (left != null) {
			left.entropy(e);
		}

		if (right != null) {
			right.entropy(e);
		}

		switch (op) {
			case '*':
				e.append("\u002E\u0032\u00D9\u002C\u0041\u00FE");
				break;
			case '/':
				e.append("\u006B\u0015\u0023\u0041\u009E\u00EB");
				break;
			case '+':
				e.append("\u00D7\u00B1\u00B0\u0039\u0033\u00C8");
				break;
			case '-':
				e.append("\u005D\u00E7\u00F0\u0094\u00C4\u0013");
				break;
			case '^':
				e.append("\u0002\u003C\u0068\u0036\u00C5\u00A0");
				break;
			case 'N':
				e.append("\u0055\u0089\u0051\u0046\u00DB\u0084");
				break;
			case 'P':
				e.append("\u008E\u00AC\u0029\u004B\u000E\u00DC");
				break;
			case '!':
				e.append("\u0019\u003A\u003E\u0053\u0014\u00EA");
				break;
			case '<':
				e.append("\u00BE\u00DB\u00C4\u00A6\u004E\u00AD");
				break;
			case '>':
				e.append("\u00C7\u00D9\u0057\u0032\u00D6\u0087");
				break;
			case 'L':
				e.append("\u00E3\u0056\u007E\u0044\u0057\u0080");
				break;
			case 'G':
				e.append("\u00B1\u002D\u002A\u00CC\u002C\u0040");
				break;
			case '=':
				e.append("\u0078\u0048\u00C2\u0095\u00A9\u00E2");
				break;
			case 'n':
				e.append("\u0036\u00CC\u0001\u003B\u002F\u00AD");
				break;
			case '&':
				e.append("\u0028\u009B\u00FB\u007F\u00DB\u009C");
				break;
			case '|':
				e.append("\u002E\u0040\u001B\u0044\u0015\u007C");
				break;
			case 'X':
				e.append("\u00A7\u002B\u0092\u00FA\u00FC\u00F9");
				break;
			case '_':
				e.append("\u0060\u002F\u0010\u00AD\u0010\u00FF");
				break;
			default:
				break;
		}
	}

	@Override
	public ASTExpression simplify() {
		left = left.simplify(left);

		right = right.simplify(right);

		if (isConstant && (type == ExpType.NumericType || type == ExpType.FlagType)) {
			double[] result = new double[1];
			if (evaluate(result, 1, null) != 1) {
				return null;
			}

			ASTExpression r = AST.makeResult(driver, result[0], tupleSize, this);
			return r;
		}

		return this;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		left = compile(left, ph);

		right = compile(right, ph);

		switch (ph) {
			case TypeCheck: {
				isConstant = left.isConstant() && (right == null || right.isConstant());
				locality = right != null ? AST.combineLocality(left.getLocality(), right.getLocality()) : left.getLocality();
				if (locality == Locality.PureNonlocal) {
					locality = Locality.ImpureNonlocal;
				}
				type = right != null ? ExpType.fromType(left.getType().getType() | right.getType().getType()) : left.getType();
				if (type == ExpType.NumericType) {
					int ls = left != null ? left.evaluate(null, 0) : 0;
					int rs = right != null ? right.evaluate(null, 0) : 0;
					switch (op) {
						case 'N':
						case 'P':
							tupleSize = ls;
							if (rs != 0) {
								driver.error("Unitary operators must have only one operand", location);
							}
							break;
						case '!':
							if (rs != 0 || ls != 1) {
								driver.error("Unitary operators must have only one scalar operand", location);
							}
							break;
						case '+':
						case '-':
						case '_':
						case '/':
						case '*':
							tupleSize = ls;
						case '=':
						case 'n':
							if (ls != rs) {
								driver.error("Operands must have the same length", location);
							}
							if (ls < 1 || rs < 1) {
								driver.error("Binary operators must have two operands", location);
							}
							break;
						default:
							if (ls != 1 || rs != 1) {
								driver.error("Binary operators must have two scalar operands", location);
							}
							break;
					}
				}
				if ("+_*<>LG=n&|X^!".indexOf(String.valueOf(op)) != -1) {
					isNatural = left.isNatural() && (right == null || right.isNatural());
				}
				if (op == '+') {
					if (type == ExpType.FlagType && type != ExpType.NumericType) {
						driver.error("Operands must be numeric or flags", location);
					}
				} else {
					if (type != ExpType.NumericType) {
						driver.error("Operand(s) must be numeric", location);
					}
				}
				if (op == '_' && !isNatural() && !ASTParameter.Impure) {
					driver.error("Proper subtraction operands must be natural", location);
				}
				break;
			}

			case Simplify: 
				break;
	
			default:
				break;
		}
		return null;
	}
}
