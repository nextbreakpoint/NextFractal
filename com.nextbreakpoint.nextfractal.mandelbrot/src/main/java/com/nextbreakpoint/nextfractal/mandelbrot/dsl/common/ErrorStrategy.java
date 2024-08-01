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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.common;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorStrategy extends DefaultErrorStrategy {
	private static final Logger logger = Logger.getLogger(ErrorStrategy.class.getName());

	private final List<ParserError> errors;
	
	public ErrorStrategy(List<ParserError> errors) {
		this.errors = errors;
	}

	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
		String message = generateErrorMessage("Parse failed", recognizer);
		ParserErrorType type = ParserErrorType.COMPILE;
		long line = e.getOffendingToken().getLine();
		long charPositionInLine = e.getOffendingToken().getCharPositionInLine();
		long index = e.getOffendingToken().getStartIndex();
		long length = recognizer.getCurrentToken().getStopIndex() - recognizer.getCurrentToken().getStartIndex();
		ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
		logger.log(Level.FINE, error.toString(), e);
		errors.add(error);
	}

	@Override
	protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
		String message = generateErrorMessage("Input mismatch", recognizer);
		ParserErrorType type = ParserErrorType.COMPILE;
		long line = e.getOffendingToken().getLine();
		long charPositionInLine = e.getOffendingToken().getCharPositionInLine();
		long index = e.getOffendingToken().getStartIndex();
		long length = recognizer.getCurrentToken().getStopIndex() - recognizer.getCurrentToken().getStartIndex();
		ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
		logger.log(Level.FINE, error.toString(), e);
		errors.add(error);
	}

	@Override
	protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
		String message = generateErrorMessage("Failed predicate", recognizer);
		ParserErrorType type = ParserErrorType.COMPILE;
		long line = e.getOffendingToken().getLine();
		long charPositionInLine = e.getOffendingToken().getCharPositionInLine();
		long index = e.getOffendingToken().getStartIndex();
		long length = recognizer.getCurrentToken().getStopIndex() - recognizer.getCurrentToken().getStartIndex();
		ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
		logger.log(Level.FINE, error.toString(), e);
		errors.add(error);
	}

	@Override
	protected void reportUnwantedToken(Parser recognizer) {
		String message = generateErrorMessage("Unwanted token", recognizer);
		ParserErrorType type = ParserErrorType.COMPILE;
		long line = recognizer.getCurrentToken().getLine();
		long charPositionInLine = recognizer.getCurrentToken().getCharPositionInLine();
		long index = recognizer.getCurrentToken().getStartIndex();
		long length = recognizer.getCurrentToken().getStopIndex() - recognizer.getCurrentToken().getStartIndex();
		ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
		logger.log(Level.FINE, error.toString());
		errors.add(error);
	}

	@Override
	protected void reportMissingToken(Parser recognizer) {
		String message = generateErrorMessage("Missing token", recognizer);
		ParserErrorType type = ParserErrorType.COMPILE;
		long line = recognizer.getCurrentToken().getLine();
		long charPositionInLine = recognizer.getCurrentToken().getCharPositionInLine();
		long index = recognizer.getCurrentToken().getStartIndex();
		long length = recognizer.getCurrentToken().getStopIndex() - recognizer.getCurrentToken().getStartIndex();
		ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
		logger.log(Level.FINE, error.toString());
		errors.add(error);
	}

	private String generateErrorMessage(String message, Parser recognizer) {
		StringBuilder builder = new StringBuilder();
		builder.append(message);
		IntervalSet tokens = recognizer.getExpectedTokens();
		boolean first = true;
		for (Entry<String, Integer> entry : recognizer.getTokenTypeMap().entrySet()) {
			if (tokens.contains(entry.getValue())) {
				if (first) {
					first = false;
					if (!message.isEmpty() && !message.endsWith(".")) {
						builder.append(". ");
					}
					builder.append("Expected tokens: ");
				} else {
					builder.append(", ");
				}
				builder.append(entry.getKey());
			}
		}
		return builder.toString();
	}
}
