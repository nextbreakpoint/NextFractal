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

import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.Stack;

public class ASTBuilder {
	private final Stack<ASTStatementList> stack = new Stack<>();
	@Getter
	private ASTStatementList statementList = new ASTStatementList();
	@Setter
    @Getter
    private ASTFractal fractal;
	private boolean isColorContext;

	public void setOrbit(ASTOrbit orbit) {
		fractal.setOrbit(orbit);
	}

	public void setOrbitBegin(ASTOrbitBegin orbitBegin) {
		fractal.getOrbit().setBegin(orbitBegin);
	}

	public void setOrbitEnd(ASTOrbitEnd orbitEnd) {
		fractal.getOrbit().setEnd(orbitEnd);
	}

	public void setOrbitLoop(ASTOrbitLoop orbitLoop) {
		fractal.getOrbit().setLoop(orbitLoop);
	}
	
	public void addOrbitTrap(ASTOrbitTrap orbitTrap) {
		fractal.getOrbit().addTrap(orbitTrap);
	}

	public void addBeginStatement(ASTStatement statement) {
		fractal.getOrbit().getBegin().addStatement(statement);
	}

	public void addEndStatement(ASTStatement statement) {
		fractal.getOrbit().getEnd().addStatement(statement);
	}

	public void addLoopStatement(ASTStatement statement) {
		fractal.getOrbit().getLoop().addStatement(statement);
	}

	public void addOrbitTrapOp(ASTOrbitTrapOp orbitTrapOp) {
		if (!fractal.getOrbit().getTraps().isEmpty()) {
			ASTOrbitTrap trap = fractal.getOrbit().getTraps().getLast();
			trap.addOperator(orbitTrapOp);
		}
	}

	public void setColor(ASTColor color) {
		fractal.setColor(color);
	}

	public void setColorInit(ASTColorInit colorInit) {
		fractal.getColor().setInit(colorInit);
	}

	public void addColorStatement(ASTStatement statement) {
		fractal.getColor().getInit().addStatement(statement);
	}

	public void addPalette(ASTPalette palette) {
		fractal.getColor().addPalette(palette);
	}
	
	public void addRule(ASTRule rule) {
		fractal.getColor().addRule(rule);
	}

	public void addPaletteElement(ASTPaletteElement element) {
		if (!fractal.getColor().getPalettes().isEmpty()) {
			ASTPalette palette = fractal.getColor().getPalettes().getLast();
			palette.addElements(element);
		}
	}

    public double parseDouble(String text) {
		return Double.parseDouble(text);
	}

	public float parseFloat(String text) {
		return Float.parseFloat(text);
	}

	public int parseInt(String text) {
		return Integer.parseInt(text);
	}

	public int parseInt(String text, int base) {
		return Integer.parseInt(text, base);
	}

	public long parseLong(String text) {
		return Long.parseLong(text);
	}

	public long parseLong(String text, int base) {
		return Long.parseLong(text, base);
	}

	public void registerStateVariable(String name, boolean real, Token location) {
		fractal.registerStateVariable(name, real, location);
	}

	public void unregisterStateVariable(String name) {
		fractal.unregisterStateVariable(name);
	}

	public void unregisterVariable(String name) {
		if (isColorContext) {
			fractal.unregisterColorVariable(name);
		} else {
			fractal.unregisterOrbitVariable(name);
		}
	}

	public void registerVariable(String name, boolean real, Token location) {
		if (isColorContext) {
			fractal.registerColorVariable(name, real, true, location);
		} else {
			fractal.registerOrbitVariable(name, real, true, location);
		}
	}

	public VariableDeclaration getVariable(String name, Token location) {
		if (isColorContext) {
			return fractal.getColorVariable(name, location);
		} else {
			return fractal.getOrbitVariable(name, location);
		}
	}

	public boolean isColorContext() {
		return isColorContext;
	}

	public void setColorContext(boolean isColorContext) {
		this.isColorContext = isColorContext;
	}

	public void pushStatementList() {
		stack.push(statementList);
		statementList = new ASTStatementList();
	}

	public void popStatementList() {
		statementList = stack.pop();
	}

	public void pushScope() {
		if (isColorContext) {
			fractal.pushColorScope();
		} else {
			fractal.pushOrbitScope();
		}
	}

	public void popScope() {
		if (isColorContext) {
			fractal.popColorScope();
		} else {
			fractal.popOrbitScope();
		}
	}

    public void appendStatement(ASTStatement statement) {
		statementList.addStatement(statement);
	}

	public void addColorStatements(ASTStatementList statementList) {
		for (ASTStatement statement : statementList.getStatements()) {
			addColorStatement(statement);
		}
	}

	public void addBeginStatements(ASTStatementList statementList) {
		for (ASTStatement statement : statementList.getStatements()) {
			addBeginStatement(statement);
		}
	}

	public void addEndStatements(ASTStatementList statementList) {
		for (ASTStatement statement : statementList.getStatements()) {
			addEndStatement(statement);
		}
	}

	public void addLoopStatements(ASTStatementList statementList) {
		for (ASTStatement statement : statementList.getStatements()) {
			addLoopStatement(statement);
		}
	}
}	
