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

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSimpleImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ContextFreeLexer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ContextFreeParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ErrorStrategy;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.PARSE;

@Log
public class CFParser {
	private static final String INCLUDE_LOCATION = "include.location";

	public CFParserResult parse(String source) throws CFParserException {
		//TODO use variation from metadata
		final int variation = 0;
		return Command.of(() -> parse(source, "CFDG3", variation, ContextFreeParser::cfdg3)).execute()
				.or(() -> Command.of(() -> parse(source, "CFDG2", variation, ContextFreeParser::cfdg2)).execute())
				.orThrow(e -> e instanceof CFParserException ? (CFParserException) e : new CFParserException("Failed to parse source", List.of()))
				.optional()
				.orElseThrow();
	}

	private CFParserResult parse(String source, String version, int variation, Consumer<ContextFreeParser> callback) throws CFParserException {
		try {
			final List<ScriptError> errors = new ArrayList<>();
			final CharStream is = CharStreams.fromReader(new StringReader(source));
			final ContextFreeLexer lexer = new ContextFreeLexer(is);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final ContextFreeParser parser = new ContextFreeParser(tokens);
			final CFDG cfdg = new CFDG(new CFDGSystem(), version);
			final CFDGBuilder builder = new CFDGBuilder(cfdg, variation);
			builder.setBasePath(getIncludeDir());
			builder.setCurrentPath(getIncludeDir());
			parser.setErrorHandler(new ErrorStrategy(errors));
			parser.setBuilder(builder);
			callback.accept(parser);
			if (builder.getMaybeVersion() != null && !version.equals(builder.getMaybeVersion())) {
				throw new CFParserException("Unexpected version", errors);
			}
			errors.addAll(cfdg.getSystem().getErrors());
			if (!errors.isEmpty()) {
				throw new CFParserException("Script syntax error", errors);
			}
			return new CFParserResult(source, () -> new CFDGSimpleImage(cfdg));
		} catch (CFParserException e) {
			log.log(Level.INFO, "Can't parse script using version " + version, e);
			throw e;
		} catch (CFDGException e) {
			final long line = e.getWhere().getLine();
			final long charPositionInLine = e.getWhere().getCharPositionInLine();
			final long index = e.getWhere().getStartIndex();
			final long length = e.getWhere().getStopIndex() - e.getWhere().getStartIndex();
			final ScriptError error = new ScriptError(PARSE, line, charPositionInLine, index, length, e.getMessage());
			log.log(Level.INFO, "Can't parse script using version " + version, e);
            throw new CFParserException("Can't parse script using version " + version, List.of(error));
		} catch (Exception e) {
            final ScriptError error = new ScriptError(PARSE, 0L, 0L, 0L, 0L, e.getMessage());
            log.log(Level.INFO, "Can't parse script using version " + version, e);
			throw new CFParserException("Can't parse script using version " + version, List.of(error));
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
		log.info("CFDG include directory: " + defaultBrowserDir);
		return defaultBrowserDir;
	}
}
