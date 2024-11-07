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
package com.nextbreakpoint.nextfractal.mandelbrot.graphics;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.RendererDelegate;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Lock;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Surface;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.strategy.JuliaStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.graphics.strategy.MandelbrotStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class Renderer {
	protected final Fractal contentRendererFractal;
	protected final Fractal previewRendererFractal;
	protected final ThreadFactory threadFactory;
	protected final GraphicsFactory renderFactory;
	protected final RendererData contentRendererData;
	protected final RendererData previewRendererData;
	protected RendererStrategy contentRendererStrategy;
	protected RendererStrategy previewRendererStrategy;
	protected Transform transform;
	protected Surface buffer;
	@Getter
	protected volatile float progress;
    @Getter
    protected volatile boolean interrupted;
	@Getter
    protected boolean initialized;
	protected boolean orbitChanged;
	protected boolean colorChanged;
	protected boolean regionChanged;
	protected boolean juliaChanged;
	protected boolean pointChanged;
	protected boolean timeChanged;
	protected double rotation;
	protected Time time;
	@Setter
    protected Tile previewTile;
	@Setter
	protected boolean opaque;
	protected boolean julia;
	protected ComplexNumber point;
	@Setter
	protected RendererDelegate delegate;
    @Setter
    protected boolean multiThread;
    @Setter
    protected boolean singlePass;
    @Setter
    protected boolean continuous;
    @Setter
    protected boolean timeAnimation;
	protected Region previewRegion;
	protected Region contentRegion;
    @Getter
    protected Region initialRegion = new Region();
    @Getter
    protected Size size;
	protected View view;
	protected Tile tile;
	private Future<?> future;
	private final ExecutorService executor;
	private final Lock lock = new Lock();

	public Renderer(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.contentRendererData = createRendererData();
		this.previewRendererData = createRendererData();
		this.contentRendererFractal = new Fractal();
		this.previewRendererFractal = new Fractal();
		this.tile = tile;
		transform = new Transform();
		time = new Time(0, 1);
		view = new View();
		opaque = true;
		executor = ExecutorUtils.newSingleThreadExecutor(threadFactory);
		ensureBufferAndSize();
	}

	public void init() {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		ensureBufferAndSize();
		contentRendererFractal.initialize();
		previewRendererFractal.initialize();
		if (contentRendererFractal.getOrbit() != null) {
			initialRegion = new Region(contentRendererFractal.getOrbit().getInitialRegion());
		} else {
			initialRegion = new Region();
		}
		initialized = true;
	}

	public void dispose() {
		ExecutorUtils.shutdown(executor);
		contentRendererData.free();
		if (previewTile != null) {
			previewRendererData.free();
		}
		if (buffer != null) {
			buffer.dispose();
			buffer = null;
		}
		future = null;
		initialized = false;
	}

	public void runTask() {
		if (!initialized) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (future == null) {
			interrupted = false;
			future = executor.submit(this::render);
		}
	}

	public void abortTask() {
		interrupted = true;
	}

	public void waitForTask() {
		try {
			if (future != null) {
				future.get();
			}
		} catch (InterruptedException _) {
			Thread.currentThread().interrupt();
		} catch (CancellationException _) {
		} catch (ExecutionException e) {
			log.log(Level.WARNING, "Task has failed", e);
		} finally {
			future = null;
		}
    }

	public void setOrbit(Orbit orbit) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		contentRendererFractal.setOrbit(orbit);
		previewRendererFractal.setOrbit(orbit);
		orbitChanged = true;
	}

	public void setColor(Color color) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		contentRendererFractal.setColor(color);
		previewRendererFractal.setColor(color);
		colorChanged = true;
	}

	public void setJulia(boolean julia) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (this.julia != julia) {
			this.julia = julia;
			juliaChanged = true;
		}
	}

	public void setPoint(ComplexNumber point) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (this.point == null || !this.point.equals(point)) {
			this.point = point;
			pointChanged = true;
		}
	}

	public void setTime(Time time) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (this.time == null || !this.time.equals(time)) {
			this.time = time;
			timeChanged = true;
		}
	}

	public void setContentRegion(Region contentRegion) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (this.contentRegion == null || !this.contentRegion.equals(contentRegion)) {
			this.contentRegion = contentRegion;
			regionChanged = true; 
		}
	}

	public void setView(View view) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		this.view = view;
		lock.lock();
		try {
			if ((rotation == 0 && view.getRotation().z() != 0) || (rotation != 0 && view.getRotation().z() == 0)) {
				rotation = view.getRotation().z();
				ensureBufferAndSize();
				orbitChanged = true;
			} else {
				rotation = view.getRotation().z();
			}
			final Region region = getInitialRegion();
			final ComplexNumber center = region.getCenter();
			transform = new Transform();
			transform.translate(view.getTranslation().x() + center.r(), view.getTranslation().y() + center.i());
			transform.rotate(-rotation * Math.PI / 180);
			transform.translate(-view.getTranslation().x() - center.r(), -view.getTranslation().y() - center.i());
			buffer.setAffine(createTransform(rotation));
			setContentRegion(computeContentRegion());
			setJulia(view.isJulia());
			setPoint(view.getPoint());
			setContinuous(view.getState().z() == 1);
			setTimeAnimation(view.getState().w() == 1);
		} finally {
			lock.unlock();
		}
	}

	//TODO is getPixels required?
	public void getPixels(int[] pixels) {
		lock.lock();
		try {
			final int bufferWidth = buffer.getSize().width();
			final int bufferHeight = buffer.getSize().height();
			final int[] bufferPixels = new int[bufferWidth * bufferHeight];
			final IntBuffer tmpBuffer = IntBuffer.wrap(bufferPixels);
			buffer.getBuffer().getImage().getPixels(tmpBuffer);
			final int tileWidth = tile.tileSize().width();
			final int tileHeight = tile.tileSize().height();
			final int borderWidth = tile.borderSize().width();
			final int borderHeight = tile.borderSize().height();
			final int offsetX = (bufferWidth - tileWidth - borderWidth * 2) / 2;
			final int offsetY = (bufferHeight - tileHeight - borderHeight * 2) / 2;
			int offset = offsetY * bufferWidth + offsetX;
			int tileOffset = 0;
			for (int y = 0; y < tileHeight; y++) {
				System.arraycopy(bufferPixels, offset, pixels, tileOffset, tileWidth);
				offset += bufferWidth;
				tileOffset += tileWidth + borderWidth * 2;
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void drawImage(final GraphicsContext gc, final int x, final int y) {
		lock.lock();
		try {
			if (buffer != null) {
				gc.save();
				// Size borderSize = buffer.getTile().borderSize();
				final Size imageSize = buffer.getTile().imageSize();
				final Size tileSize = buffer.getTile().tileSize();
				gc.setAffineTransform(buffer.getAffine());
				gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.height() - imageSize.height());
				// gc.setStroke(renderFactory.createColor(1, 0, 0, 1));
				// gc.strokeRect(x + borderSize.width(), y + getSize().height() - imageSize.height() - borderSize.height(), tileSize.width(), tileSize.height());
				gc.restore();
			}
		} finally {
			lock.unlock();
		}
	}

//	public void drawImage(final GraphicsContext gc, final int x, final int y, final int w, final int h) {
//		lock.lock();
//		try {
//			if (buffer != null) {
//				gc.save();
//				final Size imageSize = buffer.getTile().imageSize();
//				final Size tileSize = buffer.getTile().tileSize();
//				gc.setAffineTransform(buffer.getAffine());
//				final double sx = w / (double) buffer.getTile().tileSize().width();
//				final double sy = h / (double) buffer.getTile().tileSize().height();
//				final int dw = (int) Math.rint(buffer.getSize().width() * sx);
//				final int dh = (int) Math.rint(buffer.getSize().height() * sy);
//				gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.height() - imageSize.height(), dw, dh);
//				gc.restore();
//			}
//		} finally {
//			lock.unlock();
//		}
//	}

	public void copyImage(final GraphicsContext gc) {
		lock.lock();
		try {
			if (buffer != null) {
				gc.save();
				gc.drawImage(buffer.getBuffer().getImage(), 0, 0);
				gc.restore();
			}
		} finally {
			lock.unlock();
		}
	}

	public List<Trap> getTraps() {
		return contentRendererFractal.getOrbit().getTraps();
	}

	private void ensureBufferAndSize() {
		final Tile newTile = computeOptimalBufferSize(tile, rotation);
		final int width = newTile.tileSize().width() + newTile.borderSize().width() * 2;
		final int height = newTile.tileSize().height() + newTile.borderSize().height() * 2;
		size = new Size(width, height);
		if (buffer != null) {
			buffer.dispose();
		}
		buffer = new Surface();
		buffer.setSize(size);
		buffer.setTile(computeOptimalBufferSize(tile, rotation));
		buffer.setBuffer(renderFactory.createBuffer(size.width(), size.height()));
		buffer.setAffine(createTransform(rotation));
	}

	protected Tile computeOptimalBufferSize(Tile tile, double rotation) {
		final Size tileSize = tile.tileSize();
		final Size imageSize = tile.imageSize();
		final Size borderSize = tile.borderSize();
		final Point tileOffset = tile.tileOffset();
		return new Tile(imageSize, tileSize, tileOffset, borderSize);
	}

	protected Size computeBufferSize(Size size) {
		final int tw = size.width();
		final int th = size.height();
		final int tileDim = (int) Math.hypot(tw, th);
		return new Size(tileDim, tileDim);
	}

	protected Size computeBufferSize(Size size, Size borderSize) {
		final Size bufferSize = computeBufferSize(size);
		final int tw = bufferSize.width();
		final int th = bufferSize.height();
		final int bw = borderSize.width();
		final int bh = borderSize.height();
		return new Size(tw + bw * 2, th + bh * 2);
	}

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
				if (previewTile != null) {
					previewRendererData.swap();
					previewRendererData.clearPixels();
				}
				update(progress, contentRendererData.getPixels());
				return;
			}
			if (contentRendererFractal.getOrbit() == null) {
				progress = 1;
				contentRendererData.swap();
				contentRendererData.clearPixels();
				if (previewTile != null) {
					previewRendererData.swap();
					previewRendererData.clearPixels();
				}
				update(progress, contentRendererData.getPixels());
				return;
			}
			if (contentRendererFractal.getColor() == null) {
				progress = 1;
				contentRendererData.swap();
				contentRendererData.clearPixels();
				if (previewTile != null) {
					previewRendererData.swap();
					previewRendererData.clearPixels();
				}
				update(progress, contentRendererData.getPixels());
				return;
			}
			final boolean orbitTime = contentRendererFractal.getOrbit().useTime() && timeAnimation;
			final boolean colorTime = contentRendererFractal.getColor().useTime() && timeAnimation;
			final boolean redraw = regionChanged || orbitChanged || juliaChanged || (julia && pointChanged) || ((orbitTime || colorTime) && timeChanged);
			timeChanged = false;
			pointChanged = false;
			orbitChanged = false;
			colorChanged = false;
			juliaChanged = false;
			regionChanged = false;
			progress = 0;
			contentRendererFractal.getOrbit().setTime(time);
			contentRendererFractal.getColor().setTime(time);
			previewRendererFractal.getOrbit().setTime(time);
			previewRendererFractal.getColor().setTime(time);
			contentRendererFractal.clearScope();
			contentRendererFractal.setPoint(point);
			previewRendererFractal.clearScope();
			previewRendererFractal.setPoint(point);
			if (julia) {
				contentRendererStrategy = new JuliaStrategy(contentRendererFractal);
			} else {
				contentRendererStrategy = new MandelbrotStrategy(contentRendererFractal);
			}
			if (previewTile != null) {
				previewRendererStrategy = new JuliaStrategy(previewRendererFractal);
			}
			final int width = getSize().width();
			final int height = getSize().height();
			contentRendererStrategy.prepare();
			if (previewTile != null) {
				previewRendererStrategy.prepare();
			}
			contentRendererData.setSize(width, height, contentRendererFractal.getStateSize());
			contentRendererData.setRegion(contentRegion);
			contentRendererData.setPoint(contentRendererFractal.getPoint());
			contentRendererData.initPositions();
			contentRendererData.swap();
			contentRendererData.clearPixels();
			if (previewTile != null) {
				previewRegion = computePreviewRegion();
				int previewWidth = previewTile.tileSize().width();
				int previewHeight = previewTile.tileSize().height();
				previewRendererData.setSize(previewWidth, previewHeight, previewRendererFractal.getStateSize());
				previewRendererData.setRegion(previewRegion);
				previewRendererData.setPoint(previewRendererFractal.getPoint());
				previewRendererData.initPositions();
				previewRendererData.swap();
				previewRendererData.clearPixels();
			}
			final MutableNumber px = new MutableNumber(0, 0);
			final MutableNumber pw = new MutableNumber(0, 0);
			final MutableNumber qx = new MutableNumber(0, 0);
			final MutableNumber qw = new MutableNumber(0, 0);
			final State p = contentRendererData.newPoint();
			final State q = previewRendererData.newPoint();
			int contentOffset = 0;
			int contentColor = 0;
			int previewOffset = 0;
			int previewColor = 0;
			final float dy = height / 5.0f;
			float ty = dy;
			if (!singlePass) {
				update(0, contentRendererData.getPixels());
			}
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					px.set(contentRendererData.point());
					pw.set(contentRendererData.positionX(x), contentRendererData.positionY(y));
					final boolean preview = isPreview(x, y);
					if (preview) {
						qx.set(previewRendererData.point());
						int kx = x + tile.tileOffset().x() - previewTile.tileOffset().x();
						int ky = y + tile.tileOffset().y() - previewTile.tileOffset().y();
						qw.set(previewRendererData.positionX(kx), previewRendererData.positionY(ky));
					}
					transform.transform(pw);
					if (redraw) {
						contentColor = contentRendererStrategy.renderPoint(p, px, pw);
						if (preview) {
							previewColor = previewRendererStrategy.renderPoint(q, qx, qw);
						}
					} else {
						contentRendererData.getPoint(contentOffset, p);
						contentColor = contentRendererStrategy.renderColor(p);
						if (preview) {
							previewRendererData.getPoint(previewOffset, q);
							previewColor = previewRendererStrategy.renderColor(q);
						}
					}
					contentRendererData.setPoint(contentOffset, p);
					if (preview) {
						previewRendererData.setPoint(previewOffset, q);
						previewRendererData.setPixel(previewOffset, opaque ? 0xFF000000 | previewColor : previewColor);
						final int mixedColor = Colors.mixColors(contentColor, previewColor, 200);
						contentRendererData.setPixel(contentOffset, opaque ? 0xFF000000 | mixedColor : mixedColor);
					} else {
						contentRendererData.setPixel(contentOffset, opaque ? 0xFF000000 | contentColor : contentColor);
					}
					contentOffset += 1;
					if (preview) {
						previewOffset += 1;
					}
					Thread.yield();
				}
				if (interrupted) {
					break;
				}
				if (y >= ty) {
					progress = y / (float)(height - 1);
					if (!singlePass) {
						update(progress, contentRendererData.getPixels());
					}
					ty += dy;
				}
				Thread.yield();
			}
			if (!interrupted) {
				progress = 1f;
				update(progress, contentRendererData.getPixels());
			}
			Thread.yield();
		} catch (Throwable e) {
			log.log(Level.WARNING, "Can't render fractal", e);
			errors.add(RendererErrors.makeError(0, 0, 0, 0, e.getMessage()));
		}
		if (!errors.isEmpty()) {
			update(progress, errors);
		}
	}

	private boolean isPreview(int x, int y) {
		if (previewTile != null) {
			final int kx = x + tile.tileOffset().x() - previewTile.tileOffset().x();
			final int ky = y + tile.tileOffset().y() - previewTile.tileOffset().y();
            if (kx >= 0 && kx < previewTile.tileSize().width()) {
                return ky >= 0 && ky < previewTile.tileSize().height();
            }
        }
		return false;
	}

	protected Region computeContentRegion() {
		final double tx = view.getTranslation().x();
		final double ty = view.getTranslation().y();
		final double tz = view.getTranslation().z();
//		final double rz = view.getRotation().z();
		
//		final double a = fastRotate ? 0 : convertDegToRad(rz);
		
		final Size imageSize = buffer.getTile().imageSize();
		final Size tileSize = buffer.getTile().tileSize();
		final Point tileOffset = buffer.getTile().tileOffset();
//		final RendererSize borderSize = buffer.getTile().borderSize();
		
		final Size baseImageSize = tile.imageSize();

		final Region region = getInitialRegion();
		
		final ComplexNumber size = region.getSize();
		final ComplexNumber center = region.getCenter();

		final double dx = tz * size.r() * 0.5;
		final double dy = tz * size.i() * 0.5;
		
		final double cx = center.r();
		final double cy = center.i();
		final double px = cx - dx + tx;
		final double py = cy - dy + ty;
		final double qx = cx + dx + tx;
		final double qy = cy + dy + ty;

		final double gx = px + (qx - px) * ((baseImageSize.width() - imageSize.width()) / 2.0 + tileOffset.x() + tileSize.width() / 2f) / (double)baseImageSize.width();
		final double gy = py + (qy - py) * ((baseImageSize.width() - imageSize.height()) / 2.0 + tileOffset.y() + tileSize.height() / 2f) / (double)baseImageSize.width();
		final double fx = gx;//Math.cos(a) * (gx - cx) + Math.sin(a) * (gy - cx) + cx; 
		final double fy = gy;//Math.cos(a) * (gy - cy) - Math.sin(a) * (gx - cx) + cy;
		final double sx = dx * (getSize().width() / (double)baseImageSize.width());
		final double sy = dy * (getSize().height() / (double)baseImageSize.width());

		return new Region(new ComplexNumber(fx - sx, fy - sy), new ComplexNumber(fx + sx, fy + sy));
	}

	private Region computePreviewRegion() {
		final Size imageSize = previewTile.tileSize();
		final Size tileSize = previewTile.tileSize();

		final Size baseImageSize = previewTile.tileSize();

		final Region region = getInitialRegion();

		final ComplexNumber size = region.getSize();
		final ComplexNumber center = region.getCenter();

		final double dx = size.r() * 0.25;
		final double dy = size.i() * 0.25 * baseImageSize.height() / baseImageSize.width();

		final double cx = center.r();
		final double cy = center.i();
		final double px = cx - dx;
		final double py = cy - dy;
		final double qx = cx + dx;
		final double qy = cy + dy;

		final double gx = px + (qx - px) * ((baseImageSize.width() - imageSize.width()) / 2.0 + tileSize.width() / 2f) / (double)baseImageSize.width();
		final double gy = py + (qy - py) * ((baseImageSize.width() - imageSize.height()) / 2.0 + tileSize.height() / 2f) / (double)baseImageSize.width();
		final double fx = gx;//Math.cos(a) * (gx - cx) + Math.sin(a) * (gy - cx) + cx;
		final double fy = gy;//Math.cos(a) * (gy - cy) - Math.sin(a) * (gx - cx) + cy;
		final double sx = dx * (getSize().width() / (double)baseImageSize.width());
		final double sy = dy * (getSize().height() / (double)baseImageSize.width());

        return new Region(new ComplexNumber(fx - sx, fy - sy), new ComplexNumber(fx + sx, fy + sy));
	}

	protected AffineTransform createTransform(double rotation) {
		final Size tileSize = buffer.getTile().tileSize();
		final Size borderSize = buffer.getTile().borderSize();
		final Point tileOffset = buffer.getTile().tileOffset();
		final int centerY = tileSize.height() / 2;
		final int offsetX = borderSize.width();
		final int offsetY = borderSize.height();
		final AffineTransform affine = renderFactory.createAffineTransform();
		affine.append(renderFactory.createTranslateAffineTransform(0, +centerY));
		affine.append(renderFactory.createScaleAffineTransform(1, -1));
		affine.append(renderFactory.createTranslateAffineTransform(0, -centerY));
		affine.append(renderFactory.createTranslateAffineTransform(tileOffset.x() - offsetX, tileOffset.y() - offsetY));
		return affine;
	}

	protected RendererData createRendererData() {
		return new RendererData();
	}

	protected void update(float progress, int[] pixels) {
		lock.lock();
		if (buffer != null) {
			buffer.getBuffer().update(pixels);
		}
		lock.unlock();
		update(progress, List.of());
	}

	protected void update(float progress, List<ScriptError> errors) {
		this.progress = progress;
		if (delegate != null) {
			delegate.onImageUpdated(progress, errors);
		}
	}
}
