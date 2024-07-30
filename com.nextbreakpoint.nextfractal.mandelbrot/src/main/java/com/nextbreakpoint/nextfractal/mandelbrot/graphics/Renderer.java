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

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.core.common.ParserError;
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
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
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
	protected boolean aborted;
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
	protected float progress;
	@Setter
    protected Tile previewTile;
	@Setter
	protected boolean opaque;
	protected boolean julia;
	protected Number point;
	@Setter
	protected RendererDelegate rendererDelegate;
	protected List<ParserError> errors = new ArrayList<>();
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
	private final RenderRunnable renderTask = new RenderRunnable();
	private final ExecutorService executor;
	private final Lock lock = new Lock();

	/**
	 * @param threadFactory
	 * @param renderFactory
	 * @param tile
	 */
	public Renderer(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.contentRendererData = createRendererData();
		this.previewRendererData = createRendererData();
		this.contentRendererFractal = new Fractal();
		this.previewRendererFractal = new Fractal();
		this.tile = tile;
		this.opaque = true;
		this.time = new Time(0, 1);
		transform = new Transform();
		view = new View();
		buffer = new Surface();
		buffer.setTile(tile);
		ensureBufferAndSize();
		buffer.setAffine(createTransform(0));
		executor = Executors.newSingleThreadExecutor(threadFactory);
	}

	/**
	 * 
	 */
	public void dispose() {
		shutdown();
		free();
	}

    /**
	 * 
	 */
	public void abortTasks() {
		interrupted = true;
//		if (future != null) {
//			future.cancel(true);
//		}
	}

	/**
	 * 
	 */
	public void waitForTasks() {
		try {
			if (future != null) {
				future.get();
				future = null;
			}
		} catch (Exception e) {
			interrupted = true;
//			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void runTask() {
		if (future == null) {
			interrupted = false;
			future = executor.submit(renderTask);
		}
	}

	/**
	 * 
	 */
	public void init() {
		initialized = true;
		contentRendererFractal.initialize();
		previewRendererFractal.initialize();
		if (contentRendererFractal.getOrbit() != null) {
			initialRegion = new Region(contentRendererFractal.getOrbit().getInitialRegion());
		} else {
			initialRegion = new Region();
		}
	}

    /**
	 * @param orbit
	 */
	public void setOrbit(Orbit orbit) {
		contentRendererFractal.setOrbit(orbit);
		previewRendererFractal.setOrbit(orbit);
		orbitChanged = true;
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		contentRendererFractal.setColor(color);
		previewRendererFractal.setColor(color);
		colorChanged = true;
	}

	/**
	 * @param julia
	 */
	public void setJulia(boolean julia) {
		if (this.julia != julia) {
			this.julia = julia;
			juliaChanged = true;
		}
	}

	/**
	 * @param point
	 */
	public void setPoint(Number point) {
		if (this.point == null || !this.point.equals(point)) {
			this.point = point;
			pointChanged = true;
		}
	}

	/**
	 * @param time
	 */
	public void setTime(Time time) {
		if (this.time == null || !this.time.equals(time)) {
			this.time = time;
			timeChanged = true;
		}
	}

	/**
	 * @param contentRegion
	 */
	public void setContentRegion(Region contentRegion) {
		if (this.contentRegion == null || !this.contentRegion.equals(contentRegion)) {
			this.contentRegion = contentRegion;
			regionChanged = true; 
		}
	}

	/**
	 * @param view
	 */
	public void setView(View view) {
		this.view = view;
		lock.lock();
		if ((rotation == 0 && view.getRotation().z() != 0) || (rotation != 0 && view.getRotation().z() == 0)) {
			rotation = view.getRotation().z();
			ensureBufferAndSize();
			orbitChanged = true;
		} else {
			rotation = view.getRotation().z();
		}
		final Region region = getInitialRegion();
		final Number center = region.getCenter();
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
		lock.unlock();
	}

    /**
	 * @param pixels
	 */
	public void getPixels(int[] pixels) {
		int bufferWidth = buffer.getSize().width();
		int bufferHeight = buffer.getSize().height();
		int[] bufferPixels = new int[bufferWidth * bufferHeight];
		IntBuffer tmpBuffer = IntBuffer.wrap(bufferPixels); 
		buffer.getBuffer().getImage().getPixels(tmpBuffer);
		int tileWidth = tile.tileSize().width();
		int tileHeight = tile.tileSize().height();
		int borderWidth = tile.borderSize().width();
		int borderHeight = tile.borderSize().height();
		int offsetX = (bufferWidth - tileWidth - borderWidth * 2) / 2;
		int offsetY = (bufferHeight - tileHeight - borderHeight * 2) / 2;
		int offset = offsetY * bufferWidth + offsetX;
		int tileOffset = 0;
		for (int y = 0; y < tileHeight; y++) {
			System.arraycopy(bufferPixels, offset, pixels, tileOffset, tileWidth);
			offset += bufferWidth;
			tileOffset += tileWidth + borderWidth * 2;
		}
	}
	
	/**
	 * @param gc
	 */
	public void drawImage(final GraphicsContext gc, final int x, final int y) {
		lock.lock();
		if (buffer != null) {
			gc.save();
//			RendererSize borderSize = buffer.getTile().borderSize();
			Size imageSize = buffer.getTile().imageSize();
			Size tileSize = buffer.getTile().tileSize();
			gc.setAffineTransform(buffer.getAffine());
			gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.height() - imageSize.height());
//			gc.setStroke(renderFactory.createColor(1, 0, 0, 1));
//			gc.strokeRect(x + borderSize.width(), y + getSize().height() - imageSize.height() - borderSize.height(), tileSize.width(), tileSize.height());
			gc.restore();
		}
		lock.unlock();
	}

	/**
	 * @param gc
	 */
	public void copyImage(final GraphicsContext gc) {
		lock.lock();
		if (buffer != null) {
			gc.save();
			gc.drawImage(buffer.getBuffer().getImage(), 0, 0);
			gc.restore();
		}
		lock.unlock();
	}

