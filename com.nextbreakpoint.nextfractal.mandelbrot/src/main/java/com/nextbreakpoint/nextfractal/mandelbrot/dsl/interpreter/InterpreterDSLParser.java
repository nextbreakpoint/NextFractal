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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ErrorStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult.Type;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.MandelbrotLexer;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.MandelbrotParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterpreterDSLParser {
    private static final Logger logger = Logger.getLogger(InterpreterDSLParser.class.getName());

    public DSLParserResult parse(String source) throws ParserException {
        List<SourceError> errors = new ArrayList<>();
        ASTFractal ast = parse(source, errors);
        return new DSLParserResult(ast, Type.INTERPRETER, source, "", "", errors, "", "");
    }

    private ASTFractal parse(String source, List<SourceError> errors) throws ParserException {
        try {
            CharStream is = CharStreams.fromReader(new StringReader(source));
            MandelbrotLexer lexer = new MandelbrotLexer(is);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MandelbrotParser parser = new MandelbrotParser(tokens);
            parser.setErrorHandler(new ErrorStrategy(errors));
            ParseTree fractalTree = parser.fractal();
            if (fractalTree != null) {
                ASTBuilder builder = parser.getBuilder();
                ASTFractal fractal = builder.getFractal();
                return fractal;
            }
        } catch (ASTException e) {
            SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
            long line = e.getLocation().getLine();
            long charPositionInLine = e.getLocation().getCharPositionInLine();
            long index = e.getLocation().getStartIndex();
            long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
            String message = e.getMessage();
            SourceError error = new SourceError(type, line, charPositionInLine, index, length, message);
            logger.log(Level.FINE, error.toString(), e);
            errors.add(error);
            throw new ParserException("Can't parse source", errors);
        } catch (Exception e) {
            SourceError.ErrorType type = SourceError.ErrorType.SCRIPT_COMPILER;
            String message = e.getMessage();
            SourceError error = new SourceError(type, 0L, 0L, 0L, 0L, message);
            logger.log(Level.FINE, error.toString(), e);
            errors.add(error);
            throw new ParserException("Can't parse source", errors);
        }
        return null;
    }
}	
