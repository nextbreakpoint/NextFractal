package com.nextbreakpoint.nextfractal.contextfree.dsl;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;

import java.util.List;

public interface CFHandle {
    void stop();

    List<ScriptError> errors();
}
