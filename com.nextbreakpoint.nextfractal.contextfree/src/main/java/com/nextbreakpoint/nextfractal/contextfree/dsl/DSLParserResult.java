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
package com.nextbreakpoint.nextfractal.contextfree.dsl;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDG;
import com.nextbreakpoint.nextfractal.core.common.SourceError;

import java.util.List;

public class DSLParserResult {
	private CFDG cfdg;
	private Type type;
	private String source;
	private List<SourceError> errors;

	public DSLParserResult(CFDG cfdg, Type type, String source, List<SourceError> errors) {
		this.cfdg = cfdg;
		this.type = type;
		this.source = source;
		this.errors = errors;
	}

	public CFDG getCFDG() {
		return cfdg;
	}

	public String getSource() {
		return source;
	}

	public List<SourceError> getErrors() {
		return errors;
	}

	public Type getType() {
		return type;
	}

	public enum Type {
		INTERPRETER
	}
}
