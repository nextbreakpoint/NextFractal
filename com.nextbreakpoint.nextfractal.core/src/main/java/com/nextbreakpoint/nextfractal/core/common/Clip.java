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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

//TODO make this class immutable
public class Clip {
    private final List<ClipEvent> events = new LinkedList<>();

    public Clip() {
    }

    public Clip(List<ClipEvent> events) {
        this.events.addAll(events);
    }

    public void append(Date date, String pluginId, String script, Metadata metadata) {
        events.add(new ClipEvent(date, pluginId, script, metadata));
    }

    public ClipEvent getFirstEvent() {
        return events.get(0);
    }

    public ClipEvent getLastEvent() {
        return events.get(events.size() - 1);
    }

    public Stream<ClipEvent> events() {
        return events.stream();
    }

    public List<ClipEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public long duration() {
        return events.size() > 1 ? events.get(events.size() - 1).getDate().getTime() - events.get(0).getDate().getTime() : 0;
    }
}
