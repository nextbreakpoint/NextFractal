/*
 * NextFractal 2.1.2-rc1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2020 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.renderer;

import com.nextbreakpoint.nextfractal.contextfree.grammar.CFDG;
import com.nextbreakpoint.nextfractal.contextfree.grammar.CFDGLogger;
import com.nextbreakpoint.nextfractal.contextfree.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.grammar.SimpleCanvas;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.core.render.RendererAffine;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererGraphicsContext;
import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererSurface;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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

/**
 * @author Andrea Medeghini
 */
public class Renderer {
	private static final Logger logger = Logger.getLogger(Renderer.class.getName());
	protected final ThreadFactory threadFactory;
	protected final RendererFactory renderFactory;
	protected volatile RendererDelegate rendererDelegate;
	protected volatile RendererSurface buffer;
	protected volatile List<SourceError> errors = new ArrayList<>();
	protected volatile boolean aborted;
	protected volatile boolean interrupted;
	protected volatile boolean cfdgChanged;
	protected volatile float progress;
	protected boolean opaque;
	protected RendererSize size;
	protected RendererTile tile;
	protected CFDG cfdg;
	private final RendererLock lock = new ReentrantRendererLock();
	private final RenderRunnable renderTask = new RenderRunnable();
	private ExecutorService executor;
	private volatile Future<?> future;
	private CFDGRenderer renderer;
	private boolean initialized;
	private String seed;

	/**
	 * @param threadFactory
	 * @param tile
	 */
	public Renderer(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.tile = tile;
		this.opaque = true;
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
		if (renderer != null) {
			renderer.setRequestStop(interrupted);
		}
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
			if (renderer != null) {
				renderer.setRequestStop(interrupted);
			}
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
//		rendererFractal.initialize();
	}

	/**
	 * @param cfdg
	 */
	public void setCFDG(CFDG cfdg) {
		this.cfdg = cfdg;
		cfdgChanged = true;
	}

	public void setSeed(String seed) {
		this.seed = seed;
		cfdgChanged = true;
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
		RendererTile newTile = computeOptimalBufferSize(tile, 0);
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
	 * 
	 */
	protected void doRender() {
		Graphics2D g2d = null;
		try {
			cfdgChanged = false;
			progress = 0;
			int width = getSize().getWidth();
			int height = getSize().getHeight();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			g2d = image.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			if (cfdg != null) {
				CFDGLogger logger = new CFDGLogger();
				cfdg.getDriver().setLogger(logger);
				cfdg.rulesLoaded();
				renderer = cfdg.renderer(tile.getImageSize().getWidth(), tile.getImageSize().getHeight(), 1, seed.hashCode(), 0.1);
				if (renderer != null) {
					renderer.setRenderListener(() -> didChanged(progress, pixels));
//					RendererFactory factory = new Java2DRendererFactory();
//					renderer.run(new RendererCanvas(factory, g2d, width, height), false);
					renderer.run(new SimpleCanvas(g2d, buffer.getTile()),true);
				}
				errors.addAll(logger.getErrors());
			}
			if (!isInterrupted()) {
				progress = 1f;
				didChanged(progress, pixels);
			}
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Cannot render fractal", e);
			errors.add(new RendererError(0, 0, 0, 0, e.getMessage()));
		} finally {
			if (g2d != null) {
				g2d.dispose();
			}
		}
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

//	/**
//	 * @return
//	 */
//	protected AffineTransform createTileTransform() {
//		final RendererSize tileSize = buffer.getTile().getTileSize();
//		final RendererSize imageSize = buffer.getTile().getImageSize();
//		final RendererSize borderSize = buffer.getTile().getBorderSize();
//		final RendererPoint tileOffset = buffer.getTile().getTileOffset();
//		final int offsetX = borderSize.getWidth();
//		final int offsetY = borderSize.getHeight();
//		final AffineTransform affine = new AffineTransform();
//		affine.translate(-tileOffset.getX() + offsetX, -tileOffset.getY() + offsetY);
//		affine.scale(imageSize.getWidth() / tileSize.getWidth(), imageSize.getHeight() / tileSize.getHeight());
//		return affine;
//	}

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
	 * 
	 */
	protected void free() {
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
