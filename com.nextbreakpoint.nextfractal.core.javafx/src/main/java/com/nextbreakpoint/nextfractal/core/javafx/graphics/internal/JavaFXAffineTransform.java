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
package com.nextbreakpoint.nextfractal.core.javafx.graphics.internal;

import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import javafx.scene.transform.Affine;

public class JavaFXAffineTransform implements AffineTransform {
	private final Affine affineTransform;
	
	public JavaFXAffineTransform(Affine affineTransform) {
		this.affineTransform = affineTransform;
	}

	@Override
	public void setAffineTransform(GraphicsContext context) {
		((JavaFXGraphicsContext)context).getGraphicsContext().setTransform(affineTransform);
	}

	@Override
	public void append(AffineTransform affineTransform) {
		this.affineTransform.append(((JavaFXAffineTransform)affineTransform).affineTransform);
	}
}
