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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionActionFired;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.event.TimeAnimationActionFired;
import com.nextbreakpoint.nextfractal.core.javafx.observable.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.Tool;
import com.nextbreakpoint.nextfractal.core.javafx.observable.ToolObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.event.ActiveToolChanged;
import com.nextbreakpoint.nextfractal.core.javafx.event.AnimationStateChanged;
import com.nextbreakpoint.nextfractal.core.javafx.viewer.Toolbar;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.tool.ToolMove;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.tool.ToolPick;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.tool.ToolRotate;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.tool.ToolZoom;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotOptions;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotSession;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.stage.Screen;

import static com.nextbreakpoint.nextfractal.core.javafx.Icons.computeOptimalLargeIconPercentage;
import static com.nextbreakpoint.nextfractal.core.javafx.Icons.createSVGIcon;

public class MandelbrotToolbar extends Toolbar {
    private final BooleanObservableValue juliaProperty;
    private final BooleanObservableValue showTrapsProperty;
    private final BooleanObservableValue showOrbitProperty;
    private final BooleanObservableValue showPreviewProperty;
    private final ToolObservableValue toolProperty;

    public MandelbrotToolbar(MetadataDelegate delegate, EventBusPublisher publisher, MandelbrotToolContext toolContext) {
        super(delegate, publisher, toolContext);

        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();

        juliaProperty = new BooleanObservableValue();
        juliaProperty.setValue(metadata.isJulia());

        showTrapsProperty = new BooleanObservableValue();
        showTrapsProperty.setValue(metadata.getOptions().isShowTraps());

        showOrbitProperty = new BooleanObservableValue();
        showOrbitProperty.setValue(metadata.getOptions().isShowOrbit());

        showPreviewProperty = new BooleanObservableValue();
        showPreviewProperty.setValue(metadata.getOptions().isShowPreview());

        toolProperty = new ToolObservableValue();
        toolProperty.setValue(new ToolZoom(toolContext, true));

        final double percentage = computeOptimalLargeIconPercentage();
        final int size = (int)Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);

        final ToggleButton zoomButton = new ToggleButton("", createSVGIcon("/magnifier.svg", size));
        final ToggleButton moveButton = new ToggleButton("", createSVGIcon("/move.svg", size));
        final ToggleButton rotateButton = new ToggleButton("", createSVGIcon("/rotate.svg", size));
        final ToggleButton pickButton = new ToggleButton("", createSVGIcon("/pin.svg", size));
        final ToggleButton juliaButton = new ToggleButton("", createSVGIcon("/julia.svg", size));
        final ToggleButton orbitButton = new ToggleButton("", createSVGIcon("/orbit.svg", size));
        final ToggleButton captureButton = new ToggleButton("", createSVGIcon("/capture.svg", size));
        final ToggleButton animationButton = new ToggleButton("", createSVGIcon("/chronometer.svg", size));

        final ToggleGroup toolsGroup = new ToggleGroup();
        toolsGroup.getToggles().add(zoomButton);
        toolsGroup.getToggles().add(moveButton);
        toolsGroup.getToggles().add(rotateButton);
        toolsGroup.getToggles().add(pickButton);

        final Button homeButton = new Button("", createSVGIcon("/center.svg", size));

        zoomButton.setTooltip(new Tooltip("Select zoom in tool"));
        moveButton.setTooltip(new Tooltip("Select move tool"));
        rotateButton.setTooltip(new Tooltip("Select rotate tool"));
        pickButton.setTooltip(new Tooltip("Select pick tool"));
        homeButton.setTooltip(new Tooltip("Reset region to initial value"));
        orbitButton.setTooltip(new Tooltip("Show/hide orbit and traps"));
        juliaButton.setTooltip(new Tooltip("Enable/disable Julia mode"));
        captureButton.setTooltip(new Tooltip("Enable/disable capture mode"));
        animationButton.setTooltip(new Tooltip("Enable/disable time animation"));

        getChildren().add(homeButton);
        getChildren().add(zoomButton);
        getChildren().add(moveButton);
        getChildren().add(rotateButton);
        getChildren().add(pickButton);
        getChildren().add(juliaButton);
        getChildren().add(orbitButton);
        getChildren().add(captureButton);
        getChildren().add(animationButton);

        getStyleClass().add("toolbar");

        zoomButton.setSelected(true);

//        toolsGroup.selectedToggleProperty().addListener((_, _, newValue) -> {
//            if (oldValue != null) {
//                ((ToggleButton) oldValue).setDisable(false);
//            }
//            if (newValue != null) {
//                ((ToggleButton) newValue).setDisable(true);
//            }
//        });

        homeButton.setOnAction(_ -> handleSessionReset());

        zoomButton.setOnAction(_ -> handleToolSelected(new ToolZoom(toolContext, true)));

        moveButton.setOnAction(_ -> handleToolSelected(new ToolMove(toolContext)));

        rotateButton.setOnAction(_ -> handleToolSelected(new ToolRotate(toolContext)));

        pickButton.setOnAction(_ -> handleToolSelected(new ToolPick(toolContext)));

        orbitButton.setOnAction(_ -> handleShowOrbitSelected(orbitButton.isSelected()));

        juliaButton.setOnAction(_ -> handleJuliaSelected(juliaButton.isSelected()));

        captureButton.setOnAction(_ -> handleCaptureSelected(captureButton.isSelected()));

        animationButton.setOnAction(e -> handleAnimationSelected(animationButton.isSelected()));

        captureProperty.addListener((_, _, newValue) -> {
            captureButton.setSelected(newValue);
        });

        animationProperty.addListener((_, _, newValue) -> {
            animationButton.setSelected(newValue);
        });

