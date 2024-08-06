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

public class ASTParen extends ASTExpression {
	private ASTExpression expression;
	
	public ASTParen(Token token, CFDGDriver driver, ASTExpression expression) {
		super(token, driver, expression.isConstant(), expression.isNatural(), expression.getType());
		this.expression = expression;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
        if (type != ExpType.Numeric) {
			driver.error("Non-numeric/flag expression in a numeric/flag context", getToken());
			return -1;
        }
        return expression.evaluate(result, length, renderer);
	}

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
        if (type != ExpType.Mod) {
			driver.error("Expression does not evaluate to an adjustment", getToken());
			return;
        }
		super.evaluate(result, shapeDest, renderer);
	}

	@Override
	public void entropy(StringBuilder e) {
		if (expression != null) {
			expression.entropy(e);
		}
		e.append("\u00E8\u00E9\u00F6\u007E\u001A\u00F1");
	}

	@Override
	public ASTExpression simplify() {
		return expression.simplify();
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		if (expression == null) return null;

		expression = compile(expression, ph);

        switch (ph) {
            case TypeCheck -> {
                constant = expression.isConstant();
                natural = expression.isNatural();
                locality = expression.getLocality();
                type = expression.getType();
            }
			case Simplify -> {
				// do nothing
			}
            default -> {
            }
        }
		return null;
	}

	@Override
	public CFStackRule evalArgs(CFDGRenderer renderer, CFStackRule parent) {
		if (type != ExpType.Rule) {
			driver.error("Evaluation of a non-shape expression in a shape context", getToken());
			return null;
		}
		return expression.evalArgs(renderer, parent);
	}
}
