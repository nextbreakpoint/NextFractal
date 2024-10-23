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
package com.nextbreakpoint.nextfractal.core.javafx.viewer;

import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStarted;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorReportChanged;
import com.nextbreakpoint.nextfractal.core.event.HideControlsFired;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataChanged;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackReportChanged;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStarted;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStopped;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.javafx.observable.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.KeyHandler;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingContext;
import com.nextbreakpoint.nextfractal.core.javafx.RenderingStrategy;
import com.nextbreakpoint.nextfractal.core.javafx.UIFactory;
import com.nextbreakpoint.nextfractal.core.javafx.event.ActiveToolChanged;
import com.nextbreakpoint.nextfractal.core.javafx.event.AnimationStateChanged;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.extern.java.Log;

import java.util.List;
import java.util.Optional;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

@Log
public class Viewer extends BorderPane {
    private final BooleanObservableValue errorProperty;
    private final PlatformEventBus eventBus;
    private final BorderPane controls;
    private final BorderPane viewer;
    private final BorderPane error;
    private AnimationTimer animationTimer;
    private RenderingContext renderingContext;
    private RenderingStrategy renderingStrategy;
    private Toolbar toolbar;
    private Session session;
    private UIFactory factory;
    private MetadataDelegate delegate;
    private KeyHandler keyHandler;
    private List<ScriptError> errors;

    public Viewer(PlatformEventBus eventBus) {
        this.eventBus = eventBus;

        errorProperty = new BooleanObservableValue();
        errorProperty.setValue(false);

        getStyleClass().add("mandelbrot");

        controls = new BorderPane();

        viewer = new BorderPane();

        error = new BorderPane();
        error.getStyleClass().add("errors");
        error.setVisible(false);

        final Pane stackPane = new Pane();
        stackPane.getChildren().add(viewer);
        stackPane.getChildren().add(controls);
        stackPane.getChildren().add(error);
        setCenter(stackPane);

        final FadeTransition toolsTransition = createFadeTransition(controls);

        controls.setOnMouseClicked(e -> {
            if (renderingContext != null && renderingContext.getTool() != null) {
                renderingContext.getTool().clicked(e);
            }
        });

        controls.setOnMousePressed(e -> {
            fadeOut(toolsTransition, _ -> {
            });
            eventBus.postEvent(HideControlsFired.builder().hide(true).build());
            if (renderingContext != null && renderingContext.getTool() != null) {
                renderingContext.getTool().pressed(e);
            }
        });

        controls.setOnMouseReleased(e -> {
            fadeIn(toolsTransition, _ -> {
            });
            eventBus.postEvent(HideControlsFired.builder().hide(false).build());
            if (renderingContext != null && renderingContext.getTool() != null) {
                renderingContext.getTool().released(e);
            }
        });

        controls.setOnMouseDragged(e -> {
            if (renderingContext != null && renderingContext.getTool() != null) {
                renderingContext.getTool().dragged(e);
            }
        });

        controls.setOnMouseMoved(e -> {
            if (renderingContext != null && renderingContext.getTool() != null) {
                renderingContext.getTool().moved(e);
            }
        });

        setOnMouseEntered(_ -> {
            fadeIn(toolsTransition, _ -> {
            });
            controls.requestFocus();
        });

        setOnMouseExited(_ -> {
            fadeOut(toolsTransition, _ -> {
            });
        });

        stackPane.setOnDragDropped(e -> e.getDragboard().getFiles().stream().findFirst()
                .ifPresent(file -> eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build())));

        stackPane.setOnDragOver(x -> Optional.of(x).filter(e -> e.getGestureSource() != stackPane)
                .filter(e -> e.getDragboard().hasFiles()).ifPresent(e -> e.acceptTransferModes(TransferMode.COPY_OR_MOVE)));

        errorProperty.addListener((_, _, newValue) -> {
            error.setVisible(newValue);
        });

        eventBus.subscribe(ActiveToolChanged.class.getSimpleName(), event -> {
            if (renderingContext != null) {
                renderingContext.setTool(((ActiveToolChanged) event).tool());
            }
        });

        eventBus.subscribe(AnimationStateChanged.class.getSimpleName(), event -> {
            if (renderingContext != null) {
                renderingContext.setTimeAnimation(((AnimationStateChanged) event).enabled());
            }
        });

