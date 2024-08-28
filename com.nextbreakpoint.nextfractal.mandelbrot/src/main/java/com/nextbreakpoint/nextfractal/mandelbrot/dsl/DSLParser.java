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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiler.Compiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
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
	private final String packageName;
	private final String className;

	public DSLParser(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
	}

    public DSLParserResult parse(String source) throws DSLParserException {
		final ASTFractal fractal = parseFractal(source);
		final String orbitScript = fractal.getOrbit().toString();
		final String colorScript = fractal.getColor().toString();
		final DSLExpressionContext context = new DSLExpressionContext();
        final DSLFractal resolvedFractal = fractal.resolve(context);
		final DSLParserResult parserResult = DSLParserResult.builder()
				.withSource(source)
				.withOrbitDSL(orbitScript)
				.withColorDSL(colorScript)
				.build();
		final Compiler compiler = new Compiler(packageName, className);
		return compiler.compile(context, parserResult, resolvedFractal);
	}

	private ASTFractal parseFractal(String source) throws DSLParserException {
		try {
			final List<ScriptError> errors = new ArrayList<>();
			final CharStream is = CharStreams.fromReader(new StringReader(source));
			final MandelbrotLexer lexer = new MandelbrotLexer(is);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final MandelbrotParser parser = new MandelbrotParser(tokens);
			parser.setErrorHandler(new ErrorStrategy(errors));
			parser.fractal();
			final ASTBuilder builder = parser.getBuilder();
			final ASTFractal fractal = builder.getFractal();
			if (!errors.isEmpty()) {
				throw new DSLParserException("Script syntax error", errors);
			}
			if (fractal == null) {
				throw new DSLParserException("Fractal not defined", errors);
			}
			if (fractal.getOrbit() == null) {
				throw new DSLParserException("Orbit not defined", errors);
			}
			if (fractal.getColor() == null) {
				throw new DSLParserException("Color not defined", errors);
			}
			return fractal;
		} catch (DSLParserException e) {
			log.log(Level.INFO, "Can't parse script", e);
			throw e;
		} catch (ASTException e) {
			final long line = e.getLocation().getLine();
			final long charPositionInLine = e.getLocation().getCharPositionInLine();
			final long index = e.getLocation().getStartIndex();
			final long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			final ScriptError error = new ScriptError(PARSE, line, charPositionInLine, index, length, e.getMessage());
			log.log(Level.INFO, "Can't parse script", e);
            throw new DSLParserException("Can't parse script", List.of(error));
		} catch (Exception e) {
			final ScriptError error = new ScriptError(PARSE, 0L, 0L, 0L, 0L, e.getMessage());
			log.log(Level.INFO, "Can't parse script", e);
            throw new DSLParserException("Can't parse script", List.of(error));
		}
	}

	public static String getClassName() {
		return "C" + System.nanoTime();
	}

	public static String getPackageName() {
		return DSLParser.class.getPackage().getName() + ".generated";
	}
}
