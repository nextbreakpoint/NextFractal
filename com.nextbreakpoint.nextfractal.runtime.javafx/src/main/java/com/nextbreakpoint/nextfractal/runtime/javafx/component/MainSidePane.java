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
package com.nextbreakpoint.nextfractal.runtime.javafx.component;

import com.nextbreakpoint.nextfractal.core.common.Clip;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipAdded;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipMoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRemoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRestored;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipsLoaded;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipsMerged;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionActionFired;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStarted;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.EditorActionFired;
import com.nextbreakpoint.nextfractal.core.event.EditorParamsChanged;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionCreated;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionResumed;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionStateChanged;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionSuspended;
import com.nextbreakpoint.nextfractal.core.event.HistorySessionAdded;
import com.nextbreakpoint.nextfractal.core.event.HistorySessionSelected;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStarted;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStopped;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.SessionErrorChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionExportRequested;
import com.nextbreakpoint.nextfractal.core.event.SessionStatusChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.event.ToggleBrowserRequested;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportState;
import com.nextbreakpoint.nextfractal.core.javafx.ExportDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.ExportPane;
import com.nextbreakpoint.nextfractal.core.javafx.HistoryPane;
import com.nextbreakpoint.nextfractal.core.javafx.JobsDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.JobsPane;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.core.javafx.StatusPane;
import com.nextbreakpoint.nextfractal.core.javafx.StringObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.editor.ScriptEditor;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import com.nextbreakpoint.nextfractal.core.common.TileUtils;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import lombok.extern.java.Log;

import java.util.List;

import static com.nextbreakpoint.nextfractal.core.javafx.Icons.createIconImage;

@Log
public class MainSidePane extends BorderPane {
    public MainSidePane(PlatformEventBus eventBus) {
        setCenter(createRootPane(eventBus));
    }

