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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ErrorStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotLexer;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.MandelbrotParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.PARSE;

@Log
public class DSLParser {
	public DSLParserResult parse(String source) throws DSLParserException {
		final List<ScriptError> errors = new ArrayList<>();
		final ASTFractal fractal = parse(source, errors);
		final String orbitScript = fractal.getOrbit().toString();
		final String colorScript = fractal.getColor().toString();
		final DSLFractal compiledFractal = fractal.compile();
		return new DSLParserResult(compiledFractal, source, orbitScript, colorScript, errors);
	}

	private ASTFractal parse(String source, List<ScriptError> errors) throws DSLParserException {
		try {
			CharStream is = CharStreams.fromReader(new StringReader(source));
			MandelbrotLexer lexer = new MandelbrotLexer(is);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			MandelbrotParser parser = new MandelbrotParser(tokens);
			parser.setErrorHandler(new ErrorStrategy(errors));
			parser.fractal();
			ASTBuilder builder = parser.getBuilder();
			final ASTFractal fractal = builder.getFractal();
			if (fractal == null || !errors.isEmpty()) {
				throw new DSLParserException("Can't parse script", errors);
			}
			if (fractal.getOrbit() == null) {
				throw new DSLParserException("Can't parse orbit", errors);
			}
			if (fractal.getColor() == null) {
				throw new DSLParserException("Can't parse color", errors);
			}
			return fractal;
		} catch (ASTException e) {
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			ScriptError error = new ScriptError(PARSE, line, charPositionInLine, index, length, message);
			log.log(Level.INFO, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse script", errors);
		} catch (Exception e) {
            String message = e.getMessage();
			ScriptError error = new ScriptError(PARSE, 0L, 0L, 0L, 0L, message);
			log.log(Level.INFO, error.toString(), e);
			errors.add(error);
			throw new DSLParserException("Can't parse script", errors);
		}
	}
}