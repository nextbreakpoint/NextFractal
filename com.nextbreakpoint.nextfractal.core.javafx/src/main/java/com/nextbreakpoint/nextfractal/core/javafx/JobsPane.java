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
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import lombok.Setter;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class JobsPane extends BorderPane {
    private static final int PADDING = 8;

    private final Map<String, JobEntry> exportEntries = new HashMap<>();
    private final ExecutorService executor;
    private final ListView<Bitmap> listView;
    private final Tile tile;
    @Setter
    private JobsDelegate delegate;

    public JobsPane(Tile tile) {
        this.tile = tile;

        listView = new ListView<>();
        listView.setFixedCellSize(tile.tileSize().height() + PADDING);
        listView.setCellFactory(_ -> new JobsListCell(tile));

        final double percentage = Icons.computeOptimalIconPercentage();
        final int buttonSize = (int) Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);

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

        final Label sizeLabel = new Label();
        final Label formatLabel = new Label();
        final Label durationLabel = new Label();

        final VBox detailsPane = new VBox(10);
        detailsPane.setAlignment(Pos.TOP_LEFT);
        detailsPane.getChildren().add(formatLabel);
        detailsPane.getChildren().add(sizeLabel);
        detailsPane.getChildren().add(durationLabel);
        detailsPane.getStyleClass().add("details");

        final BorderPane jobsPane = new BorderPane();
        jobsPane.setCenter(listView);
        jobsPane.setBottom(detailsPane);

        setCenter(jobsPane);
        setBottom(exportControls);

        getStyleClass().add("jobs");

        final List<Button> buttonsList = Arrays.asList(suspendButton, resumeButton, removeButton);
        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> updateButtons(buttonsList, c.getList().isEmpty()));

        suspendButton.setOnAction(_ -> selectedItems(listView).filter(bitmap -> !isExportSessionSuspended(bitmap))
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionSuspended((ExportSession) bitmap.getProperty("exportSession")))));

        resumeButton.setOnAction(_ -> selectedItems(listView).filter(this::isExportSessionSuspended)
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionResumed((ExportSession) bitmap.getProperty("exportSession")))));

        removeButton.setOnAction(_ -> selectedItems(listView)
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionStopped((ExportSession) bitmap.getProperty("exportSession")))));

        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> itemSelected(listView, sizeLabel, formatLabel, durationLabel));

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("Jobs"));
    }

    private boolean isExportSessionSuspended(Bitmap bitmap) {
        final ExportSession exportSession = (ExportSession) bitmap.getProperty("exportSession");
        final JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        return exportEntry != null && exportEntry.state() == ExportSessionState.SUSPENDED;
    }

    private void updateButtons(List<Button> buttons, boolean disabled) {
        buttons.forEach(button -> button.setDisable(disabled));
    }

    private Stream<Bitmap> selectedItems(ListView<Bitmap> jobsList) {
        return jobsList.getSelectionModel().getSelectedItems().stream();
    }

    private int computePercentage(double percentage) {
        return (int) Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);
    }

