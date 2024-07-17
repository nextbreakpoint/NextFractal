/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.core.javafx.render;

import com.nextbreakpoint.nextfractal.core.render.RendererColor;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import javafx.scene.paint.Color;

public class JavaFXRendererColor implements RendererColor {
	private final Color color;
	
	public JavaFXRendererColor(double red, double green, double blue, double opacity) {
		color = new Color(red, green, blue, opacity);
	}

	@Override
	public void setStroke(RendererGraphicsContext context) {
		((JavaFXRendererGraphicsContext)context).getGraphicsContext().setStroke(color);
	}
	
	@Override
	public void setFill(RendererGraphicsContext context) {
		((JavaFXRendererGraphicsContext)context).getGraphicsContext().setFill(color);
	}
}
