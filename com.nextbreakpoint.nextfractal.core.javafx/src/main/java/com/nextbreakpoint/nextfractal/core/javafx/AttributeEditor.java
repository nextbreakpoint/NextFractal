package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.common.Session;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AttributeEditor extends BorderPane {
    private AttributeEditorDelegate delegate;

    public abstract void loadSession(Session session);

    public abstract Session updateSession(Session session);
}
