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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionActionFired;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.event.TimeAnimationActionFired;
import com.nextbreakpoint.nextfractal.core.javafx.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.EventBusPublisher;
import com.nextbreakpoint.nextfractal.core.javafx.MetadataDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.Tool;
import com.nextbreakpoint.nextfractal.core.javafx.ToolObservableValue;
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

import static com.nextbreakpoint.nextfractal.core.javafx.Icons.computeOptimalLargeIconPercentage;
import static com.nextbreakpoint.nextfractal.core.javafx.Icons.createIconImage;

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

        final ToggleButton zoomButton = new ToggleButton("", createIconImage("/icon-zoomin.png", computeOptimalLargeIconPercentage()));
        final ToggleButton moveButton = new ToggleButton("", createIconImage("/icon-move.png", computeOptimalLargeIconPercentage()));
        final ToggleButton rotateButton = new ToggleButton("", createIconImage("/icon-rotate.png", computeOptimalLargeIconPercentage()));
        final ToggleButton pickButton = new ToggleButton("", createIconImage("/icon-pick.png", computeOptimalLargeIconPercentage()));
        final ToggleButton juliaButton = new ToggleButton("", createIconImage("/icon-julia.png", computeOptimalLargeIconPercentage()));
        final ToggleButton orbitButton = new ToggleButton("", createIconImage("/icon-orbit.png", computeOptimalLargeIconPercentage()));
        final ToggleButton captureButton = new ToggleButton("", createIconImage("/icon-capture.png", computeOptimalLargeIconPercentage()));
        final ToggleButton animationButton = new ToggleButton("", createIconImage("/icon-cron.png", computeOptimalLargeIconPercentage()));

        final ToggleGroup toolsGroup = new ToggleGroup();
        toolsGroup.getToggles().add(zoomButton);
        toolsGroup.getToggles().add(moveButton);
        toolsGroup.getToggles().add(rotateButton);
        toolsGroup.getToggles().add(pickButton);

        final Button homeButton = new Button("", createIconImage("/icon-home.png", computeOptimalLargeIconPercentage()));

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

//        toolsGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//            if (oldValue != null) {
//                ((ToggleButton) oldValue).setDisable(false);
//            }
//            if (newValue != null) {
//                ((ToggleButton) newValue).setDisable(true);
//            }
//        });

        homeButton.setOnAction(e -> handleSessionReset());

        zoomButton.setOnAction(e -> handleToolSelected(new ToolZoom(toolContext, true)));

        moveButton.setOnAction(e -> handleToolSelected(new ToolMove(toolContext)));

        rotateButton.setOnAction(e -> handleToolSelected(new ToolRotate(toolContext)));

        pickButton.setOnAction(e -> handleToolSelected(new ToolPick(toolContext)));

        orbitButton.setOnAction(e -> handleShowOrbitSelected(orbitButton.isSelected()));

        juliaButton.setOnAction(e -> handleJuliaSelected(juliaButton.isSelected()));

        captureButton.setOnAction(e -> handleCaptureSelected(captureButton.isSelected()));

        animationButton.setOnAction(e -> handleAnimationSelected(animationButton.isSelected()));

        captureProperty.addListener((observable, oldValue, newValue) -> {
            captureButton.setSelected(newValue);
        });

        animationProperty.addListener((observable, oldValue, newValue) -> {
            animationButton.setSelected(newValue);
        });

        juliaProperty.addListener((observable, oldValue, newValue) -> {
            juliaButton.setSelected(newValue);
        });

        showOrbitProperty.addListener((observable, oldValue, newValue) -> {
            orbitButton.setSelected(newValue);
        });

        showTrapsProperty.addListener((observable, oldValue, newValue) -> {
//            trapsButton.setSelected(newValue);
        });

        showPreviewProperty.addListener((observable, oldValue, newValue) -> {
//            previewButton.setSelected(newValue);
        });

        toolProperty.addListener((observable, oldValue, newValue) -> {
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
