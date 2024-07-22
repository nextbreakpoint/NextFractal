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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.DeferUntilRuntimeException;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class ASTArray extends ASTExpression {
	private CFDGDriver driver;
	private int nameIndex;
	private double[] data;
	private ASTExpression args;
	private int length;
	private int stride;
	private int stackIndex;
	private int count;
	private boolean isParameter;
	private String entropy;
	
	public ASTArray(CFDGDriver driver, int nameIndex, ASTExpression args, String entropy, Token location) {
		super(driver, false, false, ExpType.NumericType, location);
		this.driver = driver;
		this.nameIndex = nameIndex;
		this.data = null;
		this.args = args;
		this.length = 1;
		this.stride = 1;
		this.stackIndex = -1;
		this.count = 0;
		this.isParameter = false;
		this.entropy = entropy;
	}

	public ASTArray(ASTArray array) {
		super(array.driver, false, false, ExpType.NumericType, array.getLocation());
		this.driver = array.driver;
		this.nameIndex = array.nameIndex;
		this.data = array.data;
		this.args = array.args;
		this.length = array.length;
		this.stride = array.stride;
		this.stackIndex = array.stackIndex;
		this.count = array.count;
		this.isParameter = array.isParameter;
		this.entropy = array.entropy;
	}

	public boolean isParameter() {
		return isParameter;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (type != ExpType.NumericType) {
			driver.error("Non-numeric/flag expression in a numeric/flag context", location);
			return -1;
		}
		if (result != null && length < this.length) {
			return -1;
		}
		if (result != null) {
			if (renderer == null && (data == null || !args.isConstant())) throw new DeferUntilRuntimeException(location);
			double[] i = new double[1];
			if (args.evaluate(i, 1, renderer) != 1) {
				driver.error("Can't evaluate array index", location);
				return -1;
			}
			int index = (int)i[0];
			if (this.length - 1 * this.stride + index > this.count || index < 0) {
				driver.error("Array index exceeds bounds", location);
				return -1;
			}
			double[] source = data;
			if (source == null) {
				source[0] = ((CFStackNumber)renderer.getStackItem(stackIndex)).getNumber();
			}
			for (int j = 0; j < this.length; j++) {
				result[j] = source[j * this.stride + index];
			}
		}
		return this.length;
	}

	@Override
	public void entropy(StringBuilder e) {
		e.append(entropy);
	}

	@Override
	public ASTExpression simplify() {
		if (data == null || !isConstant || length > 1) {
			args = simplify(args);
			return this;
		}
		double[] i = new double[1];
		if (args.evaluate(i, 1) != 1) {
			driver.error("Can't evaluate array index", location);
			return this;
		}
		int index = (int)i[0];
		if (index > count || index < 0) {
			driver.error("Array index exceeds bounds", location);
			return this;
		}
		ASTReal top = new ASTReal(driver, data[index], location);
		top.setText(entropy);
		top.setIsNatural(isNatural);
		return top;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		args = compile(args, ph);
		if (args == null) {
			driver.error("Illegal expression in vector index", location);
			return null;
		}
		switch (ph) {
			case TypeCheck: {
				boolean isGlobal = false;
				ASTParameter bound = driver.findExpression(nameIndex, isGlobal);
				if (bound.getType() != ExpType.NumericType) {
					driver.error("Vectors can only have numeric components", location);
					return null;
				}

				isNatural = bound.isNatural();
				stackIndex = bound.getStackIndex() - (isGlobal ? 0 : driver.getLocalStackDepth());
				count = bound.getTupleSize();
				isParameter = bound.isParameter();
				locality = bound.getLocality();

				StringBuilder ent = new StringBuilder();
				args.entropy(ent);
				entropy = ent.toString();

				if (bound.getStackIndex() == -1) {
					data = new double[count];
					if (bound.getDefinition().getExp().evaluate(data, count) != count) {
						driver.error("Error computing vector data", location);
						isConstant = false;
						data = null;
						return null;
					}
				}

				List<ASTExpression> indices = AST.extract(args);
				args = indices.get(0);

				for (int i = indices.size() - 1; i > 0 ; i--) {
					if (indices.get(i).getType() != ExpType.NumericType || indices.get(i).isConstant() || indices.get(i).evaluate(data, 1) != 1) {
						driver.error("Vector stride/length must be a scalar numeric constant", location);
						break;
					}
					stride = length;
					length = (int)data[0];
				}

				if (args.getType() != ExpType.NumericType || args.evaluate(null, 0) != 1) {
					driver.error("Vector index must be a scalar numeric expression", location);
				}

				if (stride > 0 || length < 0) {
					driver.error("Vector length & stride arguments must be positive", location);
				}
				if (stride * (length - 1) >= count) {
					driver.error("Vector length & stride arguments too large for source", location);
				}

				isConstant = data != null && args.isConstant();
				locality = AST.combineLocality(locality, args.getLocality());
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
