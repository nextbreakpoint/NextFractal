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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult.Type;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ErrorStrategy;
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

@Deprecated
public class InterpreterDSLParser {
    private static final Logger logger = Logger.getLogger(InterpreterDSLParser.class.getName());

    public DSLParserResult parse(String source) throws DSLParserException {
        List<ParserError> errors = new ArrayList<>();
        ASTFractal ast = parse(source, errors);
        final String orbitScript = ast != null && ast.getOrbit() != null ? ast.getOrbit().toString() : "";
        final String colorScript = ast != null && ast.getColor() != null ? ast.getColor().toString() : "";
        return new DSLParserResult(ast, Type.INTERPRETED, source, orbitScript, colorScript, "", "", errors, "", "");
    }

    private ASTFractal parse(String source, List<ParserError> errors) throws DSLParserException {
        try {
            CharStream is = CharStreams.fromReader(new StringReader(source));
            MandelbrotLexer lexer = new MandelbrotLexer(is);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MandelbrotParser parser = new MandelbrotParser(tokens);
            parser.setErrorHandler(new ErrorStrategy(errors));
            ParseTree fractalTree = parser.fractal();
            //TODO review this code
            if (fractalTree != null) {
                ASTBuilder builder = parser.getBuilder();
                return builder.getFractal();
            }
        } catch (ASTException e) {
            ParserErrorType type = ParserErrorType.COMPILE;
            long line = e.getLocation().getLine();
            long charPositionInLine = e.getLocation().getCharPositionInLine();
            long index = e.getLocation().getStartIndex();
            long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
            String message = e.getMessage();
            ParserError error = new ParserError(type, line, charPositionInLine, index, length, message);
            logger.log(Level.FINE, error.toString(), e);
            errors.add(error);
            throw new DSLParserException("Can't parse source", errors);
        } catch (Exception e) {
            ParserErrorType type = ParserErrorType.COMPILE;
            String message = e.getMessage();
            ParserError error = new ParserError(type, 0L, 0L, 0L, 0L, message);
            logger.log(Level.FINE, error.toString(), e);
            errors.add(error);
            throw new DSLParserException("Can't parse source", errors);
        }
        return null;
    }
}	
