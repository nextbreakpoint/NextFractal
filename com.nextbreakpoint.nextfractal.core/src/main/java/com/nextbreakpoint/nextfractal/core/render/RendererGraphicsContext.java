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

public interface RendererGraphicsContext {
	public static final int JOIN_MITER = 1;
	public static final int JOIN_ROUND = 2;
	public static final int JOIN_BEVEL = 3;
	public static final int CAP_BUTT = 1;
	public static final int CAP_ROUND = 2;
	public static final int CAP_SQUARE = 3;
	public static final int EVEN_ODD = 0;
	public static final int NON_ZERO = 1;

	public void setStroke(RendererColor c);

	public void setFill(RendererColor c);
	
	public void setFont(RendererFont font);

	public void setWindingRule(int windingRule);

	public void rect(int x, int y, int width, int height);
	
	public void stroke();
	
	public void fill();

	public void clip();

	public void beginPath();

	public void closePath();

	public void moveTo(float x, float y);
	
	public void lineTo(float x, float y);

	public void quadTo(float x1, float y1, float x2, float y2);

	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3);

//	public void arcTo(float rx, float ry, float angle, float largeArcFlag, float seepwFlag, float x, float y);

	public void strokeRect(int x, int y, int width, int height);
	
	public void fillRect(int x, int y, int width, int height);
	
	public void strokeText(String text, int x, int y);

	public void fillText(String text, int x, int y);

	public void drawImage(RendererImage image, int x, int y);

	public void drawImage(RendererImage image, int x, int y, int w, int h);

	public void clearRect(int x, int y, int width, int height);

	public void setAffine(RendererAffine t);

	public void save();

	public void restore();

	public void setClip(int x, int y, int width, int height);

	public void setAlpha(double alpha);

	public void setStrokeLine(float width, int cap, int join, float miterLimit);
}
