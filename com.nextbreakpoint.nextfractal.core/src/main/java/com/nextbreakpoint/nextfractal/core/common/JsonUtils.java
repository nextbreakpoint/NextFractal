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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtils {
    private JsonUtils() {}

    public static String getString(JsonNode clipEvent, String field) {
        final JsonNode node = clipEvent.get(field);
        if (node != null) {
            return node.asText();
        }
        return null;
    }

    public static Long getLong(JsonNode clipEvent, String field) {
        final JsonNode node = clipEvent.get(field);
        if (node != null) {
            return node.asLong();
        }
        return null;
    }

    public static Stream<JsonNode> getClips(JsonNode clips) {
        return clips.isArray() ? StreamSupport.stream(clips.spliterator(), false) : Stream.of();
    }

    public static Stream<JsonNode> getEvents(JsonNode events) {
        return (events != null && events.isArray()) ? StreamSupport.stream(events.spliterator(), false) : Stream.of();
    }
}
