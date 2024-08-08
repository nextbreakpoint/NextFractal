package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import java.util.Comparator;

public class CaseRangeComparator implements Comparator<CaseRange> {
    @Override
    public int compare(CaseRange o1, CaseRange o2) {
        return Long.compare(o1.high(), o2.low());
    }
}
