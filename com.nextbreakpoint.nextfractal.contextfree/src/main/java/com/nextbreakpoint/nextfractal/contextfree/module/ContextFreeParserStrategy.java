package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.contextfree.core.ParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.core.common.Block;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
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
        return Command.of(() -> new DSLParser().parse(session.script()))
                .map(ContextFreeParserStrategy::processResult)
                .map(result -> new ParserResult(session, result.getErrors(), computeHighlighting(session.script()), result))
                .execute()
                .orThrow(RuntimeException::new)
                .get();
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

    public static DSLParserResult processResult(DSLParserResult parserResult) {
        return Block.begin(DSLParserResult.class)
                .end(parserResult)
                .execute()
                .observe()
                .onFailure(e -> processCompilerErrors(parserResult, e))
                .get()
                .orThrow(RuntimeException::new)
                .get();
    }

    private static void processCompilerErrors(DSLParserResult result, Exception e) {
        if (e instanceof ParserException) {
            result.getErrors().addAll(((ParserException) e).getErrors());
        }
    }
}
