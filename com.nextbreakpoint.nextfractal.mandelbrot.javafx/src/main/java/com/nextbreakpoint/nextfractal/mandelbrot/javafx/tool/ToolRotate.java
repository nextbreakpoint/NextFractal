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
package com.nextbreakpoint.nextfractal.mandelbrot.javafx.tool;

import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.javafx.Tool;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.MandelbrotToolContext;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadata;
import javafx.scene.input.MouseEvent;

public class ToolRotate implements Tool {
	private final MandelbrotToolContext context;
	private volatile boolean pressed;
	private volatile boolean changed;
	private volatile boolean redraw;
	private volatile boolean active;
	private Long lastTimeInMillis;
	private double x0;
	private double y0;
	private double x1;
	private double y1;
	private double a0;
	private double a1;
	private double r0;
	private double i0;

	public ToolRotate(MandelbrotToolContext context) {
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
		if (active) {
			x1 = (e.getX() - context.getWidth() / 2) / context.getWidth();
			y1 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
			changed = true;
			redraw = true;
		}
	}

	@Override
	public void released(MouseEvent e) {
		pressed = false;
		redraw = true;
		if (active) {
			x1 = (e.getX() - context.getWidth() / 2) / context.getWidth();
			y1 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
			changed = true;
		}
		active = !active;
	}

	@Override
	public void pressed(MouseEvent e) {
		if (active) {
			x1 = (e.getX() - context.getWidth() / 2) / context.getWidth();
			y1 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
			final MandelbrotMetadata oldView = context.getMetadata();
			final double[] t = oldView.getTranslation().toArray();
			final double[] r = oldView.getRotation().toArray();
			a0 = r[2] * Math.PI / 180;
			a1 = Math.atan2(y1 - y0, x1 - x0);
			r0 = t[0];
			i0 = t[1];
		} else {
			x1 = x0 = (e.getX() - context.getWidth() / 2) / context.getWidth();
			y1 = y0 = (context.getHeight() / 2 - e.getY()) / context.getHeight();
		}
		pressed = true;
	}

	@Override
	public void update(long timeInMillis, boolean timeAnimation) {
		final MandelbrotMetadata oldMetadata = context.getMetadata();
		Time time = oldMetadata.time();
		if (timeAnimation || lastTimeInMillis == null) {
			if (lastTimeInMillis == null) {
				lastTimeInMillis = timeInMillis;
			}
			time = new Time(time.value() + (timeInMillis - lastTimeInMillis) / 1000.0, time.scale());
			lastTimeInMillis = timeInMillis;
		} else {
			lastTimeInMillis = null;
		}
		if (changed) {
			final double[] t = oldMetadata.getTranslation().toArray();
			final double[] r = oldMetadata.getRotation().toArray();
			final double[] s = oldMetadata.getScale().toArray();
			final double[] p = oldMetadata.getPoint().toArray();
			final boolean j = oldMetadata.isJulia();
			final double z = t[2];
			final ComplexNumber size = context.getInitialSize();
			final double a2 = Math.atan2(y1 - y0, x1 - x0) - a1;
			final double tx = x0 * z * size.r();
			final double ty = y0 * z * size.r();
			final double qx = (Math.cos(a0) * tx + Math.sin(a0) * ty);
			final double qy = (Math.cos(a0) * ty - Math.sin(a0) * tx);
			final double px = - qx;
			final double py = - qy;
			final double gx = (Math.cos(a2) * px + Math.sin(a2) * py);
			final double gy = (Math.cos(a2) * py - Math.sin(a2) * px);
			final double dx = gx - px;
			final double dy = gy - py;
			final double x = r0 + dx;
			final double y = i0 + dy;
			final double a = (a0 + a2) * 180 / Math.PI;
			final MandelbrotMetadata newMetadata = new MandelbrotMetadata(new double[] { x, y, z, t[3] }, new double[] { 0, 0, a, r[3] }, s, p, time, j, oldMetadata.getOptions());
			context.setView(newMetadata, pressed, !pressed);
			changed = false;
		} else if (timeAnimation) {
			final MandelbrotMetadata newMetadata = new MandelbrotMetadata(oldMetadata.getTranslation(), oldMetadata.getRotation(), oldMetadata.getScale(), oldMetadata.getPoint(), time, oldMetadata.isJulia(), oldMetadata.getOptions());
			context.setTime(newMetadata, true, false);
		}
	}

	@Override
	public void forceChanged() {
		changed = true;
	}

	@Override
	public boolean isChanged() {
		boolean result = redraw;
		redraw = false;
		return result;
	}

	@Override
	public void draw(GraphicsContext gc) {
		final double dw = context.getWidth();
		final double dh = context.getHeight();
		gc.clearRect(0, 0, (int)dw, (int)dh);
		if (active) {
			gc.setStroke(context.getGraphicsFactory().createColor(1, 1, 0, 1));
			final double cx = dw / 2;
			final double cy = dh / 2;
			final int px = (int) Math.rint(cx + x0 * dw);
			final int py = (int) Math.rint(cy - y0 * dh);
			gc.beginPath();
			gc.moveTo(px - 4, py - 4);
			gc.lineTo(px + 4, py + 4);
			gc.moveTo(px - 4, py + 4);
			gc.lineTo(px + 4, py - 4);
			gc.stroke();
			gc.setStroke(context.getGraphicsFactory().createColor(1, 1, 0, 1));
			final int qx = (int) Math.rint(cx + x1 * dw);
			final int qy = (int) Math.rint(cy - y1 * dh);
			gc.beginPath();
			gc.moveTo(qx - 4, qy - 4);
			gc.lineTo(qx + 4, qy + 4);
			gc.moveTo(qx - 4, qy + 4);
			gc.lineTo(qx + 4, qy - 4);
			gc.stroke();
		}
	}
}
