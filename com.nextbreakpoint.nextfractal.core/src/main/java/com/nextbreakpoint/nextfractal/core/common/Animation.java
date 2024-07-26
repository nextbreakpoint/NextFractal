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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Animation {
    private static final Logger logger = Logger.getLogger(Animation.class.getName());

    private final List<AnimationClip> clips;
    private final int frameCount;
    private final int frameRate;

    public Animation(List<AnimationClip> clips, int frameRate) {
        this.clips = new ArrayList<>(clips);
        this.frameRate = frameRate;
        final long duration = clips.stream().mapToLong(AnimationClip::duration).sum();
        this.frameCount = computeFrameCount(0, duration, frameRate);
    }

    public List<AnimationFrame> generateFrames() {
        final List<AnimationFrame> frames = new LinkedList<>();
        if (!clips.isEmpty() && clips.getFirst().events().size() > 1) {
            int currentClip = 0;
            int currentEvent = 0;
            float frameIndex = 0;
            float time = 0;
            float prevTime = 0;
            AnimationEvent event = clips.getFirst().events().getFirst();
            long baseTime = event.date().getTime();
            AnimationFrame lastFrame = null;
            logger.fine("0) clip " + currentClip + ", event " + currentEvent);
            while (frameIndex < frameCount && currentClip < clips.size() && currentEvent < clips.get(currentClip).events().size()) {
                currentEvent += 1;
                while (currentClip < clips.size() && currentEvent >= clips.get(currentClip).events().size()) {
                    currentClip += 1;
                    currentEvent = 0;
                    if (currentClip < clips.size() && !clips.get(currentClip).events().isEmpty()) {
                        baseTime = clips.get(currentClip).events().getFirst().date().getTime();
                        prevTime = time;
                    }
                    lastFrame = null;
                }
                logger.fine("1) clip " + currentClip + ", event " + currentEvent);
                if (currentClip < clips.size() && currentEvent < clips.get(currentClip).events().size()) {
                    AnimationEvent nextEvent = clips.get(currentClip).events().get(currentEvent);
                    float frameTime = frameIndex / frameRate;
                    time = prevTime + (nextEvent.date().getTime() - baseTime) / 1000f;
                    while (frameTime - time < 0.01f) {
                        logger.fine("1) frame " + frameIndex + ", time " + frameTime);
                        AnimationFrame frame = new AnimationFrame(event.pluginId(), event.script(), event.metadata(), true, false);
                        if (lastFrame != null && lastFrame.isSame(frame)) {
                            logger.fine("1) not key frame");
                            frame = new AnimationFrame(event.pluginId(), event.script(), event.metadata(), false, true);
                        }
                        lastFrame = frame;
                        frames.add(frame);
                        frameIndex += 1;
                        frameTime = frameIndex / frameRate;
                    }
                    event = nextEvent;
                } else {
                    float frameTime = frameIndex / frameRate;
                    logger.fine("2) frame " + frameIndex + ", time " + frameTime);
                    AnimationFrame frame = new AnimationFrame(event.pluginId(), event.script(), event.metadata(), true, false);
                    if (lastFrame != null && lastFrame.isSame(frame)) {
                        logger.fine("2) not key frame");
                        frame = new AnimationFrame(event.pluginId(), event.script(), event.metadata(), false, true);
                    }
                    lastFrame = frame;
                    frames.add(frame);
                    frameIndex += 1;
                }
            }
        }
        logger.fine("3) frame count " + frames.size() + ", frame rate " + frameRate + " fps");
        logger.info("Total frames generated: " + frames.size());
        return frames;
    }

    private int computeFrameCount(double startTime, double stopTime, float frameRate) {
        return (int) Math.floor((stopTime - startTime) / frameRate);
    }
}
