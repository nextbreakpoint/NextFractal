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
package com.nextbreakpoint.nextfractal.core.javafx.viewer;

import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
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
import com.nextbreakpoint.nextfractal.core.event.SessionReportChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.javafx.BooleanObservableValue;
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

import java.util.List;
import java.util.Optional;

import static com.nextbreakpoint.nextfractal.core.javafx.UIPlugins.tryFindFactory;

public class Viewer extends BorderPane {
	private final BooleanObservableValue errorProperty;
	private final PlatformEventBus eventBus;
	private final int width;
	private final int height;
	private final BorderPane controls;
	private final BorderPane viewer;
	private final BorderPane errors;
	private AnimationTimer animationTimer;
	private RenderingContext renderingContext;
	private RenderingStrategy renderingStrategy;
	private Toolbar toolbar;
	private Session session;
	private UIFactory factory;
	private MetadataDelegate delegate;
	private KeyHandler keyHandler;

	public Viewer(PlatformEventBus eventBus, int width, int height) {
		this.eventBus = eventBus;
		this.width = width;
		this.height = height;

		errorProperty = new BooleanObservableValue();
		errorProperty.setValue(false);

		getStyleClass().add("mandelbrot");

		controls = new BorderPane();
		controls.setMinWidth(width);
		controls.setMaxWidth(width);
		controls.setPrefWidth(width);
		controls.setMinHeight(height);
		controls.setMaxHeight(height);
		controls.setPrefHeight(height);

		viewer = new BorderPane();
		viewer.setMinWidth(width);
		viewer.setMaxWidth(width);
		viewer.setPrefWidth(width);
		viewer.setMinHeight(height);
		viewer.setMaxHeight(height);
		viewer.setPrefHeight(height);

		errors = new BorderPane();
		errors.setMinWidth(width);
		errors.setMaxWidth(width);
		errors.setPrefWidth(width);
		errors.setMinHeight(height);
		errors.setMaxHeight(height);
		errors.setPrefHeight(height);
		errors.getStyleClass().add("errors");
		errors.setVisible(false);

		final Pane stackPane = new Pane();
		stackPane.getChildren().add(viewer);
		stackPane.getChildren().add(controls);
		stackPane.getChildren().add(errors);
		setCenter(stackPane);

		final FadeTransition toolsTransition = createFadeTransition(controls);

		controls.setOnMouseClicked(e -> {
			if (renderingContext != null && renderingContext.getTool() != null) {
				renderingContext.getTool().clicked(e);
			}
		});

		controls.setOnMousePressed(e -> {
			fadeOut(toolsTransition, x -> {
			});
			eventBus.postEvent(HideControlsFired.builder().hide(true).build());
			if (renderingContext != null && renderingContext.getTool() != null) {
				renderingContext.getTool().pressed(e);
			}
		});

		controls.setOnMouseReleased(e -> {
			fadeIn(toolsTransition, x -> {
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

		this.setOnMouseEntered(e -> {
			fadeIn(toolsTransition, x -> {
			});
			controls.requestFocus();
		});

		this.setOnMouseExited(e -> {
			fadeOut(toolsTransition, x -> {
			});
		});

		stackPane.setOnDragDropped(e -> e.getDragboard().getFiles().stream().findFirst()
				.ifPresent(file -> eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build())));

		stackPane.setOnDragOver(x -> Optional.of(x).filter(e -> e.getGestureSource() != stackPane)
				.filter(e -> e.getDragboard().hasFiles()).ifPresent(e -> e.acceptTransferModes(TransferMode.COPY_OR_MOVE)));

		errorProperty.addListener((observable, oldValue, newValue) -> {
			errors.setVisible(newValue);
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

		eventBus.subscribe(PlaybackStarted.class.getSimpleName(), event -> {
			if (renderingContext != null) {
				renderingContext.setPlayback(true);
			}
		});

		eventBus.subscribe(PlaybackStopped.class.getSimpleName(), event -> {
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

		eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> handleSessionTerminated());

//		eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionLoaded(((SessionDataLoaded) event).session(), ((SessionDataLoaded) event).continuous()));
		eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionChanged(((SessionDataChanged) event).session(), ((SessionDataChanged) event).continuous()));

		eventBus.subscribe(CaptureSessionStarted.class.getSimpleName(), event -> toolbar.setCaptureEnabled(true));
		eventBus.subscribe(CaptureSessionStopped.class.getSimpleName(), event -> toolbar.setCaptureEnabled(false));

		eventBus.subscribe(AnimationStateChanged.class.getSimpleName(), event -> toolbar.setAnimationEnabled(((AnimationStateChanged) event).enabled()));

		eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> toolbar.setAnimationEnabled(false));

//		eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> handleSessionLoaded(((PlaybackDataLoaded) event).session(), ((PlaybackDataLoaded) event).continuous()));
		eventBus.subscribe(PlaybackDataChanged.class.getSimpleName(), event -> handleSessionChanged(((PlaybackDataChanged) event).session(), ((PlaybackDataChanged) event).continuous()));

		eventBus.subscribe(PlaybackStarted.class.getSimpleName(), event -> toolbar.setDisable(true));
		eventBus.subscribe(PlaybackStopped.class.getSimpleName(), event -> toolbar.setDisable(false));

		Platform.runLater(controls::requestFocus);

		startAnimationTimer();
	}

	private void handleReportChanged(Session session, Boolean continuous, ParserResult report) {
		if (factory == null || !this.session.getPluginId().equals(session.getPluginId())) {
			// session is being used in the constructors of the strategy classes
			this.session = session;

			onSessionChanged(session);
		}

		this.session = session;

		if (toolbar != null && continuous == Boolean.FALSE) {
			toolbar.bindSession(session);
		}

		if (delegate != null && renderingContext != null) {
			delegate.updateRenderingContext(renderingContext);
		}

		if (renderingStrategy != null) {
			final List<SourceError> errorList = renderingStrategy.updateCoordinators(report.result());

			errorProperty.setValue(!errorList.isEmpty());
		}
	}

	private void handleSessionChanged(Session session, Boolean continuous) {
		if (factory == null || !this.session.getPluginId().equals(session.getPluginId())) {
			// session is being used in the constructors of the strategy classes
			this.session = session;

			onSessionChanged(session);
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

	private void onSessionChanged(Session session) {
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

		factory = tryFindFactory(session.getPluginId()).optional().orElse(null);

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
			animationTimer.start();
		}
	}

	private void stopAnimationTimer() {
		if (animationTimer != null) {
			animationTimer.stop();
			animationTimer = null;
		}
	}

	private class ViewerAnimationTimer extends AnimationTimer {
		private static final long FRAME_LENGTH_IN_NANOS = 1000000000 / 25;

		private long lastTimestamp;

		@Override
		public void handle(long timestamp) {
			if (timestamp - lastTimestamp > FRAME_LENGTH_IN_NANOS) {
				if (renderingStrategy != null) {
					renderingStrategy.updateAndRedraw(timestamp / 1000000L);
				}
				lastTimestamp = timestamp;
			}
		}
	}
}
