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
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import lombok.Setter;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.concurrent.ExecutorService;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class HistoryPane extends BorderPane {
    private static final int PADDING = 8;

    private final ExecutorService executor;
    private final ListView<Bitmap> listView;
    private final Tile tile;
    @Setter
    private HistoryDelegate delegate;

    public HistoryPane(Tile tile) {
        this.tile = tile;

        listView = new ListView<>();
        listView.setFixedCellSize(tile.tileSize().height() + PADDING);
        listView.setCellFactory(_ -> new HistoryListCell(tile));
        listView.setTooltip(new Tooltip("Previous images"));

        final BorderPane historyPane = new BorderPane();
        historyPane.setCenter(listView);

        getStyleClass().add("history");

        setCenter(historyPane);

        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> _) -> itemSelected(listView));

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("History"));
    }

    private void itemSelected(ListView<Bitmap> listView) {
        final int index = listView.getSelectionModel().getSelectedIndex();
        if (index >= 0) {
            if (delegate != null) {
                final Bitmap bitmap = listView.getItems().get(index);
                final Session session = (Session) bitmap.getProperty("session");
                Platform.runLater(() -> delegate.sessionChanged(session));
            }
        }
    }

    private void submitItem(Session session, ImageComposer composer) {
        executor.submit(() -> Command.of(() -> composer.renderImage(session.script(), session.metadata()))
                .execute().optional().ifPresent(pixels -> Platform.runLater(() -> addItem(listView, session, pixels, composer.getSize()))));
    }

    private void addItem(ListView<Bitmap> listView, Session session, IntBuffer pixels, Size size) {
        final Bitmap bitmap = new SimpleBitmap(size.width(), size.height(), pixels);
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
        return factory.createImageComposer(ThreadUtils.createPlatformThreadFactory("History Image Composer"), tile, true);
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }
}
