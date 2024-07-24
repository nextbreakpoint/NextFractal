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
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportState;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

public class JobsPane extends BorderPane {
    private static Logger logger = Logger.getLogger(JobsPane.class.getName());
    private static final int PADDING = 8;

    private Map<String, JobEntry> exportEntries = new HashMap<>();
    private final ExecutorService executor;
    private final ListView<Bitmap> listView;
    private JobsDelegate delegate;
    private RendererTile tile;

    public JobsPane(RendererTile tile) {
        this.tile = tile;

        listView = new ListView<>();
        listView.setFixedCellSize(tile.getTileSize().getHeight() + PADDING);
        listView.setCellFactory(view -> new JobsListCell(tile));

        HBox exportControls = new HBox(0);
        exportControls.setAlignment(Pos.CENTER);
        Button suspendButton = new Button("", createIconImage("/icon-suspend.png", 0.015));
        Button resumeButton = new Button("", createIconImage("/icon-resume.png", 0.015));
        Button removeButton = new Button("", createIconImage("/icon-remove.png", 0.015));
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

        Label sizeLabel = new Label();
        Label formatLabel = new Label();
        Label durationLabel = new Label();

        VBox detailsPane = new VBox(10);
        detailsPane.setAlignment(Pos.TOP_LEFT);
        detailsPane.getChildren().add(formatLabel);
        detailsPane.getChildren().add(sizeLabel);
        detailsPane.getChildren().add(durationLabel);
        detailsPane.getStyleClass().add("details");

        BorderPane jobsPane = new BorderPane();
        jobsPane.setCenter(listView);
        jobsPane.setBottom(detailsPane);

        setCenter(jobsPane);
        setBottom(exportControls);

        getStyleClass().add("jobs");

        List<Button> buttonsList = Arrays.asList(suspendButton, resumeButton, removeButton);
        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> updateButtons(buttonsList, c.getList().size() == 0));

