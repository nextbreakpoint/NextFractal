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
package com.nextbreakpoint.nextfractal.core.render;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Stack;

public class Java2DRendererGraphicsContext implements RendererGraphicsContext {
	private Graphics2D g2d;
	private Stack<State> stack = new Stack<>();
	private GeneralPath shape;
	private RendererColor strokeColor;
	private RendererColor fillColor;

	public Java2DRendererGraphicsContext(Graphics2D g2d) {
		this.g2d = g2d;
	}

	public void setStroke(RendererColor color) {
		strokeColor = color;
	}

	public void setFill(RendererColor color) {
		fillColor = color;
	}

	public void setFont(RendererFont font) {
		font.setFont(this);
	}

	public void setWindingRule(int windingRule) {
		if (windingRule == 0) {
			shape.setWindingRule(Path2D.WIND_EVEN_ODD);
		} else {
			shape.setWindingRule(Path2D.WIND_NON_ZERO);
		}
	}

	public void rect(int x, int y, int width, int height) {
		if (shape == null) {
			beginPath();
		}
		shape.append(new Rectangle2D.Double(x, y, width, height), false);
	}
	
	public void stroke() {
		if (shape != null) {
			strokeColor.setStroke(this);
			g2d.draw(shape);
		}
	}

	public void fill() {
		if (shape != null) {
			fillColor.setFill(this);
			g2d.fill(shape);
		}
	}

	public void clip() {
		if (shape != null) {
			g2d.clip(shape);
		}
	}

	public void beginPath() {
		shape = new GeneralPath();
	}

	public void closePath() {
		if (shape != null) {
			shape.closePath();
		}
	}

	public void strokeRect(int x, int y, int width, int height) {
		Color color = g2d.getColor();
		strokeColor.setStroke(this);
		g2d.drawRect(x, y, width, height);
		g2d.setColor(color);
	}
	
	public void fillRect(int x, int y, int width, int height) {
		Color color = g2d.getColor();
		fillColor.setFill(this);
		g2d.fillRect(x, y, width, height);
		g2d.setColor(color);
	}
	
	public void strokeText(String text, int x, int y) {
		Color color = g2d.getColor();
		strokeColor.setStroke(this);
		g2d.drawString(text, x, y);
		g2d.setColor(color);
	}

	public void fillText(String text, int x, int y) {
		Color color = g2d.getColor();
		fillColor.setFill(this);
		g2d.drawString(text, x, y);
		g2d.setColor(color);
	}

	public void drawImage(RendererImage image, int x, int y) {
		image.draw(this, x, y);
	}

	public void drawImage(RendererImage image, int x, int y, int w, int h) {
		image.draw(this, x, y, w, h);
	}

	public void clearRect(int x, int y, int width, int height) {
		Color color = g2d.getColor();
		g2d.setColor(new Color(0, 0, 0, 0));
		g2d.clearRect(x, y, width, height);
		g2d.setColor(color);
	}

	public void setAffine(RendererAffine affine) {
		affine.setAffine(this);
	}

	public void save() {
		State state = new State();
		state.transform = g2d.getTransform();
		state.clip = g2d.getClip();
		stack.push(state);
	}

	public void restore() {
		State state = stack.pop();
		g2d.setTransform(state.transform);
		g2d.setClip(state.clip);
	}

	public Graphics2D getGraphicsContext() {
		return g2d;
	}

	public void setClip(int x, int y, int width, int height) {
		g2d.setClip(x, y, width, height);
	}

	public void clearClip() {
		g2d.setClip(null);
	}
	
	private class State {
		AffineTransform transform;
		Shape clip;
	}

	public void moveTo(float x, float y) {
		shape.moveTo(x, y);
	}

	public void lineTo(float x, float y) {
		shape.lineTo(x, y);
	}

	public void quadTo(float x1, float y1, float x2, float y2) {
		shape.quadTo(x1, y1, x2, y2);
	}

	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		shape.curveTo(x1, y1, x2, y2, x3, y3);
	}

//	public void arcTo(float rx, float ry, float angle, float largeArcFlag, float seepwFlag, float x, float y) {
//	}

	public void setAlpha(double alpha) {
		g2d.setComposite(AlphaComposite.SrcOver.derive((float)alpha));
	}

	public void setStrokeLine(float width, int cap, int join, float miterLimit) {
		g2d.setStroke(new BasicStroke(width, mapToCap(cap), mapToJoin(join), miterLimit));
	}

	private int mapToJoin(int join) {
		switch (join) {
			case JOIN_MITER:
				return BasicStroke.JOIN_MITER;
			case JOIN_ROUND:
				return BasicStroke.JOIN_ROUND;
			case JOIN_BEVEL:
				return BasicStroke.JOIN_BEVEL;
			default:
				throw new RuntimeException("Invalid line join " + join);
		}
	}

	private int mapToCap(int cap) {
		switch (cap) {
			case CAP_BUTT:
				return BasicStroke.CAP_BUTT;
			case CAP_ROUND:
				return BasicStroke.CAP_ROUND;
			case CAP_SQUARE:
				return BasicStroke.CAP_SQUARE;
			default:
				throw new RuntimeException("Invalid line cap " + cap);
		}
	}
}
