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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.PARSE;

@Getter
@Log
public class CFDGSystem {
	private final List<ScriptError> errors = new ArrayList<>();
	private boolean errorOccurred;

	public void info(String message, ASTWhere where) {
		log.log(Level.INFO, message + (where != null ? " [" + where.getLine() + ":" + where.getCharPositionInLine() + "]" : ""));
	}

	public void warning(String message, ASTWhere where) {
		log.log(Level.WARNING, message + (where != null ? " [" + where.getLine() + ":" + where.getCharPositionInLine() + "]" : ""));
	}

	public void error(String message, ASTWhere where) {
		errorOccurred = true;
		log.log(Level.WARNING, message + (where != null ? " [" + where.getLine() + ":" + where.getCharPositionInLine() + "]" : ""));
		if (where != null) {
			errors.add(makeError(where.getLine(), where.getCharPositionInLine(), where.getStartIndex(), where.getStopIndex() - where.getStartIndex(), message));
		} else {
			errors.add(makeError(0, 0, 0, 0, message));
		}
	}

	public void fail(String message) {
		errorOccurred = true;
		log.log(Level.SEVERE, message);
//		throw new RuntimeException(message);
	}

	private static ScriptError makeError(long line, long charPositionInLine, long index, long length, String message) {
		return new ScriptError(PARSE, line, charPositionInLine, index, length, message);
	}
}
