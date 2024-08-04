package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.nextfractal.contextfree.core.ParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGParserResult;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpans;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpansBuilder;

import java.util.ArrayList;
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
            final CFDGParser parser = new CFDGParser();
            final CFDGParserResult result = parser.parse(session.script());
            return new ParserResult(session, result.errors(), computeHighlighting(session.script()), result);
        } catch (ParserException e) {
            final CFDGParserResult result = new CFDGParserResult(null, session.script(), e.getErrors());
            return new ParserResult(session, e.getErrors(), computeHighlighting(session.script()), result);
        } catch (Exception e) {
            final List<ScriptError> errors = new ArrayList<>();
            errors.add(new ScriptError(PARSE, 0, 0, 0, 0, e.getMessage()));
            final CFDGParserResult result = new CFDGParserResult(null, session.script(), errors);
            return new ParserResult(session, errors, computeHighlighting(session.script()), result);
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
