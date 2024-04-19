/*
 * NextFractal 2.1.5
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
package com.nextbreakpoint.nextfractal.contextfree.javafx;

import com.nextbreakpoint.Try;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGInterpreter;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeMetadata;
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeSession;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParser;
import com.nextbreakpoint.nextfractal.contextfree.dsl.DSLParserResult;
import com.nextbreakpoint.nextfractal.contextfree.core.ParserException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.renderer.RendererCoordinator;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.core.common.EventBus;
import com.nextbreakpoint.nextfractal.core.event.EditorDataChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.event.HistorySessionAdded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataChanged;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.event.RenderErrorChanged;
import com.nextbreakpoint.nextfractal.core.event.RenderStatusChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionErrorChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionReportChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionStatusChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.javafx.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.StringObservableValue;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import com.nextbreakpoint.nextfractal.core.javafx.render.JavaFXRendererFactory;
import com.nextbreakpoint.nextfractal.core.common.Block;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderPane extends BorderPane {
	private static final int FRAME_LENGTH_IN_MILLIS = 1000 / 50;
	private static final Logger logger = Logger.getLogger(RenderPane.class.getName());
	private final ThreadFactory renderThreadFactory;
	private final JavaFXRendererFactory renderFactory;
	private final StringObservableValue errorProperty;
	private final StringObservableValue statusProperty;
	private final BooleanObservableValue hideErrorsProperty;
	private RendererCoordinator coordinator;
	private AnimationTimer timer;
	private int width;
	private int height;
	private int rows;
	private int columns;
	private CFDG cfdg;
	private String cfdgSource = "";
	private volatile boolean hasError;
	private ContextFreeSession contextFreeSession;

	public RenderPane(ContextFreeSession session, EventBus eventBus, int width, int height, int rows, int columns) {
		this.width = width;
		this.height = height;
		this.rows = rows;
		this.columns = columns;

		contextFreeSession = session;

		errorProperty = new StringObservableValue();
		errorProperty.setValue(null);

		statusProperty = new StringObservableValue();
		statusProperty.setValue(null);

		hideErrorsProperty = new BooleanObservableValue();
		hideErrorsProperty.setValue(true);
		
		renderThreadFactory = new DefaultThreadFactory("ContextFree Coordinator", true, Thread.MIN_PRIORITY + 2);

		renderFactory = new JavaFXRendererFactory();

		Map<String, Integer> hints = new HashMap<>();
		coordinator = createCoordinator(hints, width, height);

		getStyleClass().add("contextfree");

//		BorderPane controls = new BorderPane();
//		controls.setMinWidth(width);
//		controls.setMaxWidth(width);
//		controls.setPrefWidth(width);
//		controls.setMinHeight(height);
//		controls.setMaxHeight(height);
//		controls.setPrefHeight(height);

		BorderPane errors = new BorderPane();
		errors.setMinWidth(width);
		errors.setMaxWidth(width);
		errors.setPrefWidth(width);
		errors.setMinHeight(height);
		errors.setMaxHeight(height);
		errors.setPrefHeight(height);
		errors.getStyleClass().add("errors");
		errors.setVisible(false);

//		HBox toolButtons = new HBox(0);
//		ToggleButton zoominButton = new ToggleButton("", createIconImage("/icon-zoomin.png"));
//		ToggleButton zoomoutButton = new ToggleButton("", createIconImage("/icon-zoomout.png"));
//		ToggleButton moveButton = new ToggleButton("", createIconImage("/icon-move.png"));
//		ToggleButton rotateButton = new ToggleButton("", createIconImage("/icon-rotate.png"));
//		ToggleGroup toolsGroup = new ToggleGroup();
//		toolsGroup.getToggles().add(zoominButton);
//		toolsGroup.getToggles().add(zoomoutButton);
//		toolsGroup.getToggles().add(moveButton);
//		toolsGroup.getToggles().add(rotateButton);
//		Button homeButton = new Button("", createIconImage("/icon-home.png"));
//		zoominButton.setTooltip(new Tooltip("Select zoom in tool"));
//		zoomoutButton.setTooltip(new Tooltip("Select zoom out tool"));
//		moveButton.setTooltip(new Tooltip("Select move tool"));
//		rotateButton.setTooltip(new Tooltip("Select rotate tool"));
//		toolButtons.getChildren().add(homeButton);
//		toolButtons.getChildren().add(zoominButton);
//		toolButtons.getChildren().add(zoomoutButton);
//		toolButtons.getChildren().add(moveButton);
//		toolButtons.getChildren().add(rotateButton);
//		toolButtons.getStyleClass().add("toolbar");

//		controls.setBottom(toolButtons);
//		toolButtons.setOpacity(0.9);

        Canvas fractalCanvas = new Canvas(width, height);
        GraphicsContext gcFractalCanvas = fractalCanvas.getGraphicsContext2D();
        gcFractalCanvas.setFill(javafx.scene.paint.Color.WHITESMOKE);
        gcFractalCanvas.fillRect(0, 0, width, height);

		Canvas toolCanvas = new Canvas(width, height);
		GraphicsContext gcToolCanvas = toolCanvas.getGraphicsContext2D();
		gcToolCanvas.setFill(javafx.scene.paint.Color.TRANSPARENT);
		gcToolCanvas.fillRect(0, 0, width, height);

//		currentTool = new ToolZoom(this, true);
//		zoominButton.setSelected(true);
//		zoominButton.setDisable(true);

//		FadeTransition toolsTransition = createFadeTransition(controls);

//		this.setOnMouseEntered(e -> fadeIn(toolsTransition, x -> {}));
		
//		this.setOnMouseExited(e -> fadeOut(toolsTransition, x -> {}));
		
		Pane stackPane = new Pane();
		stackPane.getChildren().add(fractalCanvas);
		stackPane.getChildren().add(toolCanvas);
//		stackPane.getChildren().add(controls);
		stackPane.getChildren().add(errors);
		setCenter(stackPane);

//		toolsGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//			if (oldValue != null) {
//				((ToggleButton)oldValue).setDisable(false);
//			}
//			if (newValue != null) {
//				((ToggleButton)newValue).setDisable(true);
//			}
//		});
		
		errorProperty.addListener((observable, oldValue, newValue) -> {
			errors.setVisible(newValue != null);
			eventBus.postEvent(RenderErrorChanged.builder().error(newValue).build());
		});

		statusProperty.addListener((observable, oldValue, newValue) -> {
			eventBus.postEvent(RenderStatusChanged.builder().status(newValue).build());
		});

		Block<ContextFreeMetadata, Exception> updateUI = data -> {};

//		heightProperty().addListener((observable, oldValue, newValue) -> {
//			toolButtons.setPrefHeight(newValue.doubleValue() * 0.07);
//		});

		stackPane.setOnDragDropped(e -> e.getDragboard().getFiles().stream().findFirst()
			.ifPresent(file -> eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build())));

		stackPane.setOnDragOver(x -> Optional.of(x).filter(e -> e.getGestureSource() != stackPane)
			.filter(e -> e.getDragboard().hasFiles()).ifPresent(e -> e.acceptTransferModes(TransferMode.COPY_OR_MOVE)));

		runTimer(fractalCanvas, toolCanvas);

		eventBus.subscribe(SessionReportChanged.class.getSimpleName(), event -> {
			ParserResult report = ((SessionReportChanged) event).result();
			List<SourceError> lastErrors = updateReport((DSLParserResult) report.result());
			if (lastErrors.isEmpty()) {
				ContextFreeSession newSession = (ContextFreeSession) ((SessionReportChanged) event).session();
				notifySessionChanged(eventBus, newSession, ((SessionReportChanged) event).continuous(), ((SessionReportChanged) event).appendToHistory());
			}
		});

//		eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> loadData((ContextFreeSession) ((SessionDataLoaded) event).session()));

		eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> updateData((ContextFreeSession) ((SessionDataChanged) event).session()));

		eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> loadData((ContextFreeSession) ((PlaybackDataLoaded) event).session()));

		eventBus.subscribe(PlaybackDataChanged.class.getSimpleName(), event -> updateData((ContextFreeSession) ((PlaybackDataChanged) event).session()));

		eventBus.subscribe(EditorDataChanged.class.getSimpleName(), event -> {
			ContextFreeSession newSession = (ContextFreeSession) ((EditorDataChanged)event).session();
			boolean continuous = ((EditorDataChanged)event).continuous();
			boolean appendToHistory = ((EditorDataChanged)event).appendToHistory();
			notifySessionChanged(eventBus, newSession, continuous, appendToHistory && !continuous);
		});

		eventBus.subscribe(RenderDataChanged.class.getSimpleName(), event -> {
			ContextFreeSession newSession = (ContextFreeSession) ((RenderDataChanged) event).session();
			boolean continuous = ((RenderDataChanged) event).continuous();
			boolean appendToHistory = ((RenderDataChanged) event).appendToHistory();
			notifySessionChanged(eventBus, newSession, continuous, appendToHistory && !continuous);
		});

		eventBus.subscribe(RenderStatusChanged.class.getSimpleName(), event -> {
			eventBus.postEvent(SessionStatusChanged.builder().status(((RenderStatusChanged) event).status()).build());
		});

		eventBus.subscribe(RenderErrorChanged.class.getSimpleName(), event -> {
			eventBus.postEvent(SessionErrorChanged.builder().error(((RenderErrorChanged) event).error()).build());
		});

		eventBus.subscribe(SessionTerminated.class.getSimpleName(), event -> dispose());
	}

	private void loadData(ContextFreeSession session) {
        Try.of(() -> generateReport(session.getScript())).filter(report -> ((DSLParserResult) report).getErrors().isEmpty()).ifPresent(report -> {
			List<SourceError> errors = updateReport(report);
			if (errors.isEmpty()) {
				updateData(session);
			}
		});
	}

	private void updateData(ContextFreeSession session) {
		contextFreeSession = session;
	}

	private void notifySessionChanged(EventBus eventBus, ContextFreeSession newSession, boolean continuous, boolean historyAppend) {
		eventBus.postEvent(SessionDataChanged.builder().session(newSession).continuous(continuous).appendToHistory(historyAppend).build());
//		if (historyAppend) {
//			eventBus.postEvent(HistorySessionAdded.builder().session(newSession).build());
//		}
	}

	private RendererCoordinator createCoordinator(Map<String, Integer> hints, int width, int height) {
		return new RendererCoordinator(renderThreadFactory, renderFactory, createSingleTile(width, height), hints);
	}

	private void dispose() {
	}

	private ImageView createIconImage(String name, double percentage) {
		int size = (int)Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);
		InputStream stream = getClass().getResourceAsStream(name);
		ImageView image = new ImageView(new Image(stream));
		image.setSmooth(true);
		image.setFitWidth(size);
		image.setFitHeight(size);
		return image;
	}

	private ImageView createIconImage(String name) {
		return createIconImage(name, 0.02);
	}

	private FadeTransition createFadeTransition(Node node) {
		FadeTransition transition = new FadeTransition();
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

	private void runTimer(Canvas fractalCanvas, Canvas toolCanvas) {
		timer = new AnimationTimer() {
			private long last;

			@Override
			public void handle(long now) {
				long time = now / 1000000;
				if (time - last > FRAME_LENGTH_IN_MILLIS) {
					if (!hasError && coordinator != null && coordinator.isInitialized()) {
						processRenderErrors();
						redrawIfPixelsChanged(fractalCanvas);
//						if (currentTool != null) {
//							currentTool.update(time);
//						}
					}
					last = time;
				}
			}
		};
		timer.start();
	}
	
	private RendererTile createTile(int row, int column) {
		int tileWidth = width / columns;
		int tileHeight = height / rows;
		RendererSize imageSize = new RendererSize(width, height);
		RendererSize tileSize = new RendererSize(tileWidth, tileHeight);
		RendererSize tileBorder = new RendererSize(0, 0);
		RendererPoint tileOffset = new RendererPoint(column * width / columns, row * height / rows);
        return new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
	}

	private RendererTile createSingleTile(int width, int height) {
		int tileWidth = width;
		int tileHeight = height;
		RendererSize imageSize = new RendererSize(width, height);
		RendererSize tileSize = new RendererSize(tileWidth, tileHeight);
		RendererSize tileBorder = new RendererSize(0, 0);
		RendererPoint tileOffset = new RendererPoint(0, 0);
		RendererTile tile = new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
		return tile;
	}

	//TODO move to strategy class
	private void updateCompilerErrors(String message, List<SourceError> errors, String source) {
		hasError = message != null;
		Platform.runLater(() -> {
			statusProperty.setValue(null);
			errorProperty.setValue(null);
			if (message != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(message);
				if (errors != null) {
					builder.append("\n\n");
					for (SourceError error : errors) {
						builder.append("Line ");
						builder.append(error.getLine());
						builder.append(": ");
						builder.append(error.getMessage());
						builder.append("\n");
					}
				}
				if (source != null) {
					builder.append("\n\n");
					builder.append(source);
					builder.append("\n");
				}
				errorProperty.setValue(builder.toString());
				statusProperty.setValue(builder.toString());
			} else {
//				statusProperty.setValue("Source compiled");
			}
		});
	}

	//TODO move to strategy class
	private void updateRendererErrors(String message, List<SourceError> errors, String source) {
		hasError = message != null;
		Platform.runLater(() -> {
			statusProperty.setValue(null);
			errorProperty.setValue(null);
			if (message != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(message);
				if (errors != null) {
					builder.append("\n\n");
					for (SourceError error : errors) {
						builder.append("Line ");
						builder.append(error.getLine());
						builder.append(": ");
						builder.append(error.getMessage());
						builder.append("\n");
					}
				}
				if (source != null) {
					builder.append("\n\n");
					builder.append(source);
					builder.append("\n");
				}
				errorProperty.setValue(builder.toString());
				statusProperty.setValue(builder.toString());
			} else {
//				statusProperty.setValue("Rendering completed");
			}
		});
	}

	private DSLParserResult generateReport(String text) throws Exception {
		return new DSLParser().parse(text);
	}

	//TODO move to strategy class
	private List<SourceError> updateReport(DSLParserResult report) {
		try {
			updateCompilerErrors(null, null, null);
			boolean[] changed = createCFDG(report);
			boolean cfdgChanged = changed[0];
			if (cfdgChanged) {
				if (logger.isLoggable(Level.FINE)) {
					RenderPane.logger.fine("CFDG is changed");
				}
			}
			if (coordinator != null) {
				coordinator.abort();
				coordinator.waitFor();
				if (cfdgChanged) {
					coordinator.setInterpreter(new CFDGInterpreter(cfdg));
					coordinator.setSeed(((ContextFreeMetadata)contextFreeSession.getMetadata()).getSeed());
				}
				coordinator.init();
				coordinator.run();
				Thread.sleep(100);
				List<SourceError> errors = coordinator.getErrors();
				if (!errors.isEmpty()) {
					updateCompilerErrors("Some runtime errors occurred", errors, null);
				}
				return errors;
			}
		} catch (ParserException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Can't render image: " + e.getMessage());
			}
			updateCompilerErrors(e.getMessage(), e.getErrors(), null);
			return e.getErrors();
		} catch (InterruptedException e) {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "Can't render image: " + e.getMessage());
			}
			updateCompilerErrors(e.getMessage(), null, null);
			return Collections.singletonList(new SourceError(SourceError.ErrorType.RUNTIME, 0, 0, 0, 0, "Interrupted"));
		}
		return Collections.emptyList();
	}

	//TODO move to strategy class
	private boolean[] createCFDG(DSLParserResult report) throws ParserException {
		if (!report.getErrors().isEmpty()) {
			cfdgSource = null;
			throw new ParserException("Failed to compile source", report.getErrors());
		}
		boolean[] changed = new boolean[] { false, false };
		String newCFDG = report.getSource();
		changed[0] = !newCFDG.equals(cfdgSource);
		cfdgSource = newCFDG;
		cfdg = report.getCFDG();
		return changed;
	}

	private void processRenderErrors() {
		if (coordinator != null) {
			List<SourceError> errors = coordinator.getErrors();
			if (errors.isEmpty()) {
				updateRendererErrors(null, null, null);
			} else {
				updateRendererErrors("SourceError", errors, null);
			}
		}
	}

	private void redrawIfPixelsChanged(Canvas canvas) {
		if (coordinator.isPixelsChanged()) {
			RendererGraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
			coordinator.drawImage(gc, 0, 0);
		}
	}
}
