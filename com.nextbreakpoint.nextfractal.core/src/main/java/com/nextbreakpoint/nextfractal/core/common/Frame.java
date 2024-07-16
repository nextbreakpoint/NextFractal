/*
 * NextFractal 2.3.0
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

import java.util.Objects;

public class Frame {
    private final String pluginId;
    private final String script;
    private final Metadata metadata;
    private final boolean keyFrame;
    private final boolean timeAnimation;

    public Frame(String pluginId, Metadata metadata, String script, boolean keyFrame, boolean timeAnimation) {
        this.pluginId = Objects.requireNonNull(pluginId);
        this.metadata = Objects.requireNonNull(metadata);
        this.script = Objects.requireNonNull(script);
        this.keyFrame = keyFrame;
        this.timeAnimation = timeAnimation;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getScript() {
        return script;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Frame that = (Frame) o;

        if (pluginId != null ? !pluginId.equals(that.pluginId) : that.pluginId != null) return false;
        if (script != null ? !script.equals(that.script) : that.script != null) return false;
        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;
    }

    @Override
    public int hashCode() {
        int result = pluginId != null ? pluginId.hashCode() : 0;
        result = 31 * result + (script != null ? script.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }

    public boolean isKeyFrame() {
        return keyFrame;
    }

    public boolean isTimeAnimation() {
        return timeAnimation;
    }
}
