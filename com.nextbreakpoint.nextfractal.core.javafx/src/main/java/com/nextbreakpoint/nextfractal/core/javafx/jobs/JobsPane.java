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
package com.nextbreakpoint.nextfractal.core.javafx.jobs;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.javafx.Icons;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformImageLoader;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class JobsPane extends BorderPane {
    private final Map<String, JobEntry> exportEntries = new HashMap<>();
    private final GridView<JobsGridViewItem> gridView;
    private final ExecutorService executor;
    @Setter
    private JobsDelegate delegate;

    public JobsPane() {
        gridView = new GridView<>(new JobsGridViewCellFactory(), false, 1);

        final int buttonSize = computeSize(Icons.computeOptimalIconPercentage());

        final HBox exportControls = new HBox(0);
        exportControls.setAlignment(Pos.CENTER);
        final Button suspendButton = new Button("", Icons.createSVGIcon("/pause.svg", buttonSize));
        final Button resumeButton = new Button("", Icons.createSVGIcon("/play.svg", buttonSize));
        final Button removeButton = new Button("", Icons.createSVGIcon("/stop.svg", buttonSize));
        suspendButton.setTooltip(new Tooltip("Suspend selected jobs"));
        resumeButton.setTooltip(new Tooltip("Resume selected jobs"));
        removeButton.setTooltip(new Tooltip("Remove selected jobs"));
        suspendButton.setDisable(true);
        resumeButton.setDisable(true);
        removeButton.setDisable(true);
        exportControls.getChildren().add(suspendButton);
        exportControls.getChildren().add(resumeButton);
        exportControls.getChildren().add(removeButton);
        exportControls.getStyleClass().add("toolbar");

        setCenter(gridView);
        setBottom(exportControls);

        getStyleClass().add("jobs");

        gridView.setDisable(false);

        final List<Button> buttonsList = List.of(suspendButton, resumeButton, removeButton);

        gridView.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener.Change<? extends JobsGridViewItem> change) -> updateButtons(buttonsList, change.getList().isEmpty()));

        suspendButton.setOnAction(_ -> selectedItems(gridView).filter(bitmap -> !isExportSessionSuspended(bitmap))
                .forEach(item -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionSuspended((ExportSession) item.getProperty("exportSession")))));

        resumeButton.setOnAction(_ -> selectedItems(gridView).filter(this::isExportSessionSuspended)
                .forEach(item -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionResumed((ExportSession) item.getProperty("exportSession")))));

        removeButton.setOnAction(_ -> selectedItems(gridView)
                .forEach(item -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionStopped((ExportSession) item.getProperty("exportSession")))));

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("Jobs"));
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }

    public void appendSession(ExportSession exportSession) {
        Platform.runLater(() -> addItem(gridView, exportSession));
    }

    public void removeSession(ExportSession exportSession) {
        Platform.runLater(() -> removeItem(gridView, exportSession));
    }

    public void updateSession(ExportSession exportSession, ExportSessionState state, Float progress) {
        final JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        if (exportEntry != null) {
            final JobEntry jobEntry = new JobEntry(exportSession, state, exportEntry.item());
            jobEntry.item().setJobProgress(progress);
            exportEntries.put(exportSession.getSessionId(), jobEntry);
        }
    }

    private boolean isExportSessionSuspended(JobsGridViewItem item) {
        final ExportSession exportSession = (ExportSession) item.getProperty("exportSession");
        final JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        return exportEntry != null && exportEntry.state() == ExportSessionState.SUSPENDED;
    }

    private void updateButtons(List<Button> buttons, boolean disabled) {
        buttons.forEach(button -> button.setDisable(disabled));
    }

    private Stream<JobsGridViewItem> selectedItems(GridView<JobsGridViewItem> gridView) {
        final MultipleSelectionModel<JobsGridViewItem> selectionModel = gridView.getSelectionModel();
        return selectionModel != null ? selectionModel.getSelectedItems().stream() : Stream.empty();
    }

    private void addItem(GridView<JobsGridViewItem> gridView, ExportSession exportSession) {
        final JobEntry exportEntry = exportEntries.remove(exportSession.getSessionId());
        if (exportEntry == null) {
            final PlatformImageLoader imageLoader = createImageLoader(exportSession);
            final JobsGridViewItem item = new JobsGridViewItem(imageLoader);
            final JobEntry jobEntry = new JobEntry(exportSession, ExportSessionState.READY, item);
            item.putProperty("exportSession", exportSession);
            exportEntries.put(exportSession.getSessionId(), jobEntry);
            if (gridView.getItems() != null) {
                gridView.getItems().addLast(item);
            }
        }
    }

    private void removeItem(GridView<JobsGridViewItem> gridView, ExportSession session) {
        final JobEntry exportEntry = exportEntries.remove(session.getSessionId());
        if (exportEntry != null) {
            if (gridView.getItems() != null) {
                gridView.getItems().remove(exportEntry.item());
            }
        }
    }

    private PlatformImageLoader createImageLoader(ExportSession session) {
        return new PlatformImageLoader(executor, () -> createBundle(session), () -> new Size((int) getWidth(), (int) getWidth()));
    }

    private Either<Bundle> createBundle(ExportSession exportSession) {
        return createSession(exportSession.getFrames().getFirst())
                .map(session -> new Bundle(session, List.of()));
    }

    private Either<Session> createSession(AnimationFrame frame) {
        return Command.of(tryFindFactory(frame.pluginId()))
                .map(factory -> factory.createSession(frame.script(), frame.metadata()))
                .execute();
    }

    private int computeSize(double percentage) {
        return (int) Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);
    }

    private record JobEntry(ExportSession exportSession, ExportSessionState state, JobsGridViewItem item) {
    }
}
