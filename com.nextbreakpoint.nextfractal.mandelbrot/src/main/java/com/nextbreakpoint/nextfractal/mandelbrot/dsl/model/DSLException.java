package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLToken;
import lombok.Getter;

@Getter
public class DSLException extends RuntimeException {
    private final DSLToken token;

    public DSLException(String message, DSLToken token) {
        super(message);
        this.token = token;
    }
}
