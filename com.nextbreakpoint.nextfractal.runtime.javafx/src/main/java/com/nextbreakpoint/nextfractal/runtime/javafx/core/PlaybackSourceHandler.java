/*
 * NextFractal 2.3.0
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
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserStrategy;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackReportChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import javafx.application.Platform;
import lombok.extern.java.Log;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@Log
public class PlaybackSourceHandler {
    private final PlatformEventBus eventBus;

    private final ExecutorService executor;

    private Session session;
    private ParserStrategy parserStrategy;

    public PlaybackSourceHandler(PlatformEventBus eventBus) {
        this.eventBus = eventBus;

        executor = Executors.newSingleThreadExecutor();
//        Cleaner.create().register(this, executor::shutdown);

        eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> handleDataLoaded(((PlaybackDataLoaded) event).session(), false));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> executor.shutdown());
    }

    public void handleSessionTerminate() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private CompletionStage<PlaybackReportChanged> computeEvent(String text, boolean appendToHistory) {
        return parserStrategy.compute(executor, createModifiedSession(text)).thenApply(result -> createReportChangedEvent(result, appendToHistory));
    }

    private Session createModifiedSession(String text) {
        return parserStrategy.createSession(session.getMetadata(), text);
    }

    private void notifyEvent(PlaybackReportChanged event) {
        // we need to ignore the event if session has changed between creation and notification
        if (Objects.equals(event.session().getMetadata(), session.getMetadata())) {
            eventBus.postEvent(event);
        }
    }

    private void handleDataLoaded(Session session, boolean appendToHistory) {
        updateSession(session);

        computeEvent(session.getScript(), appendToHistory)
                .whenComplete((newEvent, throwable) -> {
                    if (throwable == null) {
                        Platform.runLater(() -> notifyEvent(newEvent));
                    } else {
                        log.log(Level.WARNING, "Can't parse source code", throwable);
                    }
                });
    }

    private void updateSession(Session session) {
        if (parserStrategy == null || !this.session.getPluginId().equals(session.getPluginId())) {
            parserStrategy = createParserStrategy(session).orElse(null);
        }

        this.session = session;
    }

    private static Either<ParserStrategy> createParserStrategy(Session session) {
        return Command.of(tryFindFactory(session.getPluginId()))
                .map(plugin -> Objects.requireNonNull(plugin.createParserStrategy()))
                .execute();
    }

    private PlaybackReportChanged createReportChangedEvent(ParserResult result, boolean appendToHistory) {
        return PlaybackReportChanged.builder()
                .result(result)
                .session(result.session())
                .continuous(false)
                .appendToHistory(appendToHistory)
                .build();
    }
}
