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
package com.nextbreakpoint.nextfractal.contextfree.core;

public class AffineTransform1D implements Cloneable {
	private double sz;
	private double tz;
	
	private AffineTransform1D(double sz, double tz) {
		this.sz = sz;
		this.tz = tz;
	}

	public AffineTransform1D() {
		this(1, 0);
	}

	public static AffineTransform1D getTranslateInstance(double tz) {
		return new AffineTransform1D(1.0, tz);
	}
	
	public static AffineTransform1D getScaleInstance(double sz) {
		return new AffineTransform1D(sz, 0.0);
	}

	public void concatenate(AffineTransform1D t) {
		this.sz = this.sz * t.sz;
		this.tz = this.tz * t.sz + t.tz;
	}

	public double tz() {
		return tz;
	}

	public void setTz(double tz) {
		this.tz = tz;
	}

	public double sz() {
		return sz;
	}

	public void setSz(double sz) {
		this.sz = sz;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// this shouldn't happen, since we are Cloneable
			throw new InternalError(e);
		}
	}
}
