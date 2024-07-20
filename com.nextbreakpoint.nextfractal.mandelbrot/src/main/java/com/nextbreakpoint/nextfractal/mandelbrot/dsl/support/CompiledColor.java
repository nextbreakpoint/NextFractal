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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.support;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.List;

public class CompiledColor {
	private Collection<CompilerVariable> colorVariables;
	private Collection<CompilerVariable> stateVariables;
	private float[] backgroundColor;
	private boolean julia;
	private List<CompiledRule> rules;
	private List<CompiledPalette> palettes;
	private List<CompiledStatement> initStatements;
	private Token location;
	
	public CompiledColor(Collection<CompilerVariable> colorVariables, Collection<CompilerVariable> stateVariables, Token location) {
		this.location = location; 
		this.colorVariables = colorVariables;
		this.stateVariables = stateVariables;
	}

	public Collection<CompilerVariable> getColorVariables() {
		return colorVariables;
	}

	public Collection<CompilerVariable> getStateVariables() {
		return stateVariables;
	}

	public boolean isJulia() {
		return julia;
	}

	public void setJulia(boolean julia) {
		this.julia = julia;
	}

	public float[] getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(float[] backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public List<CompiledRule> getRules() {
		return rules;
	}

	public void setRules(List<CompiledRule> rules) {
		this.rules = rules;
	}

	public List<CompiledPalette> getPalettes() {
		return palettes;
	}

	public void setPalettes(List<CompiledPalette> palettes) {
		this.palettes = palettes;
	}

	public List<CompiledStatement> getInitStatements() {
		return initStatements;
	}

	public void setInitStatements(List<CompiledStatement> initStatements) {
		this.initStatements = initStatements;
	}

	public Token getLocation() {
		return location;
	}
}