        juliaProperty.addListener((_, _, newValue) -> {
            juliaButton.setSelected(newValue);
        });

        showOrbitProperty.addListener((_, _, newValue) -> {
            orbitButton.setSelected(newValue);
        });

        showTrapsProperty.addListener((_, _, newValue) -> {
//            trapsButton.setSelected(newValue);
        });

        showPreviewProperty.addListener((_, _, newValue) -> {
//            previewButton.setSelected(newValue);
        });

        toolProperty.addListener((_, _, newValue) -> {
            if (newValue instanceof ToolPick) {
                pickButton.setSelected(true);
            } else if (newValue instanceof ToolZoom) {
                zoomButton.setSelected(true);
            } else if (newValue instanceof ToolMove) {
                moveButton.setSelected(true);
            } else if (newValue instanceof ToolRotate) {
                rotateButton.setSelected(true);
            }
        });

        Platform.runLater(() -> publisher.postEvent(ActiveToolChanged.builder().tool(toolProperty.getValue()).build()));
    }

    public void bindSession(Session session) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) session.metadata();

        showPreviewProperty.setValue(metadata.getOptions().isShowPreview());
        showOrbitProperty.setValue(metadata.getOptions().isShowOrbit());
        showTrapsProperty.setValue(metadata.getOptions().isShowTraps());
        juliaProperty.setValue(metadata.isJulia());

        if (metadata.isJulia() && toolProperty.getValue() instanceof ToolPick) {
            toolProperty.setValue(new ToolZoom((MandelbrotToolContext) toolContext, true));
            publisher.postEvent(ActiveToolChanged.builder().tool(toolProperty.getValue()).build());
        }
    }

    private void handleSessionReset() {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        final Double4D translation = Double4D.builder().withZ(1).build();
        final Double4D rotation = Double4D.builder().build();
        final Double4D scale = Double4D.builder().withX(1).withY(1).withZ(1).withW(1).build();
        final MandelbrotMetadata newMetadata = metadata.toBuilder().withTranslation(translation).withRotation(rotation).withScale(scale).build();
        final MandelbrotSession newSession = (MandelbrotSession) delegate.newSession(newMetadata);
        final boolean appendToHistory = delegate.hasChanged(newSession);
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(false).appendToHistory(appendToHistory).build());
    }

    private void handleToolSelected(Tool tool) {
        toolProperty.setValue(tool);
        handleShowPreviewSelected(toolProperty.getValue() instanceof ToolPick);
        publisher.postEvent(ActiveToolChanged.builder().tool(toolProperty.getValue()).build());
    }

    private void handleAnimationSelected(boolean selected) {
        if (selected) {
            publisher.postEvent(TimeAnimationActionFired.builder().action("start").build());
        } else {
            publisher.postEvent(TimeAnimationActionFired.builder().action("stop").build());
        }
        // could be in application handler?
        publisher.postEvent(AnimationStateChanged.builder().enabled(selected).build());
    }

    private void handleCaptureSelected(boolean selected) {
        if (selected) {
            publisher.postEvent(CaptureSessionActionFired.builder().action("start").build());
        } else {
            publisher.postEvent(CaptureSessionActionFired.builder().action("stop").build());
        }
    }

    private void handleJuliaSelected(boolean selected) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        final MandelbrotOptions newOptions = metadata.getOptions().toBuilder().withShowPreview(metadata.getOptions().isShowPreview() && !selected).build();
        final MandelbrotMetadata newMetadata = metadata.toBuilder().withJulia(selected).withOptions(newOptions).build();
        final MandelbrotSession newSession = (MandelbrotSession) delegate.newSession(newMetadata);
        final boolean appendToHistory = delegate.hasChanged(newSession);
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(false).appendToHistory(appendToHistory).build());
    }

    private void handleShowPreviewSelected(boolean selected) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        final MandelbrotOptions newOptions = metadata.getOptions().toBuilder().withShowPreview(selected).withShowPoint(metadata.getOptions().isShowOrbit() || selected).build();
        final MandelbrotMetadata newMetadata = metadata.toBuilder().withJulia(metadata.isJulia() && !selected).withOptions(newOptions).build();
        final MandelbrotSession newSession = (MandelbrotSession) delegate.newSession(newMetadata);
        final boolean appendToHistory = delegate.hasChanged(newSession);
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(false).appendToHistory(appendToHistory).build());
    }

    private void handleShowOrbitSelected(boolean selected) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        final MandelbrotOptions newOptions = metadata.getOptions().toBuilder().withShowOrbit(selected).withShowPoint(metadata.getOptions().isShowPreview() || selected).build();
        final MandelbrotMetadata newMetadata = metadata.toBuilder().withOptions(newOptions).build();
        final MandelbrotSession newSession = (MandelbrotSession) delegate.newSession(newMetadata);
        final boolean appendToHistory = delegate.hasChanged(newSession);
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(false).appendToHistory(appendToHistory).build());
    }

    private void handleShowTrapsSelected(boolean selected) {
        final MandelbrotMetadata metadata = (MandelbrotMetadata) delegate.getMetadata();
        final MandelbrotOptions newOptions = metadata.getOptions().toBuilder().withShowTraps(selected).build();
        final MandelbrotMetadata newMetadata = metadata.toBuilder().withOptions(newOptions).build();
        final MandelbrotSession newSession = (MandelbrotSession) delegate.newSession(newMetadata);
        final boolean appendToHistory = delegate.hasChanged(newSession);
        publisher.postEvent(RenderDataChanged.builder().session(newSession).continuous(false).appendToHistory(appendToHistory).build());
    }
}
