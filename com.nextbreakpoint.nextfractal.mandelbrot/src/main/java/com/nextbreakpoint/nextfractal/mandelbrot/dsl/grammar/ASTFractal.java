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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.Stack;

public class ASTFractal extends ASTObject {
	private ASTScope stateVars = new ASTScope();
	private Stack<ASTScope> orbitVars = new Stack<>();
	private Stack<ASTScope> colorVars = new Stack<>();
	private ASTOrbit orbit;
	private ASTColor color;

	public ASTFractal(Token location) {
		super(location);
		orbitVars.push(new ASTScope());
		colorVars.push(new ASTScope());
		registerOrbitVariable("x", false, false, location);
		registerOrbitVariable("w", false, false, location);
		registerOrbitVariable("n", true, false, location);
	}

	public ASTOrbit getOrbit() {
		return orbit;
	}
	
	public void setOrbit(ASTOrbit orbit) {
		this.orbit = orbit;
	}
	
	public ASTColor getColor() {
		return color;
	}
	
	public void setColor(ASTColor color) {
		this.color = color;
	}

	public void registerStateVariable(String varName, boolean real, Token location) {
		CompilerVariable variable = orbitVars.peek().getVariable(varName);
		if (variable == null) {
			registerOrbitVariable(varName, real, true, location);
		} else if (variable.isReal() != real) {
			throw new ASTException("Variable already defined with different type: " + location.getText(), location);
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

	public CompilerVariable getOrbitVariable(String name, Token location) {
		CompilerVariable var = orbitVars.peek().getVariable(name);
		if (var == null) {
			throw new ASTException("Variable not defined: " + location.getText(), location);
		}
		return var;
	}

	public CompilerVariable getColorVariable(String name, Token location) {
		CompilerVariable var = colorVars.peek().getVariable(name);
		if (var == null) {
			var = orbitVars.peek().getVariable(name);
			if (var == null) {
				throw new ASTException("Variable not defined: " + location.getText(), location);
			}
		}
		return var;
	}

	public Collection<CompilerVariable> getStateVariables() {
		return stateVars.values();
	}

	public Collection<CompilerVariable> getOrbitVariables() {
		return orbitVars.peek().values();
	}

	public Collection<CompilerVariable> getColorVariables() {
		return colorVars.peek().values();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("orbit = {");
		builder.append(orbit);
		builder.append("},color = {");
		builder.append(color);
		builder.append("}");
		return builder.toString();
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
}
