/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.core.javafx;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class StatusPane extends BorderPane {
	private StringObservableValue messageProperty;

	public StatusPane() {
		messageProperty = new StringObservableValue();

		TextArea message = new TextArea();
		message.setEditable(false);

		VBox box = new VBox();
		box.setAlignment(Pos.TOP_CENTER);
		box.getChildren().add(message);
		box.getStyleClass().add("status");

		setCenter(box);

		messageProperty().addListener((observable, oldValue, newValue) -> message.setText(newValue));
	}

	public void setMessage(String text) {
		messageProperty.setValue(text);
	}

	public ObservableValue<String> messageProperty() {
		return messageProperty;
	}
}
