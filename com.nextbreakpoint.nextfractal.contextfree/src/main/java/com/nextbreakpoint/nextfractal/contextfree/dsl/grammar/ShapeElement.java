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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ShapeType;

import java.util.ArrayList;
import java.util.List;

class ShapeElement {
	private String name;
	private boolean hasRules;
	private boolean isShape;
	private ShapeType shapeType;
	private List<ASTParameter> parameters = new ArrayList<ASTParameter>();
	private int argSize;
	private boolean shouldHaveNoParams;

	public ShapeElement(String name) {
		this.name = name;
		this.hasRules = false;
		this.isShape = false;
		this.shapeType = ShapeType.NewShape;
		this.argSize = 0;
		this.shouldHaveNoParams = false;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasRules() {
		return hasRules;
	}

	public void setHasRules(boolean hasRules) {
		this.hasRules = hasRules;
	}

	public boolean isShape() {
		return isShape;
	}

	public void setIsShape(boolean isShape) {
		this.isShape = isShape;
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public List<ASTParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ASTParameter> parameters) {
		this.parameters = parameters;
	}

	public int getArgSize() {
		return argSize;
	}

	public void setArgSize(int argSize) {
		this.argSize = argSize;
	}

	public boolean isShouldHaveNoParams() {
		return shouldHaveNoParams;
	}

	public void setShouldHaveNoParams(boolean shouldHaveNoParams) {
		this.shouldHaveNoParams = shouldHaveNoParams;
	}
}
