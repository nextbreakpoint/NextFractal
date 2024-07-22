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
package com.nextbreakpoint.nextfractal.core.javafx.params;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditor;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditorFactory;
import com.nextbreakpoint.nextfractal.core.javafx.AttributeEditorPlugins;
import com.nextbreakpoint.nextfractal.core.params.Attribute;
import com.nextbreakpoint.nextfractal.core.params.Group;
import com.nextbreakpoint.nextfractal.core.params.Parameters;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

@Log
public class ParametersEditor extends VBox {
	private static final int SPACING = 5;

	private final Map<String, AttributeEditor> editors = new HashMap<>();

	@Setter
	@Getter
	private ParametersEditorDelegate delegate;

	private Session session;

	public ParametersEditor(Parameters parameters) {
		super(SPACING * 2);

		final var groupPanes = parameters.getGroups()
				.stream()
				.map(group -> createGroupPane(group, editors))
				.toList();

		getChildren().addAll(groupPanes);
	}

	public void bindSession(Session session) {
		this.session = session;

		editors.values().forEach(editor -> editor.loadSession(session));
	}

	private void onEditorChanged(AttributeEditor editor) {
		this.session = editor.updateSession(session);

		if (delegate != null) {
			delegate.onSessionChanged(session);
		}
	}

	private VBox createGroupPane(Group group, Map<String, AttributeEditor> editors) {
		final var groupPane = new VBox(SPACING);

		final var label = new Label(group.getName());

		groupPane.getChildren().addAll(label);

		final var attributeEditors = group.getAttributes().stream()
				.map(attribute -> createAttributeEditor(attribute, editors))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.toList();

		groupPane.getChildren().addAll(attributeEditors);

		return groupPane;
	}

	private Optional<AttributeEditor> createAttributeEditor(Attribute attribute, Map<String, AttributeEditor> editors) {
		final AttributeEditor editor = findAttributeEditorFactory(attribute)
				.map(plugin -> Objects.requireNonNull(plugin.createAttributeEditor(attribute)))
				.observe()
				.onFailure(e -> log.log(Level.INFO, "Cannot create attribute editor: " + attribute.getKey()))
				.get()
				.orElse(null);

		if (editor != null) {
			editors.put(attribute.getKey(), editor);
			editor.setDelegate(this::onEditorChanged);
		}

		return Optional.ofNullable(editor);
	}

	private static Either<AttributeEditorFactory> findAttributeEditorFactory(Attribute attribute) {
		return AttributeEditorPlugins.tryFindFactory("key-" + attribute.getKey())
				.or(() -> AttributeEditorPlugins.tryFindFactory("logical-type-" + attribute.getLogicalType()));
	}
}
