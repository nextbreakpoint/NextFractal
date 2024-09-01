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
import java.util.Date;
import java.util.List;

public record AnimationClip(List<AnimationEvent> events) {
    public AnimationClip() {
        this(List.of());
    }

    public AnimationClip appendEvent(Date date, String pluginId, String script, Metadata metadata) {
        final var events = new ArrayList<>(this.events);
        events.add(new AnimationEvent(date, pluginId, script, metadata));
        return new AnimationClip(events);
    }

    public AnimationEvent getFirstEvent() {
        return events.getFirst();
    }

    public AnimationEvent getLastEvent() {
        return events.getLast();
    }

    public List<AnimationEvent> events() {
        return new ArrayList<>(events);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public long duration() {
        return events.size() > 1 ? getLastEvent().date().getTime() - getFirstEvent().date().getTime() : 0;
    }
}
