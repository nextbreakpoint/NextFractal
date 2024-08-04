package com.nextbreakpoint.nextfractal.mandelbrot.dsl;

import org.antlr.v4.runtime.Token;

public class DSLToken {
    private final Token token;
    private final String code;

    public DSLToken(Token token, String code) {
        this.token = token;
        this.code = code;
    }

    public String getText() {
        return token.getText();
    }

    public int getLine() {
        return token.getLine();
    }

    public int getCharPositionInLine() {
        return token.getCharPositionInLine();
    }

    public int getStartIndex() {
        return token.getStartIndex();
    }

    public int getStopIndex() {
        return token.getStopIndex();
    }

    @Override
    public String toString() {
        return code;
    }
}
