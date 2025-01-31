/*
 * NextFractal 2.4.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.contextfree.javafx.parameter;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.misc.AdvancedTextField;
import com.nextbreakpoint.nextfractal.core.javafx.parameter.AttributeEditor;
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
