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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.awt.geom.AffineTransform;

public class ASTPathOp extends ASTReplacement {
	private ASTExpression arguments;
	private ASTModification oldStyleArguments;
	@Getter
    private int argCount;
	@Getter
    private long flags;
	
	public ASTPathOp(Token token, CFDGDriver driver, String op, ASTModification args) {
		super(token, driver, op);
		this.arguments = null;
		this.oldStyleArguments = args;
		this.argCount = 0;
		this.flags = 0;
	}

	public ASTPathOp(Token token, CFDGDriver driver, String op, ASTExpression args) {
		super(token, driver, op);
		this.arguments = args;
		this.oldStyleArguments = null;
		this.argCount = 0;
		this.flags = 0;
	}

	public ASTExpression getArguments() {
		return arguments != null ? arguments : oldStyleArguments;
	}

    @Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		if (renderer.getCurrentPath().isCached()) {
			return;
		}
		double[] opData = new double[7];
		pushData(opData, renderer);
		renderer.getCurrentPath().addPathOp(this, opData, parent, tr, renderer);
	}

	@Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		arguments = compile(arguments, ph);

		if (oldStyleArguments != null) {
			oldStyleArguments.compile(ph);
		}
        switch (ph) {
            case TypeCheck -> {
                if (oldStyleArguments != null) {
                    makePositional();
                } else {
                    checkArguments();
                }
            }
            case Simplify -> {
                pathDataConst();
                arguments = simplify(arguments);
            }
            default -> {
            }
        }
	}

	private void pushData(double[] opData, CFDGRenderer renderer) {
		if (arguments != null) {
			if (arguments.evaluate(opData, 7, renderer) < 0) {
				driver.error("Can't evaluate arguments", getToken());
			}
		} else {
			getChildChange().getModData().getTransform().getMatrix(opData);
		}
	}

	private void pathDataConst() {
		if (arguments != null && arguments.isConstant()) {
			double[] data = new double[7];
			if (arguments.evaluate(data, 7) < 0) {
				driver.error("Can't evaluate arguments", getToken());
			}
			arguments = null;
			getChildChange().getModData().setTransform(new AffineTransform(data));
		}
	}

	private void checkArguments() {
		if (arguments != null) {
			argCount = arguments.evaluate(null, 0);
		}
		
		for (int i = 0; arguments != null && i < arguments.size(); i++) {
			ASTExpression temp = arguments.getChild(i);
            switch (temp.getType()) {
                case Flag -> {
                    if (i != arguments.size() - 1) {
                        driver.error("Flags must be the last argument", getToken());
                    }
                    if (temp instanceof ASTReal rf) {
                        flags |= (int) rf.getValue();
                    } else {
                        driver.error("Flag expressions must be constant", getToken());
                    }
                    argCount--;
                }
                case Numeric -> {
                }
                default -> driver.error("Path operation arguments must be numeric expressions or flags", getToken());
            }
		}

        switch (getPathOp()) {
            case LINETO, LINEREL, MOVETO, MOVEREL -> {
                if (argCount != 2) {
                    driver.error("Move/line path operation requires two arguments", getToken());
                }
            }
            case ARCTO, ARCREL -> {
                if (argCount != 3 && argCount != 5) {
                    driver.error("Arc path operations require three or five arguments", getToken());
                }
            }
            case CURVETO, CURVEREL -> {
                if ((flags & FlagType.CF_CONTINUOUS.getMask()) != 0) {
                    if (argCount != 2 && argCount != 4) {
                        driver.error("Continuous curve path operations require two or four arguments", getToken());
                    }
                } else {
                    if (argCount != 4 && argCount != 6) {
                        driver.error("Non-continuous curve path operations require four or six arguments", getToken());
                    }
                }
            }
            case CLOSEPOLY -> {
                if (argCount > 0) {
                    driver.error("CLOSEPOLY takes no arguments, only flags", getToken());
                }
            }
            default -> {
            }
        }
	}

	private void makePositional() {
		long[] value = new long[1];
		ASTExpression w = ASTUtils.getFlagsAndStroke(driver, oldStyleArguments.getModExp(), value);
		flags = value[0];
		if (w != null) {
			driver.error("Stroke width not allowed in a path operation", w.getToken());
		}

		ASTExpression ax = null;
		ASTExpression ay = null;
		ASTExpression ax1 = null;
		ASTExpression ay1 = null;
		ASTExpression ax2 = null;
		ASTExpression ay2 = null;
		ASTExpression arx = null;
		ASTExpression ary = null;
		ASTExpression ar = null;

		for (ASTModTerm term : oldStyleArguments.getModExp()) {
            switch (term.getModType()) {
                case x -> ax = acquireTerm(ax, term);
                case y -> ay = acquireTerm(ay, term);
                case x1 -> ax1 = acquireTerm(ax1, term);
                case y1 -> ay1 = acquireTerm(ay1, term);
                case x2 -> ax2 = acquireTerm(ax2, term);
                case y2 -> ay2 = acquireTerm(ay2, term);
                case xrad -> arx = acquireTerm(arx, term);
                case yrad -> ary = acquireTerm(ary, term);
                case rotate -> ar = acquireTerm(ar, term);
                case z, zsize -> driver.error("Z changes are not permitted in paths", term.getToken());
                case unknown -> driver.error("Unrecognized element in a path operation", term.getToken());
                default -> driver.error("Unrecognized element in a path operation", term.getToken());
            }
		}

		ASTExpression xy = null;
		if (getPathOp() != PathOp.CLOSEPOLY) {
			xy = parseXY(ax, ay, 0.0, getToken());
			ax = null;
			ay = null;
		}

		//TODO controllare

        switch (getPathOp()) {
            case LINETO, LINEREL, MOVETO, MOVEREL -> arguments = xy;
            case ARCTO, ARCREL -> {
                if (arx != null && ary != null) {
                    ASTExpression rxy = parseXY(arx, ary, 1.0, getToken());
                    arx = null;
                    ary = null;
                    ASTExpression angle = ar;
                    if (angle == null) {
                        angle = new ASTReal(getToken(), driver, 0.0);
                    }
                    if (angle.getType() != ExpType.Numeric || angle.evaluate(null, 0) != 1) {
                        driver.error("Arc angle must be a scalar value", angle.getToken());
                    }
                    arguments = xy.append(rxy).append(angle);
                } else {
                    ASTExpression radius = ar;
                    ar = null;
                    if (radius == null) {
                        radius = new ASTReal(getToken(), driver, 1.0);
                    }
                    if (radius.getType() != ExpType.Numeric || radius.evaluate(null, 0) != 1) {
                        driver.error("Arc radius must be a scalar value", radius.getToken());
                    }
                    arguments = xy.append(radius);
                }
            }
            case CURVETO, CURVEREL -> {
                ASTExpression xy1 = null;
                ASTExpression xy2 = null;
                if (ax1 != null || ay1 != null) {
                    xy1 = parseXY(ax1, ay1, 0.0, getToken());
                } else {
                    flags |= FlagType.CF_CONTINUOUS.getMask();
                }
                if (ax2 != null || ay2 != null) {
                    xy2 = parseXY(ax2, ay2, 0.0, getToken());
                }
                ax1 = null;
                ay1 = null;
                ax2 = null;
                ay2 = null;
                arguments = xy.append(xy1).append(xy2);
            }
            case CLOSEPOLY -> {
				//noop
            }
            default -> {
            }
        }

		rejectTerm(ax);
		rejectTerm(ay);
		rejectTerm(ar);
		rejectTerm(arx);
		rejectTerm(ary);
		rejectTerm(ax1);
		rejectTerm(ay1);
		rejectTerm(ax2);
		rejectTerm(ay2);

		argCount = arguments != null ? arguments.evaluate(null, 0) : 0;
		oldStyleArguments = null;
	}

	private ASTExpression acquireTerm(ASTExpression exp, ASTModTerm term) {
		if (exp != null) {
			driver.error("Duplicate argument", exp.getToken());
			driver.error("Conflicts with this argument", term.getToken());
		}
		return term.getArguments();
	}

	private void rejectTerm(ASTExpression exp) {
		if (exp != null) {
			driver.error("Illegal argument", exp.getToken());
		}
	}

	private ASTExpression parseXY(ASTExpression ax, ASTExpression ay, double def, Token token) {
		//TODO controllare
		if (ax == null) {
			ax = new ASTReal(token, driver, def);
		}
		int sz = 0;
		if (ax.getType() == ExpType.Numeric) {
			sz = ax.evaluate(null, 0);
		} else {
			driver.error("Path argument must be a scalar value", ax.getToken());
		}
		if (sz == 1 && ay == null) {
			ay = new ASTReal(token, driver, def);
		}
		if (ay != null && sz >= 0) {
			if (ay.getType() == ExpType.Numeric) {
				sz += ay.evaluate(null, 0);
			} else {
				driver.error("Path argument must be a scalar value", ay.getToken());
			}
		}
		if (sz != 2) {
			driver.error("Error parsing path operation arguments", token);
		}
		return ax.append(ay);
	}
}
