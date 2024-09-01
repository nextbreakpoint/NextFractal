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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics;

import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import lombok.Setter;

public class RendererData {
	protected double[] positionX;
	protected double[] positionY;
	protected MutableNumber point;
	protected int[] newPixels;
	protected int[] oldPixels;
	protected double[] newCache;
	protected double[] oldCache;
	protected int width; 
	protected int height;
	protected int depth;
	@Setter
	protected Region region;

	public RendererData() {
		region = new Region();
		point = new MutableNumber(0, 0);
	}

	public void free() {
		positionX = null;
		positionY = null;
		newPixels = null;
		oldPixels = null;
		region = null;
		point = null;
		newCache = null;
		oldCache = null;
	}

	public void setSize(final int width, final int height, final int depth) {
		if (this.width != width || this.height != height) {
			realloc(width, height);
			this.width = width;
			this.height = height;
			newCache = new double[width * height * 2 * depth];
			oldCache = new double[width * height * 2 * depth];
			this.depth = depth;
		} else {
			if (this.depth != depth) {
				newCache = new double[width * height * 2 * depth];
				oldCache = new double[width * height * 2 * depth];
				this.depth = depth;
			}
		}
	}

	protected void realloc(final int width, final int height) {
		positionX = new double[width];
		positionY = new double[height];
		newPixels = new int[width * height];
		oldPixels = new int[width * height];
	}

	public void swap() {
		final double[] tmpCache = oldCache;
		oldCache = newCache;
		newCache = tmpCache;
		final int[] tmpPixels = oldPixels;
		oldPixels = newPixels;
		newPixels = tmpPixels;
	}

	public double left() {
		return region.left();
	}

	public double right() {
		return region.right();
	}

	public double bottom() {
		return region.bottom();
	}

	public double top() {
		return region.top();
	}

	public ComplexNumber point() {
		return point;
	}
	
	public void setPoint(ComplexNumber point) {
		this.point.set(point);
	}

	public void setPixel(int offset, int argb) {
		newPixels[offset] = argb;
	}

	public int getPixel(int offset) {
		return newPixels[offset];
	}

	public int[] getPixels() {
		return newPixels;
	}

	public double positionX(int i) {
		return positionX[i];
	}

	public double positionY(int i) {
		return positionY[i];
	}

	public double[] positionX() {
		return positionX;
	}

	public double[] positionY() {
		return positionY;
	}

	public void setPositionX(int i, double position) {
		positionX[i] = position;
	}

	public void setPositionY(int i, double position) {
		positionY[i] = position;
	}

	public void initPositions() {
		final int sizex = width;
		final int sizey = height;
		final double stepx = (right() - left()) / (sizex - 1);
		final double stepy = (top() - bottom()) / (sizey - 1);
		double posx = left();
		double posy = bottom();
		for (int x = 0; x < sizex; x++) {
			positionX[x] = posx;
			posx += stepx;
		}
		for (int y = 0; y < sizey; y++) {
			positionY[y] = posy;
			posy += stepy;
		}
	}

	public State newPoint() {
		return new State(depth);
	}

	public void getPoint(int offset, State p) {
		int d2 = depth * 2;
		int o1 = offset * d2 + 0;
		int o2 = offset * d2 + 1;
		for (int k = 0, j = 0; j < d2; j += 2, k += 1) {
			double r = oldCache[o1 + j];
			double i = oldCache[o2 + j];
			p.vars()[k].set(r, i);
		}
	}

	public void setPoint(int offset, State p) {
		int d2 = depth * 2;
		int o1 = offset * d2 + 0;
		int o2 = offset * d2 + 1;
		for (int k = 0, j = 0; j < d2; j += 2, k += 1) {
			MutableNumber var = p.vars()[k];
			newCache[o1 + j] = var.r();
			newCache[o2 + j] = var.i();
		}
	}

	public void movePixels(int from, int to, int length) {
		System.arraycopy(newPixels, from, newPixels, to, length);
	}

	public void moveCache(int from, int to, int length) {
		int d2 = 2 * depth;
		System.arraycopy(newCache, from * d2, newCache, to * d2, length * d2);
	}

	public void copyPixels(int from, int to, int length) {
		System.arraycopy(oldPixels, from, newPixels, to, length);
	}

	public void copyCache(int from, int to, int length) {
		int d2 = 2 * depth;
		System.arraycopy(oldCache, from * d2, newCache, to * d2, length * d2);
	}

	public void clearPixels() {
		for (int i = 0; i < width; i++) {
			newPixels[i] = 0xFF000000;
		}
		for (int y = 1; y < height; y++) {
			System.arraycopy(newPixels, 0, newPixels, y * width, width);
		}
	}
}
