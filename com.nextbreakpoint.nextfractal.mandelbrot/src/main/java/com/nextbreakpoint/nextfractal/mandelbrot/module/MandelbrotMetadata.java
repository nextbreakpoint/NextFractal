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
package com.nextbreakpoint.nextfractal.mandelbrot.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(exclude = "time")
@Builder(setterPrefix = "with", toBuilder = true)
public class MandelbrotMetadata implements Metadata {
	@Getter
    private final Double4D translation;
	@Getter
    private final Double4D rotation;
	@Getter
    private final Double4D scale;
	@Getter
    private final Double2D point;
	private final Time time;
	@Getter
    private final boolean julia;
	@Getter
    private final MandelbrotOptions options;

	public MandelbrotMetadata() {
		this(new Double4D(0,0,1,0), new Double4D(0,0,0,0), new Double4D(1,1,1,1), new Double2D(0, 0), new Time(0, 1), false, new MandelbrotOptions());
	}

	public MandelbrotMetadata(Double4D translation, Double4D rotation, Double4D scale, Double2D point, Time time, boolean julia, MandelbrotOptions options) {
		this.translation = translation;
		this.rotation = rotation;
		this.scale = scale;
		this.point = point;
		this.time = time;
		this.julia = julia;
		this.options = options;
	}

	public MandelbrotMetadata(double[] translation, double[] rotation, double[] scale, double[] point, Time time, boolean julia, MandelbrotOptions options) {
		this(new Double4D(translation), new Double4D(rotation), new Double4D(scale), new Double2D(point), time, julia, options);
	}

	public MandelbrotMetadata(Double[] translation, Double[] rotation, Double[] scale, Double[] point, Time time, boolean julia, MandelbrotOptions options) {
		this(new Double4D(translation), new Double4D(rotation), new Double4D(scale), new Double2D(point), time, julia, options);
	}

	public MandelbrotMetadata(MandelbrotMetadata other) {
		this(other.getTranslation(), other.getRotation(), other.getScale(), other.getPoint(), other.time(), other.isJulia(), other.getOptions());
	}

	public MandelbrotMetadata(MandelbrotMetadata other, MandelbrotOptions options) {
		this(other.getTranslation(), other.getRotation(), other.getScale(), other.getPoint(), other.time(), other.isJulia(), options);
	}

    @JsonProperty("time")
	@Override
	public Time time() {
		return time;
	}

    @Override
	public String toString() {
		return "[translation=" + translation + ", rotation=" + rotation + ", scale=" + scale + ", point=" + point + ", time=" + time + ", julia=" + julia + ", options=" + options + "]";
	}
}
