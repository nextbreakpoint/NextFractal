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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledColorExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledExpression;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPaletteElement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrapOp;

public interface ASTExpressionCompiler {
	CompiledExpression compile(ASTNumber number);

	CompiledExpression compile(ASTFunction function);

	CompiledExpression compile(ASTOperator operator);

	CompiledExpression compile(ASTParen paren);

	CompiledExpression compile(ASTVariable variable);

	CompiledCondition compile(ASTConditionCompareOp compareOp);

	CompiledCondition compile(ASTConditionLogicOp logicOp);

	CompiledCondition compile(ASTConditionTrap trap);

	CompiledCondition compile(ASTConditionJulia condition);

	CompiledCondition compile(ASTConditionParen condition);

	CompiledCondition compile(ASTConditionNeg condition);

	CompiledCondition compile(ASTRuleLogicOp logicOp);

	CompiledCondition compile(ASTRuleCompareOp compareOp);

	CompiledColorExpression compile(ASTColorPalette palette);

	CompiledColorExpression compile(ASTColorComponent component);

	CompiledStatement compile(ASTConditionalStatement statement);

	CompiledStatement compile(ASTAssignStatement statement);

	CompiledStatement compile(ASTStopStatement statement);

	CompiledTrap compile(ASTOrbitTrap astOrbitTrap);

	CompiledTrapOp compile(ASTOrbitTrapOp astOrbitTrapOp);

	CompiledPalette compile(ASTPalette astPalette);

	CompiledPaletteElement compile(ASTPaletteElement astPaletteElement);

	CompiledExpression compile(ASTConditionalExpression astConditionalExpression);
}
