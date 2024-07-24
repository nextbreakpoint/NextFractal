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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;

import java.nio.IntBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

public class HistoryPane extends BorderPane {
    private static Logger logger = Logger.getLogger(HistoryPane.class.getName());
    private static final int PADDING = 8;

    private final ExecutorService executor;
    private ListView<Bitmap> listView;
    private HistoryDelegate delegate;
    private RendererTile tile;

    public HistoryPane(RendererTile tile) {
        this.tile = tile;

        listView = new ListView<>();
        listView.setFixedCellSize(tile.getTileSize().getHeight() + PADDING);
        listView.setCellFactory(view -> new HistoryListCell(tile));
        listView.setTooltip(new Tooltip("Previous images"));

        BorderPane historyPane = new BorderPane();
        historyPane.setCenter(listView);

        getStyleClass().add("history");

        setCenter(historyPane);

        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> itemSelected(listView));

        executor = Executors.newSingleThreadExecutor(new DefaultThreadFactory("History", true, Thread.MIN_PRIORITY));
    }

    private DefaultThreadFactory createThreadFactory(String name) {
        return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
    }

    private void itemSelected(ListView<Bitmap> listView) {
        int index = listView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            if (delegate != null) {
                Bitmap bitmap = listView.getItems().get(index);
                Session session = (Session) bitmap.getProperty("session");
                Platform.runLater(() -> delegate.sessionChanged(session));
            }
        }
    }

    private void submitItem(Session session, ImageComposer composer) {
        executor.submit(() -> Command.of(() -> composer.renderImage(session.script(), session.metadata()))
                .execute().optional().ifPresent(pixels -> Platform.runLater(() -> addItem(listView, session, pixels, composer.getSize()))));
    }

    private void addItem(ListView<Bitmap> listView, Session session, IntBuffer pixels, RendererSize size) {
        BrowseBitmap bitmap = new BrowseBitmap(size.getWidth(), size.getHeight(), pixels);
        bitmap.setProperty("session", session);
        listView.getItems().addFirst(bitmap);
    }

    public void appendSession(Session session) {
        Command.of(tryFindFactory(session.pluginId()))
                .map(this::createImageComposer)
                .execute()
                .optional()
                .ifPresent(composer -> submitItem(session, composer));
    }

    private ImageComposer createImageComposer(CoreFactory factory) {
        return factory.createImageComposer(createThreadFactory("History Composer"), tile, true);
    }

    public void setDelegate(HistoryDelegate delegate) {
        this.delegate = delegate;
    }

    public void dispose() {
        List<ExecutorService> executors = List.of(executor);
        executors.forEach(ExecutorService::shutdownNow);
        executors.forEach(this::await);
    }

    private void await(ExecutorService executor) {
        Command.of(() -> executor.awaitTermination(5000, TimeUnit.MILLISECONDS))
                .execute()
                .observe()
                .onFailure(e -> logger.warning("Await termination timeout"))
                .get();
    }
}
