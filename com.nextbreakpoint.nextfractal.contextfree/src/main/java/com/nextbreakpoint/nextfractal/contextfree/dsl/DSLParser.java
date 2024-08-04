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

import com.nextbreakpoint.nextfractal.contextfree.core.ParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGLexer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGLogger;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ErrorStrategy;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.exceptions.CFDGException;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.PARSE;

@Log
public class DSLParser {
	private static final String INCLUDE_LOCATION = "include.location";

	public DSLParserResult parse(String source) throws ParserException {
		List<ScriptError> errors = new ArrayList<>();
		CFDG cfdg = parse(source, errors);
		return new DSLParserResult(cfdg, DSLParserResult.Type.INTERPRETER, source, errors);
	}

	private CFDG parse(String source, List<ScriptError> errors) throws ParserException {
		try {
			CharStream is = CharStreams.fromReader(new StringReader(source));
			CFDGLexer lexer = new CFDGLexer(is);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			CFDGParser parser = new CFDGParser(tokens);
			parser.setDriver(new CFDGDriver());
			CFDGLogger logger = new CFDGLogger();
			parser.getDriver().setLogger(logger);
			parser.setErrorHandler(new ErrorStrategy(errors));
			parser.getDriver().setCurrentPath(getIncludeDir());
			parser.choose();
			errors.addAll(logger.getErrors());
			final CFDG cfdg = parser.getDriver().getCFDG();
			if (cfdg == null || !errors.isEmpty()) {
				throw new ParserException("Can't parse source", errors);
			}
			return cfdg;
		} catch (CFDGException e) {
            long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
            ScriptError error = new ScriptError(PARSE, line, charPositionInLine, index, length, e.getMessage());
			log.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		} catch (Exception e) {
            ScriptError error = new ScriptError(PARSE, 0L, 0L, 0L, 0L, e.getMessage());
			log.log(Level.FINE, error.toString(), e);
			errors.add(error);
			throw new ParserException("Can't parse source", errors);
		}
	}

	private String getIncludeDir() {
		String defaultBrowserDir = System.getProperty(INCLUDE_LOCATION, "[user.home]");
		String userHome = System.getProperty("user.home");
		String userDir = System.getProperty("user.dir");
		String currentDir = new File(".").getAbsoluteFile().getParent();
		defaultBrowserDir = defaultBrowserDir.replace("[current.path]", currentDir);
		defaultBrowserDir = defaultBrowserDir.replace("[user.home]", userHome);
		defaultBrowserDir = defaultBrowserDir.replace("[user.dir]", userDir);
		log.info("includeDir = " + defaultBrowserDir);
		return defaultBrowserDir;
	}
}
