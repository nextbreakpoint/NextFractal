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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.Objects;

public record AnimationFrame(String pluginId, String script, Metadata metadata, boolean keyFrame, boolean repeated) {
    public AnimationFrame(String pluginId, String script, Metadata metadata, boolean keyFrame, boolean repeated) {
        this.pluginId = Objects.requireNonNull(pluginId);
        this.script = Objects.requireNonNull(script);
        this.metadata = Objects.requireNonNull(metadata);
        this.keyFrame = keyFrame;
        this.repeated = repeated;
    }

    public boolean isSame(AnimationFrame other) {
        return pluginId.equals(other.pluginId) && script.equals(other.script) && metadata.equals(other.metadata);
    }
}
