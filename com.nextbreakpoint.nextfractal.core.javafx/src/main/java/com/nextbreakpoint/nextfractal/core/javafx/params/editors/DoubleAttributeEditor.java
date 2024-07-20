package com.nextbreakpoint.nextfractal.core.javafx.params.editors;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.AdvancedTextField;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditor;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import javafx.scene.control.Tooltip;
import org.reactfx.EventStreams;

import java.time.Duration;

public class DoubleAttributeEditor extends AttributeEditor {
    private final Attribute attribute;
    private final AdvancedTextField textField;

    public DoubleAttributeEditor(Attribute attribute) {
        this.attribute = attribute;

        textField = new AdvancedTextField();
        textField.setRestrict(getRestriction());
        textField.setTooltip(new Tooltip(attribute.getName()));

        setCenter(textField);

        if (!attribute.isReadOnly()) {
            EventStreams.changesOf(textField.textProperty())
                    .successionEnds(Duration.ofMillis(500))
                    .subscribe(change -> {
                        if (getDelegate() != null) {
                            getDelegate().onEditorChanged(this);
                        }
                    });
        } else {
            textField.setEditable(false);
        }
    }

    @Override
    public void loadSession(Session session) {
        textField.setText(attribute.getMapper().apply(session));
    }

    @Override
    public Session updateSession(Session session) {
        return attribute.getCombiner().apply(session, textField.getText());
    }

    private String getRestriction() {
        return "-?\\d*\\.?\\d*";
    }
}