    private Pane createRootPane(PlatformEventBus eventBus) {
        final RendererTile tile = createRendererTile();

        final StringObservableValue errorProperty = new StringObservableValue();

        final ScriptEditor editorPane = new ScriptEditor(eventBus);

        final MainParamsPane paramsPane = new MainParamsPane(eventBus);

        final StatusPane statusPane = new StatusPane();

        final ExportPane exportPane = new ExportPane(tile);

        final HistoryPane historyPane = new HistoryPane(tile);

        final JobsPane jobsPane = new JobsPane(tile);

        final StackPane sidebarPane = new StackPane();
        sidebarPane.getStyleClass().add("sidebar");
        sidebarPane.getChildren().add(jobsPane);
        sidebarPane.getChildren().add(historyPane);
        sidebarPane.getChildren().add(exportPane);
        sidebarPane.getChildren().add(paramsPane);

        final Pane sourcePane = new Pane();
        final HBox sourceButtons = new HBox(0);
        sourceButtons.setAlignment(Pos.CENTER);
        final Button browseButton = new Button("", createIconImage("/icon-grid.png"));
        final Button storeButton = new Button("", createIconImage("/icon-store.png"));
        final Button renderButton = new Button("", createIconImage("/icon-run.png"));
        final Button loadButton = new Button("", createIconImage("/icon-load.png"));
        final Button saveButton = new Button("", createIconImage("/icon-save.png"));
        final ToggleButton jobsButton = new ToggleButton("", createIconImage("/icon-tool.png"));
        final ToggleButton paramsButton = new ToggleButton("", createIconImage("/icon-edit.png"));
        final ToggleButton exportButton = new ToggleButton("", createIconImage("/icon-export.png"));
        final ToggleButton historyButton = new ToggleButton("", createIconImage("/icon-time.png"));
        final ToggleButton statusButton = new ToggleButton("", createIconImage("/icon-warn.png"));
        browseButton.setTooltip(new Tooltip("Show/hide projects"));
        storeButton.setTooltip(new Tooltip("Save project"));
        renderButton.setTooltip(new Tooltip("Render image"));
        loadButton.setTooltip(new Tooltip("Load project from file"));
        saveButton.setTooltip(new Tooltip("Save project to file"));
        jobsButton.setTooltip(new Tooltip("Show/hide background jobs"));
        paramsButton.setTooltip(new Tooltip("Show/hide project parameters"));
        exportButton.setTooltip(new Tooltip("Show/hide capture and export controls"));
        historyButton.setTooltip(new Tooltip("Show/hide changes history"));
        statusButton.setTooltip(new Tooltip("Show/hide errors console"));
        sourceButtons.getChildren().add(browseButton);
        sourceButtons.getChildren().add(storeButton);
        sourceButtons.getChildren().add(loadButton);
        sourceButtons.getChildren().add(saveButton);
        sourceButtons.getChildren().add(renderButton);
        sourceButtons.getChildren().add(paramsButton);
        sourceButtons.getChildren().add(historyButton);
        sourceButtons.getChildren().add(exportButton);
        sourceButtons.getChildren().add(jobsButton);
        sourceButtons.getChildren().add(statusButton);
        sourceButtons.getStyleClass().add("toolbar");
        sourceButtons.getStyleClass().add("menubar");
        sourcePane.getChildren().add(editorPane);
        sourcePane.getChildren().add(sourceButtons);
        sourcePane.getChildren().add(statusPane);
        sourcePane.getChildren().add(sidebarPane);
        browseButton.setOnAction(e -> eventBus.postEvent(ToggleBrowserRequested.builder().build()));
        storeButton.setOnAction(e -> eventBus.postEvent(EditorActionFired.builder().action("store").build()));
        renderButton.setOnAction(e -> eventBus.postEvent(EditorActionFired.builder().action("reload").build()));
        loadButton.setOnAction(e -> eventBus.postEvent(EditorActionFired.builder().action("load").build()));
        saveButton.setOnAction(e -> eventBus.postEvent(EditorActionFired.builder().action("save").build()));

        final TranslateTransition sidebarTransition = createTranslateTransition(sidebarPane);
        final TranslateTransition statusTransition = createTranslateTransition(statusPane);

        statusButton.setSelected(true);

        final ToggleGroup viewGroup = new ToggleGroup();
        viewGroup.getToggles().add(jobsButton);
        viewGroup.getToggles().add(historyButton);
        viewGroup.getToggles().add(paramsButton);
        viewGroup.getToggles().add(exportButton);

        final StackPane rootPane = new StackPane();
        rootPane.getChildren().add(sourcePane);

        rootPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            double width = newValue.doubleValue();
            sourceButtons.setPrefWidth(width);
            editorPane.setPrefWidth(width);
            sidebarPane.setPrefWidth(width * 0.4);
            statusPane.setPrefWidth(width);
            sourceButtons.setLayoutX(0);
            editorPane.setLayoutX(0);
            sidebarPane.setLayoutX(width);
            statusPane.setLayoutX(0);
        });

        rootPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            double height = newValue.doubleValue();
            sourceButtons.setPrefHeight(height * 0.07);
            editorPane.setPrefHeight(height * 0.78);
            sidebarPane.setPrefHeight(height * 0.78);
            statusPane.setPrefHeight(height * 0.15);
            sourceButtons.setLayoutY(0);
            editorPane.setLayoutY(height * 0.07);
            sidebarPane.setLayoutY(height * 0.07);
            statusPane.setLayoutY(height * 0.85);
        });

        sidebarPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            double width = newValue.doubleValue();
            historyPane.setPrefWidth(width);
            paramsPane.setPrefWidth(width);
            exportPane.setPrefWidth(width);
            jobsPane.setPrefWidth(width);
            historyPane.setMaxWidth(width);
            paramsPane.setMaxWidth(width);
            exportPane.setMaxWidth(width);
            jobsPane.setMaxWidth(width);
        });

        sidebarPane.heightProperty().addListener((observable, oldValue, newValue) -> {
            double height = newValue.doubleValue();
            historyPane.setPrefHeight(height);
            paramsPane.setPrefHeight(height);
            exportPane.setPrefHeight(height);
            jobsPane.setPrefHeight(height);
            historyPane.setMaxHeight(height);
            paramsPane.setMaxHeight(height);
            exportPane.setMaxHeight(height);
            jobsPane.setMaxHeight(height);
        });

        errorProperty.addListener((source, oldValue, newValue) -> {
            storeButton.setDisable(newValue != null);
            saveButton.setDisable(newValue != null);
            exportPane.setDisable(newValue != null);
            paramsPane.setParamsDisable(newValue != null);
        });

        historyButton.selectedProperty().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                sidebarPane.getChildren().remove(historyPane);
                sidebarPane.getChildren().add(historyPane);
            }
        });

        paramsButton.selectedProperty().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                sidebarPane.getChildren().remove(paramsPane);
                sidebarPane.getChildren().add(paramsPane);
            }
        });

        jobsButton.selectedProperty().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                sidebarPane.getChildren().remove(jobsPane);
                sidebarPane.getChildren().add(jobsPane);
            }
        });

        exportButton.selectedProperty().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                sidebarPane.getChildren().remove(exportPane);
                sidebarPane.getChildren().add(exportPane);
            }
        });

        viewGroup.selectedToggleProperty().addListener((source, oldValue, newValue) -> {
            if (newValue != null) {
                showSidebar(sidebarTransition, a -> {
                });
            } else {
                hideSidebar(sidebarTransition, a -> {
                });
            }
        });

        statusButton.selectedProperty().addListener((source, oldValue, newValue) -> {
            if (newValue) {
                showStatus(statusTransition, a -> {
                });
            } else {
                hideStatus(statusTransition, a -> {
                });
            }
        });

        sidebarPane.translateXProperty().addListener((source, oldValue, newValue) -> {
            final double width = rootPane.getWidth() + newValue.doubleValue();
            editorPane.prefWidthProperty().setValue(width);
        });

        statusPane.translateYProperty().addListener((source, oldValue, newValue) -> {
            final double height = rootPane.getHeight() - statusPane.getHeight() - sourceButtons.getHeight() + newValue.doubleValue();
            editorPane.prefHeightProperty().setValue(height);
            sidebarPane.prefHeightProperty().setValue(height);
        });

        exportPane.setExportDelegate(new ExportDelegate() {
            @Override
            public void createSession(RendererSize size, String format) {
                if (errorProperty.getValue() == null) {
                    eventBus.postEvent(SessionExportRequested.builder().size(size).format(format).build());
                }
            }

            @Override
            public void startCaptureSession() {
                if (errorProperty.getValue() == null) {
                    eventBus.postEvent(CaptureSessionActionFired.builder().action("start").build());
                }
            }

            @Override
            public void stopCaptureSession() {
                if (errorProperty.getValue() == null) {
                    eventBus.postEvent(CaptureSessionActionFired.builder().action("stop").build());
                }
            }

            @Override
            public void playbackStart(List<Clip> clips) {
                if (errorProperty.getValue() == null) {
                    eventBus.postEvent(PlaybackStarted.builder().clips(clips).build());
                }
            }

            @Override
            public void captureSessionAdded(Clip clip) {
                eventBus.postEvent(CaptureClipAdded.builder().clip(clip).build());
            }

            @Override
            public void captureSessionRemoved(Clip clip) {
                eventBus.postEvent(CaptureClipRemoved.builder().clip(clip).build());
            }

            @Override
            public void captureSessionRestored(Clip clip) {
                eventBus.postEvent(CaptureClipRestored.builder().clip(clip).build());
            }

            @Override
            public void captureSessionMoved(int fromIndex, int toIndex) {
                eventBus.postEvent(CaptureClipMoved.builder().fromIndex(fromIndex).toIndex(toIndex).build());
            }
        });

        jobsPane.setDelegate(new JobsDelegate() {
            @Override
            public void sessionSuspended(ExportSession session) {
                eventBus.postEvent(ExportSessionSuspended.builder().session(session).build());
            }

            @Override
            public void sessionResumed(ExportSession session) {
                eventBus.postEvent(ExportSessionResumed.builder().session(session).build());
            }

            @Override
            public void sessionStopped(ExportSession session) {
                eventBus.postEvent(ExportSessionStopped.builder().session(session).build());
            }
        });

        historyPane.setDelegate(session -> eventBus.postEvent(HistorySessionSelected.builder().session(session).build()));

        eventBus.subscribe(SessionStatusChanged.class.getSimpleName(), event -> statusPane.setMessage(((SessionStatusChanged) event).status()));

        eventBus.subscribe(SessionErrorChanged.class.getSimpleName(), event -> errorProperty.setValue(((SessionErrorChanged) event).error()));

        eventBus.subscribe(HistorySessionAdded.class.getSimpleName(), event -> historyPane.appendSession(((HistorySessionAdded) event).session()));

        eventBus.subscribe(ExportSessionCreated.class.getSimpleName(), event -> jobsButton.setSelected(true));
        eventBus.subscribe(ExportSessionCreated.class.getSimpleName(), event -> jobsPane.appendSession(((ExportSessionCreated) event).session()));

        eventBus.subscribe(CaptureSessionStarted.class.getSimpleName(), event -> handleSessionStarted(exportPane, exportButton, ((CaptureSessionStarted) event).clip()));

        eventBus.subscribe(CaptureSessionStopped.class.getSimpleName(), event -> handleSessionStopped(exportPane, exportButton, ((CaptureSessionStopped) event).clip()));

        eventBus.subscribe(CaptureClipsLoaded.class.getSimpleName(), event -> exportPane.loadClips(((CaptureClipsLoaded) event).clips()));

        eventBus.subscribe(CaptureClipsMerged.class.getSimpleName(), event -> exportPane.mergeClips(((CaptureClipsMerged) event).clips()));

        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> errorProperty.setValue(null));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> jobsPane.dispose());
        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> historyPane.dispose());

        eventBus.subscribe(ExportSessionStateChanged.class.getSimpleName(), event -> handleExportSessionStateChanged(jobsPane, ((ExportSessionStateChanged) event).session(), ((ExportSessionStateChanged) event).state(), ((ExportSessionStateChanged) event).progress()));

        eventBus.subscribe(CaptureSessionStarted.class.getSimpleName(), event -> exportPane.setCaptureSelected(true));

        eventBus.subscribe(CaptureSessionStopped.class.getSimpleName(), event -> exportPane.setCaptureSelected(false));

        eventBus.subscribe(PlaybackStarted.class.getSimpleName(), event -> handlePlaybackClipsStart(rootPane));

        eventBus.subscribe(PlaybackStopped.class.getSimpleName(), event -> handlePlaybackClipsStop(rootPane));

        eventBus.subscribe(HistorySessionSelected.class.getSimpleName(), event -> handleHistorySessionSelected(eventBus, (HistorySessionSelected) event));

        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionDataChanged(eventBus, (SessionDataChanged) event));

        return rootPane;
    }

    private void handleSessionDataChanged(PlatformEventBus eventBus, SessionDataChanged event) {
        if (!event.continuous()) {
            eventBus.postEvent(EditorParamsChanged.builder().session(event.session()).build());
        }
    }

    private static void handleHistorySessionSelected(PlatformEventBus eventBus, HistorySessionSelected event) {
        eventBus.postEvent(SessionDataLoaded.builder().session(event.session()).continuous(false).appendToHistory(false).build());
    }

    private void handlePlaybackClipsStart(Pane rootPane) {
        rootPane.setDisable(true);
        BoxBlur effect = new BoxBlur();
        effect.setIterations(1);
        rootPane.setEffect(effect);
    }

    private void handlePlaybackClipsStop(Pane rootPane) {
        rootPane.setEffect(null);
        rootPane.setDisable(false);
    }

    private void handleExportSessionStateChanged(JobsPane jobsPane, ExportSession exportSession, ExportState state, Float progress) {
        log.info("Session " + exportSession.getSessionId() + " state " + state.name());
        if (state == ExportState.FINISHED) {
            jobsPane.removeSession(exportSession);
        } else {
            jobsPane.updateSession(exportSession, state, progress);
        }
    }

    private void handleSessionStarted(ExportPane exportPane, ToggleButton exportButton, Clip clip) {
        log.info("Session started");
    }

    private void handleSessionStopped(ExportPane exportPane, ToggleButton exportButton, Clip clip) {
        log.info("Session stopped: clip duration " + clip.duration() + "ms");
        if (!clip.isEmpty()) exportPane.appendClip(clip);
        exportButton.setSelected(true);
    }

    private static TranslateTransition createTranslateTransition(Node node) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(javafx.util.Duration.seconds(0.5));
        return transition;
    }

    private static void showSidebar(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateX() != -((Pane) transition.getNode()).getWidth()) {
            transition.setFromX(transition.getNode().getTranslateX());
            transition.setToX(-((Pane) transition.getNode()).getWidth());
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private static void hideSidebar(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateX() != 0) {
            transition.setFromX(transition.getNode().getTranslateX());
            transition.setToX(0);
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private static void showStatus(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateY() != 0) {
            transition.setFromY(transition.getNode().getTranslateY());
            transition.setToY(0);
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private static void hideStatus(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateY() != ((Pane) transition.getNode()).getHeight()) {
            transition.setFromY(transition.getNode().getTranslateY());
            transition.setToY(((Pane) transition.getNode()).getHeight());
            transition.setOnFinished(handler);
            transition.play();
        }
    }

//    private static void showPanel(TranslateTransition transition, EventHandler<ActionEvent> handler) {
//        transition.stop();
//        if (transition.getNode().getTranslateX() != -((Pane) transition.getNode()).getWidth()) {
//            transition.setFromX(transition.getNode().getTranslateX());
//            transition.setToX(-((Pane) transition.getNode()).getWidth());
//            transition.setOnFinished(handler);
//            transition.play();
//        }
//    }
//
//    private static void hidePanel(TranslateTransition transition, EventHandler<ActionEvent> handler) {
//        transition.stop();
//        if (transition.getNode().getTranslateX() != 0) {
//            transition.setFromX(transition.getNode().getTranslateX());
//            transition.setToX(0);
//            transition.setOnFinished(handler);
//            transition.play();
//        }
//    }

    private static RendererTile createRendererTile() {
        return TileUtils.createRendererTile(Screen.getPrimary().getVisualBounds().getWidth());
    }
}
