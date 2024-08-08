package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pointer<T> {
    private T ref;

    public Pointer(T ref) {
        this.ref = ref;
    }
}
