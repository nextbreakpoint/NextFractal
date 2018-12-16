/*
 * NextFractal 2.1.0
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2018 Andrea Medeghini
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

import java.util.Date;
import java.util.Objects;

public class ClipEvent {
    private final Date date;
    private final String pluginId;
    private final String script;
    private final Metadata metadata;

    public ClipEvent(Date date, String pluginId, String script, Metadata metadata) {
        this.date = Objects.requireNonNull(date);
        this.pluginId = Objects.requireNonNull(pluginId);
        this.script = Objects.requireNonNull(script);
        this.metadata = Objects.requireNonNull(metadata);
    }

    public Date getDate() {
        return date;
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
}
