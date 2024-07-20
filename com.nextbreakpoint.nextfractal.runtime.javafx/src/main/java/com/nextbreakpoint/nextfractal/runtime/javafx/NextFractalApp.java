/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.runtime.javafx;

import com.nextbreakpoint.nextfractal.core.event.CaptureClipAdded;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipMoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRemoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRestored;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionActionFired;
import com.nextbreakpoint.nextfractal.core.event.EditorActionFired;
import com.nextbreakpoint.nextfractal.core.event.EditorDeleteFilesRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorGrammarSelected;
import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorSaveFileRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorStoreFileRequested;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionCreated;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionResumed;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionStateChanged;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionSuspended;
import com.nextbreakpoint.nextfractal.core.event.SessionBundleLoaded;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionErrorChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionExportRequested;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.event.WorkspaceChanged;
import com.nextbreakpoint.nextfractal.core.export.ExportRenderer;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.runtime.export.ExportServiceDelegate;
import com.nextbreakpoint.nextfractal.runtime.export.SimpleExportRenderer;
import com.nextbreakpoint.nextfractal.runtime.javafx.component.MainCentralPane;
import com.nextbreakpoint.nextfractal.runtime.javafx.component.MainSidePane;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.ApplicationHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.PlaybackSourceHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.SessionSourceHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.SimpleExportService;
import com.nextbreakpoint.nextfractal.runtime.javafx.utils.ApplicationUtils;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.java.Log;

import java.io.File;

import static com.nextbreakpoint.nextfractal.runtime.javafx.utils.Constants.DEFAULT_PLUGIN_ID;

@Log
public class NextFractalApp extends Application {
    private File workspace;
    private File examples;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        workspace = ApplicationUtils.getWorkspace();
        examples = ApplicationUtils.getExamples();

        final PlatformEventBus eventBus = new PlatformEventBus("Main");

        log.info(ApplicationUtils.getNoticeMessage());

        ApplicationUtils.printPlugins();

        ApplicationUtils.checkJavaCompiler();

        final Screen primaryScreen = Screen.getPrimary();

        final Rectangle2D visualBounds = primaryScreen.getVisualBounds();
        log.info("Screen size = (" + visualBounds.getWidth() + ", " + visualBounds.getHeight() + "), dpi = " + primaryScreen.getDpi());

        int renderSize = ApplicationUtils.computeRenderSize(visualBounds);
        int editorWidth = (int) Math.rint(renderSize * 0.7);
        log.info("Optimal image size = " + renderSize + "px");

        final int optimalFontSize =  ApplicationUtils.computeOptimalFontSize(primaryScreen);
        log.info("Optimal font size = " + optimalFontSize + "pt");

        final StackPane rootPane = new StackPane();

        final DoubleProperty fontSize = new SimpleDoubleProperty(optimalFontSize);
        rootPane.styleProperty().bind(Bindings.format("-fx-font-size: %.2fpt;", fontSize));

        final ExportServiceDelegate delegate = (session, state, progress) -> eventBus.postEvent(ExportSessionStateChanged.builder().session(session).state(state).progress(progress).build());
        final ExportRenderer exportRenderer = new SimpleExportRenderer(ThreadUtils.createThreadFactory("Export Renderer"));
        final ExportService exportService = new SimpleExportService(delegate, ThreadUtils.createThreadFactory("Export Service"), exportRenderer);

        final var applicationHandler = new ApplicationHandler(eventBus);
        final var sessionSourceHandler = new SessionSourceHandler(eventBus);
        final var playbackSourceHandler = new PlaybackSourceHandler(eventBus);

