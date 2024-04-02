/*
 * NextFractal 2.1.5
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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx;

import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import javafx.scene.input.MouseEvent;

public class ToolMove implements Tool {
	private ToolContext context;
	private volatile boolean pressed;
	private volatile boolean changed;
	private volatile boolean redraw;
	private Long lastTimeInMillis;
	private double x0;
	private double y0;
	private double x1;
	private double y1;

	public ToolMove(ToolContext context) {
		this.context = context;
	}
	
	@Override
	public void clicked(MouseEvent e) {
	}

	@Override
	public void moved(MouseEvent e) {
	}

	@Override
	public void dragged(MouseEvent e) {
		x1 = (e.getX() - context.getWidth() / 2) / context.getWidth();
		y1 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
		changed = true;
		redraw = true;
	}

	@Override
	public void released(MouseEvent e) {
		x1 = (e.getX() - context.getWidth() / 2) / context.getWidth();
		y1 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
		pressed = false;
		changed = true;
		redraw = true;
	}

	@Override
	public void pressed(MouseEvent e) {
		x1 = x0 = (e.getX() - context.getWidth() / 2) / context.getWidth();
		y1 = y0 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
		pressed = true;
		redraw = true;
	}

	@Override
	public void update(long timeInMillis, boolean timeAnimation) {
		MandelbrotMetadata oldMetadata = context.getMetadata();
		Time time = oldMetadata.getTime();
		if (timeAnimation || lastTimeInMillis == null) {
			if (lastTimeInMillis == null) {
				lastTimeInMillis = timeInMillis;
			}
			time = new Time(time.getValue() + (timeInMillis - lastTimeInMillis) / 1000.0, time.getScale());
			lastTimeInMillis = timeInMillis;
		} else {
			lastTimeInMillis = null;
		}
		if (changed) {
			double[] t = oldMetadata.getTranslation().toArray();
			double[] r = oldMetadata.getRotation().toArray();
			double[] s = oldMetadata.getScale().toArray();
			double[] p = oldMetadata.getPoint().toArray();
			boolean j = oldMetadata.isJulia();
			double x = t[0];
			double y = t[1];
			double z = t[2];
			double a = r[2] * Math.PI / 180;
			Number size = context.getInitialSize();
			double dx = x1 - x0;
			double dy = y1 - y0;
			x -= z * size.r() * (Math.cos(a) * dx + Math.sin(a) * dy);
			y -= z * size.i() * (Math.cos(a) * dy - Math.sin(a) * dx);
			x0 = x1;
			y0 = y1;
			MandelbrotMetadata newMetadata = new MandelbrotMetadata(new double[] { x, y, z, t[3] }, new double[] { 0, 0, r[2], r[3] }, s, p, time, j, oldMetadata.getOptions());
			context.setView(newMetadata, pressed, !pressed);
			changed = false;
		} else if (timeAnimation) {
			MandelbrotMetadata newMetadata = new MandelbrotMetadata(oldMetadata.getTranslation(), oldMetadata.getRotation(), oldMetadata.getScale(), oldMetadata.getPoint(), time, oldMetadata.isJulia(), oldMetadata.getOptions());
			context.setTime(newMetadata, true, false);
		}
	}

	@Override
	public boolean isChanged() {
		boolean result = redraw;
		redraw = false;
		return result;
	}

	@Override
	public void draw(RendererGraphicsContext gc) {
		double dw = context.getWidth();
		double dh = context.getHeight();
		gc.clearRect(0, 0, (int)dw, (int)dh);
		if (pressed) {
			gc.setStroke(context.getRendererFactory().createColor(1, 1, 0, 1));
			double cx = dw / 2;
			double cy = dh / 2;
			int qx = (int) Math.rint(cx + x1 * dw);
			int qy = (int) Math.rint(cy - y1 * dh);
			gc.beginPath();
			gc.moveTo(qx - 4, qy - 4);
			gc.lineTo(qx + 4, qy + 4);
			gc.moveTo(qx - 4, qy + 4);
			gc.lineTo(qx + 4, qy - 4);
			gc.stroke();
		}
	}
}
