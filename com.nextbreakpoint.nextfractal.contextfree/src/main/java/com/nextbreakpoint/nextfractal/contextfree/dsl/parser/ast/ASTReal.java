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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

@Getter
public class ASTReal extends ASTExpression {
	private final double value;
	@Setter
    private String text;

	public ASTReal(CFDGDriver driver, double value, Token location) {
		super(driver, true, false, ExpType.Numeric, location);
		this.value = value;
		natural = Math.floor(value) == value && value >= 0 && value < 9007199254740992.0;
		locality = Locality.PureLocal;
	}

	public ASTReal(CFDGDriver driver, String text, boolean negative, Token location) {
		super(driver, true, false, ExpType.Numeric, location);
		if (negative) {
			this.text = "-" + text;
			this.value = Double.parseDouble(text);
		} else {
			this.text = text;
			this.value = Double.parseDouble(text);
		}
		natural = Math.floor(value) == value && value >= 0 && value < 9007199254740992.0;
		locality = Locality.PureLocal;
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
