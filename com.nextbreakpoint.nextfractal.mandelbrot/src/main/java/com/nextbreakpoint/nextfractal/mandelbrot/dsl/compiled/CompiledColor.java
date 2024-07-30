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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledPalette;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.sun.source.tree.BlockTree;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.List;

@Getter
public class CompiledColor {
	private final Token location;
	private final Collection<Variable> colorVariables;
	private final Collection<Variable> stateVariables;
	@Setter
    private float[] backgroundColor;
	@Setter
    private boolean julia;
	@Setter
    private List<CompiledRule> rules;
	@Setter
    private List<CompiledPalette> palettes;
	@Getter
    @Setter
    private CompiledColorInt init;

	public CompiledColor(Collection<Variable> colorVariables, Collection<Variable> stateVariables, Token location) {
		this.location = location; 
		this.colorVariables = colorVariables;
		this.stateVariables = stateVariables;
	}
}