        eventBus.subscribe(PlaybackStarted.class.getSimpleName(), _ -> {
            if (renderingContext != null) {
                renderingContext.setPlayback(true);
            }
        });

        eventBus.subscribe(PlaybackStopped.class.getSimpleName(), _ -> {
            if (renderingContext != null) {
                renderingContext.setPlayback(false);
            }
        });

        eventBus.subscribe(EditorReportChanged.class.getSimpleName(), event -> {
            handleReportChanged(((EditorReportChanged) event).session(), ((EditorReportChanged) event).continuous(), ((EditorReportChanged) event).result());
            //TODO coordinator errors are not displayed in status panel
//            eventBus.postEvent(SessionErrorChanged.builder().error(message).build());
//            eventBus.postEvent(SessionStatusChanged.builder().status(message).build());
        });

        eventBus.subscribe(PlaybackReportChanged.class.getSimpleName(), event -> {
            handleReportChanged(((PlaybackReportChanged) event).session(), ((PlaybackReportChanged) event).continuous(), ((PlaybackReportChanged) event).result());
            //TODO coordinator errors are not displayed in status panel
//            eventBus.postEvent(SessionErrorChanged.builder().error(message).build());
//            eventBus.postEvent(SessionStatusChanged.builder().status(message).build());
        });

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> handleSessionTerminated());

//		eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionLoaded(((SessionDataLoaded) event).session(), ((SessionDataLoaded) event).continuous()));
        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionChanged(((SessionDataChanged) event).session(), ((SessionDataChanged) event).continuous()));

        eventBus.subscribe(CaptureSessionStarted.class.getSimpleName(), _ -> toolbar.setCaptureEnabled(true));
        eventBus.subscribe(CaptureSessionStopped.class.getSimpleName(), _ -> toolbar.setCaptureEnabled(false));

        eventBus.subscribe(AnimationStateChanged.class.getSimpleName(), event -> toolbar.setAnimationEnabled(((AnimationStateChanged) event).enabled()));

        eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), _ -> toolbar.setAnimationEnabled(false));

