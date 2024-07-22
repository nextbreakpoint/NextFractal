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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.javafx.render.JavaFXRendererFactory;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class GridViewCell extends BorderPane {
	private JavaFXRendererFactory renderFactory = new JavaFXRendererFactory();
	private boolean redraw;
	private Canvas canvas;
	private Object data;
	private int index;
	
	public GridViewCell(int index, int width, int height) {
		this.index = index;

		canvas = new Canvas(width, height);
		
		setCenter(canvas);
		
		widthProperty().addListener((observable, oldValue, newValue) -> update());
		heightProperty().addListener((observable, oldValue, newValue) -> update());
	}

	public void update() {
		GridItem item = (GridItem)data;
		if (item != null) {
			if (item.isDirty()) {
				item.setDirty(false);
				redraw = true;
			}
			GridItemRenderer renderer = item.getRenderer();
			if (renderer != null) {
				if (redraw || renderer.isPixelsChanged()) {
					RendererGraphicsContext gc = renderFactory.createGraphicsContext(canvas.getGraphicsContext2D());
					renderer.drawImage(gc, 0, 0);
					redraw = false;
				}
			} else if (redraw) {
				GraphicsContext g2d = canvas.getGraphicsContext2D();
				g2d.setFill(Color.WHITE);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				g2d.setFill(Color.DARKGRAY);
				g2d.setTextAlign(TextAlignment.CENTER);
				BrowseBitmap bitmap = item.getBitmap();
				if (item.getErrors().size() > 0) {
					g2d.fillText("Error", getWidth() / 2, getHeight() / 2);
				} else if (bitmap == null) {
					g2d.fillText("Rendering...", getWidth() / 2, getHeight() / 2);
				}
				redraw = false;
			}
			if (item.isSelected()) {
				GraphicsContext g2d = canvas.getGraphicsContext2D();
				g2d.setStroke(Color.YELLOW);
				g2d.setLineWidth(5);
				g2d.strokeRect(0, 0, getWidth(), getHeight());
			}
		} else {
			if (redraw) {
				GraphicsContext g2d = canvas.getGraphicsContext2D();
				g2d.setFill(Color.WHITE);
				g2d.fillRect(0, 0, getWidth(), getHeight());
				redraw = false;
			}
		}
	}

	public void setData(Object data) {
		if (this.data != data) {
			this.data = data;
			redraw = true;
			//update();
		}
	}

	public int getIndex() {
		return index;
	}
}
