/*
 * NextFractal 2.1.2-rc2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;

public interface ToolContext {
	public double getWidth();

	public double getHeight();

	public Number getInitialSize();

	public Number getInitialCenter();

	public double getZoomSpeed();

	public RendererFactory getRendererFactory();

	public MandelbrotMetadata getMetadata();

	public void setView(MandelbrotMetadata view, boolean continuous, boolean appendHistory);

	public void setTime(MandelbrotMetadata metadata, boolean continuous, boolean appendHistory);

	public void setPoint(MandelbrotMetadata view, boolean continuous, boolean appendHistory);
}
