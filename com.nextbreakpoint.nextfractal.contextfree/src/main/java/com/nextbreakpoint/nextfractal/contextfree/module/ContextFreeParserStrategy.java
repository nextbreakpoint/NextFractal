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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFParserResult;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpans;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpansBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.PARSE;

public class ContextFreeParserStrategy implements ParserStrategy {
    private static final Pattern HIGHLIGHTING_PATTERN = createHighlightingPattern();

    @Override
    public CompletionStage<ParserResult> compute(Executor executor, Session session) {
        return CompletableFuture.supplyAsync(() -> createParserResult(session), executor);
    }

    @Override
    public Session createSession(Metadata metadata, String source) {
        return new ContextFreeSession(source, (ContextFreeMetadata) metadata);
    }

    private ParserResult createParserResult(Session session) {
        try {
            final CFParser parser = new CFParser();
            final CFParserResult parserResult = parser.parse(session.script());
            return new ParserResult(session, List.of(), computeHighlighting(session.script()), parserResult);
        } catch (CFParserException e) {
            final CFParserResult parserResult = new CFParserResult(session.script(), null);
            return new ParserResult(session, e.getErrors(), computeHighlighting(session.script()), parserResult);
        } catch (Exception e) {
            final List<ScriptError> errors = List.of(new ScriptError(PARSE, 0, 0, 0, 0, e.getMessage()));
            final CFParserResult parserResult = new CFParserResult(session.script(), null);
            return new ParserResult(session, errors, computeHighlighting(session.script()), parserResult);
        }
    }

    private GenericStyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = HIGHLIGHTING_PATTERN.matcher(text);
        int lastKeywordEnd = 0;
        GenericStyleSpansBuilder<Collection<String>> spansBuilder = new GenericStyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher
                    .group("KEYWORD") != null ? "contextfree-keyword" : matcher
                    .group("FUNCTION") != null ? "contextfree-function" : matcher
                    .group("PAREN") != null ? "contextfree-paren" : matcher
                    .group("BRACE") != null ? "contextfree-brace" : matcher
                    .group("OPERATOR") != null ? "contextfree-operator" : matcher
                    .group("PATHOP") != null ? "contextfree-pathop" : null;
            spansBuilder.addSpan(List.of("code"), matcher.start() - lastKeywordEnd);
            spansBuilder.addSpan(List.of(styleClass != null ? styleClass : "code"), matcher.end() - matcher.start());
            lastKeywordEnd = matcher.end();
        }
        spansBuilder.addSpan(List.of("code"), text.length() - lastKeywordEnd);
        return spansBuilder.build();
    }

    private static Pattern createHighlightingPattern() {
        String[] KEYWORDS = new String[]{
                "startshape", "background", "include", "import", "tile", "rule", "path", "shape", "loop", "finally", "if", "switch", "case", "CF_INFINITY", "\u221E", "LET"
        };

        String[] FUNCTIONS = new String[]{
                "time", "timescale", "x", "y", "z", "rotate", "r", "size", "s", "skew", "flip", "f", "hue", "h", "saturation", "sat", "brightness", "b", "alpha", "a", "x1", "x2", "y1", "y2", "rx", "ry", "width", "transform", "trans", "param", "p", "clone"
        };

        String[] PATHOP = new String[]{
                "CIRCLE", "SQUARE", "TRIANGLE", "STROKE", "FILL", "MOVETO", "LINETO", "ARCTO", "CURVETO", "MOVEREL", "LINEREL", "ARCREL", "CURVEREL", "CLOSEPOLY"
        };

        String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
        String FUNCTION_PATTERN = "\\b(" + String.join("|", FUNCTIONS) + ")\\b";
        String PATHOP_PATTERN = "\\b(" + String.join("|", PATHOP) + ")\\b";
        String PAREN_PATTERN = "\\(|\\)|\\[|\\]";
        String BRACE_PATTERN = "\\{|\\}";
        String OPERATOR_PATTERN = "\\.\\.|\\u2026|\\+/-|\\u00b1";

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
