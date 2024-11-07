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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;

import java.util.stream.IntStream;

// renderimpl.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2006-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2006-2014 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA
//
// Mark Lentczner can be contacted at markl@glyphic.com or at
// Mark Lentczner, 1209 Villa St., Mountain View, CA 94041-1123, USA

public class OutputBounds {
    private final AffineTransformTime timeBounds;
    private final CFDGRenderer renderer;
    private final Integer[] frameCounts;
    private final double frameScale;
    private Bounds[] frameBounds;
    private double scale;
    private final int width;
    private final int height;
    private final int frames;

    public OutputBounds(int frames, AffineTransformTime timeBounds, int width, int height, CFDGRenderer renderer) {
        this.frames = frames;
        this.timeBounds = timeBounds;
        this.width = width;
        this.height = height;
        this.renderer = renderer;

        frameScale = (double)frames / (timeBounds.getEnd() - timeBounds.getBegin());
        frameBounds = new Bounds[frames];
        frameCounts = new Integer[frames];

        IntStream.range(0, frames).forEach(i -> frameBounds[i] = null);
        IntStream.range(0, frames).forEach(i -> frameCounts[i] = 0);
    }

    public void apply(FinishedShape shape) {
        if (renderer.isRequestStop() || renderer.isRequestFinishUp()) {
            throw new CFDGStopException("Stopping", ASTWhere.DEFAULT_WHERE);
        }

        if (scale == 0.0) {
            // If we don't know the approximate scale yet then just
            // make an educated guess.
            scale = (width + height) / Math.sqrt(Math.abs(shape.getWorldState().getTransform().getDeterminant()));
        }

        final AffineTransformTime frameTime = shape.getWorldState().getTransformTime();
        frameTime.translate(-timeBounds.getBegin());
        frameTime.scale(frameScale);

        final int begin = frameTime.getBegin() < frames ? (int)Math.floor(frameTime.getBegin()) : (frames - 1);
        final int end = frameTime.getEnd() < frames ? (int)Math.floor(frameTime.getEnd()) : (frames - 1);

        for (int frame = begin; frame <= end; frame++) {
            frameBounds[frame].merge(shape.bounds());
        }

        frameCounts[begin] += 1;
    }

    public void finalAccumulate() {
    }

    public void backwardFilter(double framesToHalf) {
        double alpha = Math.pow(0.5, 1.0 / framesToHalf);

        int frames = frameBounds.length;
        if (frames == 0) {
            return;
        }

        Bounds prev = frameBounds[frameBounds.length - 1];
        for (int i = frameBounds.length - 2; i >= 0; i--) {
            Bounds curr = frameBounds[i];
            frameBounds[i] = curr.interpolate(prev, alpha);
            prev = curr;
        }
    }

    //TODO verify smooth
    public void smooth(int window) {
        int frames = frameBounds.length;
        if (frames == 0) {
            return;
        }

        resizeBounds(frames + window - 1);

        final double factor = 1.0 - window;

        final Bounds accum = new Bounds();
        for (int i = 0; i < window; i++) {
            accum.gather(frameBounds[i], factor);
        }

        int i = 0;
        int j = window;
        for (;;) {
            Bounds old = frameBounds[i];
            frameBounds[i++] = accum;
            accum.gather(old, -factor);
            if (j == frameBounds.length - 1) {
                break;
            }
            accum.gather(frameBounds[j++], factor);
        }

        resizeBounds(frames);
    }

    private void resizeBounds(int length) {
        if (length > frameBounds.length) {
            final Bounds[] tempFrameBounds = frameBounds;
            frameBounds = new Bounds[length];
            System.arraycopy(tempFrameBounds, 0, frameBounds, 0, tempFrameBounds.length);
            for (int i = 0; i < length - frameBounds.length; i++) {
                frameBounds[tempFrameBounds.length + i] = frameBounds[tempFrameBounds.length - 1];
            }
        } else {
            final Bounds[] tempFrameBounds = frameBounds;
            frameBounds = new Bounds[length];
            System.arraycopy(tempFrameBounds, 0, frameBounds, 0, length);
        }
    }

    public Bounds frameBounds(int index) {
        return frameBounds[index];
    }

    public int frameCount(int index) {
        return frameCounts[index];
    }
}
