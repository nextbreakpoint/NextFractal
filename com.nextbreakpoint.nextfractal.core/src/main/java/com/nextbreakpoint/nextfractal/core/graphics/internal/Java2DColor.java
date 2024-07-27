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
package com.nextbreakpoint.nextfractal.core.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.Color;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;

public class Java2DColor implements Color {
	private final java.awt.Color color;
	
	public Java2DColor(double red, double green, double blue, double opacity) {
		color = new java.awt.Color((float)red, (float)green, (float)blue, (float)opacity);
	}

	@Override
	public void setStroke(GraphicsContext context) {
		((Java2DGraphicsContext)context).getGraphicsContext().setColor(color);
	}
	
	@Override
	public void setFill(GraphicsContext context) {
		((Java2DGraphicsContext)context).getGraphicsContext().setColor(color);
	}
}