//	/**
//	 * @param gc
//	 * @param x
//	 * @param y
//	 * @param w
//	 * @param h
//	 */
//	public void drawImage(final RendererGraphicsContext gc, final int x, final int y, final int w, final int h) {
//		lock.lock();
//		if (buffer != null) {
//			gc.save();
//			RendererSize imageSize = buffer.getTile().imageSize();
//			RendererSize tileSize = buffer.getTile().tileSize();
//			gc.setAffine(buffer.getAffine());
//			final double sx = w / (double) buffer.getTile().tileSize().width();
//			final double sy = h / (double) buffer.getTile().tileSize().height();
//			final int dw = (int) Math.rint(buffer.getSize().width() * sx);
//			final int dh = (int) Math.rint(buffer.getSize().height() * sy);
//			gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.height() - imageSize.height(), dw, dh);
//			gc.restore();
//		}
//		lock.unlock();
//	}

	private void ensureBufferAndSize() {
		Tile newTile = computeOptimalBufferSize(tile, rotation);
		int width = newTile.tileSize().width() + newTile.borderSize().width() * 2;
		int height = newTile.tileSize().height() + newTile.borderSize().height() * 2;
		size = new Size(width, height);
		buffer.setSize(size);
		buffer.setTile(newTile);
		buffer.setBuffer(renderFactory.createBuffer(size.width(), size.height()));
	}

	/**
	 * @param tile
	 * @param rotation
	 * @return
	 */
	protected Tile computeOptimalBufferSize(Tile tile, double rotation) {
		Size tileSize = tile.tileSize();
		Size imageSize = tile.imageSize();
		Size borderSize = tile.borderSize();
		Point tileOffset = tile.tileOffset();
		return new Tile(imageSize, tileSize, tileOffset, borderSize);
	}

	/**
	 * @param size
	 * @return
	 */
	protected Size computeBufferSize(Size size) {
		int tw = size.width();
		int th = size.height();
		int tileDim = (int) Math.hypot(tw, th);
		return new Size(tileDim, tileDim);
	}

	/**
	 * @param size
	 * @param borderSize
	 * @return
	 */
	protected Size computeBufferSize(Size size, Size borderSize) {
		Size bufferSize = computeBufferSize(size);
		int tw = bufferSize.width();
		int th = bufferSize.height();
		int bw = borderSize.width();
		int bh = borderSize.height();
		return new Size(tw + bw * 2, th + bh * 2);
	}

	/**
	 * 
	 */
	protected void doRender() {
		try {
//			if (isInterrupted()) {
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
				didChanged(progress, contentRendererData.getPixels());
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
				didChanged(progress, contentRendererData.getPixels());
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
				didChanged(progress, contentRendererData.getPixels());
				return;
			}
			boolean orbitTime = contentRendererFractal.getOrbit().useTime() && timeAnimation;
			boolean colorTime = contentRendererFractal.getColor().useTime() && timeAnimation;
			final boolean redraw = regionChanged || orbitChanged || juliaChanged || (julia && pointChanged) || ((orbitTime || colorTime) && timeChanged);
			timeChanged = false;
			pointChanged = false;
			orbitChanged = false;
			colorChanged = false;
			juliaChanged = false;
			regionChanged = false;
			aborted = false;
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
			int width = getSize().width();
			int height = getSize().height();
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
			float dy = height / 5.0f;
			float ty = dy;
			if (!singlePass) {
				didChanged(0, contentRendererData.getPixels());
			}
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					px.set(contentRendererData.point());
					pw.set(contentRendererData.positionX(x), contentRendererData.positionY(y));
					boolean preview = isPreview(x, y);
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
				if (isInterrupted()) {
					aborted = true;
					break;
				}
				if (y >= ty) {
					progress = y / (float)(height - 1);
					if (!singlePass) {
						didChanged(progress, contentRendererData.getPixels());
					}
					ty += dy;
				}
				Thread.yield();
			}
			if (!aborted) {
				progress = 1f;
				didChanged(progress, contentRendererData.getPixels());
			}
			Thread.yield();
		} catch (Throwable e) {
			log.log(Level.WARNING, "Can't render fractal", e);
			errors.add(RendererErrors.makeError(0, 0, 0, 0, e.getMessage()));
		}
	}

	private boolean isPreview(int x, int y) {
		if (previewTile != null) {
			int kx = x + tile.tileOffset().x() - previewTile.tileOffset().x();
			int ky = y + tile.tileOffset().y() - previewTile.tileOffset().y();
            if (kx >= 0 && kx < previewTile.tileSize().width()) {
                return ky >= 0 && ky < previewTile.tileSize().height();
            }
        }
		return false;
	}

	/**
	 *
	 */
	protected Region computeContentRegion() {
		final double tx = view.getTranslation().x();
		final double ty = view.getTranslation().y();
		final double tz = view.getTranslation().z();
//		final double rz = view.getRotation().z();
		
//		double a = fastRotate ? 0 : convertDegToRad(rz);
		
		final Size imageSize = buffer.getTile().imageSize();
		final Size tileSize = buffer.getTile().tileSize();
		final Point tileOffset = buffer.getTile().tileOffset();
//		final RendererSize borderSize = buffer.getTile().borderSize();
		
		final Size baseImageSize = tile.imageSize();

		final Region region = getInitialRegion();
		
		final Number size = region.getSize();
		final Number center = region.getCenter();

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

		final Region newRegion = new Region(new Number(fx - sx, fy - sy), new Number(fx + sx, fy + sy));
//		logger.info(newRegion.toString());
		return newRegion;
	}

	private Region computePreviewRegion() {
		final Size imageSize = previewTile.tileSize();
		final Size tileSize = previewTile.tileSize();

		final Size baseImageSize = previewTile.tileSize();

		final Region region = getInitialRegion();

		final Number size = region.getSize();
		final Number center = region.getCenter();

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

		final Region newRegion = new Region(new Number(fx - sx, fy - sy), new Number(fx + sx, fy + sy));
//		logger.info(newRegion.toString());
		return newRegion;
	}

	/**
	 * @param rotation
	 * @return
	 */
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
	
	/**
	 * @param progress
	 * @param pixels
	 */
	protected void didChanged(float progress, int[] pixels) {
		lock.lock();
		if (buffer != null) {
			buffer.getBuffer().update(pixels);
		}
		lock.unlock();
		if (rendererDelegate != null) {
			rendererDelegate.updateImageInBackground(progress);
		}
	}

	/**
	 * @return
	 */
	protected RendererData createRendererData() {
		return new RendererData();
	}

	/**
	 * 
	 */
	protected void free() {
		contentRendererData.free();
		if (previewTile != null) {
			previewRendererData.free();
		}
		if (buffer != null) {
			buffer.dispose();
			buffer = null;
		}
	}

	/**
	 * 
	 */
	protected void shutdown() {
		executor.shutdownNow();
		try {
			executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
		}
	}

	private class RenderRunnable implements Runnable {
		@Override
		public void run() {
			if (initialized) {
				doRender();
			}
		}
	}

	public List<Trap> getTraps() {
		return contentRendererFractal.getOrbit().getTraps();
	}

	public List<ParserError> getErrors() {
		List<ParserError> result = new ArrayList<>(errors);
		errors.clear();
		return result;
	}
}
