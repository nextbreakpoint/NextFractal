package com.nextbreakpoint.nextfractal.contextfree.javafx.editors;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.AdvancedTextField;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditor;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import javafx.scene.control.Tooltip;
import org.reactfx.EventStreams;

import java.time.Duration;

public class SeedAttributeEditor extends AttributeEditor {
    private final Attribute attribute;
    private final AdvancedTextField textField;

    public SeedAttributeEditor(Attribute attribute) {
        this.attribute = attribute;

        textField = new AdvancedTextField();
        textField.setRestrict(getRestriction());
        textField.setTransform(String::toUpperCase);
        textField.setTooltip(new Tooltip(attribute.getName()));

        setCenter(textField);

        EventStreams.changesOf(textField.textProperty())
                .successionEnds(Duration.ofMillis(500))
                .subscribe(change -> {
                    if (getDelegate() != null) {
                        getDelegate().onEditorChanged(this);
                    }
                });
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
        return "[A-Z]*";
    }
}
