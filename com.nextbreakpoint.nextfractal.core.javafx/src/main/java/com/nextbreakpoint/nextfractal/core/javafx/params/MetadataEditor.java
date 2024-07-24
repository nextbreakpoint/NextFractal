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
import com.nextbreakpoint.nextfractal.core.common.ParamsStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.EditorDataChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorParamsActionFired;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionDataLoaded;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.core.params.Parameters;
import javafx.scene.layout.BorderPane;

import java.util.Objects;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

public class MetadataEditor extends BorderPane {
	private final PlatformEventBus eventBus;
	private Session activeSession;
	private Session editedSession;
	private ParamsStrategy paramsStrategy;
	private ParametersEditor editor;

	public MetadataEditor(PlatformEventBus eventBus) {
		this.eventBus = eventBus;

		setDisabled(true);

		eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionDataLoaded((SessionDataLoaded) event));

		eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionDataChanged((SessionDataChanged) event));

		eventBus.subscribe(EditorParamsActionFired.class.getSimpleName(), event -> {
			final String action = ((EditorParamsActionFired) event).action();
			if (activeSession != null && action.equals("cancel")) editor.bindSession(activeSession);
			if (editedSession != null && action.equals("apply")) notifyEditorDataChanged(editedSession);
		});
	}

	private void notifyEditorDataChanged(Session session) {
		eventBus.postEvent(EditorDataChanged.builder().session(session).continuous(false).appendToHistory(true).build());
	}

	private void onSessionChanged(Session session) {
		editedSession = session;
	}

	private void handleSessionDataLoaded(SessionDataLoaded event) {
		updateSession(event.session(), event.continuous());
	}

	private void handleSessionDataChanged(SessionDataChanged event) {
		updateSession(event.session(), event.continuous());
	}

	private void updateSession(Session session, boolean continuous) {
		if (paramsStrategy == null || !this.activeSession.pluginId().equals(session.pluginId())) {
			paramsStrategy = createParamsStrategy(session).orElse(null);

			final Parameters parameters = paramsStrategy.create(this.activeSession);
			editor = new ParametersEditor(parameters);
			editor.setDelegate(this::onSessionChanged);
			setCenter(editor);

			setDisabled(false);
		}

		this.activeSession = session;
		if (continuous == Boolean.FALSE) {
			editor.bindSession(this.activeSession);
		}
	}

	private static Either<ParamsStrategy> createParamsStrategy(Session session) {
		return Command.of(tryFindFactory(session.pluginId()))
				.map(plugin -> Objects.requireNonNull(plugin.createParamsStrategy()))
				.execute();
	}
}
