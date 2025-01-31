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
package com.nextbreakpoint.nextfractal.runtime.javafx.component;

import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStarted;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.EditorDeleteFilesRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataChanged;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStarted;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStopped;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.event.ToggleBrowserRequested;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.core.javafx.browse.BrowseDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.browse.BrowsePane;
import com.nextbreakpoint.nextfractal.core.javafx.observable.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.playback.PlaybackDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.playback.PlaybackPane;
import com.nextbreakpoint.nextfractal.core.javafx.playback.RecordingPane;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class MainCentralPane extends BorderPane {
    public MainCentralPane(PlatformEventBus eventBus, File workspace, File examples) {
        final MainRenderPane renderPane = new MainRenderPane(eventBus);

        final BrowsePane browsePane = new BrowsePane(workspace, examples);

        final PlaybackPane playbackPane = new PlaybackPane();
        final RecordingPane recordingPane = new RecordingPane();

        final BooleanObservableValue toggleProperty = new BooleanObservableValue();
        toggleProperty.setValue(false);

        final TranslateTransition browserTransition = createTranslateTransition(browsePane);

        browsePane.setDelegate(new BrowseDelegate() {
			@Override
			public void didSelectFile(BrowsePane source, File file) {
                eventBus.postEvent(ToggleBrowserRequested.builder().build());
                eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build());
			}

            @Override
            public void didDeleteFiles(List<File> files) {
                eventBus.postEvent(EditorDeleteFilesRequested.builder().files(files).build());
            }

            @Override
			public void didClose(BrowsePane source) {
                eventBus.postEvent(ToggleBrowserRequested.builder().build());
			}
		});

        playbackPane.setDelegate(new PlaybackDelegate() {
            @Override
            public void playbackStopped() {
                eventBus.postEvent(PlaybackStopped.builder().build());
            }

            @Override
            public void loadSessionData(Session session, boolean continuous, boolean appendToHistory) {
                final var event = PlaybackDataLoaded.builder()
                        .session(session)
                        .continuous(continuous)
                        .appendToHistory(appendToHistory)
                        .build();
                eventBus.postEvent(event);
            }

            @Override
            public void updateSessionData(Session session, boolean continuous, boolean appendToHistory) {
                final var event = PlaybackDataChanged.builder()
                        .session(session)
                        .continuous(continuous)
                        .appendToHistory(appendToHistory)
                        .build();
                eventBus.postEvent(event);
            }
        });

        final Pane stackPane = new Pane();
        stackPane.getChildren().add(renderPane);
        stackPane.getChildren().add(playbackPane);
        stackPane.getChildren().add(recordingPane);
        stackPane.getChildren().add(browsePane);

        playbackPane.setVisible(false);

        recordingPane.setDisable(true);
        recordingPane.setVisible(false);

        setCenter(stackPane);

        widthProperty().addListener((_, _, newValue) -> {
            final double width = newValue.doubleValue();
            renderPane.setPrefWidth(width);
            browsePane.setPrefWidth(width);
            playbackPane.setPrefWidth(width);
            recordingPane.setPrefWidth(width);
        });

        heightProperty().addListener((_, _, newValue) -> {
            final double height = newValue.doubleValue();
            renderPane.setPrefHeight(height);
            browsePane.setPrefHeight(height);
            playbackPane.setPrefHeight(height);
            recordingPane.setPrefHeight(height);
            browsePane.setTranslateY(-height);
        });

        toggleProperty.addListener((_, _, newValue) -> {
            if (newValue) {
                browsePane.enable();
                showBrowser(browserTransition, _ -> renderPane.disable());
                browsePane.reload();
            } else {
                renderPane.enable();
                hideBrowser(browserTransition, _ -> browsePane.disable());
            }
        });

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> browsePane.dispose());
        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> playbackPane.dispose());
        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> recordingPane.dispose());

        eventBus.subscribe(ToggleBrowserRequested.class.getSimpleName(), _ -> toggleProperty.setValue(!toggleProperty.getValue()));

        eventBus.subscribe(PlaybackStarted.class.getSimpleName(), event -> {
            browsePane.setDisable(true);
            renderPane.setDisable(true);
            toggleProperty.setValue(false);
            playbackPane.setVisible(true);
            playbackPane.setClips(((PlaybackStarted)event).clips());
            playbackPane.start();
        });

        eventBus.subscribe(PlaybackStopped.class.getSimpleName(), _ -> {
            playbackPane.stop();
            browsePane.setDisable(false);
            renderPane.setDisable(false);
            playbackPane.setVisible(false);
        });

        eventBus.subscribe(CaptureSessionStarted.class.getSimpleName(), _ -> {
            recordingPane.setVisible(true);
            recordingPane.start();
        });

        eventBus.subscribe(CaptureSessionStopped.class.getSimpleName(), _ -> {
            recordingPane.setVisible(false);
            recordingPane.stop();
        });

        renderPane.enable();
    }

//    private void handleHideControls(FadeTransition transition, Boolean hide) {
//        if (hide) {
//            fadeOut(transition, x -> {});
//        } else {
//            fadeIn(transition, x -> {});
//        }
//    }
//
//    private void fadeOut(FadeTransition transition, EventHandler<ActionEvent> handler) {
//        transition.stop();
//        if (transition.getNode().getOpacity() != 0) {
//            transition.setFromValue(transition.getNode().getOpacity());
//            transition.setToValue(0);
//            transition.setOnFinished(handler);
//            transition.play();
//        }
//    }
//
//    private void fadeIn(FadeTransition transition, EventHandler<ActionEvent> handler) {
//        transition.stop();
//        if (transition.getNode().getOpacity() != 0.9) {
//            transition.setFromValue(transition.getNode().getOpacity());
//            transition.setToValue(0.9);
//            transition.setOnFinished(handler);
//            transition.play();
//        }
//    }

    private void showBrowser(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateY() != 0) {
            transition.setFromY(transition.getNode().getTranslateY());
            transition.setToY(0);
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private void hideBrowser(TranslateTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getTranslateY() != -((Pane)transition.getNode()).getHeight()) {
            transition.setFromY(transition.getNode().getTranslateY());
            transition.setToY(-((Pane)transition.getNode()).getHeight());
            transition.setOnFinished(handler);
            transition.play();
        }
    }

//    private FadeTransition createFadeTransition(Node node) {
//        FadeTransition transition = new FadeTransition();
//        transition.setNode(node);
//        transition.setDuration(Duration.seconds(0.5));
//        return transition;
//    }

    private TranslateTransition createTranslateTransition(Node node) {
        TranslateTransition transition = new TranslateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(1.0));
        transition.setInterpolator(Interpolator.EASE_BOTH);
//        transition.setInterpolator(new BounceInterpolator());
        return transition;
    }

    private static class BounceInterpolator extends Interpolator {
        @Override
        protected double curve(double t) {
            double freq = 2;
            double decay = 2;
            double dur = 0.7;
            if (t < dur) {
                return t;
            } else {
                double amp = 1/dur;
                double w = freq*Math.PI*2;
                return 1 + amp*(Math.sin(t*w)/Math.exp(decay*t)/w);
            }
        }
    }
}
