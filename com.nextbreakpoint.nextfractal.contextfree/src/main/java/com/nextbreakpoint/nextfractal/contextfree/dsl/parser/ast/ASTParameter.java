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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;

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
@EqualsAndHashCode(callSuper = false)
public class ASTParameter extends ASTObject {
	public static boolean Impure = false;

	private ExpType type = ExpType.None;
	@Setter
    private boolean parameter;
	private boolean loopIndex;
	@Setter
    private boolean natural;
	@Setter
    private Locality locality;
	@Setter
    private int stackIndex = -1;
	private int tupleSize = -1;
	private int nameIndex = -1;
	@Setter
	private ASTDefine definition;

	public ASTParameter(CFDGSystem system, ASTWhere where) {
		super(system, where);
	}

	public ASTParameter(CFDGSystem system, ASTWhere where, String type, int nameIndex) {
		super(system, where);
		init(type, nameIndex);
	}

	public ASTParameter(CFDGSystem system, ASTWhere where, int nameIndex, ASTDefine definition) {
		super(system, where);
		init(nameIndex, definition);
	}

	public ASTParameter(CFDGSystem system, ASTWhere where, int nameIndex) {
		super(system, where);
		loopIndex = true;
		this.nameIndex = nameIndex;
	}

	public ASTParameter(CFDGSystem system, ASTParameter param) {
		super(system, param.getWhere());
		parameter = param.parameter;
		loopIndex = param.loopIndex;
		natural = param.natural;
		locality = param.locality;
		stackIndex = param.stackIndex;
		tupleSize = param.tupleSize;
		nameIndex = param.nameIndex;
		definition = param.definition;
	}

    public void init(String type, int nameIndex) {
		locality = Locality.PureNonLocal;
		final int[] tupleSizeVal = new int[1];
		final boolean[] isNaturalVal = new boolean[1];
		this.type = AST.decodeType(system, type, tupleSizeVal, isNaturalVal, getWhere());
		tupleSize = tupleSizeVal[0];
		natural = isNaturalVal[0];
		this.nameIndex = nameIndex;
		definition = null;
	}

	public void init(int paramName, ASTDefine def) {
		type = def.getExpType();
		locality = def.getExp() != null ? def.getExp().getLocality() : Locality.PureLocal;
		tupleSize = def.getTupleSize();
		if (type == ExpType.Numeric) {
			natural = def.getExp() != null && def.getExp().isNatural();
			if (tupleSize == 0) {
				tupleSize = 1;  // loop index
			}
			if (tupleSize < 1 || tupleSize > AST.MAX_VECTOR_SIZE) {
				system.error("Illegal vector size (<1 or >%d)".formatted(AST.MAX_VECTOR_SIZE), getWhere());
			}
		}
		nameIndex = paramName;
		if (def.getDefineType() == DefineType.Const) {
			definition = def;
		}
	}

	public void checkParam() {
		if (nameIndex == -1) {
			system.error("Reserved keyword used for parameter name", getWhere());
		}
	}

	//TODO verify
	public static int checkType(CFDGBuilder builder, ASTWhere where, List<? extends ASTParameter> types, ASTExpression args, boolean checkNumber) {
		// Walks down the right edge of an expression tree checking that the types
		// of the children match the specified argument types
		if ((types == null || types.isEmpty()) && args == null) {
			return 0;
		}
		if (types == null || types.isEmpty()) {
			builder.error("Arguments are not expected", where);
			return -1;
		}
		if (args == null) {
			builder.error("Arguments are expected", where);
			return -1;
		}

		final boolean justCount = args.getType() == ExpType.None;

		int size = 0;

		final Iterator<? extends ASTParameter> it = types.iterator();

		for (ASTExpression arg : AST.extract(args)) {
			if (!it.hasNext()) {
				builder.error("Too many arguments", args.getWhere());
				return -1;
			}
			final ASTParameter param = it.next();
			if (!justCount) {
				if (param.getType() != arg.getType()) {
					builder.error("Incorrect argument type", args.getWhere());
					builder.error("This is the expected type", param.getWhere());
				}
				if (param.isNatural() && !arg.isNatural() && !Impure) {
					builder.error("This expression does not satisfy the natural number requirement", args.getWhere());
				}
				if (param.getType() == ExpType.Numeric && param.getTupleSize() != arg.evaluate(builder, null, 0)) {
					if (param.getTupleSize() == 1) {
						builder.error("This argument should be scalar", args.getWhere());
					} else {
						builder.error("This argument should be a vector", args.getWhere());
						builder.error("This is the expected type", param.getWhere());
					}
				}
				if (arg.getLocality() != Locality.PureLocal && arg.getLocality() != Locality.PureNonLocal && param.getType() == ExpType.Numeric && !param.isNatural() && !Impure && checkNumber) {
					builder.error("This expression does not satisfy the number parameter requirement", args.getWhere());
				}
			}
			size += param.getTupleSize();
        }

		if (it.hasNext()) {
			final ASTParameter param = it.next();
			builder.error("Not enough arguments.", args.getWhere());
			builder.error("Expecting this argument.", param.getWhere());
		}

		return size;
	}

	//TODO verify
	public ASTExpression constCopy(CFDGBuilder builder, String entropy) {
        switch (type) {
            case Numeric -> {
                final double[] data = new double[tupleSize];
				final boolean natural = this.natural;
				final int valCount = definition.getExp().evaluate(builder, data, tupleSize);
                if (valCount != tupleSize || valCount == 0) {
                    system.error("Unexpected compile error", getWhere());  // this also shouldn't happen
                }
				if (valCount < 1)
					return new ASTReal(system, definition.getExp().getWhere(), 0.0);     // shouldn't happen, but we don't want to crash if it does
				// Create a new cons-list based on the evaluated variable's expression
                ASTExpression list = null;
                for (int i = 0; i < valCount; i++) {
                    ASTReal next = new ASTReal(system, getWhere(), data[i]);
					if (list == null) {
						next.setText(entropy);
					}
                    list = list != null ? list.append(next) : next;
                }
                list.setNatural(natural);
                list.setLocality(locality);
                return list;
            }
            case Mod -> {
                ASTModification ret;
                if (definition.getExp() instanceof ASTModification) {
                    ret = new ASTModification(system, getWhere(), (ASTModification) definition.getExp());
                } else {
                    ret = new ASTModification(system, getWhere(), definition.getChildChange());
                }
                ret.setLocality(locality);
                return ret;
            }
            case Rule -> {
				// This must be bound to an ASTruleSpecifier, otherwise it would not be constant
                if (definition.getExp() instanceof ASTRuleSpecifier r) {
                    ASTRuleSpecifier ret = new ASTRuleSpecifier(system, getWhere(), r.getShapeType(), entropy, null, null);
                    ret.grab(r);
                    ret.setLocality(locality);
                    return ret;
                } else {
                    system.error("Internal error computing bound rule specifier", getWhere());
                }
            }
            default -> {
            }
        }
		return null;
	}

	public boolean compare(ASTParameter p) {
		if (type != p.type) return true;
        return type == ExpType.Numeric && tupleSize != p.tupleSize;
    }

	public boolean compare(ASTExpression e) {
		if (type != e.type) return true;
        return type == ExpType.Numeric && tupleSize != e.evaluate(null, null, null, 0);
    }
}
