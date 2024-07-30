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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@EqualsAndHashCode(exclude = "time")
@Builder(setterPrefix = "with", toBuilder = true)
public class ContextFreeMetadata implements Metadata {
	@Getter
    private final String seed;
	private final Time time = new Time(0, 1);

	public ContextFreeMetadata() {
		this.seed = "ABCD";
	}

	public ContextFreeMetadata(String seed) {
		this.seed = Objects.requireNonNull(seed);
	}

	public ContextFreeMetadata(ContextFreeMetadata other) {
		seed = other.seed;
	}

	@JsonProperty("time")
	@Override
	public Time time() {
		return time;
	}
}
