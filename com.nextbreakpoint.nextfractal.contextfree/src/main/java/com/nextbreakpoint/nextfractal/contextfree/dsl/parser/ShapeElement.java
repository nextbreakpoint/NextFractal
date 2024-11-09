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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

class ShapeElement {
	@Getter
	private final ASTWhere where;
	@Setter
	@Getter
	private List<ASTParameter> parameters = new ArrayList<>();
	@Setter
	@Getter
    private String name;
	@Setter
	@Getter
    private boolean shape;
	@Setter
	@Getter
    private ShapeType shapeType;
	@Setter
	@Getter
    private int argSize;
	@Setter
	@Getter
    private boolean shouldHaveNoParams;
	@Setter
	private boolean hasRules;

	public ShapeElement(ASTWhere where, String name) {
		this.where = where;
		this.name = name;
		this.hasRules = false;
		this.shape = false;
		this.shapeType = ShapeType.NewShape;
		this.argSize = 0;
		this.shouldHaveNoParams = false;
	}

    public boolean hasRules() {
		return hasRules;
	}

	public ASTWhere getFirstUse() {
		return where;
	}
}
