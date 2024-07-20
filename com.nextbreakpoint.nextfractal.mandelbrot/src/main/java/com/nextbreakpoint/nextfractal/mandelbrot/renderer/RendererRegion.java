/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.mandelbrot.renderer;

import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;

public class RendererRegion {
	private double x0;
	private double y0;
	private double x1;
	private double y1;
	private Number center;
	private Number size;

	public RendererRegion() {
		setPoints(new Number(0,0), new Number(0,0));
	}
	
	public RendererRegion(Number a, Number b) {
		setPoints(a, b);
	}
	
	public RendererRegion(Number[] points) {
		setPoints(points[0], points[1]);
	}

	/**
	 * @param a
	 * @param b
	 */
	public void setPoints(Number a, Number b) {
		this.x0 = a.r();
		this.y0 = a.i();
		this.x1 = b.r();
		this.y1 = b.i();
		size = new Number(x1 - x0, y1 - y0);
		center = new Number((x0 + x1) / (2 * size.r()), (y0 + y1) / (2 * size.i()));
	}

	/**
	 * @return
	 */
	public Number getCenter() {
		return center;
	}

	/**
	 * @return
	 */
	public Number getSize() {
		return size;
	}
	
	/**
	 * @return
	 */
	public double left() {
		return x0;
	}

	/**
	 * @return
	 */
	public double right() {
		return x1;
	}

	/**
	 * @return
	 */
	public double bottom() {
		return y0;
	}

	/**
	 * @return
	 */
	public double top() {
		return y1;
	}

	@Override
	public String toString() {
		return "[a=(" + x0 + "," + y0 + "), b=(" + x1 + "," + y1 + "), center=" + center + ", size=" + size + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((center == null) ? 0 : center.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RendererRegion other = (RendererRegion) obj;
		if (center == null) {
			if (other.center != null)
				return false;
		} else if (!center.equals(other.center))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (Double.doubleToLongBits(x0) != Double.doubleToLongBits(other.x0))
			return false;
		if (Double.doubleToLongBits(x1) != Double.doubleToLongBits(other.x1))
			return false;
		if (Double.doubleToLongBits(y0) != Double.doubleToLongBits(other.y0))
			return false;
		if (Double.doubleToLongBits(y1) != Double.doubleToLongBits(other.y1))
			return false;
		return true;
	}
}
