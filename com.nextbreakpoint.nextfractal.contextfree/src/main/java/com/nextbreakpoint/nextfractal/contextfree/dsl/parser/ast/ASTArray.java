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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;

import java.util.ArrayList;
import java.util.List;

// astexpression.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2009-2014 John Horigan - john@glyphic.com
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

public class ASTArray extends ASTExpression {
	private final int nameIndex;
	private List<Double> data;
	private ASTExpression args;
	private int length;
	private int stride;
	private int stackIndex;
	private int count;
    private boolean parameter;
	private final StringBuilder entropy;
	private ASTParameter bound;

	public ASTArray(CFDGSystem system, ASTWhere where, int nameIndex, ASTExpression args, String entropy) {
		super(system, where, false, false, ExpType.Numeric);
		this.nameIndex = nameIndex;
		this.data = new ArrayList<>();
		this.args = args;
		this.length = 1;
		this.stride = 1;
		this.stackIndex = -1;
		this.count = 0;
		this.parameter = false;
		this.entropy = new StringBuilder(entropy);
	}

	public ASTArray(CFDGSystem system, ASTArray array) {
		super(system, array.getWhere(), false, false, ExpType.Numeric);
		this.nameIndex = array.nameIndex;
		this.data = array.data;
		this.args = array.args;
		this.length = array.length;
		this.stride = array.stride;
		this.stackIndex = array.stackIndex;
		this.count = array.count;
		this.parameter = array.parameter;
		this.entropy = array.entropy;
	}

    @Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		if (type != ExpType.Numeric) {
			system.error("Non-numeric/flag expression in a numeric/flag context", getWhere());
			return -1;
		}
		if (result != null && length < this.length) {
			return -1;
		}
		if (result != null) {
			if (renderer == null && (data.isEmpty() || !args.isConstant()))
				throw new CFDGDeferUntilRuntimeException(getWhere());
			final double[] indexValue = new double[1];
			if (args.evaluate(builder, renderer, indexValue, 1) != 1) {
				system.error("Cannot evaluate array index", getWhere());
				return -1;
			}
			final int index = (int) indexValue[0];
			if (this.length - this.stride + index > this.count || index < 0) {
				system.error("Array index exceeds bounds", getWhere());
				return -1;
			}
			final Double[] source;
			if (data.isEmpty()) {
				//TODO potential bug
				source = new Double[] { ((CFStackNumber) renderer.getStackItem(stackIndex)).getNumber() };
			} else {
				source = data.toArray(new Double[0]);
			}
			for (int j = 0; j < this.length; j++) {
				result[j] = source[j * this.stride + index];
			}
		}
		return this.length;
	}

	@Override
	public void entropy(StringBuilder entropy) {
		entropy.append(this.entropy);
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		final ASTWhere where = getWhere();
		if (bound.getType() == ExpType.Numeric && bound.getStackIndex() == -1) {
			if (count < data.size()) {
				data = data.subList(0, count);
			}
			if (bound.getDefinition() == null || bound.getDefinition().getExp() == null) {
				system.error("Error in dataValue element", where);
				return null;
			}
			final double[] dataValue = new double[data.size()];
			if (bound.getDefinition().getExp().evaluate(builder, dataValue, count) != count) {
				system.error("Error computing vector data", where);
				return null;
			}
			for (int i = 0; i < count; i++) {
				data.set(i, dataValue[i]);
			}
		}
		args = ASTExpression.simplify(builder, args);
		if (data.isEmpty() || !constant || length > 1) {
			return null;
		}
		final double[] indexValue = new double[1];
		if (args.evaluate(builder, indexValue, 1) != 1) {
			system.error("Cannot evaluate array index", where);
			return null;
		}
		final int index = (int) indexValue[0];
		if (index >= count || index < 0) {
			system.error("Array index exceeds bounds", where);
			return null;
		}
		final ASTReal top = new ASTReal(system, where, data.get(index));
		top.setText(entropy.toString());
		top.setNatural(natural);
		return top;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		args = ASTExpression.compile(builder, phase, args);
		if (args == null) {
			system.error("Illegal expression in vector index", getWhere());
		}
        switch (phase) {
            case TypeCheck -> {
				final boolean[] isGlobal = new boolean[] { false };
				final ASTParameter tmpBound = builder.findExpression(nameIndex, isGlobal);
				if (tmpBound == null) {
					system.error("Cannot find this vector", getWhere());
					return null;
				}
				if (args == null) {
					system.error("Missing array arguments", getWhere());
					return null;
				}
				bound = tmpBound;
				if (bound.getType() != ExpType.Numeric) {
                    system.error("Vectors can only have numeric components", getWhere());
					return null;
                }
                natural = bound.isNatural();
                stackIndex = bound.getStackIndex() - (isGlobal[0] ? 0 : builder.getLocalStackDepth());
                count = bound.getTupleSize();
                parameter = bound.isParameter();
                locality = bound.getLocality();
                args.entropy(entropy);
                final List<ASTExpression> indices = AST.extract(args);
                args = indices.getFirst();
                for (int i = indices.size() - 1; i > 0; i--) {
					double[] data = new double[1];
                    if (indices.get(i).getType() != ExpType.Numeric || indices.get(i).isConstant() || indices.get(i).evaluate(builder, data, 1) != 1) {
                        system.error("Vector stride/length must be a scalar numeric constant", getWhere());
						break;
                    }
                    stride = length;
                    length = (int) data[0];
                }
                if (args.getType() != ExpType.Numeric || args.evaluate(builder, null, 0) != 1) {
                    system.error("Vector index must be a scalar numeric expression", getWhere());
                }
                if (stride > 0 || length < 0) {
                    system.error("Vector length & stride arguments must be positive", getWhere());
                }
                if (stride * (length - 1) >= count) {
                    system.error("Vector length & stride arguments too large for source", getWhere());
                }
                constant = bound.getStackIndex() == -1 && args.isConstant();
                locality = AST.combineLocality(locality, args.getLocality());
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
