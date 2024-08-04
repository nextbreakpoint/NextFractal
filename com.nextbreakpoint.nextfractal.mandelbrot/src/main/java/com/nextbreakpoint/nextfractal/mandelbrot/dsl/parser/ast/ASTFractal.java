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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFractal;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.Stack;

public class ASTFractal extends ASTObject {
	private final ASTScope stateVars = new ASTScope();
	private final Stack<ASTScope> orbitVars = new Stack<>();
	private final Stack<ASTScope> colorVars = new Stack<>();
	@Setter
    @Getter
    private ASTOrbit orbit;
	@Setter
    @Getter
    private ASTColor color;

	public ASTFractal(Token location) {
		super(location);
		orbitVars.push(new ASTScope());
		colorVars.push(new ASTScope());
		registerOrbitVariable("x", false, false, location);
		registerOrbitVariable("w", false, false, location);
		registerOrbitVariable("n", true, false, location);
	}

    public void registerStateVariable(String varName, boolean real, Token location) {
		VariableDeclaration variable = orbitVars.peek().getVariable(varName);
		if (variable == null) {
			registerOrbitVariable(varName, real, true, location);
		} else if (variable.real() != real) {
			throw new ASTException("Variable already defined with different type: " + location.getText(), new DSLToken(location, toString()));
		}
		if (stateVars.getVariable(varName) == null) {
			variable = orbitVars.peek().getVariable(varName);
			stateVars.putVariable(varName, variable);
		}
	}

	public void unregisterStateVariable(String varName) {
		stateVars.deleteVariable(varName);
	}

	public void registerOrbitVariable(String name, boolean real, boolean create, Token location) {
		orbitVars.peek().registerVariable(name, real, create, location);
	}

	public void registerColorVariable(String name, boolean real, boolean create, Token location) {
		colorVars.peek().registerVariable(name, real, create, location);
	}

	public void unregisterOrbitVariable(String name) {
		orbitVars.peek().deleteVariable(name);
	}

	public void unregisterColorVariable(String name) {
		colorVars.peek().deleteVariable(name);
	}

	public VariableDeclaration getOrbitVariable(String name, Token location) {
		VariableDeclaration var = orbitVars.peek().getVariable(name);
		if (var == null) {
			throw new ASTException("Variable not defined: " + location.getText(), new DSLToken(location, toString()));
		}
		return var;
	}

	public VariableDeclaration getColorVariable(String name, Token location) {
		VariableDeclaration var = colorVars.peek().getVariable(name);
		if (var == null) {
			var = orbitVars.peek().getVariable(name);
			if (var == null) {
				throw new ASTException("Variable not defined: " + location.getText(), new DSLToken(location, toString()));
			}
		}
		return var;
	}

	public Collection<VariableDeclaration> getStateVariables() {
		return stateVars.values();
	}

	public Collection<VariableDeclaration> getOrbitVariables() {
		return orbitVars.peek().values();
	}

	public Collection<VariableDeclaration> getColorVariables() {
		return colorVars.peek().values();
	}

	public String toString() {
        return "orbit = {" + orbit + "},color = {" + color + "}";
	}

	public void pushOrbitScope() {
		ASTScope astScope = new ASTScope();
		astScope.copy(orbitVars.peek());
		orbitVars.push(astScope);
	}

	public void popOrbitScope() {
		orbitVars.pop();
	}

	public void pushColorScope() {
		ASTScope astScope = new ASTScope();
		astScope.copy(colorVars.peek());
		colorVars.push(astScope);
	}

	public void popColorScope() {
		colorVars.pop();
	}

	public DSLFractal compile() throws DSLParserException {
		final ASTVariables variables = new ASTVariables(orbitVars.peek(), colorVars.peek(), stateVars);
        return new DSLFractal(orbit.compile(variables), color.compile(variables));
	}
}
