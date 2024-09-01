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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.IOUtils;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.common.Time;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.io.InputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@EqualsAndHashCode(callSuper = false)
@Builder(setterPrefix = "with", toBuilder = true)
@AllArgsConstructor
public class MandelbrotSession extends Session {
	private static final Logger logger = Logger.getLogger(MandelbrotSession.class.getName());

	private final MandelbrotMetadata metadata;
	private final String script;

	public MandelbrotSession() {
		this(getInitialScript(), new MandelbrotMetadata(new Double4D(0, 0, 1,0), new Double4D(0, 0, 0,0), new Double4D(1, 1, 1,1), new Double2D(0, 0), new Time(0, 1), false, new MandelbrotOptions()));
	}

	public MandelbrotSession(String script, MandelbrotMetadata metadata) {
		Objects.requireNonNull(metadata);
		Objects.requireNonNull(script);
		this.metadata = metadata;
		this.script = script;
	}

	@Override
    public String pluginId() {
        return MandelbrotFactory.PLUGIN_ID;
    }

	@Override
	public String grammar() {
		return MandelbrotFactory.GRAMMAR;
	}

	@Override
	public String script() {
		return script;
	}

	@Override
	public Metadata metadata() {
		return metadata;
	}

	@Override
	public Session withSource(String source) {
		return toBuilder().withScript(source).build();
	}

	@Override
	public Session withMetadata(Metadata metadata) {
		return toBuilder().withMetadata((MandelbrotMetadata) metadata).build();
	}

	private static String getInitialScript() {
		return Command.of(() -> IOUtils.readString(Objects.requireNonNull(getResourceAsStream())))
				.execute()
				.observe()
				.onFailure(e -> logger.log(Level.WARNING, "Can't load resource /mandelbrot.txt", e))
				.get()
				.orElse(null);
	}

	private static InputStream getResourceAsStream() {
		return MandelbrotSession.class.getResourceAsStream("/mandelbrot.txt");
	}

	@Override
	public String toString() {
		return "{pluginId=" + pluginId() + ", metadata=" + metadata + ", script='" + script + "'}";
	}
}
