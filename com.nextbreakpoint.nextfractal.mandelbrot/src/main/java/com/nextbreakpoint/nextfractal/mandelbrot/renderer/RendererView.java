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

import com.nextbreakpoint.nextfractal.core.common.Double4D;
import com.nextbreakpoint.nextfractal.core.common.Integer4D;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;

public class RendererView {
	private Double4D translation;
	private Double4D rotation;
	private Double4D scale;
	private Integer4D state;
	private Number point;
	private boolean julia;
	
	public RendererView() {
		translation = new Double4D(0, 0, 1, 0);
		rotation = new Double4D(0, 0, 0, 0);
		scale = new Double4D(1, 1, 1, 1);
		state = new Integer4D(0, 0, 0, 0);
		point = new Number(0, 0);
	}

	public Double4D getTraslation() {
		return translation;
	}

	public void setTraslation(Double4D translation) {
		this.translation = translation;
	}

	public Double4D getRotation() {
		return rotation;
	}

	public void setRotation(Double4D rotation) {
		this.rotation = rotation;
	}

	public Double4D getScale() {
		return scale;
	}

	public void setScale(Double4D scale) {
		this.scale = scale;
	}

	public Number getPoint() {
		return point;
	}

	public void setPoint(Number point) {
		this.point = point;
	}

	public boolean isJulia() {
		return julia;
	}

	public void setJulia(boolean julia) {
		this.julia = julia;
	}

	public Integer4D getState() {
		return state;
	}

	public void setState(Integer4D state) {
		this.state = state;
	}
}
