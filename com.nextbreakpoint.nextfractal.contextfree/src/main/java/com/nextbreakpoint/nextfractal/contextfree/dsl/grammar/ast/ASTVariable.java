/*
 * NextFractal 2.3.0
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
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFStackModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.DeferUntilRuntimeException;
import org.antlr.v4.runtime.Token;

public class ASTVariable extends ASTExpression {
	private String text;
	private int stringIndex;
	private int stackIndex;
	private int count;
	private boolean isParameter;
	private CFDGDriver driver;

	public ASTVariable(CFDGDriver driver, int stringIndex, String text, Token location) {
		super(driver, location);
		this.driver = driver;
		this.stringIndex = stringIndex;
		this.isParameter = false;
		this.stackIndex = 0;
		this.text = text;
		this.count = 0;
	}

	public String getText() {
		return text;
	}

	public int getStringIndex() {
		return stringIndex;
	}

	public int getStackIndex() {
		return stackIndex;
	}

	public int getCount() {
		return count;
	}

	public boolean isParameter() {
		return isParameter;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (type != ExpType.NumericType) {
            driver.error("Non-numeric variable in a numeric context", location);
            return -1;
        }
		if (result != null && length < count) {
			return -1;
		}
        if (result != null) {
            if (renderer == null) throw new DeferUntilRuntimeException(location);
            for (int i = 0; i < count; ++i) {
				result[i] = ((CFStackNumber)renderer.getStackItem(stackIndex + i)).getNumber();
            }
        }
        return count;
	}

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		if (type != ExpType.ModType) {
            driver.error("Non-adjustment variable referenced in an adjustment context", location);
        }
		if (renderer == null) throw new DeferUntilRuntimeException(location);
        Modification mod = ((CFStackModification)renderer.getStackItem(stackIndex)).getModification();
        if (shapeDest) {
        	result.concat(mod);
        } else {
        	if (result.merge(mod)) {
    			renderer.colorConflict(getLocation());
        	}
        }
	}

	@Override
	public void entropy(StringBuilder e) {
		e.append(text);
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		switch (ph) {
			case TypeCheck: {
				boolean isGlobal = false;
				ASTParameter bound = driver.findExpression(stringIndex, isGlobal);
				if (bound == null) {
					driver.error("internal error", location);
					return null;
				}
				String name = driver.shapeToString(stringIndex);
				if (bound.getStackIndex() == -1) {
					ASTExpression ret = bound.constCopy(name);
					if (ret == null) {
						driver.error("internal error", location);
					}
					return ret;
				} else {
					if (bound.getType() == ExpType.RuleType) {
						ASTRuleSpecifier ret = new ASTRuleSpecifier(driver, stringIndex, name, location);
						ret.compile(ph);
						return ret;
					}
					count = bound.getType() == ExpType.NumericType ? bound.getTupleSize() : 1;
					stackIndex = bound.getStackIndex() - (isGlobal ? 0 : driver.getLocalStackDepth());
					type = bound.getType();
					isNatural = bound.isNatural();
					locality = bound.getLocality();
					isParameter = bound.isParameter();
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
}
