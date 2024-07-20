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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class ClipProcessor {
    private static final Logger logger = Logger.getLogger(ClipProcessor.class.getName());

    public static final int FRAMES_PER_SECOND = 25;

    private List<Clip> clips;
    private int frameCount;
    private int frameRate;

    public ClipProcessor(List<Clip> clips, int frameRate) {
        this.clips = clips;
        this.frameRate = frameRate;
        long duration = clips.stream().mapToLong(clip -> clip.duration()).sum();
        this.frameCount = computeFrameCount(0, duration, frameRate);
    }

    private int computeFrameCount(double startTime, double stopTime, float frameRate) {
        return (int) Math.floor((stopTime - startTime) / frameRate);
    }

    public List<Frame> generateFrames() {
        List<Frame> frames = new LinkedList<>();
        if (clips.size() > 0 && clips.get(0).getEvents().size() > 1) {
            int currentClip = 0;
            int currentEvent = 0;
            float frameIndex = 0;
            float time = 0;
            float prevTime = 0;
            ClipEvent event = clips.get(0).getEvents().get(0);
            long baseTime = event.getDate().getTime();
            logger.fine("0) clip " + currentClip + ", event " + currentEvent);
            Frame lastFrame = null;
            while (frameIndex < frameCount && currentClip < clips.size() && currentEvent < clips.get(currentClip).getEvents().size()) {
                currentEvent += 1;
                while (currentClip < clips.size() && currentEvent >= clips.get(currentClip).getEvents().size()) {
                    currentClip += 1;
                    currentEvent = 0;
                    if (currentClip < clips.size() && clips.get(currentClip).getEvents().size() > 0) {
                        baseTime = clips.get(currentClip).getEvents().get(0).getDate().getTime();
                        prevTime = time;
                    }
                    lastFrame = null;
                }
                logger.fine("1) clip " + currentClip + ", event " + currentEvent);
                if (currentClip < clips.size() && currentEvent < clips.get(currentClip).getEvents().size()) {
                    ClipEvent nextEvent = clips.get(currentClip).getEvents().get(currentEvent);
                    float frameTime = frameIndex / frameRate;
                    time = prevTime + (nextEvent.getDate().getTime() - baseTime) / 1000f;
                    while (frameTime - time < 0.01f) {
                        logger.fine("1) frame " + frameIndex + ", time " + frameTime);
                        Frame frame = new Frame(event.getPluginId(), event.getMetadata(), event.getScript(), true, false);
                        if (lastFrame != null && lastFrame.equals(frame)) {
                            logger.fine("1) not key frame");
                            frame = new Frame(event.getPluginId(), event.getMetadata(), event.getScript(), false, true);
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
                    Frame frame = new Frame(event.getPluginId(), event.getMetadata(), event.getScript(), true, false);
                    if (lastFrame != null && lastFrame.equals(frame)) {
                        logger.fine("2) not key frame");
                        frame = new Frame(event.getPluginId(), event.getMetadata(), event.getScript(), false, true);
                    }
                    lastFrame = frame;
                    frames.add(frame);
                    frameIndex += 1;
                }
            }
        }
        logger.fine("3) frame count " + frames.size() + ", frame rate " + frameRate + " fps");
        logger.info("Generated " + frames.size() + " frames");
        return frames;
    }
}
