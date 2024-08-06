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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class ASTParameter {
	public static boolean Impure = false;

	private final CFDGDriver driver;
	@Getter
    private ExpType type = ExpType.None;
	@Getter
    @Setter
    private boolean parameter;
	@Getter
    private boolean loopIndex;
	@Getter
    @Setter
    private boolean natural;
	@Setter
    @Getter
    private Locality locality;
	@Setter
    @Getter
    private int stackIndex = -1;
	@Getter
    private int nameIndex = -1;
	@Getter
    private int tupleSize = -1;
	@Getter
    private ASTDefine definition;
	@Setter
    @Getter
    protected Token location;

	public ASTParameter(CFDGDriver driver, Token location) {
		this.driver = driver;
		this.location = location;
	}

	public ASTParameter(CFDGDriver driver, String type, int nameIndex, Token location) {
		this.driver = driver;
		this.location = location;
		init(type, nameIndex);
	}

	public ASTParameter(CFDGDriver driver, int nameIndex, ASTDefine definition, Token location) {
		this.driver = driver;
		this.location = location;
		init(nameIndex, definition);
	}

	public ASTParameter(CFDGDriver driver, int nameIndex, boolean natural, boolean local, Token location) {
		this.driver = driver;
		this.loopIndex = true;
		this.natural = natural;
		this.nameIndex = nameIndex;
		this.location = location;
	}

	public ASTParameter(ASTParameter param) {
		this.driver = param.driver;
		this.parameter = param.parameter;
		this.loopIndex = param.loopIndex;
		this.stackIndex = param.stackIndex;
		this.nameIndex = param.nameIndex;
		this.natural = param.natural;
		this.locality = param.locality;
		this.tupleSize = param.tupleSize;
		this.location = param.location;
	}

    public void init(String type, int nameIndex) {
		locality = Locality.PureNonLocal;
		int[] tupleSizeVal = new int[1];
		boolean[] isNaturalVal = new boolean[1];
		this.type = ASTUtils.decodeType(driver, type, tupleSizeVal, isNaturalVal, location);
		tupleSize = tupleSizeVal[0];
		natural = isNaturalVal[0];
		this.nameIndex = nameIndex;
		definition = null;
	}

	public void init(int nameIndex, ASTDefine definition) {
		type = definition.getExpType();
		locality = definition.getExp() != null ? definition.getExp().getLocality() : Locality.PureLocal;
		tupleSize = definition.getTupleSize();
		if (type == ExpType.Numeric) {
			natural = definition.getExp() != null && definition.getExp().isNatural();
			if (tupleSize == 0) {
				tupleSize = 1;
			}
			if (tupleSize < 1 || tupleSize > 1000000000) {
				driver.error("Illegal vector size (<1 or >99)", location);
			}
		}
		this.nameIndex = nameIndex;
		if (definition.getDefineType() == DefineType.Const) {
			this.definition = definition;
		}
	}

	public void checkParam() {
		if (nameIndex == -1) {
			throw new RuntimeException("Reserved keyword used for parameter name");
		}
	}

	//TODO controllare

	public boolean compare(ASTParameter p) {
		if (type != p.type) return true;
        return type == ExpType.Numeric && tupleSize != p.tupleSize;
    }

	public boolean compare(ASTExpression e) {
		if (type != e.type) return true;
        return type == ExpType.Numeric && tupleSize != e.evaluate(null, 0, null);
    }

	public static int checkType(CFDGDriver driver, List<? extends ASTParameter> types, ASTExpression args, boolean checkNumber) {
		// Walks down the right edge of an expression tree checking that the types
		// of the children match the specified argument types
		if ((types == null || types.isEmpty()) && args == null) {
			return 0;
		}
		if (types == null || types.isEmpty()) {
			driver.error("Arguments are not expected", args.getLocation());
			return -1;
		}
		if (args == null) {
			driver.error("Arguments are expected", null);
			return -1;
		}

		boolean justCount = args.getType() == ExpType.None;

		int count = 0;
		int size = 0;
		int expect = args.size();

		for (ASTParameter param : types) {
			size += param.getTupleSize();
			count += 1;
			if (justCount) {
				continue;
			}
			if (count > expect) {
				driver.error("Not enough arguments", args.getLocation());
				return -1;
			}
			ASTExpression arg = args.getChild(count - 1);
			if (param.getType() != arg.getType()) {
				driver.error("Incorrect argument type", arg.getLocation());
				driver.error("This is the expected type", param.getLocation());
				return -1;
			}
			if (param.isNatural() && !arg.isNatural() && !Impure) {
				driver.error("This expression does not satisfy the natural number requirement", arg.getLocation());
			}
			if (param.getType() == ExpType.Numeric && param.getTupleSize() != arg.evaluate(null, 0)) {
				if (param.getTupleSize() == 1) {
					driver.error("This argument should be scalar", arg.getLocation());
				} else {
					driver.error("This argument should be a vector", arg.getLocation());
					driver.error("This is the expected type", param.getLocation());
				}
				return -1;
			}
			if (arg.getLocality() != Locality.PureLocal && arg.getLocality() != Locality.PureNonLocal && param.getType() == ExpType.Numeric && !param.isNatural() && !Impure && checkNumber) {
				driver.error("This expression does not satisfy the number parameter requirement", arg.getLocation());
				return -1;
			}
		}

		if (count < expect) {
			driver.error("Too many arguments", args.getChild(count).getLocation());
			return -1;
		}

		return size;
	}

	public ASTExpression constCopy(String entropy) {
        switch (type) {
            case Numeric -> {
                double[] data = new double[tupleSize];
                boolean natural = this.natural;
                int valCount = definition.getExp().evaluate(data, tupleSize);
                if (valCount != tupleSize) {
                    driver.error("Unexpected compile error", getLocation());
                }
                ASTReal top = new ASTReal(driver, data[0], definition.getExp().getLocation());
                top.setText(entropy);
                ASTExpression list = top;
                for (int i = 1; i < valCount; i++) {
                    ASTReal next = new ASTReal(driver, data[i], getLocation());
                    list = list.append(next);
                }
                list.setNatural(natural);
                list.setLocality(locality);
                return list;
            }
            case Mod -> {
                ASTModification ret;
                if (definition.getExp() instanceof ASTModification) {
                    ret = new ASTModification(definition.driver, (ASTModification) definition.getExp(), getLocation());
                } else {
                    ret = new ASTModification(definition.driver, definition.getChildChange(), getLocation());
                }
                ret.setLocality(locality);
                return ret;
            }
            case Rule -> {
                if (definition.getExp() instanceof ASTRuleSpecifier r) {
                    ASTRuleSpecifier ret = new ASTRuleSpecifier(definition.driver, r.getShapeType(), entropy, null, null, getLocation());
                    ret.grab(r);
                    ret.setLocality(locality);
                    return ret;
                } else {
                    driver.error("Internal error computing bound rule specifier", getLocation());
                }
            }
            default -> {
            }
        }
		return null;
	}
}