//		eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> handleSessionLoaded(((PlaybackDataLoaded) event).session(), ((PlaybackDataLoaded) event).continuous()));
        eventBus.subscribe(PlaybackDataChanged.class.getSimpleName(), event -> handleSessionChanged(((PlaybackDataChanged) event).session(), ((PlaybackDataChanged) event).continuous()));

        eventBus.subscribe(PlaybackStarted.class.getSimpleName(), _ -> toolbar.setDisable(true));
        eventBus.subscribe(PlaybackStopped.class.getSimpleName(), _ -> toolbar.setDisable(false));

        Platform.runLater(controls::requestFocus);

        widthProperty().addListener((_, _, newValue) -> {
            double width = newValue.doubleValue();
            controls.setMinWidth(width);
            controls.setMaxWidth(width);
            controls.setPrefWidth(width);
            viewer.setMinWidth(width);
            viewer.setMaxWidth(width);
            viewer.setPrefWidth(width);
            error.setMinWidth(width);
            error.setMaxWidth(width);
            error.setPrefWidth(width);

            initialize();
        });

        heightProperty().addListener((_, _, newValue) -> {
            double height = newValue.doubleValue();
            controls.setMinHeight(height);
            controls.setMaxHeight(height);
            controls.setPrefHeight(height);
            viewer.setMinHeight(height);
            viewer.setMaxHeight(height);
            viewer.setPrefHeight(height);
            error.setMinHeight(height);
            error.setMaxHeight(height);
            error.setPrefHeight(height);

            initialize();
        });
    }

    private void handleReportChanged(Session session, Boolean continuous, ParserResult result) {
        if (factory == null || !this.session.pluginId().equals(session.pluginId())) {
            // session is being used in the constructors of the strategy classes
            this.session = session;

            initialize();
        }

        this.session = session;

        if (toolbar != null && continuous == Boolean.FALSE) {
            toolbar.bindSession(session);
        }

        if (delegate != null && renderingContext != null) {
            delegate.updateRenderingContext(renderingContext);
        }

        if (renderingStrategy != null) {
            renderingStrategy.updateCoordinators(result);
        }

        errorProperty.setValue(!result.errors().isEmpty());
    }

    private void handleSessionChanged(Session session, Boolean continuous) {
        if (factory == null || !this.session.pluginId().equals(session.pluginId())) {
            // session is being used in the constructors of the strategy classes
            this.session = session;

            initialize();
        }

        this.session = session;

        if (toolbar != null && continuous == Boolean.FALSE) {
            toolbar.bindSession(session);
        }

        if (delegate != null && renderingContext != null) {
            delegate.updateRenderingContext(renderingContext);
        }

        if (renderingStrategy != null && renderingContext != null) {
            renderingStrategy.updateCoordinators(session, continuous, renderingContext.isTimeAnimation());
        }
    }

    private void initialize() {
        //TODO ensure that initialize works when view size is zero

        if (session == null) {
            return;
        }

        if (keyHandler != null) {
            controls.removeEventHandler(KeyEvent.KEY_RELEASED, keyHandler);
            keyHandler = null;
        }

        controls.setBottom(null);
        viewer.setCenter(null);

        if (renderingStrategy != null) {
            final var strategy = renderingStrategy;
            renderingStrategy = null;
            strategy.disposeCoordinators();
        }

        if (renderingContext != null) {
            renderingContext.dispose();
            renderingContext = null;
        }

        delegate = null;

        int width = (int) getWidth();
        int height = (int) getHeight();

        factory = tryFindFactory(session.pluginId()).optional().orElse(null);

        if (factory == null) {
            return;
        }

        renderingContext = factory.createRenderingContext();

        if (renderingContext == null) {
            return;
        }

        delegate = factory.createMetadataDelegate(eventBus::postEvent, () -> Viewer.this.session);

        if (delegate == null) {
            return;
        }

        renderingStrategy = factory.createRenderingStrategy(renderingContext, delegate, width, height);

        if (renderingStrategy == null) {
            return;
        }

        final var toolContext = factory.createToolContext(renderingContext, renderingStrategy, delegate, width, height);

        toolbar = factory.createToolbar(eventBus::postEvent, delegate, toolContext);

        if (toolbar != null) {
            toolbar.setOpacity(0.9);
            toolbar.setPrefHeight(getHeight() * 0.07);

            controls.setBottom(toolbar);
        }

        //TODO remove width and height
        final Pane renderingPanel = factory.createRenderingPanel(renderingContext, width, height);

        if (renderingPanel != null) {
            viewer.setCenter(renderingPanel);
        }

        keyHandler = factory.createKeyHandler(renderingContext, delegate);

        if (keyHandler != null) {
            controls.addEventHandler(KeyEvent.KEY_RELEASED, keyHandler);
        }
    }

    private void handleSessionTerminated() {
        stopAnimationTimer();

        if (renderingStrategy != null) {
            renderingStrategy.disposeCoordinators();
        }
    }

    private FadeTransition createFadeTransition(Node node) {
        final FadeTransition transition = new FadeTransition();
        transition.setNode(node);
        transition.setDuration(Duration.seconds(0.5));
        return transition;
    }

    private void fadeOut(FadeTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getOpacity() != 0) {
            transition.setFromValue(transition.getNode().getOpacity());
            transition.setToValue(0);
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private void fadeIn(FadeTransition transition, EventHandler<ActionEvent> handler) {
        transition.stop();
        if (transition.getNode().getOpacity() != 0.9) {
            transition.setFromValue(transition.getNode().getOpacity());
            transition.setToValue(0.9);
            transition.setOnFinished(handler);
            transition.play();
        }
    }

    private void startAnimationTimer() {
        if (animationTimer == null) {
            animationTimer = new ViewerAnimationTimer();
        }
        animationTimer.start();
    }

    private void stopAnimationTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public void enable() {
        log.info("Viewer enabled");
        startAnimationTimer();
    }

    public void disable() {
        log.info("Viewer disabled");
        stopAnimationTimer();
    }

    private class ViewerAnimationTimer extends AnimationTimer {
        private static final long FRAME_LENGTH_IN_NANOS = 1000000000 / 25;

        private long lastTimestamp;

        @Override
        public void handle(long timestamp) {
            if (timestamp - lastTimestamp > FRAME_LENGTH_IN_NANOS) {
                if (renderingStrategy != null) {
                    renderingStrategy.updateAndRedraw(timestamp / 1000000L);
                    final List<ScriptError> newErrors = renderingStrategy.getErrors();
                    if (newErrors != errors) {
                        errors = newErrors;
                        //TODO show errors in console
                        errorProperty.setValue(!errors.isEmpty());
                    }
                }
                lastTimestamp = timestamp;
            }
        }
    }
}
