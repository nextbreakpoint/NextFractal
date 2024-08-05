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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpans;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpansBuilder;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;

public class MandelbrotParserStrategy implements ParserStrategy {
    private static final Pattern HIGHLIGHTING_PATTERN = createHighlightingPattern();

    @Override
    public CompletionStage<ParserResult> compute(Executor executor, Session session) {
        return CompletableFuture.supplyAsync(() -> createParserResult(session), executor);
    }

    @Override
    public Session createSession(Metadata metadata, String source) {
        return new MandelbrotSession(source, (MandelbrotMetadata) metadata);
    }

    private ParserResult createParserResult(Session session) {
        try {
            final DSLCompiler compiler = new DSLCompiler(DSLParser.getPackageName(), DSLParser.getClassName());
            final DSLParser parser = new DSLParser(compiler);
            final DSLExpressionContext expressionContext = new DSLExpressionContext();
            final DSLParserResult parserResult = parser.parse(expressionContext, session.script());
            //TODO is create required here?
            parserResult.colorClassFactory().create();
            parserResult.orbitClassFactory().create();
            return new ParserResult(session, List.of(), computeHighlighting(session.script()), parserResult);
        } catch (DSLException e) {
            final DSLParserResult result = DSLParserResult.builder().withSource(session.script()).build();
            return new ParserResult(session, e.getErrors(), computeHighlighting(session.script()), result);
        } catch (Exception e) {
            final List<ScriptError> errors = new ArrayList<>();
            errors.add(new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage()));
            final DSLParserResult result = DSLParserResult.builder().withSource(session.script()).build();
            return new ParserResult(session, errors, computeHighlighting(session.script()), result);
        }
    }

    private GenericStyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = HIGHLIGHTING_PATTERN.matcher(text);
        int lastKeywordEnd = 0;
        GenericStyleSpansBuilder<Collection<String>> spansBuilder = new GenericStyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher
                .group("KEYWORD") != null ? "mandelbrot-keyword" : matcher
                .group("FUNCTION") != null ? "mandelbrot-function" : matcher
                .group("PAREN") != null ? "mandelbrot-paren" : matcher
                .group("BRACE") != null ? "mandelbrot-brace" : matcher
                .group("OPERATOR") != null ? "mandelbrot-operator" : matcher
                .group("PATHOP") != null ? "mandelbrot-pathop" : null;
            spansBuilder.addSpan(List.of("code"), matcher.start() - lastKeywordEnd);
            spansBuilder.addSpan(List.of(styleClass != null ? styleClass : "code"), matcher.end() - matcher.start());
            lastKeywordEnd = matcher.end();
        }
        spansBuilder.addSpan(List.of("code"), text.length() - lastKeywordEnd);
        return spansBuilder.build();
    }

    private static Pattern createHighlightingPattern() {
        String[] KEYWORDS = new String[] {
            "fractal", "orbit", "color", "begin", "loop", "end", "rule", "trap", "palette", "if", "else", "stop", "init"
        };

        String[] FUNCTIONS = new String[] {
            "re", "im", "mod", "pha", "log", "exp", "sqrt", "mod2", "abs", "ceil", "floor", "pow", "hypot", "atan2", "min", "max", "cos", "sin", "tan", "asin", "acos", "atan", "time", "square", "saw", "ramp", "pulse"
        };

        String[] PATHOP = new String[] {
            "MOVETO", "MOVEREL", "LINETO", "LINEREL", "ARCTO", "ARCREL", "QUADTO", "QUADREL", "CURVETO", "CURVEREL", "CLOSE"
        };

        String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
        String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
        String PATHOP_PATTERN = "\\b(" + String.join("|", PATHOP) + ")\\b";
        String PAREN_PATTERN = "\\(|\\)";
        String BRACE_PATTERN = "\\{|\\}";
        String OPERATOR_PATTERN = "\\*|\\+|-|/|\\^|<|>|\\||&|=|#|;|\\[|\\]";

        return Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<FUNCTION>" + FUNCTION_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<OPERATOR>" + OPERATOR_PATTERN + ")"
            + "|(?<PATHOP>" + PATHOP_PATTERN + ")"
        );
    }
}