        eventBus.subscribe(EditorGrammarSelected.class.getSimpleName(), event -> applicationHandler.handleGrammarSelected(((EditorGrammarSelected) event).grammar()));

        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> applicationHandler.handleSessionChanged(((SessionDataChanged) event).session()));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> applicationHandler.handleSessionTerminate(exportService));

        eventBus.subscribe(ExportSessionCreated.class.getSimpleName(), event -> applicationHandler.handleExportSessionCreated(exportService, ((ExportSessionCreated) event).session()));

        eventBus.subscribe(ExportSessionStopped.class.getSimpleName(), event -> applicationHandler.handleExportSessionStopped(exportService, ((ExportSessionStopped) event).session()));

        eventBus.subscribe(ExportSessionResumed.class.getSimpleName(), event -> applicationHandler.handleExportSessionResumed(exportService, ((ExportSessionResumed) event).session()));

        eventBus.subscribe(ExportSessionSuspended.class.getSimpleName(), event -> applicationHandler.handleExportSessionSuspended(exportService, ((ExportSessionSuspended) event).session()));

        eventBus.subscribe(EditorLoadFileRequested.class.getSimpleName(), event -> applicationHandler.handleLoadFile(((EditorLoadFileRequested) event).file()));

        eventBus.subscribe(EditorSaveFileRequested.class.getSimpleName(), event -> applicationHandler.handleSaveFile(((EditorSaveFileRequested) event).file()));

        eventBus.subscribe(EditorStoreFileRequested.class.getSimpleName(), event -> applicationHandler.handleStoreFile(((EditorStoreFileRequested) event).file()));

        eventBus.subscribe(EditorDeleteFilesRequested.class.getSimpleName(), event -> applicationHandler.handleDeleteFiles(((EditorDeleteFilesRequested) event).files()));

        eventBus.subscribe(SessionErrorChanged.class.getSimpleName(), event -> applicationHandler.handleErrorChanged(((SessionErrorChanged) event).error()));

        eventBus.subscribe(CaptureSessionActionFired.class.getSimpleName(), event -> applicationHandler.handleCaptureSession(((CaptureSessionActionFired) event).action()));

        eventBus.subscribe(CaptureClipRestored.class.getSimpleName(), event -> applicationHandler.handleClipRestored(((CaptureClipRestored) event).clip()));

        eventBus.subscribe(CaptureClipRemoved.class.getSimpleName(), event -> applicationHandler.handleClipRemoved(((CaptureClipRemoved) event).clip()));

        eventBus.subscribe(CaptureClipAdded.class.getSimpleName(), event -> applicationHandler.handleClipAdded(((CaptureClipAdded) event).clip()));

        eventBus.subscribe(CaptureClipMoved.class.getSimpleName(), event -> applicationHandler.handleClipMoved(((CaptureClipMoved) event).fromIndex(), ((CaptureClipMoved) event).toIndex()));

        eventBus.subscribe(SessionBundleLoaded.class.getSimpleName(), event -> applicationHandler.handleBundleLoaded((SessionBundleLoaded) event));

        eventBus.subscribe(SessionExportRequested.class.getSimpleName(), event -> applicationHandler.handleExportSession(primaryStage, (SessionExportRequested) event));

        eventBus.subscribe(EditorActionFired.class.getSimpleName(), event -> applicationHandler.handleEditorAction(primaryStage, ((EditorActionFired) event).action()));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> sessionSourceHandler.handleSessionTerminate());

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> playbackSourceHandler.handleSessionTerminate());

        final Pane mainPane = createMainPane(eventBus, editorWidth, renderSize, renderSize);

        rootPane.getChildren().add(mainPane);

        final Scene scene = new Scene(rootPane, renderSize + editorWidth, renderSize);
        log.info("Scene size = (" + scene.getWidth() + ", " + scene.getHeight() + ")");

        ApplicationUtils.loadStyleSheets(scene);

        primaryStage.setOnCloseRequest(e -> {
            if (!applicationHandler.handleConfirmTerminate(exportService)) {
                e.consume();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setTitle(ApplicationUtils.getApplicationName());
        primaryStage.show();

        // the following events are required to initialise the application
        Platform.runLater(() -> eventBus.postEvent(WorkspaceChanged.builder().file(workspace).build()));
        Platform.runLater(() -> applicationHandler.handleGrammarSelected(System.getProperty("com.nextbreakpoint.nextfractal.default.grammar", DEFAULT_PLUGIN_ID)));
    }

    private Pane createMainPane(PlatformEventBus eventBus, int editorWidth, int renderWidth, int height) {
        final int width = renderWidth + editorWidth;
        final Pane mainPane = new Pane();
        mainPane.setPrefWidth(width);
        mainPane.setPrefHeight(height);
        mainPane.setMinWidth(width);
        mainPane.setMinHeight(height);
        mainPane.setMaxWidth(width);
        mainPane.setMaxHeight(height);
        final Pane centralPane = createCentralPane(eventBus, renderWidth, height);
        final Pane sidePane = createSidePane(eventBus, editorWidth, height);
        mainPane.getChildren().add(centralPane);
        mainPane.getChildren().add(sidePane);
        mainPane.getStyleClass().add("application");
        sidePane.setLayoutX(renderWidth);
        return mainPane;
    }

    private Pane createCentralPane(PlatformEventBus eventBus, int width, int height) {
        final MainCentralPane pane = new MainCentralPane(eventBus, width, height, workspace, examples);
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        pane.setMinWidth(width);
        pane.setMinHeight(height);
        pane.setMaxWidth(width);
        pane.setMaxHeight(height);
        pane.getStyleClass().add("central-pane");
        return pane;
    }

    private Pane createSidePane(PlatformEventBus eventBus, int width, int height) {
        final MainSidePane pane = new MainSidePane(eventBus);
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        pane.getStyleClass().add("side-pane");
        return pane;
    }
}
