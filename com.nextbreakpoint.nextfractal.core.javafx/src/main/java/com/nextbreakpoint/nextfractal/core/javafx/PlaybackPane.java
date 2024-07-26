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

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Animation;
import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import lombok.extern.java.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.Constants.FRAMES_PER_SECOND;
import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

@Log
public class PlaybackPane extends Pane {
    private final List<AnimationFrame> frames = new LinkedList<>();
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> future;
    private int frameIndex = -1;
    private AnimationFrame lastFrame;
    private PlaybackDelegate delegate;

    public PlaybackPane() {
        executor = Executors.newSingleThreadScheduledExecutor(Objects.requireNonNull(createThreadFactory("Playback")));

        setOnMouseClicked(e -> {
            if (delegate != null) {
                delegate.playbackStopped();
            }
        });

        widthProperty().addListener((observable, oldValue, newValue) -> {
        });

        heightProperty().addListener((observable, oldValue, newValue) -> {
        });
    }

    private DefaultThreadFactory createThreadFactory(String name) {
        return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
    }

    private void playNextFrame() {
        try {
            frameIndex += 1;
            if (frameIndex < frames.size()) {
                AnimationFrame frame = frames.get(frameIndex);
                if (delegate != null) {
                    if (lastFrame == null || !lastFrame.pluginId().equals(frame.pluginId()) || !lastFrame.script().equals(frame.script())) {
                        Command.of(tryFindFactory(frame.pluginId()))
                                .map(factory -> factory.createSession(frame.script(), frame.metadata()))
                                .execute()
                                .optional()
                                .ifPresent(session -> Platform.runLater(() -> delegate.loadSessionData(session, false, false)));
                    } else if (!lastFrame.metadata().equals(frame.metadata())) {
                        Command.of(tryFindFactory(frame.pluginId()))
                                .map(factory -> factory.createSession(frame.script(), frame.metadata()))
                                .execute()
                                .optional()
                                .ifPresent(session -> Platform.runLater(() -> delegate.updateSessionData(session, true, false)));
                    } else if (!lastFrame.metadata().time().equals(frame.metadata().time())) {
                        Command.of(tryFindFactory(frame.pluginId()))
                                .map(factory -> factory.createSession(frame.script(), frame.metadata()))
                                .execute()
                                .optional()
                                .ifPresent(session -> Platform.runLater(() -> delegate.updateSessionData(session, true, false)));
                    }
                }
                lastFrame = frame;
            } else {
                frameIndex = 0;
                lastFrame = null;
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Can't playback frame", e);
        }
    }

    public void setDelegate(PlaybackDelegate delegate) {
        this.delegate = delegate;
    }

    public void setClips(List<AnimationClip> clips) {
        if (future == null) {
            frames.clear();
            final Animation animation = new Animation(clips, FRAMES_PER_SECOND);
            frames.addAll(animation.generateFrames());
        }
    }

    public void start() {
        stop();
        frameIndex = 0;
        lastFrame = null;
        future = executor.scheduleAtFixedRate(this::playNextFrame, 0, 1000 / FRAMES_PER_SECOND, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (future != null) {
            future.cancel(true);
            try {
                future.get();
            } catch (Exception e) {
            }
            future = null;
        }
    }
}
