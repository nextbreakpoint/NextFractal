package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import lombok.Getter;
import org.antlr.v4.runtime.Token;

@Getter
public class DSLException extends RuntimeException {
    private final Token location;

    public DSLException(String message, Token location) {
        super(message);
        this.location = location;
    }
}
