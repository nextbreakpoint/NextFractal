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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import lombok.Getter;

import java.util.Objects;

public class Region {
	private double x0;
	private double y0;
	private double x1;
	private double y1;
    @Getter
    private ComplexNumber center;
    @Getter
    private ComplexNumber size;

	public Region() {
		setPoints(new ComplexNumber(0,0), new ComplexNumber(0,0));
	}
	
	public Region(ComplexNumber a, ComplexNumber b) {
		setPoints(a, b);
	}
	
	public Region(ComplexNumber[] points) {
		setPoints(points[0], points[1]);
	}

	public void setPoints(ComplexNumber a, ComplexNumber b) {
		this.x0 = a.r();
		this.y0 = a.i();
		this.x1 = b.r();
		this.y1 = b.i();
		size = new ComplexNumber(x1 - x0, y1 - y0);
		center = new ComplexNumber((x0 + x1) / (2 * size.r()), (y0 + y1) / (2 * size.i()));
	}

	public double left() {
		return x0;
	}

	public double right() {
		return x1;
	}

	public double bottom() {
		return y0;
	}

	public double top() {
		return y1;
	}

	@Override
	public String toString() {
		return "[a=(" + x0 + "," + y0 + "), b=(" + x1 + "," + y1 + "), center=" + center + ", size=" + size + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Region region = (Region) o;
		return Double.compare(x0, region.x0) == 0 && Double.compare(y0, region.y0) == 0 && Double.compare(x1, region.x1) == 0 && Double.compare(y1, region.y1) == 0 && Objects.equals(center, region.center) && Objects.equals(size, region.size);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x0, y0, x1, y1, center, size);
	}
}
