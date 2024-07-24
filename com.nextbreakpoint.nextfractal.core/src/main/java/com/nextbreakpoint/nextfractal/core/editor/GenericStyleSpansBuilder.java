package com.nextbreakpoint.nextfractal.core.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenericStyleSpansBuilder<T> {
    private final List<GenericStyleSpan<T>> styleSpans = new ArrayList<>();

    public void addSpan(T styles, int index) {
        styleSpans.add(new GenericStyleSpan<>(styles, index));
    }

    public GenericStyleSpans<T> build() {
        return new GenericStyleSpans<>(Collections.unmodifiableList(styleSpans));
    }
}
