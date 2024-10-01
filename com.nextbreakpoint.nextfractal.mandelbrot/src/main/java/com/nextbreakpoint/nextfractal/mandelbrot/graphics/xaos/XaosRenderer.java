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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics.xaos;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.Renderer;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererData;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.RendererErrors;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.State;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.strategy.JuliaStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.strategy.MandelbrotStrategy;
import lombok.extern.java.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.mandelbrot.module.SystemProperties.PROPERTY_MANDELBROT_RENDERING_XAOS_OVERLAPPING_ENABLED;

/*
 *     XaoS, a fast portable realtime fractal zoomer
 *                  Copyright (C) 1996,1997 by
 *
 *      Jan Hubicka          (hubicka@paru.cas.cz)
 *      Thomas Marsh         (tmarsh@austin.ibm.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

@Log
public final class XaosRenderer extends Renderer {
	static {
		if (XaosConstants.PRINT_MULTABLE) {
			log.fine("Multable:");
			for (int i = -XaosConstants.FPRANGE; i < XaosConstants.FPRANGE; i++) {
				log.fine("i = " + i + ", i * i = " + XaosConstants.MULTABLE[XaosConstants.FPRANGE + i]);
			}
		}
	}
	
	private boolean overlapping;
	private boolean isSolidguessSupported;
	private boolean isVerticalSymmetrySupported;
	private boolean isHorizontalSymmetrySupported;
	private final XaosRendererData xaosRendererData;
	private boolean cacheActive;
	private ExecutorService executor;
	private Future<?> futureLines;
	private Future<?> futureColumns;
	private final Runnable redrawLinesRunnable = () -> prepareLines(true);
	private final Runnable refreshLinesRunnable = () -> prepareLines(false);
	private final Runnable redrawColumnsRunnable = () -> prepareColumns(true);
	private final Runnable refreshColumnsRunnable = () -> prepareColumns(false);

	public XaosRenderer(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile) {
		super(threadFactory, renderFactory, tile);
		this.xaosRendererData = (XaosRendererData) contentRendererData;
		if (multiThread) {
			executor = ExecutorUtils.newSingleThreadExecutor(threadFactory);
		}
		if (Boolean.getBoolean(PROPERTY_MANDELBROT_RENDERING_XAOS_OVERLAPPING_ENABLED)) {
			overlapping = true;
		}
	}
	
	@Override
	public void dispose() {
		ExecutorUtils.shutdown(executor);
		super.dispose();
	}

	@Override
	protected RendererData createRendererData() {
		return new XaosRendererData();
	}

	@Override
	protected void render() {
		final List<ScriptError> errors = new ArrayList<>();
		try {
//			if (interrupted) {
//				progress = 0;
//				contentRendererData.swap();
//				contentRendererData.clearPixels();
//				didChanged(progress, contentRendererData.getPixels());
//				return;
//			}
			if (contentRendererFractal == null) {
				progress = 1;
				contentRendererData.swap();
				contentRendererData.clearPixels();
				update(progress, contentRendererData.getPixels());
				return;
			}
			if (contentRendererFractal.getOrbit() == null) {
				progress = 1;
				contentRendererData.swap();
				contentRendererData.clearPixels();
				update(progress, contentRendererData.getPixels());
				return;
			}
			if (contentRendererFractal.getColor() == null) {
				progress = 1;
				contentRendererData.swap();
				contentRendererData.clearPixels();
				update(progress, contentRendererData.getPixels());
				return;
			}
			boolean copyOfTimeChanged = timeChanged;
			boolean orbitTimeChanged = copyOfTimeChanged && contentRendererFractal.getOrbit().useTime();
			boolean colorTimeChanged = copyOfTimeChanged && contentRendererFractal.getColor().useTime();
			boolean copyOfRegionChanged = regionChanged;
			final boolean calculate = (!continuous && copyOfRegionChanged) || orbitChanged || juliaChanged || (julia && pointChanged) || orbitTimeChanged;
			final boolean refresh = !calculate && (colorChanged || colorTimeChanged);
			final boolean oldActiveCache = cacheActive;
			cacheActive = refresh || !continuous || orbitTimeChanged || colorTimeChanged;
			final boolean redraw = (cacheActive && !oldActiveCache) || calculate;
//			log.info("julia " + julia + ", cache " + cacheActive + ", redraw " + redraw + ", refresh " + refresh + ", continuous " + continuous + ", timeChanged " + copyOfTimeChanged);
			timeChanged = false;
			pointChanged = false;
			orbitChanged = false;
			colorChanged = false;
			juliaChanged = false;
			regionChanged = false;
			progress = 0;
			contentRendererFractal.getOrbit().setTime(time);
			contentRendererFractal.getColor().setTime(time);
			contentRendererFractal.clearScope();
			contentRendererFractal.setPoint(point);
			if (julia) {
				contentRendererStrategy = new JuliaStrategy(contentRendererFractal);
			} else {
				contentRendererStrategy = new MandelbrotStrategy(contentRendererFractal);
			}
			contentRendererStrategy.prepare();
			int width = getSize().width();
			int height = getSize().height();
			contentRendererData.setSize(width, height, contentRendererFractal.getStateSize());
			contentRendererData.setPoint(contentRendererFractal.getPoint());
			if (copyOfRegionChanged) {
				contentRendererData.setRegion(contentRegion);
			}
			if (XaosConstants.PRINT_REGION) {
				log.fine("Region: (" + xaosRendererData.left() + "," + xaosRendererData.bottom() + ") -> (" + xaosRendererData.right() + "," + xaosRendererData.top() + ")");
			}
			isSolidguessSupported = XaosConstants.USE_SOLIDGUESS && contentRendererStrategy.isSolidGuessSupported();
			isVerticalSymmetrySupported = XaosConstants.USE_SYMETRY && contentRendererStrategy.isVerticalSymmetrySupported();
			isHorizontalSymmetrySupported = XaosConstants.USE_SYMETRY && contentRendererStrategy.isHorizontalSymmetrySupported();
			if (XaosConstants.DUMP) {
				log.fine("Solidguess supported = " + isSolidguessSupported);
				log.fine("Vertical symetry supported = " + isVerticalSymmetrySupported);
				log.fine("Horizontal symetry supported = " + isHorizontalSymmetrySupported);
			}
			if (executor != null && XaosConstants.USE_MULTITHREAD && !XaosConstants.DUMP_XAOS) {
				if (redraw) {
					futureLines = executor.submit(redrawLinesRunnable);
//					futureColumns = executor.submit(redrawColumnsRunnable);
				} else {
					futureLines = executor.submit(refreshLinesRunnable);
//					futureColumns = executor.submit(refreshColumnsRunnable);
				}
			} else {
				prepareColumns(redraw);
//				prepareLines(redraw);
			}
			prepareLines(redraw);
			if (executor != null && XaosConstants.USE_MULTITHREAD && !XaosConstants.DUMP_XAOS) {
				if (futureLines != null) {
					futureLines.get();
				}
//				if (futureColumns != null) {
//					futureColumns.get();
//				}
			}
			if (XaosConstants.PRINT_REALLOCTABLE) {
				log.fine("ReallocTable X:");
				for (XaosRealloc element : xaosRendererData.reallocX()) {
					if (!XaosConstants.PRINT_ONLYNEW || element.calculate) {
						log.fine(element.toString());
					}
				}
				log.fine("ReallocTable Y:");
				for (XaosRealloc element : xaosRendererData.reallocY()) {
					if (!XaosConstants.PRINT_ONLYNEW || element.calculate) {
						log.fine(element.toString());
					}
				}
			}
			contentRendererData.swap();
			processReallocTable(continuous && !redraw, refresh);
			updatePositions();
		} catch (Throwable e) {
			log.log(Level.WARNING, "Rendering error", e);
			errors.add(RendererErrors.makeError(0, 0, 0, 0, e.getMessage()));
		}
		if (!errors.isEmpty()) {
			update(progress, errors);
		}
	}


	private void prepareLines(boolean redraw) {
		final double beginy = xaosRendererData.bottom();
		final double endy = xaosRendererData.top();
		double stepy = 0;
		if (redraw || !XaosConstants.USE_XAOS) {
			stepy = initReallocTableAndPosition(xaosRendererData.reallocY(), xaosRendererData.positionY(), beginy, endy);
		}
		else {
			stepy = makeReallocTable(xaosRendererData.reallocY(), xaosRendererData.dynamicY(), beginy, endy, xaosRendererData.positionY(), !cacheActive);
		}
		final double symy = contentRendererStrategy.getVerticalSymmetryPoint();
		if (isVerticalSymmetrySupported && contentRendererStrategy.isVerticalSymmetrySupported() && (!((beginy > symy) || (symy > endy)))) {
			prepareSymmetry(xaosRendererData.reallocY(), (int) ((symy - beginy) / stepy), symy, stepy);
		}
	}

	private void prepareColumns(boolean redraw) {
		final double beginx = xaosRendererData.left();
		final double endx = xaosRendererData.right();
		double stepy = 0;
		if (redraw || !XaosConstants.USE_XAOS) {
			stepy = initReallocTableAndPosition(xaosRendererData.reallocX(), xaosRendererData.positionX(), beginx, endx);
		}
		else {
			stepy = makeReallocTable(xaosRendererData.reallocX(), xaosRendererData.dynamicX(), beginx, endx, xaosRendererData.positionX(), !cacheActive);
		}
		final double symy = contentRendererStrategy.getHorizontalSymmetryPoint();
		if (isVerticalSymmetrySupported && contentRendererStrategy.isVerticalSymmetrySupported() && (!((beginx > symy) || (symy > endx)))) {
			prepareSymmetry(xaosRendererData.reallocY(), (int) ((symy - beginx) / stepy), symy, stepy);
		}
	}

	private double initReallocTableAndPosition(final XaosRealloc[] realloc, final double[] position, final double begin, final double end) {
		if (XaosConstants.DUMP) {
			log.fine("Init realloc and position...");
		}
		final double step = (end - begin) / (realloc.length - 1);
		double tmpPosition = begin;
		XaosRealloc tmpRealloc = null;
		for (int i = 0; i < realloc.length; i++) {
			tmpRealloc = realloc[i];
			position[i] = tmpPosition;
			tmpRealloc.position = position[i];
			tmpRealloc.calculate = true;
			tmpRealloc.refreshed = false;
			tmpRealloc.dirty = true;
			tmpRealloc.isCached = false;
			tmpRealloc.isFilled = false;
			tmpRealloc.plus = i;
			tmpRealloc.symTo = -1;
			tmpRealloc.symRef = -1;
			tmpPosition += step;
		}
		return step;
	}

	private void updatePositions() {
		if (XaosConstants.DUMP) {
			log.fine("Update positions...");
		}
		for (int k = 0; k < xaosRendererData.reallocX().length; k++) {
			xaosRendererData.setPositionX(k, xaosRendererData.reallocX()[k].position);
		}
		for (int k = 0; k < xaosRendererData.reallocY().length; k++) {
			xaosRendererData.setPositionY(k, xaosRendererData.reallocY()[k].position);
		}
	}

	private int price(final int p1, final int p2) {
		return XaosConstants.MULTABLE[(XaosConstants.FPRANGE + p1) - p2];
	}

	private void addPrices(final XaosRealloc[] realloc, int r1, final int r2) {
		// if (r1 < r2)
		while (r1 < r2) {
			final int r3 = r1 + ((r2 - r1) >> 1);
			realloc[r3].priority = (realloc[r2].position - realloc[r3].position) * realloc[r3].priority;
			if (realloc[r3].symRef != -1) {
				realloc[r3].priority /= 2.0;
			}
			addPrices(realloc, r1, r3);
			// XaosFractalRenderer.addPrices(realloc, r3 + 1, r2);
			r1 = r3 + 1;
		}
	}

	private void prepareSymmetry(final XaosRealloc[] realloc, final int symi, double symPosition, final double step) {
		if (XaosConstants.DUMP) {
			log.fine("Prepare symetry...");
		}
		int i = 0;
		int j = 0;
		double tmp;
		double abs;
		double distance;
		double tmpPosition;
		final int size = realloc.length;
		final int max = size - XaosConstants.RANGE - 1;
		int min = XaosConstants.RANGE;
		int istart = 0;
		XaosRealloc tmpRealloc = null;
		XaosRealloc symRealloc = null;
		symPosition *= 2;
		int symj = (2 * symi) - size;
		if (symj < 0) {
			symj = 0;
		}
		distance = step * XaosConstants.RANGE;
		for (i = symj; i < symi; i++) {
			if (realloc[i].symTo != -1) {
				continue;
			}
			tmpRealloc = realloc[i];
			tmpPosition = tmpRealloc.position;
			tmpRealloc.symTo = (2 * symi) - i;
			if (tmpRealloc.symTo > max) {
				tmpRealloc.symTo = max;
			}
			j = ((tmpRealloc.symTo - istart) > XaosConstants.RANGE) ? (-XaosConstants.RANGE) : (-tmpRealloc.symTo + istart);
			if (tmpRealloc.calculate) {
				while ((j < XaosConstants.RANGE) && ((tmpRealloc.symTo + j) < (size - 1))) {
					tmp = symPosition - realloc[tmpRealloc.symTo + j].position;
					abs = Math.abs(tmp - tmpPosition);
					if (abs < distance) {
						if (((i == 0) || (tmp > realloc[i - 1].position)) && (tmp < realloc[i + 1].position)) {
							distance = abs;
							min = j;
						}
					}
					else if (tmp < tmpPosition) {
						break;
					}
					j += 1;
				}
			}
			else {
				while ((j < XaosConstants.RANGE) && ((tmpRealloc.symTo + j) < (size - 1))) {
					if (tmpRealloc.calculate) {
						tmp = symPosition - realloc[tmpRealloc.symTo + j].position;
						abs = Math.abs(tmp - tmpPosition);
						if (abs < distance) {
							if (((i == 0) || (tmp > realloc[i - 1].position)) && (tmp < realloc[i + 1].position)) {
								distance = abs;
								min = j;
							}
						}
						else if (tmp < tmpPosition) {
							break;
						}
					}
					j += 1;
				}
			}
			tmpRealloc.symTo += min;
			symRealloc = realloc[tmpRealloc.symTo];
			if ((min == XaosConstants.RANGE) || (tmpRealloc.symTo <= symi) || (symRealloc.symTo != -1) || (symRealloc.symRef != -1)) {
				tmpRealloc.symTo = -1;
				continue;
			}
			if (!tmpRealloc.calculate) {
				tmpRealloc.symTo = -1;
				if ((symRealloc.symTo != -1) || !symRealloc.calculate) {
					continue;
				}
				symRealloc.plus = tmpRealloc.plus;
				symRealloc.symTo = i;
				istart = tmpRealloc.symTo - 1;
				symRealloc.calculate = false;
				symRealloc.refreshed = false;
				symRealloc.dirty = true;
				symRealloc.isCached = false;
				tmpRealloc.symRef = tmpRealloc.symTo;
				symRealloc.position = symPosition - tmpRealloc.position;
			}
			else {
				if (symRealloc.symTo != -1) {
					tmpRealloc.symTo = -1;
					continue;
				}
				tmpRealloc.plus = symRealloc.plus;
				istart = tmpRealloc.symTo - 1;
				tmpRealloc.calculate = false;
				tmpRealloc.refreshed = false;
				tmpRealloc.dirty = true;
				tmpRealloc.isCached = false;
				symRealloc.symRef = i;
				tmpRealloc.position = symPosition - symRealloc.position;
			}
		}
	}

	private void prepareMove(final XaosChunkTable movetable, final XaosRealloc[] reallocX) {
		if (XaosConstants.DUMP) {
			log.fine("Prepare move...");
		}
		final XaosChunk[] table = movetable.data;
		XaosChunk tmpData = null;
		int i = 0;
		int j = 0;
		int s = 0;
		while (i < reallocX.length) {
			if (!reallocX[i].dirty) {
				tmpData = table[s];
				tmpData.to = i;
				tmpData.length = 1;
				tmpData.from = reallocX[i].plus;
				for (j = i + 1; j < reallocX.length; j++) {
					if (reallocX[j].dirty || ((j - reallocX[j].plus) != (tmpData.to - tmpData.from))) {
						break;
					}
					tmpData.length += 1;
				}
				i = j;
				s += 1;
			}
			else {
				i += 1;
			}
		}
		tmpData = table[s];
		tmpData.length = 0;
		if (XaosConstants.PRINT_MOVETABLE) {
			log.fine("Movetable:");
			for (i = 0; table[i].length > 0; i++) {
				log.fine("i = " + i + " " + table[i].toString());
			}
		}
	}

	private void prepareFill(final XaosChunkTable filltable, final XaosRealloc[] reallocX) {
		if (XaosConstants.DUMP) {
			log.fine("Prepare fill...");
		}
		final XaosChunk[] table = filltable.data;
		XaosChunk tmpData = null;
		int i = 0;
		int j = 0;
		int k = 0;
		int s = 0;
		int n = 0;
		for (i = 0; i < reallocX.length; i++) {
			if (reallocX[i].dirty) {
				j = i - 1;
				for (k = i + 1; (k < reallocX.length) && reallocX[k].dirty; k++) {
					;
				}
				while ((i < reallocX.length) && reallocX[i].dirty) {
					if ((k < reallocX.length) && ((j < i) || ((reallocX[i].position - reallocX[j].position) > (reallocX[k].position - reallocX[i].position)))) {
						j = k;
					}
					else {
						if (j < 0) {
							break;
						}
					}
					n = k - i;
					tmpData = table[s];
					tmpData.length = n;
					tmpData.from = j;
					tmpData.to = i;
					while (n > 0) {
						reallocX[i].position = reallocX[j].position;
						reallocX[i].isCached = reallocX[j].isCached;
						reallocX[i].isFilled = true;
						reallocX[i].dirty = false;
						n -= 1;
						i += 1;
					}
					s += 1;
				}
			}
		}
		tmpData = table[s];
		tmpData.length = 0;
		if (XaosConstants.PRINT_FILLTABLE) {
			log.fine("Filltable:");
			for (i = 0; table[i].length > 0; i++) {
				log.fine("i = " + i + " " + table[i].toString());
			}
		}
	}

	private double makeReallocTable(final XaosRealloc[] realloc, final XaosDynamic dynamic, final double begin, final double end, final double[] position, final boolean invalidate) {
		if (XaosConstants.DUMP) {
			log.fine("Make realloc...");
		}
		XaosRealloc tmpRealloc = null;
		XaosPrice prevData = null;
		XaosPrice bestData = null;
		XaosPrice tmpData = null;
		int bestPrice = XaosConstants.MAX_PRICE;
		int price = 0;
		int price1 = 0;
		int i = 0;
		int w = 0;
		int p = 0;
		int ps = 0;
		int pe = 0;
		int ps1 = 0;
		int wend = 0;
		int flag = 0;
		final int size = realloc.length;
		final double step = (end - begin) / (size - 1);
		final double tofix = (size * XaosConstants.FPMUL) / (end - begin);
		final int[] delta = dynamic.delta;
		delta[size] = Integer.MAX_VALUE;
		for (i = size - 1; i >= 0; i--) {
			delta[i] = (int) ((position[i] - begin) * tofix);
			if (delta[i] > delta[i + 1]) {
				delta[i] = delta[i + 1];
			}
		}
//		if (XaosConstants.DUMP_XAOS) {
//			log.fine("positions (fixed point):");
//			for (i = 0; i < size; i++) {
//				log.fine(String.valueOf(delta[i]));
//			}
//		}
		for (i = 0; i < size; i++) {
			dynamic.swap();
			wend = w - XaosConstants.FPRANGE;
			if (XaosConstants.DUMP_XAOS) {
//				log.fine("a0) yend = " + yend);
			}
			if (wend < 0) {
				wend = 0;
			}
			p = ps;
			while (delta[p] < wend) {
				p += 1;
			}
			ps1 = p;
			wend = w + XaosConstants.FPRANGE;
			if (XaosConstants.DUMP_XAOS) {
//				log.fine("a1) yend = " + yend);
			}
			if (XaosConstants.DUMP_XAOS) {
				log.fine("b0) i = " + i + ", w = " + w + ", ps1 = " + ps1 + ", ps = " + ps + ", pe = " + pe);
			}
			if (ps != pe && p > ps) {
				if (p < pe) {
					prevData = dynamic.oldBest[p - 1];
					if (XaosConstants.DUMP_XAOS) {
						log.fine("c0) previous = " + prevData.toString());
					}
				} else {
					prevData = dynamic.oldBest[pe - 1];
					if (XaosConstants.DUMP_XAOS) {
						log.fine("c1) previous = " + prevData.toString());
					}
				}
				price1 = prevData.price;
			} else {
				if (i > 0) {
					prevData = dynamic.calData[i - 1];
					price1 = prevData.price;
					if (XaosConstants.DUMP_XAOS) {
						log.fine("c2) previous = " + prevData.toString());
					}
				} else {
					prevData = null;
					price1 = 0;
					if (XaosConstants.DUMP_XAOS) {
						log.fine("c3) previous = null");
					}
				}
			}
			tmpData = dynamic.calData[i];
			price = price1 + XaosConstants.NEW_PRICE;
			if (XaosConstants.DUMP_XAOS) {
				log.fine("d0) add element " + i + ": price = " + price + " (previous price = " + price1 + ")");
			}
			bestData = tmpData;
			bestPrice = price;
			tmpData.price = price;
			tmpData.pos = -1;
			tmpData.previous = prevData;
			if (XaosConstants.DUMP_XAOS) {
				// Toolbox.println("d1) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
			}
			if (ps != pe) {
				if (p == ps) {
					if (delta[p] != delta[p + 1]) {
						prevData = dynamic.calData[i - 1];
						price1 = prevData.price;
						price = price1 + price(delta[p], w);
						if (XaosConstants.DUMP_XAOS) {
							log.fine("g0) approximate element " + i + " with old element " + p + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[(p << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = p;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								// Toolbox.println("g1) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
							}
						}
					}
					if (XaosConstants.DUMP_XAOS) {
						log.fine("g2) store data: p = " + p + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p++] = bestData;
				}
				prevData = null;
				price1 = price;
				while (p < pe) {
					if (delta[p] != delta[p + 1]) {
						// if (prevData != dynamic.oldBest[p - 1])
						// {
						prevData = dynamic.oldBest[p - 1];
						price1 = prevData.price;
						price = price1 + XaosConstants.NEW_PRICE;
						if (XaosConstants.DUMP_XAOS) {
							log.fine("h0) add element " + i + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[((p - 1) << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = -1;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								log.fine("h1) store data: p - 1 = " + (p - 1) + ", bestdata = " + bestData.toString());
							}
							dynamic.newBest[p - 1] = bestData;
							if (XaosConstants.DUMP_XAOS) {
								// Toolbox.println("h2) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
							}
						}
						price = price1 + price(delta[p], w);
						if (XaosConstants.DUMP_XAOS) {
							log.fine("h3) approximate element " + i + " with old element " + p + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[(p << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = p;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								// Toolbox.println("h4) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
							}
						} else if (delta[p] > w) {
							if (XaosConstants.DUMP_XAOS) {
//								log.fine("h5) store data: p = " + p + ", bestdata = " + bestData.toString());
							}
							dynamic.newBest[p++] = bestData;
							break;
						}
						// }
					}
					if (XaosConstants.DUMP_XAOS) {
						log.fine("h6) store data: p = " + p + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p++] = bestData;
				}
				while (p < pe) {
					if (delta[p] != delta[p + 1]) {
						// if (prevData != dynamic.oldBest[p - 1])
						// {
						prevData = dynamic.oldBest[p - 1];
						price1 = prevData.price;
						price = price1 + XaosConstants.NEW_PRICE;
						if (XaosConstants.DUMP_XAOS) {
							log.fine("i0) add element " + i + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[((p - 1) << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = -1;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								log.fine("i1) store data: p - 1 = " + (p - 1) + ", bestdata = " + bestData.toString());
							}
							dynamic.newBest[p - 1] = bestData;
							if (XaosConstants.DUMP_XAOS) {
								// Toolbox.println("i2) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
							}
						}
						price = price1 + price(delta[p], w);
						if (XaosConstants.DUMP_XAOS) {
							log.fine("i3) approximate element " + i + " with old element " + p + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[(p << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = p;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								//log.fine("i4) bestprice = " + bestPrice + ", bestdata = " + bestData.toString());
							}
						}
						// }
					}
					if (XaosConstants.DUMP_XAOS) {
						log.fine("i5) store data: p = " + p + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p++] = bestData;
				}
				if (p > ps) {
					prevData = dynamic.oldBest[p - 1];
					price1 = prevData.price;
				}
				else {
					prevData = dynamic.calData[i - 1];
					price1 = prevData.price;
				}
				price = price1 + XaosConstants.NEW_PRICE;
				if (XaosConstants.DUMP_XAOS) {
					log.fine("l0) add element " + i + ": price = " + price + " (previous price = " + price1 + ")");
				}
				if ((price < bestPrice) && (p > ps1)) {
					tmpData = dynamic.conData[((p - 1) << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
					bestData = tmpData;
					bestPrice = price;
					tmpData.price = price;
					tmpData.pos = -1;
					tmpData.previous = prevData;
					if (XaosConstants.DUMP_XAOS) {
						log.fine("l1) store data: p - 1 = " + (p - 1) + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p - 1] = bestData;
					if (XaosConstants.DUMP_XAOS) {
						// Toolbox.println("l2) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
					}
				}
				while (delta[p] < wend) {
					if (delta[p] != delta[p + 1]) {
						price = price1 + price(delta[p], w);
						if (XaosConstants.DUMP_XAOS) {
							log.fine("l3) approximate element " + i + " with old element " + p + ": price = " + price + " (previous price = " + price1 + ")");
						}
						if (price < bestPrice) {
							tmpData = dynamic.conData[(p << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
							bestData = tmpData;
							bestPrice = price;
							tmpData.price = price;
							tmpData.pos = p;
							tmpData.previous = prevData;
							if (XaosConstants.DUMP_XAOS) {
								// Toolbox.println("l4) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
							}
						} else if (delta[p] > w) {
							break;
						}
					}
					if (XaosConstants.DUMP_XAOS) {
						log.fine("l5) store data: p = " + p + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p++] = bestData;
				}
				while (delta[p] < wend) {
					if (XaosConstants.DUMP_XAOS) {
//						log.fine("l6) store data: p = " + p + ", bestdata = " + bestData.toString());
					}
					dynamic.newBest[p++] = bestData;
				}
			} else {
				if (delta[p] < wend) {
					if (i > 0) {
						prevData = dynamic.calData[i - 1];
						price1 = prevData.price;
						if (XaosConstants.DUMP_XAOS) {
							log.fine("e0) previous = " + prevData.toString());
						}
					} else {
						prevData = null;
						price1 = 0;
						if (XaosConstants.DUMP_XAOS) {
							log.fine("e1) previous = null");
						}
					}
					while (delta[p] < wend) {
						if (delta[p] != delta[p + 1]) {
							price = price1 + price(delta[p], w);
							if (XaosConstants.DUMP_XAOS) {
								log.fine("f0) approximate element " + i + " with old element " + p + ": price = " + price + " (previous price = " + price1 + ")");
							}
							if (price < bestPrice) {
								tmpData = dynamic.conData[(p << XaosConstants.DSIZE) + (i & XaosConstants.MASK)];
								bestData = tmpData;
								bestPrice = price;
								tmpData.price = price;
								tmpData.pos = p;
								tmpData.previous = prevData;
								if (XaosConstants.DUMP_XAOS) {
									// Toolbox.println("f1) bestprice = " + bestprice + ", bestdata = " + bestdata.toString());
								}
							} else if (delta[p] > w) {
								break;
							}
						}
						if (XaosConstants.DUMP_XAOS) {
							log.fine("f2) store data: p = " + p + ", bestdata = " + bestData.toString());
						}
						dynamic.newBest[p++] = bestData;
					}
					while (delta[p] < wend) {
						if (XaosConstants.DUMP_XAOS) {
//							log.fine("f3) store data: p = " + p + ", bestdata = " + bestData.toString());
						}
						dynamic.newBest[p++] = bestData;
					}
				}
			}
			ps = ps1;
			ps1 = pe;
			pe = p;
			w += XaosConstants.FPMUL;
		}
		if ((begin * XaosConstants.FPMUL > delta[0]) && (end * XaosConstants.FPMUL < delta[size - 1])) {
			flag = 1;
		}
		if ((delta[0] > 0) && (delta[size - 1] < (size * XaosConstants.FPMUL))) {
			flag = 2;
		}
		if (XaosConstants.DUMP_XAOS) {
			log.fine("flag = " + flag);
		}
		if (XaosConstants.DUMP_XAOS) {
			log.fine("best table:");
		}
		for (i = size - 1; i >= 0; i--) {
			if (XaosConstants.DUMP_XAOS) {
				log.fine("data = " + bestData.toString());
			}
			tmpData = bestData.previous;
			tmpRealloc = realloc[i];
			tmpRealloc.symTo = -1;
			tmpRealloc.symRef = -1;
			if (bestData.pos < 0) {
				tmpRealloc.calculate = true;
				tmpRealloc.refreshed = false;
				tmpRealloc.isCached = false;
				tmpRealloc.isFilled = false;
				tmpRealloc.dirty = true;
				tmpRealloc.plus = tmpRealloc.pos;
			} else {
				tmpRealloc.plus = bestData.pos;
				tmpRealloc.position = position[bestData.pos];
				if (invalidate) {
					tmpRealloc.isCached = false;
					tmpRealloc.isFilled = false;
				}
				tmpRealloc.refreshed = false;
				tmpRealloc.calculate = false;
				tmpRealloc.dirty = false;
			}
			bestData = tmpData;
		}
		newPositions(realloc, size, begin, end, step, position, flag);
		return step;
	}

	private void newPositions(final XaosRealloc[] realloc, final int size, double begin1, final double end1, final double step, final double[] position, final int flag) {
		XaosRealloc tmpRealloc = null;
		double delta = 0;
		double begin = 0;
		double end = 0;
		final int l = size;
		int s = -1;
		int e = -1;
		if (begin1 > end1) {
			begin1 = end1;
		}
		if (XaosConstants.PRINT_POSITIONS) {
			log.fine("Positions :");
		}
		while (s < (l - 1)) {
			e = s + 1;
			if (realloc[e].calculate) {
				while (e < l) {
					if (!realloc[e].calculate) {
						break;
					}
					e++;
				}
				if (e < l) {
					end = realloc[e].position;
				}
				else {
					end = end1;
				}
				if (s < 0) {
					begin = begin1;
				}
				else {
					begin = realloc[s].position;
				}
				if ((e == l) && (begin > end)) {
					end = begin;
				}
				if ((e - s) == 2) {
					delta = (end - begin) * 0.5;
				}
				else {
					delta = (end - begin) / (e - s);
				}
				switch (flag) {
					case 1: {
						for (s++; s < e; s++) {
							begin += delta;
							tmpRealloc = realloc[s];
							tmpRealloc.position = begin;
							tmpRealloc.priority = 1 / (1 + (Math.abs((position[s] - begin)) * step));
							if (XaosConstants.PRINT_POSITIONS) {
								log.fine("pos = " + s + ", position = " + tmpRealloc.position + ", price = " + tmpRealloc.priority);
							}
						}
						break;
					}
					case 2: {
						for (s++; s < e; s++) {
							begin += delta;
							tmpRealloc = realloc[s];
							tmpRealloc.position = begin;
							tmpRealloc.priority = Math.abs((position[s] - begin)) * step;
							if (XaosConstants.PRINT_POSITIONS) {
								log.fine("pos = " + s + ", position = " + tmpRealloc.position + ", price = " + tmpRealloc.priority);
							}
						}
						break;
					}
					default: {
						for (s++; s < e; s++) {
							begin += delta;
							tmpRealloc = realloc[s];
							tmpRealloc.position = begin;
							tmpRealloc.priority = 1.0;
							if (XaosConstants.PRINT_POSITIONS) {
								log.fine("pos = " + s + ", position = " + tmpRealloc.position + ", price = " + tmpRealloc.priority);
							}
						}
						break;
					}
				}
			}
			s = e;
		}
	}

	private void processReallocTable(final boolean continuous, final boolean refresh) {
		move();
		int[] offset = prepareOffset();
		if (refresh) {
			refreshAll(offset);
		}
		if (continuous && XaosConstants.USE_XAOS) {
			int total = 0;
			total = initPrices(xaosRendererData.queue(), total, xaosRendererData.reallocX());
			total = initPrices(xaosRendererData.queue(), total, xaosRendererData.reallocY());
			if (XaosConstants.DUMP) {
				log.fine("total = " + total);
			}
			if (total > 0) {
				if (total > 1) {
					sortQueue(xaosRendererData.queue(), 0, total - 1);
				}
				processQueue(total);
			}
		}
		renderReallocTable(continuous, offset);
	}

	private void refreshAll(int[] offset) {
		if (XaosConstants.DUMP) {
			log.fine("Refresh all...");
		}
//		XaosRealloc[] tmpRealloc;
//		int s;
//		int i;
//		for (s = 0; s < XaosConstants.STEPS; s++) {
//            tmpRealloc = xaosRendererData.reallocY();
//            for (i = offset[s]; i < tmpRealloc.length; i += XaosConstants.STEPS) {
//                refreshLine(tmpRealloc[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
//            }
//            tmpRealloc = xaosRendererData.reallocX();
//            for (i = offset[s]; i < tmpRealloc.length; i += XaosConstants.STEPS) {
//                refreshColumn(tmpRealloc[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
//            }
//			if (interrupted) {
//				break;
//			}
//        }
		for (XaosRealloc element : xaosRendererData.reallocY()) {
			refreshLine(element, xaosRendererData.reallocX(), xaosRendererData.reallocY());
		}
		for (XaosRealloc element : xaosRendererData.reallocX()) {
			refreshColumn(element, xaosRendererData.reallocX(), xaosRendererData.reallocY());
		}
	}

	private void renderReallocTable(boolean continuous, int[] offset) {
		if (XaosConstants.DUMP) {
			log.fine("Process realloc...");
		}
		int i;
		int s;
		XaosRealloc[] tmpRealloc;
		@SuppressWarnings("unused")
		int tocalcx = 0;
		@SuppressWarnings("unused")
		int tocalcy = 0;
		tmpRealloc = xaosRendererData.reallocX();
		for (i = 0; i < tmpRealloc.length; i++) {
			if (tmpRealloc[i].calculate) {
				tocalcx++;
			}
		}
		tmpRealloc = xaosRendererData.reallocY();
		for (i = 0; i < tmpRealloc.length; i++) {
			if (tmpRealloc[i].calculate) {
				tocalcy++;
			}
		}
		long oldTime = System.currentTimeMillis();
		for (s = 0; !interrupted && s < XaosConstants.STEPS; s++) {
			tmpRealloc = xaosRendererData.reallocY();
			for (i = offset[s]; !interrupted && i < tmpRealloc.length; i += XaosConstants.STEPS) {
				if (tmpRealloc[i].calculate || !tmpRealloc[i].isCached || tmpRealloc[i].isFilled) {
					renderLine(tmpRealloc[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
					tocalcy -= 1;
				}
				if (interrupted) {
					break;
				}
			}
			tmpRealloc = xaosRendererData.reallocX();
			for (i = offset[s]; !interrupted && i < tmpRealloc.length; i += XaosConstants.STEPS) {
				if (tmpRealloc[i].calculate || !tmpRealloc[i].isCached || tmpRealloc[i].isFilled) {
					renderColumn(tmpRealloc[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
					tocalcx -= 1;
				}
				if (interrupted) {
					break;
				}
			}
			long newTime = System.currentTimeMillis();
			if (!interrupted && (continuous || newTime - oldTime > 500)) {
				tmpRealloc = xaosRendererData.reallocY();
				for (i = 0; i < tmpRealloc.length; i++) {
					tmpRealloc[i].changeDirty = tmpRealloc[i].dirty;
					tmpRealloc[i].changeIsCached = tmpRealloc[i].isCached;
					tmpRealloc[i].changeIsFilled = tmpRealloc[i].isFilled;
					tmpRealloc[i].changePosition = tmpRealloc[i].position;
				}
				tmpRealloc = xaosRendererData.reallocX();
				for (i = 0; i < tmpRealloc.length; i++) {
					tmpRealloc[i].changeDirty = tmpRealloc[i].dirty;
					tmpRealloc[i].changeIsCached = tmpRealloc[i].isCached;
					tmpRealloc[i].changeIsFilled = tmpRealloc[i].isFilled;
					tmpRealloc[i].changePosition = tmpRealloc[i].position;
				}
				progress = (s + 1f) / (float)XaosConstants.STEPS;
				fill();
				update(progress, contentRendererData.getPixels());
				tmpRealloc = xaosRendererData.reallocY();
				for (i = 0; i < tmpRealloc.length; i++) {
					tmpRealloc[i].dirty = tmpRealloc[i].changeDirty;
					tmpRealloc[i].isCached = tmpRealloc[i].changeIsCached;
					tmpRealloc[i].isFilled = tmpRealloc[i].changeIsFilled;
					tmpRealloc[i].position = tmpRealloc[i].changePosition;
				}
				tmpRealloc = xaosRendererData.reallocX();
				for (i = 0; i < tmpRealloc.length; i++) {
					tmpRealloc[i].dirty = tmpRealloc[i].changeDirty;
					tmpRealloc[i].isCached = tmpRealloc[i].changeIsCached;
					tmpRealloc[i].isFilled = tmpRealloc[i].changeIsFilled;
					tmpRealloc[i].position = tmpRealloc[i].changePosition;
				}
				oldTime = newTime;
			}
			Thread.yield();
		}
		if (!interrupted) {
			progress = 1f;
		}
		fill();
		update(progress, contentRendererData.getPixels());
		Thread.yield();
	}

	private int[] prepareOffset() {
		final int[] position = xaosRendererData.position();
		final int[] offset = xaosRendererData.offset();
		position[0] = 1;
		offset[0] = 0;
		int s = 1;
		int i = 0;
		int j = 0;
		for (i = 1; i < XaosConstants.STEPS; i++) {
			position[i] = 0;
		}
		while (s < XaosConstants.STEPS) {
			for (i = 0; i < XaosConstants.STEPS; i++) {
				if (position[i] == 0) {
					for (j = i; j < XaosConstants.STEPS; j++) {
						if (position[j] != 0) {
							break;
						}
					}
					position[offset[s] = (j + i) >> 1] = 1;
					s += 1;
				}
			}
		}
		return offset;
	}

	private void move() {
		prepareMove(xaosRendererData.moveTable(), xaosRendererData.reallocX());
		doMove(xaosRendererData.moveTable(), xaosRendererData.reallocY());
	}

	private void fill() {
		if (isVerticalSymmetrySupported && isHorizontalSymmetrySupported) {
			doSymetry(xaosRendererData.reallocX(), xaosRendererData.reallocY());
		}
		prepareFill(xaosRendererData.fillTable(), xaosRendererData.reallocX());
		doFill(xaosRendererData.fillTable(), xaosRendererData.reallocY());
	}

	private int initPrices(final XaosRealloc[] queue, int total, final XaosRealloc[] realloc) {
		int i = 0;
		int j = 0;
		for (i = 0; i < realloc.length; i++) {
			if (realloc[i].calculate) {
				for (j = i; (j < realloc.length) && realloc[j].calculate; j++) {
					queue[total++] = realloc[j];
				}
				if (j == realloc.length) {
					j -= 1;
				}
				addPrices(realloc, i, j);
				i = j;
			}
		}
		return total;
	}

	private void sortQueue(final XaosRealloc[] queue, final int l, final int r) {
		final double m = (queue[l].priority + queue[r].priority) / 2.0;
		XaosRealloc t = null;
		int i = l;
		int j = r;
		do {
			while (queue[i].priority > m) {
				i++;
			}
			while (queue[j].priority < m) {
				j--;
			}
			if (i <= j) {
				t = queue[i];
				queue[i] = queue[j];
				queue[j] = t;
				i++;
				j--;
			}
		}
		while (j >= i);
		if (l < j) {
			sortQueue(queue, l, j);
		}
		if (r > i) {
			sortQueue(queue, i, r);
		}
	}

	private void processQueue(final int size) {
		if (XaosConstants.DUMP) {
			log.fine("Process queue...");
		}
		for (int i = 0; i < size; i++) {
			if (xaosRendererData.queue()[i].line) {
				renderLine(xaosRendererData.queue()[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
			}
			else {
				renderColumn(xaosRendererData.queue()[i], xaosRendererData.reallocX(), xaosRendererData.reallocY());
			}
			if (interrupted) {
				break;
			}
			Thread.yield();
		}
	}

	private void doSymetry(final XaosRealloc[] reallocX, final XaosRealloc[] reallocY) {
		if (XaosConstants.DUMP) {
			log.fine("Do symetry...");
		}
		final int rowsize = getSize().width();
		int from_offset = 0;
		int to_offset = 0;
		int i = 0;
		int j = 0;
		for (i = 0; i < reallocY.length; i++) {
			if ((reallocY[i].symTo >= 0) && (!reallocY[reallocY[i].symTo].dirty)) {
				from_offset = reallocY[i].symTo * rowsize;
				xaosRendererData.movePixels(from_offset, to_offset, rowsize);
				if (cacheActive) {
					xaosRendererData.moveCache(from_offset, to_offset, rowsize);
				}
				if (XaosConstants.SHOW_SYMETRY) {
					for (int k = 0; k < rowsize; k++) {
						xaosRendererData.setPixel(to_offset + k, Colors.mixColors(xaosRendererData.getPixel(from_offset + k), 0xFFFF0000, 127));
					}
				}
				reallocY[i].dirty = false;
				reallocY[i].isCached = cacheActive;
			}
			to_offset += rowsize;
			Thread.yield();
		}
		for (i = 0; i < reallocX.length; i++) {
			if ((reallocX[i].symTo >= 0) && (!reallocX[reallocX[i].symTo].dirty)) {
				to_offset = i;
				from_offset = reallocX[i].symTo;
				for (j = 0; j < reallocY.length; j++) {
					xaosRendererData.movePixels(from_offset, to_offset, 1);
					if (cacheActive) {
						xaosRendererData.moveCache(from_offset, to_offset, 1);
					}
					if (XaosConstants.SHOW_SYMETRY) {
						xaosRendererData.setPixel(to_offset, Colors.mixColors(xaosRendererData.getPixel(from_offset), 0xFFFF0000, 127));
					}
					to_offset += rowsize;
					from_offset += rowsize;
				}
				reallocX[i].dirty = false;
				reallocX[i].isCached = cacheActive;
			}
			Thread.yield();
		}
	}
	
	private void doMove(final XaosChunkTable movetable, final XaosRealloc[] reallocY) {
		if (XaosConstants.DUMP) {
			log.fine("Do move...");
		}
		final XaosChunk[] table = movetable.data;
		XaosChunk tmpData = null;
		final int rowsize = getSize().width();
		int new_offset = 0;
		int old_offset = 0;
		int from = 0;
		int to = 0;
		int i = 0;
		int s = 0;
		for (i = 0; i < reallocY.length; i++) {
			if (!reallocY[i].dirty) {
				s = 0;
				old_offset = reallocY[i].plus * rowsize;
				while ((tmpData = table[s]).length > 0) {
					from = old_offset + tmpData.from;
					to = new_offset + tmpData.to;
					xaosRendererData.copyPixels(from, to, tmpData.length);
					if (cacheActive) {
						xaosRendererData.copyCache(from, to, tmpData.length);
					}
					s += 1;
				}
			}
			new_offset += rowsize;
			Thread.yield();
		}
	}

	private void doFill(final XaosChunkTable filltable, final XaosRealloc[] reallocY) {
		if (XaosConstants.DUMP) {
			log.fine("Do fill...");
		}
		final XaosChunk[] table = filltable.data;
		XaosChunk tmpData = null;
		final int rowsize = getSize().width();
		int from_offset = 0;
		int to_offset = 0;
		int from = 0;
		int to = 0;
		int i = 0;
		int j = 0;
		int k = 0;
		int t = 0;
		int s = 0;
		int c = 0;
		int d = 0;
		int q = 0;
		final State p = xaosRendererData.newPoint();
		for (i = 0; i < reallocY.length; i++) {
			if (reallocY[i].dirty) {
				j = i - 1;
				for (k = i + 1; (k < reallocY.length) && reallocY[k].dirty; k++) {
					;
				}
				while ((i < reallocY.length) && reallocY[i].dirty) {
					if ((k < reallocY.length) && ((j < i) || ((reallocY[i].position - reallocY[j].position) > (reallocY[k].position - reallocY[i].position)))) {
						j = k;
					}
					else {
						if (j < 0) {
							break;
						}
					}
					to_offset = i * rowsize;
					from_offset = j * rowsize;
					if (!reallocY[j].dirty) {
						s = 0;
						while ((tmpData = table[s]).length > 0) {
							from = from_offset + tmpData.from;
							to = from_offset + tmpData.to;
							c = xaosRendererData.getPixel(from);
							if (cacheActive) {
								xaosRendererData.getPoint(from, p);
							}
							for (t = 0; t < tmpData.length; t++) {
								d = to + t;
								xaosRendererData.setPixel(d, c);
								if (cacheActive) {
									xaosRendererData.setPoint(d, p);
								}
								if (XaosConstants.SHOW_FILL) {
									xaosRendererData.setPixel(d, Colors.mixColors(c, 0xFF00FF00, 127));
								}
							}
							s += 1;
						}
					}
					xaosRendererData.movePixels(from_offset, to_offset, rowsize);
					if (cacheActive) {
						xaosRendererData.moveCache(from_offset, to_offset, rowsize);
					}
					reallocY[i].position = reallocY[j].position;
					reallocY[i].isCached = reallocY[j].isCached;
					reallocY[i].isFilled = true;
					reallocY[i].dirty = false;
					i += 1;
				}
			} else {
				s = 0;
				from_offset = i * rowsize;
				while ((tmpData = table[s]).length > 0) {
					from = from_offset + tmpData.from;
					to = from_offset + tmpData.to;
					c = xaosRendererData.getPixel(from);
					if (cacheActive) {
						xaosRendererData.getPoint(from, p);
					}
					for (t = 0; t < tmpData.length; t++) {
						d = to + t;
						xaosRendererData.setPixel(d, c);
						if (cacheActive) {
							xaosRendererData.setPoint(d, p);
						}
						if (XaosConstants.SHOW_FILL) {
							xaosRendererData.setPixel(d, Colors.mixColors(c,0xFF00FF00, 127));
						}
					}
					s += 1;
				}
			}
			Thread.yield();
		}
	}

	private void renderLine(final XaosRealloc realloc, final XaosRealloc[] reallocX, final XaosRealloc[] reallocY) {
		if (XaosConstants.PRINT_CALCULATE) {
			log.fine("Calculate line " + realloc.pos);
		}
		final int rowsize = getSize().width();
		final double position = realloc.position;
		final int r = realloc.pos;
		int offset = r * rowsize;
		int i;
		int j;
		int k;
		int n;
		int c;
		int distl = 0;
		int distr = 0;
		int distu = 0;
		int distd = 0;
		int offsetu;
		int offsetd;
		int offsetl;
		int offsetul;
		int offsetur;
		int offsetdl;
		int offsetdr;
		int rend = r - XaosConstants.GUESS_RANGE;
		MutableNumber z = new MutableNumber(0, 0);
		MutableNumber w = new MutableNumber(0, 0);
		final State p = xaosRendererData.newPoint();
		if (rend < 0) {
			rend = 0;
		}
		for (i = r - 1; (i >= rend) && reallocY[i].dirty; i--) {
			;
		}
		distu = r - i;
		rend = r + XaosConstants.GUESS_RANGE;
		if (rend >= reallocY.length) {
			rend = reallocY.length - 1;
		}
		for (j = r + 1; (j < rend) && reallocY[j].dirty; j++) {
			;
		}
		distd = j - r;
		if ((!isSolidguessSupported) || (i < 0) || (j >= reallocY.length) || reallocY[i].dirty || reallocY[j].dirty) {
			for (k = 0; k < reallocX.length; k++) {
				if (!reallocX[k].dirty) {
					z.set(xaosRendererData.point());
					w.set(reallocX[k].position, position);
					c = contentRendererStrategy.renderPoint(p, z, w);
					xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
					xaosRendererData.setPoint(offset, p);
					if (XaosConstants.SHOW_CALCULATE) {
						xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF00FF, 127));
					}
				}
				offset += 1;
			}
		}
		else {
			distr = 0;
			distl = Integer.MAX_VALUE / 2;
			offsetu = offset - (distu * rowsize);
			offsetd = offset + (distd * rowsize);
			for (k = 0; k < reallocX.length; k++) {
				if (!reallocX[k].dirty) {
					if (distr <= 0) {
						rend = k + XaosConstants.GUESS_RANGE;
						if (rend >= reallocX.length) {
							rend = reallocX.length - 1;
						}
						for (j = k + 1; (j < rend) && reallocX[j].dirty; j++) {
							distr = j - k;
						}
						if (j >= rend) {
							distr = Integer.MAX_VALUE / 2;
						}
					}
					if ((distr < (Integer.MAX_VALUE / 4)) && (distl < (Integer.MAX_VALUE / 4))) {
						offsetl = offset - distl;
						offsetul = offsetu - distl;
						offsetdl = offsetd - distl;
						offsetur = offsetu + distr;
						offsetdr = offsetd + distr;
						n = xaosRendererData.getPixel(offsetl);
						if (cacheActive) {
							xaosRendererData.getPoint(offset, p);
						}
						if ((n == xaosRendererData.getPixel(offsetu)) && (n == xaosRendererData.getPixel(offsetd)) && (n == xaosRendererData.getPixel(offsetul)) && (n == xaosRendererData.getPixel(offsetur)) && (n == xaosRendererData.getPixel(offsetdl)) && (n == xaosRendererData.getPixel(offsetdr))) {
							xaosRendererData.setPixel(offset, n);
							if (cacheActive) {
								xaosRendererData.setPoint(offset, p);
							}
							if (XaosConstants.SHOW_SOLIDGUESS) {
								xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF0000, 127));
							}
						}
						else {
							z.set(xaosRendererData.point());
							w.set(reallocX[k].position, position);
							c = contentRendererStrategy.renderPoint(p, z, w);
							xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
							xaosRendererData.setPoint(offset, p);
							if (XaosConstants.SHOW_CALCULATE) {
								xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFFFF00, 127));
							}
						}
					}
					else {
						z.set(xaosRendererData.point());
						w.set(reallocX[k].position, position);
						c = contentRendererStrategy.renderPoint(p, z, w);
						xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
						xaosRendererData.setPoint(offset, p);
						if (XaosConstants.SHOW_CALCULATE) {
							xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFFFF00, 127));
						}
					}
					distl = 0;
				}
				offset += 1;
				offsetu += 1;
				offsetd += 1;
				distr -= 1;
				distl += 1;
			}
		}
		Thread.yield();
		realloc.dirty = false;
		realloc.calculate = false;
		realloc.refreshed = false;
		realloc.isCached = cacheActive;
	}

	private void renderColumn(final XaosRealloc realloc, final XaosRealloc[] reallocX, final XaosRealloc[] reallocY) {
		if (XaosConstants.PRINT_CALCULATE) {
			log.fine("Calculate column " + realloc.pos);
		}
		final int rowsize = getSize().width();
		final double position = realloc.position;
		final int r = realloc.pos;
		int offset = r;
		int rend = r - XaosConstants.GUESS_RANGE;
		int i;
		int j;
		int k;
		int n;
		int c;
		int distl = 0;
		int distr = 0;
		int distu = 0;
		int distd = 0;
		int offsetl;
		int offsetr;
		int offsetu;
		int offsetlu;
		int offsetru;
		int offsetld;
		int offsetrd;
		int sumu;
		int sumd;
		MutableNumber z = new MutableNumber(0, 0);
		MutableNumber w = new MutableNumber(0, 0);
		final State p = xaosRendererData.newPoint();
		if (rend < 0) {
			rend = 0;
		}
		for (i = r - 1; (i >= rend) && reallocX[i].dirty; i--) {
			;
		}
		distl = r - i;
		rend = r + XaosConstants.GUESS_RANGE;
		if (rend >= reallocX.length) {
			rend = reallocX.length - 1;
		}
		for (j = r + 1; (j < rend) && reallocX[j].dirty; j++) {
			;
		}
		distr = j - r;
		if ((!isSolidguessSupported) || (i < 0) || (j >= reallocX.length) || reallocX[i].dirty || reallocX[j].dirty) {
			for (k = 0; k < reallocY.length; k++) {
				if (!reallocY[k].dirty) {
					z.set(xaosRendererData.point());
					w.set(position, reallocY[k].position);
					c = contentRendererStrategy.renderPoint(p, z, w);
					xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
					xaosRendererData.setPoint(offset, p);
					if (XaosConstants.SHOW_CALCULATE) {
						xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF00FF, 127));
					}
				}
				offset += rowsize;
			}
		}
		else {
			distd = 0;
			distu = Integer.MAX_VALUE / 2;
			offsetl = offset - distl;
			offsetr = offset + distr;
			for (k = 0; k < reallocY.length; k++) {
				if (!reallocY[k].dirty) {
					if (distd <= 0) {
						rend = k + XaosConstants.GUESS_RANGE;
						if (rend >= reallocY.length) {
							rend = reallocY.length - 1;
						}
						for (j = k + 1; (j < rend) && reallocY[j].dirty; j++) {
							distd = j - k;
						}
						if (j >= rend) {
							distd = Integer.MAX_VALUE / 2;
						}
					}
					if ((distd < (Integer.MAX_VALUE / 4)) && (distu < (Integer.MAX_VALUE / 4))) {
						sumu = distu * rowsize;
						sumd = distd * rowsize;
						offsetu = offset - sumu;
						offsetlu = offsetl - sumu;
						offsetru = offsetr - sumu;
						offsetld = offsetl + sumd;
						offsetrd = offsetr + sumd;
						n = xaosRendererData.getPixel(offsetu);
						if (cacheActive) {
							xaosRendererData.getPoint(offset, p);
						}
						if ((n == xaosRendererData.getPixel(offsetl)) && (n == xaosRendererData.getPixel(offsetr)) && (n == xaosRendererData.getPixel(offsetlu)) && (n == xaosRendererData.getPixel(offsetru)) && (n == xaosRendererData.getPixel(offsetld)) && (n == xaosRendererData.getPixel(offsetrd))) {
							xaosRendererData.setPixel(offset, n);
							if (cacheActive) {
								xaosRendererData.setPoint(offset, p);
							}
							if (XaosConstants.SHOW_SOLIDGUESS) {
								xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF0000, 127));
							}
						}
						else {
							z.set(xaosRendererData.point());
							w.set(position, reallocY[k].position);
							c = contentRendererStrategy.renderPoint(p, z, w);
							xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
							xaosRendererData.setPoint(offset, p);
							if (XaosConstants.SHOW_CALCULATE) {
								xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFFFF00, 127));
							}
						}
					}
					else {
						z.set(xaosRendererData.point());
						w.set(position, reallocY[k].position);
						c = contentRendererStrategy.renderPoint(p, z, w);
						xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
						xaosRendererData.setPoint(offset, p);
						if (XaosConstants.SHOW_CALCULATE) {
							xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFFFF00, 127));
						}
					}
					distu = 0;
				}
				offset += rowsize;
				offsetl += rowsize;
				offsetr += rowsize;
				distd -= 1;
				distu += 1;
			}
		}
		Thread.yield();
		realloc.dirty = false;
		realloc.calculate = false;
		realloc.refreshed = false;
		realloc.isCached = cacheActive;
	}

	private void refreshLine(final XaosRealloc realloc, final XaosRealloc[] reallocX, final XaosRealloc[] reallocY) {
		if (XaosConstants.DUMP) {
			log.fine("Refresh line...");
		}
		final int rowsize = getSize().width();
		int offset = realloc.pos * rowsize;
		int c = 0;
		State p = xaosRendererData.newPoint();
		if (realloc.isCached && !realloc.refreshed) {
			for (final XaosRealloc tmpRealloc : reallocX) {
				if (tmpRealloc.isCached && !tmpRealloc.refreshed) {
					xaosRendererData.getPoint(offset, p);
					c = contentRendererStrategy.renderColor(p);
					xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
					if (XaosConstants.SHOW_REFRESH) {
						xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFF0000FF, 127));
					}
				}
				offset += 1;
			}
			realloc.refreshed = true;
		} else if (!realloc.isCached) {
			if (XaosConstants.SHOW_UNCACHED) {
				xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF0000, 127));
			}
		}
		Thread.yield();
	}

	private void refreshColumn(final XaosRealloc realloc, final XaosRealloc[] reallocX, final XaosRealloc[] reallocY) {
		if (XaosConstants.DUMP) {
			log.fine("Refresh column...");
		}
		final int rowsize = getSize().width();
		int offset = realloc.pos;
		int c = 0;
		State p = xaosRendererData.newPoint();
		if (realloc.isCached && !realloc.refreshed) {
			for (final XaosRealloc tmpRealloc : reallocY) {
				if (tmpRealloc.isCached && !tmpRealloc.refreshed) {
					xaosRendererData.getPoint(offset, p);
					c = contentRendererStrategy.renderColor(p);
					xaosRendererData.setPixel(offset, opaque ? 0xFF000000 | c : c);
					if (XaosConstants.SHOW_REFRESH) {
						xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFF0000FF, 127));
					}
				}
				offset += rowsize;
			}
			realloc.refreshed = true;
		} else if (!realloc.isCached) {
			if (XaosConstants.SHOW_UNCACHED) {
				xaosRendererData.setPixel(offset, Colors.mixColors(xaosRendererData.getPixel(offset), 0xFFFF0000, 127));
			}
		}
		Thread.yield();
	}

	@Override
	public void getPixels(int[] pixels) {
//		int bufferWidth = buffer.getSize().getWidth();
//		int bufferHeight = buffer.getSize().getHeight();
//		int[] bufferPixels = new int[bufferWidth * bufferHeight];
//		IntBuffer tmpBuffer = IntBuffer.wrap(bufferPixels); 
//		buffer.getBuffer().getImage().getPixels(tmpBuffer);
//		int bufferImageWidth = buffer.getTile().getImageSize().getWidth();
//		int bufferImgeHeight = buffer.getTile().getImageSize().getHeight();
//		int bufferTileWidth = buffer.getTile().getTileSize().getWidth();
//		int bufferTileHeight = buffer.getTile().getTileSize().getHeight();
//		int bufferTileOffsetX = buffer.getTile().getTileOffset().getX();
//		int bufferTileOffsetY = buffer.getTile().getTileOffset().getY();
//		int imageWidth = tile.getImageSize().getWidth();
//		int imageHeight = tile.getImageSize().getHeight();
//		int tileWidth = tile.getTileSize().getWidth();
//		int tileHeight = tile.getTileSize().getHeight();
//		int tileOffsetX = tile.getTileOffset().getX();
//		int tileOffsetY = tile.getTileOffset().getY();
//		int borderWidth = tile.getBorderSize().getWidth();
//		int borderHeight = tile.getBorderSize().getHeight();
	}

	@Override
	protected Tile computeOptimalBufferSize(Tile tile, double rotation) {
		Size tileSize = tile.tileSize();
		Size imageSize = tile.imageSize();
		Size borderSize = tile.borderSize();
		Point tileOffset = tile.tileOffset();
		if (rotation == 0) {
			return new Tile(imageSize, tileSize, tileOffset, borderSize);
		} else {
			Size newImageSize = computeBufferSize(imageSize);
			if (overlapping) {
				int width = (newImageSize.width() - imageSize.width()) / 2;
				int height = (newImageSize.height() - imageSize.height()) / 2;
				Size newBorderSize = new Size(width, height);
				return new Tile(imageSize, tileSize, tileOffset, newBorderSize);
			} else {
				int hcells = (int) Math.rint(imageSize.width() / (double)tileSize.width());
				int vcells = (int) Math.rint(imageSize.height() / (double)tileSize.height());
				int hpos = (int) Math.rint(tileOffset.x() / (double)tileSize.width());
				int vpos = (int) Math.rint(tileOffset.y() / (double)tileSize.height());
				int width = (int) Math.rint(newImageSize.width() / (double)hcells);
				int height = (int) Math.rint(newImageSize.height() / (double)vcells);
				Size newTileSize = new Size(width, height);
				int offsetX = (int) Math.rint(newTileSize.width() * hpos);
				int offsetY = (int) Math.rint(newTileSize.height() * vpos);
				Point newTileOffset = new Point(offsetX, offsetY);
				return new Tile(newImageSize, newTileSize, newTileOffset, borderSize);
			}
		}
	}

	@Override
	protected AffineTransform createTransform(double rotation) {
		Size baseImageSize = tile.imageSize();
		final Size tileSize = buffer.getTile().tileSize();
		final Size imageSize = buffer.getTile().imageSize();
		final Size borderSize = buffer.getTile().borderSize();
		final Point tileOffset = buffer.getTile().tileOffset();
		int offsetX = borderSize.width();
		int offsetY = borderSize.height();
		int rotCenterX = offsetX + imageSize.width() / 2 - tileOffset.x();
		int rotCenterY = offsetY + imageSize.height() / 2 + tileSize.height() - imageSize.height() - tileOffset.y();
		if (!overlapping) {
			offsetX += (imageSize.width() - baseImageSize.width()) / 2;
			offsetY -= (imageSize.height() - baseImageSize.height()) / 2;
		}
		final int centerY = tileSize.height() / 2;
		final AffineTransform affine = renderFactory.createAffineTransform();
		affine.append(renderFactory.createTranslateAffineTransform(0, +centerY));
		affine.append(renderFactory.createScaleAffineTransform(1, -1));
		affine.append(renderFactory.createTranslateAffineTransform(0, -centerY));
		affine.append(renderFactory.createTranslateAffineTransform(tileOffset.x() - offsetX, tileOffset.y() - offsetY));
		affine.append(renderFactory.createRotateAffineTransform(rotation, rotCenterX, rotCenterY));
		return affine;
	}
}
