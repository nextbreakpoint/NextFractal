/*
 * NextFractal 2.4.0
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
package com.nextbreakpoint.nextfractal.core.graphics;

public interface GraphicsContext {
	int JOIN_MITER = 1;
	int JOIN_ROUND = 2;
	int JOIN_BEVEL = 3;
	int CAP_BUTT = 1;
	int CAP_ROUND = 2;
	int CAP_SQUARE = 3;
	int EVEN_ODD = 0;
	int NON_ZERO = 1;

	void setStroke(Color c);

	void setFill(Color c);
	
	void setFont(Font font);

	void setWindingRule(int windingRule);

	void rect(int x, int y, int width, int height);
	
	void stroke();
	
	void fill();

	void clip();

	void beginPath();

	void closePath();

	void moveTo(float x, float y);
	
	void lineTo(float x, float y);

	void quadTo(float x1, float y1, float x2, float y2);

	void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3);

//	void arcTo(float rx, float ry, float angle, float largeArcFlag, float seepwFlag, float x, float y);

	void strokeRect(int x, int y, int width, int height);
	
	void fillRect(int x, int y, int width, int height);
	
	void strokeText(String text, int x, int y);

	void fillText(String text, int x, int y);

	void drawImage(Image image, int x, int y);

	void drawImage(Image image, int x, int y, int w, int h);

	void clearRect(int x, int y, int width, int height);

	void setAffineTransform(AffineTransform t);

	void save();

	void restore();

	void setClip(int x, int y, int width, int height);

	void setAlpha(double alpha);

	void setStrokeLine(float width, int cap, int join, float miterLimit);
}
