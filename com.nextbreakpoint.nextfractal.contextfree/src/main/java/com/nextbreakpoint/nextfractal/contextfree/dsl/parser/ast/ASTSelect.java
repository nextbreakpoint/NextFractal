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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class ASTSelect extends ASTExpression {
	private static final int NOT_CACHED = -1;
	private List<ASTExpression> arguments;
	private ASTExpression selector;
	private int tupleSize;
	private int indexCache;
	private String entropy;
	private boolean isSelect;

	public ASTSelect(CFDGDriver driver, ASTExpression arguments, boolean asIf, Token location) {
		super(driver, location);
		tupleSize = -1;
		indexCache = NOT_CACHED;
		selector = arguments;
		isConstant = false;
		isSelect = asIf;
		if (selector == null || selector.size() < 3) {
			driver.error("select()/if() function requires arguments", location);
		}
	}

	public boolean isSelect() {
		return isSelect;
	}

	@Override
	public CFStackRule evalArgs(CFDGRenderer renderer, CFStackRule parent) {
		if (type != ExpType.RuleType) {
			driver.error("Evaluation of a non-shape select() in a shape context", location);
		}
		return arguments.get(getIndex(renderer)).evalArgs(renderer, parent);
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (type != ExpType.NumericType) {
			driver.error("Evaluation of a non-shape select() in a numeric context", location);
			return -1;
		}

		if (result == null)
			return tupleSize;

		return arguments.get(getIndex(renderer)).evaluate(result, length, renderer);
	}

	@Override
	public void evaluate(Modification modification, boolean shapeDest, CFDGRenderer renderer) {
		if (type != ExpType.ModType) {
			driver.error("Evaluation of a non-adjustment select() in an adjustment context", location);
			return;
		}

		arguments.get(getIndex(renderer)).evaluate(modification, shapeDest, renderer);
	}

	@Override
	public void entropy(StringBuilder e) {
		e.append(entropy);
	}

	@Override
	public ASTExpression simplify() {
		if (indexCache == NOT_CACHED) {
			for (int i  = 0; i < arguments.size(); i++) {
				arguments.set(i, arguments.get(i).simplify());
			}
			selector = selector.simplify();
			return this;
		}
		ASTExpression chosenOne = arguments.get(indexCache);
		return chosenOne.simplify();
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		if (selector == null) {
			return null;
		}
		for (int i  = 0; i < arguments.size(); i++) {
			arguments.set(i, compile(arguments.get(i), ph));
		}
		selector = compile(selector, ph);

		switch (ph) {
			case TypeCheck: {
				StringBuilder e = new StringBuilder();
				selector.entropy(e);
				e.append("\u00B5\u00A2\u004A\u0074\u00A9\u00DF");
				entropy = e.toString();
				locality = selector.getLocality();

				arguments = AST.extract(selector);
				selector = arguments.get(0);
				arguments.remove(0);

				if (selector.getType() != ExpType.NumericType || selector.evaluate(null, 0) != 1) {
					driver.error("if()/select() selector must be a numeric scalar", selector.getLocation());
					return null;
				}

				if (arguments.size() < 2) {
					driver.error("if()/select() selector must have at least two arguments", selector.getLocation());
					return null;
				}

				type = arguments.get(0).getType();
				isNatural = arguments.get(0).isNatural();
				tupleSize = type == ExpType.NumericType ? arguments.get(0).evaluate(null, 0) : 1;
				for (int i = 1; i < arguments.size(); i++) {
					ASTExpression argument = arguments.get(i);
					if (type != argument.getType()) {
						driver.error("select()/if() choices must be of same type", argument.getLocation());
					} else if (type == ExpType.NumericType && tupleSize != -1 && argument.evaluate(null, 0) != tupleSize) {
						driver.error("select()/if() choices must be of same length", argument.getLocation());
						tupleSize = -1;
					}
					isNatural = isNatural && argument.isNatural();
				}

				if (selector.isConstant()) {
					indexCache = getIndex(null);
					isConstant = arguments.get(indexCache).isConstant();
					locality = arguments.get(indexCache).getLocality();
					isNatural = arguments.get(indexCache).isNatural();
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

	public int getIndex(CFDGRenderer renderer) {
		if (indexCache != NOT_CACHED) {
			return indexCache;
		}

		double select[] = new double[] { 0.0 };
		selector.evaluate(select, 1, renderer);

		if (isSelect) {
			return select[0] != 0 ? 0 : 1;
		}

		int i = (int)select[0];

		if (i >= arguments.size()) {
			return arguments.size() - 1;
		}

		return i;
	}
}
