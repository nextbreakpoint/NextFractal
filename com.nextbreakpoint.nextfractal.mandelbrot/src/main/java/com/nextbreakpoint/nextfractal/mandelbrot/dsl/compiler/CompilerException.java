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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@Getter
public class CompilerException extends DSLParserException {
	private final String source;

	public CompilerException(String message, String source, ScriptError error) {
		this(message, source, List.of(error));
	}

	public CompilerException(String message, String source, List<ScriptError> errors) {
		super(message, errors);
		this.source = Objects.requireNonNull(source);
	}

	@Override
	public String toString() {
		return "%s:\n%s\n".formatted(getMessage(), source);
	}
}
