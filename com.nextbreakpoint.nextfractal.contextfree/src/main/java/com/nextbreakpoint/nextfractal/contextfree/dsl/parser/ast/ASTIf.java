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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

public class ASTIf extends ASTReplacement {
	private ASTExpression condition;
	@Getter
    private final ASTRepContainer thenBody;
	@Getter
    private final ASTRepContainer elseBody;
	
	public ASTIf(Token token, CFDGDriver driver, ASTExpression condition) {
		super(token, driver, null, RepElemType.empty);
		thenBody = new ASTRepContainer(token, driver);
		elseBody = new ASTRepContainer(token, driver);
		this.condition = condition;
	}

    @Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		condition = compile(condition, ph);
		thenBody.compile(ph, null, null);
		elseBody.compile(ph, null, null);

        switch (ph) {
            case TypeCheck -> {
                if (condition.getType() != ExpType.Numeric || condition.evaluate(null, 0) != 1) {
                    driver.error("If condition must be a numeric scalar", condition.getToken());
                }
            }
            case Simplify -> condition = simplify(condition);
            default -> {
            }
        }
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		double[] cond = new double[1];
		if (condition.evaluate(cond, 1, renderer) != 1) {
			driver.error("Error evaluating if condition", getToken());
			return;
		}
		if (cond[0] != 0) {
			thenBody.traverse(parent, tr, renderer, false);
		} else {
			elseBody.traverse(parent, tr, renderer, false);
		}
	}
}
