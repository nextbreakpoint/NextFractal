/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.mandelbrot.renderer;

import com.nextbreakpoint.nextfractal.core.common.Colors;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.core.common.Time;
import com.nextbreakpoint.nextfractal.core.render.*;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Color;
import com.nextbreakpoint.nextfractal.mandelbrot.core.MutableNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Trap;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.strategy.JuliaRendererStrategy;
import com.nextbreakpoint.nextfractal.mandelbrot.renderer.strategy.MandelbrotRendererStrategy;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Renderer {
	private static final Logger logger = Logger.getLogger(Renderer.class.getName());
	protected final RendererFractal contentRendererFractal;
	protected final RendererFractal previewRendererFractal;
	protected final ThreadFactory threadFactory;
	protected final RendererFactory renderFactory;
	protected final RendererData contentRendererData;
	protected final RendererData previewRendererData;
	protected volatile RendererStrategy contentRendererStrategy;
	protected volatile RendererStrategy previewRendererStrategy;
	protected volatile RendererDelegate rendererDelegate;
	protected volatile RendererTransform transform;
	protected volatile RendererSurface buffer;
	protected volatile boolean aborted;
	protected volatile boolean interrupted;
	protected volatile boolean initialized;
	protected volatile boolean orbitChanged;
	protected volatile boolean colorChanged;
	protected volatile boolean regionChanged;
	protected volatile boolean juliaChanged;
	protected volatile boolean pointChanged;
	protected volatile boolean timeChanged;
	protected volatile List<SourceError> errors = new ArrayList<>();
	protected volatile float progress;
	protected volatile double rotation;
	protected volatile Time time;
	protected volatile RendererTile previewTile;
	protected boolean julia;
	protected boolean opaque;
	protected Number point;
	protected boolean multiThread;
	protected boolean singlePass;
	protected boolean continuous;
	protected boolean timeAnimation;
	protected RendererRegion previewRegion;
	protected RendererRegion contentRegion;
	protected RendererRegion initialRegion = new RendererRegion();
	protected RendererSize size;
	protected RendererView view;
	protected RendererTile tile;
	private final RendererLock lock = new RendererLock();
	private final RenderRunnable renderTask = new RenderRunnable();
	private ExecutorService executor;
	private volatile Future<?> future;

	/**
	 * @param threadFactory
	 * @param renderFactory
	 * @param tile
	 */
	public Renderer(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.contentRendererData = createRendererData();
		this.previewRendererData = createRendererData();
		this.contentRendererFractal = new RendererFractal();
		this.previewRendererFractal = new RendererFractal();
		this.tile = tile;
		this.opaque = true;
		this.time = new Time(0, 1);
		transform = new RendererTransform();
		view = new RendererView();
		buffer = new RendererSurface(); 
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
	 * @return
	 */
	public RendererSize getSize() {
		return size;
	}

	/**
	 * @return
	 */
	public boolean isInterrupted() {
		return interrupted;
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
	 * @return
	 */
	public RendererDelegate getRendererDelegate() {
		return rendererDelegate;
	}

	/**
	 * @param rendererDelegate
	 */
	public void setRendererDelegate(RendererDelegate rendererDelegate) {
		this.rendererDelegate = rendererDelegate;
	}

	/**
	 * @return
	 */
	public float getProgress() {
		return progress;
	}

	/**
	 * 
	 */
	public void init() {
		initialized = true;
		contentRendererFractal.initialize();
		previewRendererFractal.initialize();
		if (contentRendererFractal.getOrbit() != null) {
			initialRegion = new RendererRegion(contentRendererFractal.getOrbit().getInitialRegion());
		} else {
			initialRegion = new RendererRegion();
		}
	}

	/**
	 * @return
	 */
	public boolean isSinglePass() {
		return singlePass;
	}

	/**
	 * @param singlePass
	 */
	public void setSinglePass(boolean singlePass) {
		this.singlePass = singlePass;
	}

	/**
	 * @param multiThread
	 */
	public void setMultiThread(boolean multiThread) {
		this.multiThread = multiThread;
	}

	/**
	 * @param continuous
	 */
	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}

	/**
	 * @param timeAnimation
	 */
	public void setTimeAnimation(boolean timeAnimation) {
		this.timeAnimation = timeAnimation;
	}

	public RendererTile getPreviewTile() {
		return previewTile;
	}

	public void setPreviewTile(RendererTile previewTile) {
		this.previewTile = previewTile;
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
	public void setContentRegion(RendererRegion contentRegion) {
		if (this.contentRegion == null || !this.contentRegion.equals(contentRegion)) {
			this.contentRegion = contentRegion;
			regionChanged = true; 
		}
	}

	/**
	 * @param view
	 */
	public void setView(RendererView view) {
		this.view = view;
		lock.lock();
		if ((rotation == 0 && view.getRotation().getZ() != 0) || (rotation != 0 && view.getRotation().getZ() == 0)) {
			rotation = view.getRotation().getZ();
			ensureBufferAndSize();
			orbitChanged = true;
		} else {
			rotation = view.getRotation().getZ();
		}
		final RendererRegion region = getInitialRegion();
		final Number center = region.getCenter();
		transform = new RendererTransform();
		transform.traslate(view.getTraslation().getX() + center.r(), view.getTraslation().getY() + center.i());
		transform.rotate(-rotation * Math.PI / 180);
		transform.traslate(-view.getTraslation().getX() - center.r(), -view.getTraslation().getY() - center.i());
		buffer.setAffine(createTransform(rotation));
		setContentRegion(computeContentRegion());
		setJulia(view.isJulia());
		setPoint(view.getPoint());
		setContinuous(view.getState().getZ() == 1);
		setTimeAnimation(view.getState().getW() == 1);
		lock.unlock();
	}

	/**
	 * @return
	 */
	public RendererRegion getInitialRegion() {
		return initialRegion;
	}

	/**
	 * @param pixels
	 */
	public void getPixels(int[] pixels) {
		int bufferWidth = buffer.getSize().getWidth();
		int bufferHeight = buffer.getSize().getHeight();
		int[] bufferPixels = new int[bufferWidth * bufferHeight];
		IntBuffer tmpBuffer = IntBuffer.wrap(bufferPixels); 
		buffer.getBuffer().getImage().getPixels(tmpBuffer);
		int tileWidth = tile.getTileSize().getWidth();
		int tileHeight = tile.getTileSize().getHeight();
		int borderWidth = tile.getBorderSize().getWidth();
		int borderHeight = tile.getBorderSize().getHeight();
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
	public void drawImage(final RendererGraphicsContext gc, final int x, final int y) {
		lock.lock();
		if (buffer != null) {
			gc.save();
//			RendererSize borderSize = buffer.getTile().getBorderSize();
			RendererSize imageSize = buffer.getTile().getImageSize();
			RendererSize tileSize = buffer.getTile().getTileSize();
			gc.setAffine(buffer.getAffine());
			gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.getHeight() - imageSize.getHeight());
//			gc.setStroke(renderFactory.createColor(1, 0, 0, 1));
//			gc.strokeRect(x + borderSize.getWidth(), y + getSize().getHeight() - imageSize.getHeight() - borderSize.getHeight(), tileSize.getWidth(), tileSize.getHeight());
			gc.restore();
		}
		lock.unlock();
	}

	/**
	 * @param gc
	 */
	public void copyImage(final RendererGraphicsContext gc) {
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
//			RendererSize imageSize = buffer.getTile().getImageSize();
//			RendererSize tileSize = buffer.getTile().getTileSize();
//			gc.setAffine(buffer.getAffine());
//			final double sx = w / (double) buffer.getTile().getTileSize().getWidth();
//			final double sy = h / (double) buffer.getTile().getTileSize().getHeight();
//			final int dw = (int) Math.rint(buffer.getSize().getWidth() * sx);
//			final int dh = (int) Math.rint(buffer.getSize().getHeight() * sy);
//			gc.drawImage(buffer.getBuffer().getImage(), x, y + tileSize.getHeight() - imageSize.getHeight(), dw, dh);
//			gc.restore();
//		}
//		lock.unlock();
//	}

	private void ensureBufferAndSize() {
		RendererTile newTile = computeOptimalBufferSize(tile, rotation);
		int width = newTile.getTileSize().getWidth() + newTile.getBorderSize().getWidth() * 2;
		int height = newTile.getTileSize().getHeight() + newTile.getBorderSize().getHeight() * 2;
		size = new RendererSize(width, height);
		buffer.setSize(size);
		buffer.setTile(newTile);
		buffer.setBuffer(renderFactory.createBuffer(size.getWidth(), size.getHeight()));
	}

	/**
	 * @param tile
	 * @param rotation
	 * @return
	 */
	protected RendererTile computeOptimalBufferSize(RendererTile tile, double rotation) {
		RendererSize tileSize = tile.getTileSize();
		RendererSize imageSize = tile.getImageSize();
		RendererSize borderSize = tile.getBorderSize();
		RendererPoint tileOffset = tile.getTileOffset();
		return new RendererTile(imageSize, tileSize, tileOffset, borderSize);
	}

	/**
	 * @param size
	 * @return
	 */
	protected RendererSize computeBufferSize(RendererSize size) {
		int tw = size.getWidth();
		int th = size.getHeight();
		int tileDim = (int) Math.hypot(tw, th);
		return new RendererSize(tileDim, tileDim);
	}

	/**
	 * @param size
	 * @param borderSize
	 * @return
	 */
	protected RendererSize computeBufferSize(RendererSize size, RendererSize borderSize) {
		RendererSize bufferSize = computeBufferSize(size);
		int tw = bufferSize.getWidth();
		int th = bufferSize.getHeight();
		int bw = borderSize.getWidth();
		int bh = borderSize.getHeight();
		return new RendererSize(tw + bw * 2, th + bh * 2);
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
				contentRendererStrategy = new JuliaRendererStrategy(contentRendererFractal);
			} else {
				contentRendererStrategy = new MandelbrotRendererStrategy(contentRendererFractal);
			}
			if (previewTile != null) {
				previewRendererStrategy = new JuliaRendererStrategy(previewRendererFractal);
			}
			int width = getSize().getWidth();
			int height = getSize().getHeight();
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
				int previewWidth = previewTile.getTileSize().getWidth();
				int previewHeight = previewTile.getTileSize().getHeight();
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
			final RendererState p = contentRendererData.newPoint();
			final RendererState q = previewRendererData.newPoint();
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
						int kx = x + tile.getTileOffset().getX() - previewTile.getTileOffset().getX();
						int ky = y + tile.getTileOffset().getY() - previewTile.getTileOffset().getY();
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
			logger.log(Level.WARNING, "Can't render fractal", e);
			errors.add(new RendererError(0, 0, 0, 0, e.getMessage()));
		}
	}

	private boolean isPreview(int x, int y) {
		if (previewTile != null) {
			int kx = x + tile.getTileOffset().getX() - previewTile.getTileOffset().getX();
			int ky = y + tile.getTileOffset().getY() - previewTile.getTileOffset().getY();
            if (kx >= 0 && kx < previewTile.getTileSize().getWidth()) {
                if (ky >= 0 && ky < previewTile.getTileSize().getHeight()) {
                    return true;
                }
            }
        }
		return false;
	}

	/**
	 * 
	 */
	protected RendererRegion computeContentRegion() {
		final double tx = view.getTraslation().getX();
		final double ty = view.getTraslation().getY();
		final double tz = view.getTraslation().getZ();
//		final double rz = view.getRotation().getZ();
		
//		double a = fastRotate ? 0 : convertDegToRad(rz);
		
		final RendererSize imageSize = buffer.getTile().getImageSize();
		final RendererSize tileSize = buffer.getTile().getTileSize();
		final RendererPoint tileOffset = buffer.getTile().getTileOffset();
//		final RendererSize borderSize = buffer.getTile().getBorderSize();
		
		final RendererSize baseImageSize = tile.getImageSize();

		final RendererRegion region = getInitialRegion();
		
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

		final double gx = px + (qx - px) * ((baseImageSize.getWidth() - imageSize.getWidth()) / 2.0 + tileOffset.getX() + tileSize.getWidth() / 2) / (double)baseImageSize.getWidth();
		final double gy = py + (qy - py) * ((baseImageSize.getWidth() - imageSize.getHeight()) / 2.0 + tileOffset.getY() + tileSize.getHeight() / 2) / (double)baseImageSize.getWidth();
		final double fx = gx;//Math.cos(a) * (gx - cx) + Math.sin(a) * (gy - cx) + cx; 
		final double fy = gy;//Math.cos(a) * (gy - cy) - Math.sin(a) * (gx - cx) + cy;
		final double sx = dx * (getSize().getWidth() / (double)baseImageSize.getWidth());
		final double sy = dy * (getSize().getHeight() / (double)baseImageSize.getWidth());

		final RendererRegion newRegion = new RendererRegion(new Number(fx - sx, fy - sy), new Number(fx + sx, fy + sy));
//		logger.info(newRegion.toString());
		return newRegion;
	}

	private RendererRegion computePreviewRegion() {
		final RendererSize imageSize = previewTile.getTileSize();
		final RendererSize tileSize = previewTile.getTileSize();

		final RendererSize baseImageSize = previewTile.getTileSize();

		final RendererRegion region = getInitialRegion();

		final Number size = region.getSize();
		final Number center = region.getCenter();

		final double dx = size.r() * 0.25;
		final double dy = size.i() * 0.25 * baseImageSize.getHeight() / baseImageSize.getWidth();

		final double cx = center.r();
		final double cy = center.i();
		final double px = cx - dx;
		final double py = cy - dy;
		final double qx = cx + dx;
		final double qy = cy + dy;

		final double gx = px + (qx - px) * ((baseImageSize.getWidth() - imageSize.getWidth()) / 2.0 + tileSize.getWidth() / 2) / (double)baseImageSize.getWidth();
		final double gy = py + (qy - py) * ((baseImageSize.getWidth() - imageSize.getHeight()) / 2.0 + tileSize.getHeight() / 2) / (double)baseImageSize.getWidth();
		final double fx = gx;//Math.cos(a) * (gx - cx) + Math.sin(a) * (gy - cx) + cx;
		final double fy = gy;//Math.cos(a) * (gy - cy) - Math.sin(a) * (gx - cx) + cy;
		final double sx = dx * (getSize().getWidth() / (double)baseImageSize.getWidth());
		final double sy = dy * (getSize().getHeight() / (double)baseImageSize.getWidth());

		final RendererRegion newRegion = new RendererRegion(new Number(fx - sx, fy - sy), new Number(fx + sx, fy + sy));
//		logger.info(newRegion.toString());
		return newRegion;
	}

	/**
	 * @param rotation
	 * @return
	 */
	protected RendererAffine createTransform(double rotation) {
		final RendererSize tileSize = buffer.getTile().getTileSize();
		final RendererSize borderSize = buffer.getTile().getBorderSize();
		final RendererPoint tileOffset = buffer.getTile().getTileOffset();
		final int centerY = tileSize.getHeight() / 2;
		final int offsetX = borderSize.getWidth();
		final int offsetY = borderSize.getHeight();
		final RendererAffine affine = renderFactory.createAffine();
		affine.append(renderFactory.createTranslateAffine(0, +centerY));
		affine.append(renderFactory.createScaleAffine(1, -1));
		affine.append(renderFactory.createTranslateAffine(0, -centerY));
		affine.append(renderFactory.createTranslateAffine(tileOffset.getX() - offsetX, tileOffset.getY() - offsetY));
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

	public List<SourceError> getErrors() {
		List<SourceError> result = new ArrayList<>(errors);
		errors.clear();
		return result;
	}

	public boolean isOpaque() {
		return opaque;
	}

	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	public boolean isInitialized() {
		return initialized;
	}
}
