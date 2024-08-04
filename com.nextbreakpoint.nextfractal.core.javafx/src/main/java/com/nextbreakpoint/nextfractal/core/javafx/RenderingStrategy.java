package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;

import java.util.List;

public interface RenderingStrategy {
    GraphicsFactory getRenderFactory();

    void updateAndRedraw(long timestampInMillis);

    void updateCoordinators(Session session, boolean continuous, boolean timeAnimation);

    List<ScriptError> updateCoordinators(Object report);

    void disposeCoordinators();
}
