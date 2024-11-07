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
package com.nextbreakpoint.nextfractal.core.javafx.history;

import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformImageLoader;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Log
public class HistoryPane extends BorderPane {
    private final GridView<HistoryGridViewItem> gridView;
    private final ExecutorService executor;
    @Setter
    private HistoryDelegate delegate;

    public HistoryPane() {
        gridView = new GridView<>(new HistoryGridViewCellFactory(), false, 1);

        final BorderPane historyPane = new BorderPane();

        historyPane.setCenter(gridView);

        getStyleClass().add("history");

        setCenter(historyPane);

        gridView.getSelectionModel().getSelectedIndices()
                .addListener((ListChangeListener.Change<? extends Integer> change) -> itemSelected(change.getList()));

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("History"));
    }

    public void appendSession(Session session) {
        final PlatformImageLoader imageLoader = createImageLoader(session);
        final HistoryGridViewItem item = new HistoryGridViewItem(imageLoader);
        item.putProperty("session", session);
        if (gridView.getItems() != null) {
            gridView.getItems().addFirst(item);
        }
    }

    private void itemSelected(ObservableList<? extends Integer> list) {
        if (delegate != null && gridView.getItems() != null && !list.isEmpty()) {
            final int index = list.getFirst();
            if (index >= 0 && index < gridView.getItems().size()) {
                final GridViewItem item = gridView.getItems().get(index);
                final Session session = (Session) item.getProperty("session");
                Platform.runLater(() -> delegate.sessionChanged(session));
            }
        }
    }

    private PlatformImageLoader createImageLoader(Session session) {
        return new PlatformImageLoader(executor, () -> createBundle(session), () -> new Size((int) getWidth(), (int) getWidth()));
    }

    private Either<Bundle> createBundle(Session session) {
        return Either.success(new Bundle(session, List.of()));
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }
}
