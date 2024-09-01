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
package com.nextbreakpoint.nextfractal.runtime.javafx;

import com.nextbreakpoint.nextfractal.core.event.ExportSessionStateChanged;
import com.nextbreakpoint.nextfractal.core.event.WorkspaceChanged;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.export.ExportSessionState;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.runtime.export.SimpleExportRenderer;
import com.nextbreakpoint.nextfractal.runtime.javafx.component.MainCentralPane;
import com.nextbreakpoint.nextfractal.runtime.javafx.component.MainSidePane;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.ApplicationHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.PlaybackSourceHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.SessionSourceHandler;
import com.nextbreakpoint.nextfractal.runtime.javafx.core.SimpleExportService;
import com.nextbreakpoint.nextfractal.runtime.javafx.utils.ApplicationUtils;
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

@Log
public class NextFractalApp extends Application {
    private PlatformEventBus eventBus;
    private File workspace;
    private File examples;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        eventBus = new PlatformEventBus("Main");

        workspace = ApplicationUtils.getWorkspace();
        examples = ApplicationUtils.getExamples();

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

        final ExportService exportService = new SimpleExportService(new SimpleExportRenderer(), this::onSessionChanged);

        final Pane mainPane = createMainPane(eventBus, editorWidth, renderSize, renderSize);

        rootPane.getChildren().add(mainPane);

        final Scene scene = new Scene(rootPane, renderSize + editorWidth, renderSize);
        log.info("Scene size = (" + scene.getWidth() + ", " + scene.getHeight() + ")");

        ApplicationUtils.loadStyleSheets(scene);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setTitle(ApplicationUtils.getApplicationName());
        primaryStage.show();

        // the following events are required to initialise the application
        Platform.runLater(() -> eventBus.postEvent(WorkspaceChanged.builder().file(workspace).build()));

        new ApplicationHandler(eventBus, exportService, primaryStage);
        new SessionSourceHandler(eventBus);
        new PlaybackSourceHandler(eventBus);
    }

    private void onSessionChanged(ExportSession session, ExportSessionState state, float progress) {
        eventBus.postEvent(ExportSessionStateChanged.builder().session(session).state(state).progress(progress).build());
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
