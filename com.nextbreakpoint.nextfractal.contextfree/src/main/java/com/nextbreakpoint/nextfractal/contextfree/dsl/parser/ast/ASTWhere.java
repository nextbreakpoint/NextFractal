package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ASTWhere {
    public static final ASTWhere DEFAULT_WHERE = new ASTWhere(0, 0, 0, 0, "");

    private final int line;
    private final int charPositionInLine;
    private final int startIndex;
    private final int stopIndex;
    private final String text;

    public ASTWhere(int line, int charPositionInLine, int startIndex, int stopIndex, String text) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
    }
}
