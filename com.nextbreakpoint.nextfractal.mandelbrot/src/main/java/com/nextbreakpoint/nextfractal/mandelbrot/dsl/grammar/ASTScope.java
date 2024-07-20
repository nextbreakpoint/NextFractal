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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ASTScope {
	private Map<String, CompilerVariable> vars = new HashMap<>();

	public Map<String, CompilerVariable> getVariables() {
		return vars;
	}

	public CompilerVariable getVariable(String name) {
		return vars.get(name);
	}
	
	public void putVariable(String varName, CompilerVariable variable) {
		vars.put(varName, variable);
	}

	public void registerVariable(String name, boolean real, boolean create, Token location) {
		CompilerVariable var = vars.get(name);
		if (var == null) {
			var = new CompilerVariable(name, real, create);
			vars.put(var.getName(), var);
		}
	}

	public Collection<CompilerVariable> values() {
		return vars.values();
	}

	public void copy(ASTScope scope) {
		vars.putAll(scope.getVariables());
	}

	public void deleteVariable(String varName) {
		vars.remove(varName);
	}
}
