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

import com.nextbreakpoint.nextfractal.core.common.Double2D;
import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.Time;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(exclude = "time")
@Builder(setterPrefix = "with", toBuilder = true)
public class MandelbrotMetadata implements Metadata {
	private final Double4D translation;
	private final Double4D rotation;
	private final Double4D scale;
	private final Double2D point;
	private final Time time;
	private final boolean julia;
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
		this(other.getTranslation(), other.getRotation(), other.getScale(), other.getPoint(), other.getTime(), other.isJulia(), other.getOptions());
	}

	public MandelbrotMetadata(MandelbrotMetadata other, MandelbrotOptions options) {
		this(other.getTranslation(), other.getRotation(), other.getScale(), other.getPoint(), other.getTime(), other.isJulia(), options);
	}

	public Double4D getTranslation() {
		return translation;
	}

	public Double4D getRotation() {
		return rotation;
	}

	public Double4D getScale() {
		return scale;
	}

	public Time getTime() {
		return time;
	}

	public Double2D getPoint() {
		return point;
	}

	public boolean isJulia() {
		return julia;
	}

	public MandelbrotOptions getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return "[translation=" + translation + ", rotation=" + rotation + ", scale=" + scale + ", point=" + point + ", time=" + time + ", julia=" + julia + ", options=" + options + "]";
	}
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//
//		MandelbrotMetadata that = (MandelbrotMetadata) o;
//
//		if (julia != that.julia) return false;
//		if (translation != null ? !translation.equals(that.translation) : that.translation != null) return false;
//		if (rotation != null ? !rotation.equals(that.rotation) : that.rotation != null) return false;
//		if (scale != null ? !scale.equals(that.scale) : that.scale != null) return false;
//		if (point != null ? !point.equals(that.point) : that.point != null) return false;
////		if (time != null ? !time.equals(that.time) : that.time != null) return false;
//		return options != null ? options.equals(that.options) : that.options == null;
//	}
//
//	@Override
//	public int hashCode() {
//		int result = translation != null ? translation.hashCode() : 0;
//		result = 31 * result + (rotation != null ? rotation.hashCode() : 0);
//		result = 31 * result + (scale != null ? scale.hashCode() : 0);
//		result = 31 * result + (point != null ? point.hashCode() : 0);
////		result = 31 * result + (time != null ? time.hashCode() : 0);
//		result = 31 * result + (julia ? 1 : 0);
//		result = 31 * result + (options != null ? options.hashCode() : 0);
//		return result;
//	}
}
