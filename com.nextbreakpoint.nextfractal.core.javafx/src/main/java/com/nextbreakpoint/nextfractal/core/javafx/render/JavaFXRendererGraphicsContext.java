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
package com.nextbreakpoint.nextfractal.core.javafx.render;

import com.nextbreakpoint.nextfractal.core.render.RendererAffine;
import com.nextbreakpoint.nextfractal.core.render.RendererColor;
import com.nextbreakpoint.nextfractal.core.render.RendererFont;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererImage;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class JavaFXRendererGraphicsContext implements RendererGraphicsContext {
	private GraphicsContext gc;

	public JavaFXRendererGraphicsContext(GraphicsContext gc) {
		this.gc = gc;
	}

	public void setStroke(RendererColor color) {
		color.setStroke(this);
	}

	public void setFill(RendererColor color) {
		color.setFill(this);
	}

	public void setFont(RendererFont font) {
		font.setFont(this);
	}

	public void setWindingRule(int windingRule) {
		if (windingRule == 0) {
			gc.setFillRule(FillRule.EVEN_ODD);
		} else {
			gc.setFillRule(FillRule.NON_ZERO);
		}
	}

	public void rect(int x, int y, int width, int height) {
		gc.rect(x, y, width, height);
	}
	
	public void beginPath() {
		gc.beginPath();
	}

	public void closePath() {
		gc.closePath();
	}

	public void stroke() {
		gc.stroke();
	}

	public void fill() {
		gc.fill();
	}

	public void clip() {
		gc.clip();
	}

	public void strokeRect(int x, int y, int width, int height) {
		gc.strokeRect(x, y, width, height);
	}
	
	public void fillRect(int x, int y, int width, int height) {
		gc.fillRect(x, y, width, height);
	}
	
	public void strokeText(String text, int x, int y) {
		gc.strokeText(text, x, y);
	}

	public void fillText(String text, int x, int y) {
		gc.fillText(text, x, y);
	}

	public void drawImage(RendererImage image, int x, int y) {
		image.draw(this, x, y);
	}

	public void drawImage(RendererImage image, int x, int y, int w, int h) {
		image.draw(this, x, y, w, h);
	}

	public void clearRect(int x, int y, int width, int height) {
		gc.clearRect(x, y, width, height);
	}

	public void setAffine(RendererAffine affine) {
		affine.setAffine(this);
	}

	public void save() {
		gc.save();
	}

	public void restore() {
		gc.restore();
	}

	public GraphicsContext getGraphicsContext() {
		return gc;
	}

	public void setClip(int x, int y, int width, int height) {
		gc.beginPath();
		gc.rect(x, y, width, height);
		gc.clip();
	}

	public void moveTo(float x, float y) {
		gc.moveTo(x, y);
	}

	public void lineTo(float x, float y) {
		gc.lineTo(x, y);
	}

	public void quadTo(float x1, float y1, float x2, float y2) {
		gc.quadraticCurveTo(x1, y1, x2, y2);
	}

	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		gc.bezierCurveTo(x1, y1, x2, y2, x3, y3);
	}

//	public void arcTo(float rx, float ry, float angle, float largeArcFlag, float seepwFlag, float x, float y) {
//	}

	public void setAlpha(double alpha) {
		gc.setGlobalAlpha(alpha);
	}

	public void setStrokeLine(float width, int cap, int join, float miterLimit) {
		gc.setLineCap(mapToCap(cap));
		gc.setLineJoin(mapToJoin(join));
		gc.setLineWidth(width);
		gc.setMiterLimit(miterLimit);
	}

	private StrokeLineJoin mapToJoin(int join) {
		switch (join) {
			case JOIN_MITER:
				return StrokeLineJoin.MITER;
			case JOIN_ROUND:
				return StrokeLineJoin.ROUND;
			case JOIN_BEVEL:
				return StrokeLineJoin.BEVEL;
			default:
				throw new RuntimeException("Invalid line join " + join);
		}
	}

	private StrokeLineCap mapToCap(int cap) {
		switch (cap) {
			case CAP_BUTT:
				return StrokeLineCap.BUTT;
			case CAP_ROUND:
				return StrokeLineCap.ROUND;
			case CAP_SQUARE:
				return StrokeLineCap.SQUARE;
			default:
				throw new RuntimeException("Invalid line cap " + cap);
		}
	}
}
