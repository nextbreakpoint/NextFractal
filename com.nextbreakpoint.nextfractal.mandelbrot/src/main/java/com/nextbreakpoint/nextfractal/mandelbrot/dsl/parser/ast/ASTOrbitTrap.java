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

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ASTOrbitTrap extends ASTObject {
	private final String name;
	private final ASTNumber center;
	private final List<ASTOrbitTrapOp> operators = new ArrayList<>();

	public ASTOrbitTrap(Token location, String name, ASTNumber center) {
		super(location);
		this.center = center;
		this.name = name;
	}

    public void addOperator(ASTOrbitTrapOp operator) {
		if ((operator.getOp().equals("MOVETO") || operator.getOp().equals("MOVETOREL")) && !operators.isEmpty()) {
			throw new ASTException("Only one initial MOVETO or MOVETOREL operator is allowed", operator.getLocation());
		}
		operators.add(operator);
	}

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name = ");
		builder.append(name);
		builder.append(",center = ");
		builder.append(center);
		builder.append(",operators = [");
		for (int i = 0; i < operators.size(); i++) {
			ASTOrbitTrapOp statement = operators.get(i);
			builder.append("{");
			builder.append(statement);
			builder.append("}");
			if (i < operators.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public DSLTrap resolve(ASTResolver resolver) {
		return resolver.resolve(this);
	}
}
