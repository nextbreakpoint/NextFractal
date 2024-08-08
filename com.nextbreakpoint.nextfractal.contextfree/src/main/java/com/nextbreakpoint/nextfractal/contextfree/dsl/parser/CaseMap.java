package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import java.util.TreeMap;

public class CaseMap<T> extends TreeMap<CaseRange, T> {
    public CaseMap() {
        super(new CaseRangeComparator());
    }

    public int count(CaseRange range) {
        return containsKey(range) ? 1 : 0;
    }
}