        suspendButton.setOnAction(e -> selectedItems(listView).filter(bitmap -> !isExportSessionSuspended(bitmap))
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionSuspended((ExportSession) bitmap.getProperty("exportSession")))));

        resumeButton.setOnAction(e -> selectedItems(listView).filter(bitmap -> isExportSessionSuspended(bitmap))
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionResumed((ExportSession) bitmap.getProperty("exportSession")))));

        removeButton.setOnAction(e -> selectedItems(listView)
            .forEach(bitmap -> Optional.ofNullable(delegate).ifPresent(delegate -> delegate.sessionStopped((ExportSession) bitmap.getProperty("exportSession")))));

        listView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Bitmap> c) -> itemSelected(listView, sizeLabel, formatLabel, durationLabel));

        executor = Executors.newSingleThreadExecutor(createThreadFactory("Jobs"));
    }

    private boolean isExportSessionSuspended(Bitmap bitmap) {
        ExportSession exportSession = (ExportSession) bitmap.getProperty("exportSession");
        JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        return exportEntry != null && exportEntry.getState() == ExportState.SUSPENDED;
    }

    private DefaultThreadFactory createThreadFactory(String name) {
        return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
    }

    private void updateButtons(List<Button> buttons, boolean disabled) {
        buttons.stream().forEach(button -> button.setDisable(disabled));
    }

    private Stream<Bitmap> selectedItems(ListView<Bitmap> jobsList) {
        return jobsList.getSelectionModel().getSelectedItems().stream();
    }

    private int computePercentage(double percentage) {
        return (int)Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);
    }

    private ImageView createIconImage(String name, double percentage) {
        int size = computePercentage(percentage);
        InputStream stream = getClass().getResourceAsStream(name);
        if (stream != null) {
            ImageView image = new ImageView(new Image(stream));
            image.setSmooth(true);
            image.setFitWidth(size);
            image.setFitHeight(size);
            return image;
        } else {
            ImageView image = new ImageView();
            image.setSmooth(true);
            image.setFitWidth(size);
            image.setFitHeight(size);
            return image;
        }
    }

    private void updateJobList(ListView<Bitmap> jobsList) {
        ObservableList<Bitmap> bitmaps = jobsList.getItems();
        for (int i = bitmaps.size() - 1; i >= 0; i--) {
            Bitmap bitmap = bitmaps.get(i);
            ExportSession session = (ExportSession) bitmap.getProperty("exportSession");
            JobEntry exportEntry = exportEntries.get(session.getSessionId());
            if (exportEntry == null) {
                bitmaps.remove(i);
            } else {
                bitmap.setProgress(exportEntry.getProgress());
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
        Bitmap bitmap = listView.getSelectionModel().getSelectedItem();
        if (bitmap != null) {
            ExportSession session = (ExportSession) bitmap.getProperty("exportSession");
            if (session.getFrameCount() <= 1) {
                sizeLabel.setText(session.getSize().getWidth() + "\u00D7" + session.getSize().getHeight() + " pixels");
                formatLabel.setText(session.getEncoder().getName() + " Image");
            } else {
                sizeLabel.setText(session.getSize().getWidth() + "\u00D7" + session.getSize().getHeight() + " pixels");
                formatLabel.setText(session.getEncoder().getName() + " Video");
                long durationInSeconds = (long)Math.rint(session.getFrameCount() / session.getFrameRate());
                long minutes = (long)Math.rint(durationInSeconds / 60.0);
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

    private void addItem(ListView<Bitmap> listView, ExportSession session, IntBuffer pixels, RendererSize size) {
        BrowseBitmap bitmap = new BrowseBitmap(size.getWidth(), size.getHeight(), pixels);
        JobEntry jobEntry = new JobEntry(session, ExportState.READY, 0f, bitmap);
        exportEntries.put(session.getSessionId(), jobEntry);
        bitmap.setProperty("exportSession", session);
        listView.getItems().add(0, bitmap);
    }

    public void setDelegate(JobsDelegate delegate) {
        this.delegate = delegate;
    }

    public void updateSessions() {
        updateJobList(listView);
    }

    public void dispose() {
        List<ExecutorService> executors = Arrays.asList(executor);
        executors.forEach(executor -> executor.shutdownNow());
        executors.forEach(executor -> await(executor));
    }

    private void await(ExecutorService executor) {
        Command.of(() -> executor.awaitTermination(5000, TimeUnit.MILLISECONDS))
                .execute()
                .observe()
                .onFailure(e -> logger.warning("Await termination timeout"))
                .get();
    }

    public void appendSession(ExportSession session) {
        Command.of(tryFindFactory(session.getFrames().getFirst().pluginId()))
                .map(this::createImageComposer)
                .execute()
                .optional()
                .ifPresent(composer -> submitItem(session, composer));
    }

    private ImageComposer createImageComposer(CoreFactory factory) {
        return factory.createImageComposer(createThreadFactory("Jobs Composer"), tile, true);
    }

    public void updateSession(ExportSession exportSession, ExportState state, Float progress) {
        JobEntry exportEntry = exportEntries.get(exportSession.getSessionId());
        if (exportEntry != null) {
            JobEntry jobEntry = new JobEntry(exportSession, state, progress, exportEntry.getBitmap());
            jobEntry.getBitmap().setProgress(exportEntry.getProgress());
            int index = listView.getItems().indexOf(jobEntry.getBitmap());
            if (listView.getSelectionModel().isSelected(index)) {
                triggerUpdate(listView, jobEntry.getBitmap(), index);
                listView.getSelectionModel().select(index);
            } else {
                triggerUpdate(listView, jobEntry.getBitmap(),index);
            }
            exportEntries.put(exportSession.getSessionId(), jobEntry);
        }
    }

    public void removeSession(ExportSession exportSession) {
        JobEntry exportEntry = exportEntries.remove(exportSession.getSessionId());
        if (exportEntry != null) {
            listView.getItems().remove(exportEntry.getBitmap());
        }
    }

    private class JobEntry {
        private ExportSession exportSession;
        private ExportState state;
        private Float progress;
        private Bitmap bitmap;

        public JobEntry(ExportSession exportSession, ExportState state, Float progress, Bitmap bitmap) {
            this.exportSession = exportSession;
            this.state = state;
            this.progress = progress;
            this.bitmap = bitmap;
        }

        public ExportSession getExportSession() {
            return exportSession;
        }

        public ExportState getState() {
            return state;
        }

        public Float getProgress() {
            return progress;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}
