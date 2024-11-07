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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import lombok.Getter;

import java.awt.geom.AffineTransform;

// astreplacement.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
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

public class ASTPathOp extends ASTReplacement {
	private ASTExpression arguments;
	private ASTModification oldStyleArguments;
	@Getter
    private int argCount;
	@Getter
    private long flags;
	
	public ASTPathOp(CFDGSystem system, ASTWhere where, String op, ASTModification args) {
		super(system, where, op);
		this.arguments = null;
		this.oldStyleArguments = args;
		this.argCount = 0;
		this.flags = 0;
	}

	public ASTPathOp(CFDGSystem system, ASTWhere where, String op, ASTExpression args) {
		super(system, where, op);
		this.arguments = args;
		this.oldStyleArguments = null;
		this.argCount = 0;
		this.flags = 0;
	}

    @Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		if (renderer.getCurrentPath().isCached()) {
			return;
		}
		final double[] opData = new double[6];
		pushData(builder, renderer, opData);
		renderer.getCurrentPath().addPathOp(this, opData, parent, tr, renderer);
	}

	@Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		super.compile(builder, phase);
		arguments = ASTExpression.compile(builder, phase, arguments);

		if (oldStyleArguments != null) {
			oldStyleArguments.compile(builder, phase);
		}

        switch (phase) {
            case TypeCheck -> {
                if (oldStyleArguments != null) {
                    makePositional(builder);
                } else {
                    checkArguments(builder);
                }
            }
            case Simplify -> {
                arguments = ASTExpression.simplify(builder, arguments);
                pathDataConst(builder);
            }
            default -> {
            }
        }
	}

	private void pushData(CFDGBuilder builder, CFDGRenderer renderer, double[] opData) {
		if (arguments != null) {
			if (arguments.evaluate(builder, renderer, opData, 6) < 0) {
				system.error("Can't evaluate arguments", getWhere());
			}
		} else {
			getChildChange().getModData().getTransform().getMatrix(opData);
		}
	}

	private void pathDataConst(CFDGBuilder builder) {
		if (arguments != null && arguments.isConstant()) {
			final double[] data = new double[6];
			if (arguments.evaluate(builder, data, 6) < 0) {
				system.error("Can't evaluate arguments", getWhere());
			}
			arguments = null;
			getChildChange().getModData().setTransform(new AffineTransform(data));
		}
	}

	private void checkArguments(CFDGBuilder builder) {
		if (arguments != null) {
			argCount = arguments.evaluate(builder, null, 0);
		}

		ASTExpression flag = null;
		for (int i = 0; arguments != null && i < arguments.size(); i++) {
			final ASTExpression arg = arguments.getChild(i);
            switch (arg.getType()) {
                case Flag -> {
					if (flag != null) {
						system.error("There can only be one flag argument", getWhere());
					}
					flag = arg;
					double[] value = new double[1];
					if (!arg.isConstant() || arg.evaluate(builder, value, 1) != 1) {
						system.error("Flag expressions must be constant", getWhere());
					}
					flags |= (long) value[0];
                    argCount--;
                }
                case Numeric -> {
					if (flag != null) {
						system.error("Flags must be the last argument", getWhere());
						flag = null;
					}
                }
                default -> system.error("Path operation arguments must be numeric expressions or flags", getWhere());
            }
		}

        switch (getPathOp()) {
            case LINETO, LINEREL, MOVETO, MOVEREL -> {
				if (flags != 0) {
					system.error("No flags can be used with this operation", getWhere());
				}
                if (argCount != 2) {
                    system.error("Move/line path operation requires two arguments", getWhere());
                }
            }
            case ARCTO, ARCREL -> {
				if ((flags & ~(FlagType.CF_ARC_CW.getMask() | FlagType.CF_ARC_LARGE.getMask())) != 0) {
					system.error("Only CF::ArcCW and CF::ArcLarge flags can be used with this operation", getWhere());
				}
                if (argCount != 3 && argCount != 5) {
                    system.error("Arc path operations require three or five arguments", getWhere());
                }
            }
            case CURVETO, CURVEREL -> {
                if ((flags & ~FlagType.CF_CONTINUOUS.getMask()) != 0) {
					system.error("Only CF::Continuous flag can be used with this operation", getWhere());
				} else if ((flags & FlagType.CF_CONTINUOUS.getMask()) != 0) {
					if (argCount != 2 && argCount != 4) {
						system.error("Continuous curve path operations require two or four arguments", getWhere());
					}
				} else {
					if (argCount != 4 && argCount != 6) {
						system.error("Non-continuous curve path operations require four or six arguments", getWhere());
					}
				}
            }
            case CLOSEPOLY -> {
				if ((flags & ~FlagType.CF_ALIGN.getMask()) != 0) {
					system.error("Only CF::Align flag can be used with this operation", getWhere());
				}
                if (argCount > 0) {
                    system.error("CLOSEPOLY takes no arguments, only flags", getWhere());
                }
            }
            default -> {
            }
        }
	}

	private void makePositional(CFDGBuilder builder) {
		if (oldStyleArguments == null) {
			system.error("Path operation arguments missing", getWhere());
			return;
		}

		final long[] value = new long[1];
		final ASTExpression w = AST.getFlagsAndStroke(system, oldStyleArguments.getModExp(), value);
		if (w != null) {
			system.error("Stroke width not allowed in a path operation", w.getWhere());
		}
		flags = value[0];

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
                case z, zsize -> system.error("Z changes are not permitted in paths", term.getWhere());
                default -> system.error("Unrecognized element in a path operation", term.getWhere());
            }
		}

		ASTExpression xy = null;
		if (getPathOp() != PathOp.CLOSEPOLY) {
			xy = parseXY(builder, ax, ay, 0.0, getWhere());
			ax = null;
			ay = null;
		}

        switch (getPathOp()) {
            case LINETO, LINEREL, MOVETO, MOVEREL -> arguments = xy;
            case ARCTO, ARCREL -> {
                if (arx != null && ary != null) {
					final ASTExpression rxy = parseXY(builder, arx, ary, 1.0, getWhere());
                    arx = null;
                    ary = null;
                    ASTExpression angle = ar;
                    if (angle == null) {
                        angle = new ASTReal(system, getWhere(), 0.0);
                    }
                    if (angle.getType() != ExpType.Numeric || angle.evaluate(builder, null, 0) != 1) {
                        system.error("Arc angle must be a scalar value", angle.getWhere());
                    }
                    arguments = xy.append(rxy).append(angle);
                } else {
                    ASTExpression radius = ar;
                    ar = null;
                    if (radius == null) {
                        radius = new ASTReal(system, getWhere(), 1.0);
                    }
                    if (radius.getType() != ExpType.Numeric || radius.evaluate(builder, null, 0) != 1) {
                        system.error("Arc radius must be a scalar value", radius.getWhere());
                    }
                    arguments = xy.append(radius);
                }
            }
            case CURVETO, CURVEREL -> {
                ASTExpression xy1 = null;
                ASTExpression xy2 = null;
                if (ax1 != null || ay1 != null) {
                    xy1 = parseXY(builder, ax1, ay1, 0.0, getWhere());
                } else {
                    flags |= FlagType.CF_CONTINUOUS.getMask();
                }
                if (ax2 != null || ay2 != null) {
                    xy2 = parseXY(builder, ax2, ay2, 0.0, getWhere());
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

		argCount = arguments != null ? arguments.evaluate(builder, null, 0) : 0;
		oldStyleArguments = null;
	}

	private ASTExpression acquireTerm(ASTExpression exp, ASTModTerm term) {
		if (exp != null) {
			system.error("Duplicate argument", exp.getWhere());
			system.error("Conflicts with this argument", term.getWhere());
		}
		return term.getArguments();
	}

	private void rejectTerm(ASTExpression exp) {
		if (exp != null) {
			system.error("Illegal argument", exp.getWhere());
		}
	}

	private ASTExpression parseXY(CFDGBuilder builder, ASTExpression ax, ASTExpression ay, double def, ASTWhere where) {
		if (ax == null) {
			ax = new ASTReal(system, where, def);
		}
		int sz = 0;
		if (ax.getType() == ExpType.Numeric) {
			sz = ax.evaluate(builder, null, 0);
		} else {
			system.error("Path argument must be a scalar value", ax.getWhere());
		}
		if (sz == 1 && ay == null) {
			ay = new ASTReal(system, where, def);
		}
		if (ay != null && sz >= 0) {
			if (ay.getType() == ExpType.Numeric) {
				sz += ay.evaluate(builder, null, 0);
			} else {
				system.error("Path argument must be a scalar value", ay.getWhere());
			}
		}
		if (sz != 2) {
			system.error("Error parsing path operation arguments", where);
		}
		return ax.append(ay);
	}
}
