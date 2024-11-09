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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Case;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CaseMap;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CaseRange;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// astreplacement.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
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

@Getter
public class ASTSwitch extends ASTReplacement {
	private final List<Case> cases = new ArrayList<>();
	private final CaseMap<ASTRepContainer> caseMap = new CaseMap<>();
	private final ASTRepContainer elseBody;
	private ASTExpression switchExp;
	
	public ASTSwitch(CFDGSystem system, ASTWhere where, ASTExpression switchExp) {
		super(system, where, null, RepElemType.empty);
		elseBody = new ASTRepContainer(system, where);
		this.switchExp = switchExp;
	}

    @Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		super.compile(builder, phase);
		switchExp = ASTExpression.compile(builder, phase, switchExp);
		List<Case> tempCases = new ArrayList<>();
		for (Case c : cases) {
			c.container().compile(builder, phase, null, null);
			tempCases.add(new Case(ASTExpression.compile(builder, phase, c.expression()), c.container()));
		}
		cases.clear();
		cases.addAll(tempCases);
		elseBody.compile(builder, phase, null, null);

		if (switchExp == null) {
			system.error("Switch selector missing", getWhere());
			return;
		}

        switch (phase) {
            case TypeCheck -> {
				if (switchExp.getType() != ExpType.Numeric || switchExp.evaluate(builder, null, 0) != 1) {
					system.error("Switch selector must be a numeric scalar", switchExp.getWhere());
				}

				for (Case c : cases) {
					final ASTExpression caseValue = c.expression();
					if (caseValue == null) {
						system.error("Case value missing", getWhere());
						return;
					}
					final ASTRepContainer caseBody = c.container();
					long low = 0;
					long high = 0;
					try {
						final double[] value = new double[2];
						if (caseValue instanceof ASTFunction func && func.getFuncType() == FuncType.RandOp) {
							// The term is a range, get the bounds
							if (func.getArguments().evaluate(builder, value, 2) != 2) {
								system.error("Case range cannot be evaluated", func.getWhere());
								continue;
							} else {
								low = (long) Math.floor(value[0]);
								high = (long) Math.floor(value[1]);
								if (high <= low) {
									system.error("Case range is reversed", func.getWhere());
									continue;
								}
							}
						} else {
							// Not a range, must be a single value
							if (caseValue.evaluate(builder, value, 1) != 1) {
								system.error("Case value cannot be evaluated", caseValue.getWhere());
								continue;
							} else {
								low = high = (long) Math.floor(value[0]);
							}
						}

						final CaseRange range = new CaseRange(low, high);
						if (caseMap.count(range) > 0) {
							system.error("Case value already in use", caseValue.getWhere());
						} else {
							caseMap.put(range, caseBody);
						}
					} catch (CFDGDeferUntilRuntimeException e) {
						system.error("Case expression is not constant", caseValue.getWhere());
					}
				}
			}
            case Simplify -> switchExp = ASTExpression.simplify(builder, switchExp);
            default -> {
            }
        }
	}

	@Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		final double[] caveValue = new double[1];
		if (switchExp.evaluate(builder, renderer, caveValue, 1) != 1) {
			system.error("Error evaluating switch selector", getWhere());
			return;
		}

		final long i = (long) Math.floor(caveValue[0]);
		final CaseRange range = new CaseRange(i, i);

		final ASTRepContainer caseBody = caseMap.get(range);
		if (caseBody != null) {
			caseBody.traverse(builder, renderer, parent, tr, false);
		} else {
			elseBody.traverse(builder, renderer, parent, tr, false);
		}
	}
	
	public void unify() {
		if (elseBody.getPathOp() != getPathOp()) {
			setPathOp(PathOp.UNKNOWN);
		}
		for (Case c : cases) {
			if (c.container().getPathOp() != getPathOp()) {
				setPathOp(PathOp.UNKNOWN);
			}
		}
	}

	public void appendCase(ASTExpression caseVal, ASTRepContainer caseBody) {
		cases.add(new Case(caseVal, caseBody));
	}
}
