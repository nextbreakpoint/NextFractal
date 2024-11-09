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
package com.nextbreakpoint.nextfractal.runtime.javafx.core;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.event.EditorReportChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorSourceChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import javafx.application.Platform;
import lombok.extern.java.Log;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@Log
public class SessionSourceHandler {
    private final PlatformEventBus eventBus;

    private final ExecutorService executor;

    private Session session;
    private ParserStrategy parserStrategy;

    public SessionSourceHandler(PlatformEventBus eventBus) {
        this.eventBus = Objects.requireNonNull(eventBus);

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createPlatformThreadFactory("Session Parser"));

        eventBus.subscribe(EditorSourceChanged.class.getSimpleName(), event -> handleSourceChanged(((EditorSourceChanged) event).source()));

        eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionLoaded((SessionDataLoaded) event));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> handleSessionTerminate());
    }

    private void handleSessionTerminate() {
        ExecutorUtils.shutdown(executor);
    }

    private void handleSourceChanged(String source) {
        onSessionChanged(session.withSource(source), true, true);
    }

    private void handleSessionLoaded(SessionDataLoaded event) {
        onSessionChanged(event.session(), false, false);
    }

    private CompletionStage<EditorReportChanged> computeEvent(String text, boolean continuous, boolean appendToHistory) {
        return parserStrategy.compute(executor, createModifiedSession(text))
                .thenApply(result -> createReportChangedEvent(result, continuous, appendToHistory));
    }

    private Session createModifiedSession(String text) {
        return parserStrategy.createSession(session.metadata(), text);
    }

    private void notifyEvent(EditorReportChanged event) {
        // we need to ignore the event if session has changed between creation and notification
        if (Objects.equals(event.session().metadata(), session.metadata())) {
            eventBus.postEvent(event);
        }
    }

    private void onSessionChanged(Session session, boolean continuous, boolean appendToHistory) {
        updateSession(session);

        computeEvent(session.script(), continuous, appendToHistory)
                .whenComplete((newEvent, throwable) -> {
                    if (throwable == null) {
                        Platform.runLater(() -> notifyEvent(newEvent));
                    } else {
                        log.log(Level.WARNING, "Can't parse source code", throwable);
                    }
                });
    }

    private void updateSession(Session session) {
        if (parserStrategy == null || !this.session.pluginId().equals(session.pluginId())) {
            parserStrategy = createParserStrategy(session).orElse(null);
        }

        this.session = session;
    }

    private static Either<ParserStrategy> createParserStrategy(Session session) {
        return Command.of(tryFindFactory(session.pluginId()))
                .map(plugin -> Objects.requireNonNull(plugin.createParserStrategy()))
                .execute();
    }

    private static EditorReportChanged createReportChangedEvent(ParserResult result, boolean continuous, boolean appendToHistory) {
        return EditorReportChanged.builder()
                .result(result)
                .session(result.session())
                .continuous(continuous)
                .appendToHistory(appendToHistory)
                .build();
    }
}