//    private ImageView createIconImage(String name, double percentage) {
//        final int size = computePercentage(percentage);
//        final InputStream stream = getClass().getResourceAsStream(name);
//        if (stream != null) {
//            final ImageView image = new ImageView(new Image(stream));
//            image.setSmooth(true);
//            image.setFitWidth(size);
//            image.setFitHeight(size);
//            return image;
//        } else {
//            final ImageView image = new ImageView();
//            image.setSmooth(true);
//            image.setFitWidth(size);
//            image.setFitHeight(size);
//            return image;
//        }
//    }

    private void updateJobList(ListView<Bitmap> jobsList) {
        final ObservableList<Bitmap> bitmaps = jobsList.getItems();
        for (int i = bitmaps.size() - 1; i >= 0; i--) {
            final Bitmap bitmap = bitmaps.get(i);
            final ExportSession session = (ExportSession) bitmap.getProperty("exportSession");
            final JobEntry exportEntry = exportEntries.get(session.getSessionId());
            if (exportEntry == null) {
                bitmaps.remove(i);
            } else {
                bitmap.setProgress(exportEntry.progress());
                if (jobsList.getSelectionModel().isSelected(i)) {
                    triggerUpdate(jobsList, bitmap, i);
                    jobsList.getSelectionModel().select(i);
                } else {
                    triggerUpdate(jobsList, bitmap, i);
                }
            }
        }
    }

    private <T> void triggerUpdate(ListView<T> listView, T newValue, int index) {
        listView.fireEvent(new ListView.EditEvent<>(listView, ListView.editCommitEvent(), newValue, index));
    }

    private void itemSelected(ListView<Bitmap> listView, Label sizeLabel, Label formatLabel, Label durationLabel) {
        final Bitmap bitmap = listView.getSelectionModel().getSelectedItem();
        if (bitmap != null) {
            final ExportSession session = (ExportSession) bitmap.getProperty("exportSession");
            if (session.getFrameCount() <= 1) {
                sizeLabel.setText(session.getFrameSize().width() + "\u00D7" + session.getFrameSize().height() + " pixels");
                formatLabel.setText(session.getEncoder().getName() + " Image");
            } else {
                sizeLabel.setText(session.getFrameSize().width() + "\u00D7" + session.getFrameSize().height() + " pixels");
                formatLabel.setText(session.getEncoder().getName() + " Video");
                final long durationInSeconds = (long)Math.rint(session.getFrameCount() / (float) session.getFrameRate());
                final long minutes = (long)Math.rint(durationInSeconds / 60.0);
                if (minutes <= 2) {
                    durationLabel.setText("Duration " + durationInSeconds + " seconds");
                } else {
                    durationLabel.setText("Duration " + minutes + " minutes");
                }
            }
        } else {
            sizeLabel.setText("");
            formatLabel.setText("");
            durationLabel.setText("");
        }
    }

    private void submitItem(ExportSession session, ImageComposer composer) {
        executor.submit(() -> Command.of(() -> renderImage(session, composer))
                .execute().optional().ifPresent(pixels -> Platform.runLater(() -> addItem(listView, session, pixels, composer.getSize()))));
    }

    private IntBuffer renderImage(ExportSession session, ImageComposer composer) {
        return composer.renderImage(session.getFrames().getFirst().script(), session.getFrames().getFirst().metadata());
    }

    private void addItem(ListView<Bitmap> listView, ExportSession session, IntBuffer pixels, Size size) {
        final BrowseBitmap bitmap = new BrowseBitmap(size.width(), size.height(), pixels);
        final JobEntry jobEntry = new JobEntry(session, ExportSessionState.READY, 0f, bitmap);
        exportEntries.put(session.getSessionId(), jobEntry);
        bitmap.setProperty("exportSession", session);
        listView.getItems().addFirst(bitmap);
    }

    public void updateSessions() {
        updateJobList(listView);
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }

    public void appendSession(ExportSession session) {
        Command.of(tryFindFactory(session.getFrames().getFirst().pluginId()))
                .map(this::createImageComposer)
                .execute()
                .optional()
                .ifPresent(composer -> submitItem(session, composer));
    }

    private ImageComposer createImageComposer(CoreFactory factory) {
        return factory.createImageComposer(ThreadUtils.createPlatformThreadFactory("Jobs Image Composer"), tile, true);
    }

    public void updateSession(ExportSession exportSession, ExportSessionState state, Float progress) {
        final JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        if (exportEntry != null) {
            final JobEntry jobEntry = new JobEntry(exportSession, state, progress, exportEntry.bitmap());
            jobEntry.bitmap().setProgress(exportEntry.progress());
//            final int index = listView.getItems().indexOf(jobEntry.bitmap());
//            if (listView.getSelectionModel().isSelected(index)) {
//                triggerUpdate(listView, jobEntry.bitmap(), index);
//                listView.getSelectionModel().select(index);
//            } else {
//                triggerUpdate(listView, jobEntry.bitmap(), index);
//            }
            exportEntries.put(exportSession.getSessionId(), jobEntry);
            listView.refresh();
        }
    }

    public void removeSession(ExportSession exportSession) {
        final JobEntry exportEntry = exportEntries.remove(exportSession.getSessionId());
        if (exportEntry != null) {
            listView.getItems().remove(exportEntry.bitmap());
        }
    }

    private record JobEntry(ExportSession exportSession, ExportSessionState state, Float progress, Bitmap bitmap) {}
}
