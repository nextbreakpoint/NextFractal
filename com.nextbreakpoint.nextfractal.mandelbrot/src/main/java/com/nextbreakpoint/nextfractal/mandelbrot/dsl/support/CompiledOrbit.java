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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.support;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledCondition;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import org.antlr.v4.runtime.Token;

import java.util.Collection;
import java.util.List;

public class CompiledOrbit {
	private Number[] region;
	private Collection<CompilerVariable> orbitVariables;
	private Collection<CompilerVariable> stateVariables;
	private List<CompiledStatement> beginStatements;
	private List<CompiledStatement> loopStatements;
	private List<CompiledStatement> endStatements;
	private List<CompiledTrap> traps;
	private CompiledCondition loopCondition;
	private int loopBegin;
	private int loopEnd;
	private Token location;

	public CompiledOrbit(Collection<CompilerVariable> orbitVariables, Collection<CompilerVariable> stateVariables, Token location) {
		this.location = location;
		this.orbitVariables = orbitVariables;
		this.stateVariables = stateVariables;
	}

	public Collection<CompilerVariable> getOrbitVariables() {
		return orbitVariables;
	}

	public Collection<CompilerVariable> getStateVariables() {
		return stateVariables;
	}

	public Number[] getRegion() {
		return region;
	}

	public void setRegion(Number[] region) {
		this.region = region;
	}

	public List<CompiledStatement> getBeginStatements() {
		return beginStatements;
	}

	public void setBeginStatements(List<CompiledStatement> beginStatements) {
		this.beginStatements = beginStatements;
	}

	public List<CompiledStatement> getLoopStatements() {
		return loopStatements;
	}

	public void setLoopStatements(List<CompiledStatement> loopStatements) {
		this.loopStatements = loopStatements;
	}

	public List<CompiledStatement> getEndStatements() {
		return endStatements;
	}

	public void setEndStatements(List<CompiledStatement> endStatements) {
		this.endStatements = endStatements;
	}

	public List<CompiledTrap> getTraps() {
		return traps;
	}

	public void setTraps(List<CompiledTrap> traps) {
		this.traps = traps;
	}

	public CompiledCondition getLoopCondition() {
		return loopCondition;
	}

	public void setLoopCondition(CompiledCondition loopCondition) {
		this.loopCondition = loopCondition;
	}

	public int getLoopBegin() {
		return loopBegin;
	}

	public void setLoopBegin(int loopBegin) {
		this.loopBegin = loopBegin;
	}

	public int getLoopEnd() {
		return loopEnd;
	}

	public void setLoopEnd(int loopEnd) {
		this.loopEnd = loopEnd;
	}

	public Token getLocation() {
		return location;
	}
}
