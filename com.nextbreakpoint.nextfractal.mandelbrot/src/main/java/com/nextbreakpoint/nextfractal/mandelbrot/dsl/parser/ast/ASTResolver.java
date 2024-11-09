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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrapOp;

public interface ASTResolver {
	DSLExpression resolve(ASTNumber number);

	DSLExpression resolve(ASTFunction function);

	DSLExpression resolve(ASTOperator operator);

	DSLExpression resolve(ASTParen paren);

	DSLExpression resolve(ASTVariable variable);

	DSLExpression resolve(ASTConditionalExpression conditional);

	DSLCondition resolve(ASTConditionCompareOp condition);

	DSLCondition resolve(ASTConditionLogicOp condition);

	DSLCondition resolve(ASTConditionTrap condition);

	DSLCondition resolve(ASTConditionJulia condition);

	DSLCondition resolve(ASTConditionParen condition);

	DSLCondition resolve(ASTConditionNeg condition);

	DSLCondition resolve(ASTRuleLogicOp operator);

	DSLCondition resolve(ASTRuleCompareOp operator);

	DSLColorExpression resolve(ASTColorPalette palette);

	DSLColorExpression resolve(ASTColorComponent component);

	DSLStatement resolve(ASTConditionalStatement statement);

	DSLStatement resolve(ASTAssignStatement statement);

	DSLStatement resolve(ASTStopStatement statement);

	DSLTrap resolve(ASTOrbitTrap orbitTrap);

	DSLTrapOp resolve(ASTOrbitTrapOp orbitTrapOp);

	DSLPalette resolve(ASTPalette palette);

	DSLPaletteElement resolve(ASTPaletteElement paletteElement);
}
