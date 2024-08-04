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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOp;

public interface ASTCompiler {
	DSLExpression compile(ASTNumber number);

	DSLExpression compile(ASTFunction function);

	DSLExpression compile(ASTOperator operator);

	DSLExpression compile(ASTParen paren);

	DSLExpression compile(ASTVariable variable);

	DSLExpression compile(ASTConditionalExpression conditional);

	DSLCondition compile(ASTConditionCompareOp condition);

	DSLCondition compile(ASTConditionLogicOp condition);

	DSLCondition compile(ASTConditionTrap condition);

	DSLCondition compile(ASTConditionJulia condition);

	DSLCondition compile(ASTConditionParen condition);

	DSLCondition compile(ASTConditionNeg condition);

	DSLCondition compile(ASTRuleLogicOp operator);

	DSLCondition compile(ASTRuleCompareOp operator);

	DSLColorExpression compile(ASTColorPalette palette);

	DSLColorExpression compile(ASTColorComponent component);

	DSLStatement compile(ASTConditionalStatement statement);

	DSLStatement compile(ASTAssignStatement statement);

	DSLStatement compile(ASTStopStatement statement);

	DSLTrap compile(ASTOrbitTrap orbitTrap);

	DSLTrapOp compile(ASTOrbitTrapOp orbitTrapOp);

	DSLPalette compile(ASTPalette palette);

	DSLPaletteElement compile(ASTPaletteElement paletteElement);
}
