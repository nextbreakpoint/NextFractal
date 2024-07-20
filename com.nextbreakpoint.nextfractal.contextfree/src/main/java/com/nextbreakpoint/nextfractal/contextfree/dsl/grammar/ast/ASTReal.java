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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.Locality;
import org.antlr.v4.runtime.Token;

public class ASTReal extends ASTExpression {
	private double value;
	private String text;

	public ASTReal(CFDGDriver driver, double value, Token location) {
		super(driver, true, false, ExpType.NumericType, location);
		this.value = value;
		isNatural = Math.floor(value) == value && value >= 0 && value < 9007199254740992.0;
		locality = Locality.PureLocal;
	}

	public ASTReal(CFDGDriver driver, String text, boolean negative, Token location) {
		super(driver, true, false, ExpType.NumericType, location);
		if (negative) {
			this.text = "-" + text;
			this.value = Double.parseDouble(text);
		} else {
			this.text = text;
			this.value = Double.parseDouble(text);
		}
		isNatural = Math.floor(value) == value && value >= 0 && value < 9007199254740992.0;
		locality = Locality.PureLocal;
	}

	public double getValue() {
		return value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@Override
	public void entropy(StringBuilder e) {
		e.append(text);
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (result != null && length < 1) {
			return -1;
		}
		if (result != null) {
			result[0] = value;
		}
		return 1;
	}
}
