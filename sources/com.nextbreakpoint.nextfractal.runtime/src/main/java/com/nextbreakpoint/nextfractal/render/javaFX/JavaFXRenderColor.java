package com.nextbreakpoint.nextfractal.render.javaFX;

import javafx.scene.paint.Color;

import com.nextbreakpoint.nextfractal.render.RenderColor;
import com.nextbreakpoint.nextfractal.render.RenderGraphicsContext;

public class JavaFXRenderColor implements RenderColor {
	private Color color;
	
	public JavaFXRenderColor(double red, double green, double blue, double opacity) {
		color = new Color(red, green, blue, opacity);
	}

	@Override
	public void setStroke(RenderGraphicsContext context) {
		((JavaFXRenderGraphicsContext)context).getGraphicsContext().setStroke(color);
	}
	
	@Override
	public void setFill(RenderGraphicsContext context) {
		((JavaFXRenderGraphicsContext)context).getGraphicsContext().setFill(color);
	}
}
