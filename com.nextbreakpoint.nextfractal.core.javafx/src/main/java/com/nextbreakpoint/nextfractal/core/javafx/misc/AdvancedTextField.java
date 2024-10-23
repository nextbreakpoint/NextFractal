/*
 * NextFractal 2.3.2
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
package com.nextbreakpoint.nextfractal.core.javafx.misc;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

public class AdvancedTextField extends TextField {
    private final StringProperty restrict = new SimpleStringProperty();

    @Setter
    @Getter
    private Function<String, String> transform = t -> t;

    public void setRestrict(String restrict) {
        this.restrict.set(restrict);
    }

    public String getRestrict() {
        return restrict.get();
    }

    public StringProperty restrictProperty() {
        return restrict;
    }

    public AdvancedTextField() {
        setAlignment(Pos.CENTER_RIGHT);
        textProperty().addListener(new ChangeListener<>() {
            private boolean ignore;

            @Override
            public void changed(ObservableValue<? extends String> observableValue, String text, String newText) {
                if (ignore)
                    return;
                String transText = transform != null ? transform.apply(newText) : newText;
                if (restrict.get() != null && !restrict.get().isEmpty() && !transText.matches(restrict.get())) {
                    updateText(text);
                } else {
                    if (!transText.equals(newText)) {
                        updateText(transText);
                    }
                }
            }

            private void updateText(String text) {
                ignore = true;
                setText(text);
                ignore = false;
            }
        });
    }
}
