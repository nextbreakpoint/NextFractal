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
package com.nextbreakpoint.nextfractal.runtime.javafx.component;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.EditorGrammarSelected;
import com.nextbreakpoint.nextfractal.core.event.EditorParamsActionFired;
import com.nextbreakpoint.nextfractal.core.event.SessionDataLoaded;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.core.javafx.parameter.MetadataEditor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import lombok.extern.java.Log;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.listGrammars;

@Log
public class MainParamsPane extends Pane {
	private static final int SPACING = 5;

	private final VBox paramsBox;
	private Session session;

	public MainParamsPane(PlatformEventBus eventBus) {
		final VBox box = new VBox(SPACING * 2);
		box.getStyleClass().add("params");

		final VBox grammarPane = new VBox(SPACING);

		final Label grammarLabel = new Label("Project grammar");
		grammarPane.getChildren().add(grammarLabel);

		final ComboBox<String> grammarCombobox = new ComboBox<>();
		grammarCombobox.getItems().addAll(listGrammars());
		grammarCombobox.getStyleClass().add("text-small");
		grammarCombobox.setTooltip(new Tooltip("Select project grammar"));
		grammarPane.getChildren().add(grammarCombobox);

		if (grammarCombobox.getItems().size() > 1) {
			box.getChildren().add(grammarPane);
		}

		paramsBox = new VBox(4);

		final MetadataEditor metadataEditorPane = new MetadataEditor(eventBus);
		paramsBox.getChildren().add(metadataEditorPane);

		final HBox buttons = new HBox(4);
		final Button applyButton = new Button("Apply");
		final Button cancelButton = new Button("Cancel");
		applyButton.setTooltip(new Tooltip("Apply parameters"));
		cancelButton.setTooltip(new Tooltip("Revert parameters"));
		buttons.getChildren().add(applyButton);
		buttons.getChildren().add(cancelButton);
		buttons.getStyleClass().add("buttons");
		buttons.getStyleClass().add("text-small");

		paramsBox.getChildren().add(buttons);

		box.getChildren().add(paramsBox);

		final ScrollPane scrollPane = new ScrollPane(box);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		getChildren().add(scrollPane);

		widthProperty().addListener((_, _, newValue) -> {
			double width = newValue.doubleValue() - getInsets().getLeft() - getInsets().getRight();
			box.setPrefWidth(width);
			grammarCombobox.setPrefWidth(width);
			applyButton.setPrefWidth(newValue.doubleValue());
			cancelButton.setPrefWidth(newValue.doubleValue());
			scrollPane.setPrefWidth(newValue.doubleValue());
        });

		heightProperty().addListener((_, _, newValue) -> {
			box.setPrefHeight(newValue.doubleValue() - getInsets().getTop() - getInsets().getBottom());
			scrollPane.setPrefHeight(newValue.doubleValue());
		});

		grammarCombobox.setOnAction(_ -> {
			final SingleSelectionModel<String> selectionModel = grammarCombobox.getSelectionModel();
			if (session != null && !selectionModel.getSelectedItem().equals(session.grammar())) {
				eventBus.postEvent(EditorGrammarSelected.builder().grammar(selectionModel.getSelectedItem()).build());
			}
		});

		cancelButton.setOnAction(e -> eventBus.postEvent(EditorParamsActionFired.builder().action("cancel").build()));
		
		applyButton.setOnAction((e) -> eventBus.postEvent(EditorParamsActionFired.builder().action("apply").build()));

		eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionDataLoaded(grammarCombobox, (SessionDataLoaded) event));
	}

	private void handleSessionDataLoaded(ComboBox<String> grammarCombobox, SessionDataLoaded event) {
		session = event.session();
		grammarCombobox.getSelectionModel().select(event.session().grammar());
	}

	public void setParamsDisable(boolean disabled) {
		paramsBox.setDisable(disabled);
	}
}
