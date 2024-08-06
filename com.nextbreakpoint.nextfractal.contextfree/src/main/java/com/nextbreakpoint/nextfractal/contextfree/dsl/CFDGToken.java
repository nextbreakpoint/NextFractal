package com.nextbreakpoint.nextfractal.contextfree.dsl;

import lombok.Getter;

public class CFDGToken {
    private final String code;
    @Getter
    private final int line;
    @Getter
    private final int charPositionInLine;
    @Getter
    private final int startIndex;
    @Getter
    private final int stopIndex;
    @Getter
    private final String text;

    public CFDGToken(String code, int line, int charPositionInLine, int startIndex, int stopIndex, String text) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
