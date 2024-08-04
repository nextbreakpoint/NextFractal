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

import org.antlr.v4.runtime.Token;

import java.util.logging.Level;

public class Logger {
    private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Logger.class.getName());

    public void error(String message, Token location) {
        logger.log(Level.WARNING, message + (location != null ? " [" + location.getLine() + ":" + location.getCharPositionInLine() + "]" : ""));
    }

    public void warning(String message, Token location) {
        logger.log(Level.WARNING, message + (location != null ? " [" + location.getLine() + ":" + location.getCharPositionInLine() + "]" : ""));
    }

    public void info(String message, Token location) {
        logger.log(Level.INFO, message + (location != null ? " [" + location.getLine() + ":" + location.getCharPositionInLine() + "]" : ""));
    }

    public void fail(String message, Token location) {
        logger.log(Level.WARNING, message + (location != null ? " [" + location.getLine() + ":" + location.getCharPositionInLine() + "]" : ""));
        throw new RuntimeException(message + (location != null ? " [" + location.getLine() + ":" + location.getCharPositionInLine() + "]" : ""));
    }
}
