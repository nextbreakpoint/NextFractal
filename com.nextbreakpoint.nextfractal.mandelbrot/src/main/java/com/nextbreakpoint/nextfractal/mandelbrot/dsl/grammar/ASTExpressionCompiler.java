/*
 * NextFractal 2.3.1
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
	public CompiledExpression compile(ASTNumber number);

	public CompiledExpression compile(ASTFunction function);

	public CompiledExpression compile(ASTOperator operator);

	public CompiledExpression compile(ASTParen paren);

	public CompiledExpression compile(ASTVariable variable);

	public CompiledCondition compile(ASTConditionCompareOp compareOp);

	public CompiledCondition compile(ASTConditionLogicOp logicOp);

	public CompiledCondition compile(ASTConditionTrap trap);

	public CompiledCondition compile(ASTConditionJulia condition);

	public CompiledCondition compile(ASTConditionParen condition);

	public CompiledCondition compile(ASTConditionNeg condition);

	public CompiledCondition compile(ASTRuleLogicOp logicOp);

	public CompiledCondition compile(ASTRuleCompareOp compareOp);

	public CompiledColorExpression compile(ASTColorPalette palette);

	public CompiledColorExpression compile(ASTColorComponent component);

	public CompiledStatement compile(ASTConditionalStatement statement);

	public CompiledStatement compile(ASTAssignStatement statement);

	public CompiledStatement compile(ASTStopStatement statement);

	public CompiledTrap compile(ASTOrbitTrap astOrbitTrap);

	public CompiledTrapOp compile(ASTOrbitTrapOp astOrbitTrapOp);

	public CompiledPalette compile(ASTPalette astPalette);

	public CompiledPaletteElement compile(ASTPaletteElement astPaletteElement);

	public CompiledExpression compile(ASTConditionalExpression astConditionalExpression);
}
