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

import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.extern.java.Log;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Log
public class RecordingPane extends Pane {
    private static final int FRAMES_PER_SECOND = 1;

    private final ScheduledExecutorService executor;
    private final Canvas canvas;
    private ScheduledFuture<?> future;
    private int frame;

    public RecordingPane() {
        executor = Executors.newSingleThreadScheduledExecutor(Objects.requireNonNull(createThreadFactory("Recording")));

        canvas = new Canvas(50, 50);

        getChildren().add(canvas);

        widthProperty().addListener((_, _, newValue) -> {
          canvas.setLayoutX(newValue.doubleValue() - 50 - 30);
        });

        heightProperty().addListener((_, _, _) -> {
            canvas.setLayoutY(30);
        });
    }

    private DefaultThreadFactory createThreadFactory(String name) {
        return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
    }

    private void updateUI() {
        frame += 1;
        final GraphicsContext g2d = canvas.getGraphicsContext2D();
        g2d.clearRect(0 ,0, canvas.getWidth(), canvas.getHeight());
        if (frame % 2 == 1) {
            g2d.setFill(Color.RED);
            g2d.setGlobalAlpha(0.8);
            g2d.fillOval(0 ,0, canvas.getWidth(), canvas.getHeight());
        }
    }

    public void start() {
        stop();
        frame = 0;
        future = executor.scheduleAtFixedRate(() -> Platform.runLater(this::updateUI), 0, 1000 / FRAMES_PER_SECOND, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (future != null) {
            future.cancel(true);
            try {
                future.get();
            } catch (Exception e) {
                log.warning(e.getMessage());
            }
            future = null;
        }
    }
}
