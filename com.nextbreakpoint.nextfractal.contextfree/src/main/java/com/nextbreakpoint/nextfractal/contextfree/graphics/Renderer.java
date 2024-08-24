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
package com.nextbreakpoint.nextfractal.contextfree.graphics;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFHandle;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.SimpleCanvas;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.graphics.AffineTransform;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Lock;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Surface;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

@Log
public class Renderer {
	protected final ThreadFactory threadFactory;
	protected final GraphicsFactory renderFactory;
	protected Surface buffer;
	protected boolean aborted;
	@Getter
    protected volatile boolean interrupted;
	@Getter
	protected float progress;
	@Getter
	private boolean initialized;
	protected boolean cfdgChanged;
	@Setter
    protected boolean opaque;
	@Setter
	protected RendererDelegate rendererDelegate;
	@Getter
    protected Size size;
	protected Tile tile;
	protected List<ScriptError> errors = new ArrayList<>();
	private Future<?> future;
	private CFDGImage cfdgImage;
	private CFHandle cfdgHandle;
	private String cfdgSeed;
	private final RenderRunnable renderTask = new RenderRunnable();
	private final ExecutorService executor;
	private final Lock lock = new Lock();

	public Renderer(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile) {
		this.threadFactory = threadFactory;
		this.renderFactory = renderFactory;
		this.tile = tile;
		this.opaque = true;
		buffer = new Surface();
		buffer.setTile(tile);
		ensureBufferAndSize();
		buffer.setAffine(createTransform(0));
		executor = Executors.newSingleThreadExecutor(threadFactory);
	}

	public void dispose() {
		shutdown();
		free();
	}

    public void abortTasks() {
		interrupted = true;
		if (cfdgHandle != null) {
			cfdgHandle.stop();
		}
//		if (future != null) {
//			future.cancel(true);
//		}
	}

	public void waitForTasks() {
		try {
			if (future != null) {
				future.get();
				future = null;
			}
		} catch (Exception e) {
			interrupted = true;
			if (cfdgHandle != null) {
				cfdgHandle.stop();
			}
//			e.printStackTrace();
		}
	}

	public void runTask() {
		if (future == null) {
			interrupted = false;
			future = executor.submit(renderTask);
		}
	}

    public void init() {
		initialized = true;
//		rendererFractal.initialize();
	}

	public void setImage(CFDGImage cfdgImage) {
		this.cfdgImage = cfdgImage;
		cfdgChanged = true;
	}

	public void setSeed(String cfdgSeed) {
		this.cfdgSeed = cfdgSeed;
		cfdgChanged = true;
	}

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

	public void copyImage(final GraphicsContext gc) {
		lock.lock();
		if (buffer != null) {
			gc.save();
			gc.drawImage(buffer.getBuffer().getImage(), 0, 0);
			gc.restore();
		}
		lock.unlock();
	}

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
		Tile newTile = computeOptimalBufferSize(tile, 0);
		int width = newTile.tileSize().width() + newTile.borderSize().width() * 2;
		int height = newTile.tileSize().height() + newTile.borderSize().height() * 2;
		size = new Size(width, height);
		buffer.setSize(size);
		buffer.setTile(newTile);
		buffer.setBuffer(renderFactory.createBuffer(size.width(), size.height()));
	}

	protected Tile computeOptimalBufferSize(Tile tile, double rotation) {
		Size tileSize = tile.tileSize();
		Size imageSize = tile.imageSize();
		Size borderSize = tile.borderSize();
		Point tileOffset = tile.tileOffset();
		return new Tile(imageSize, tileSize, tileOffset, borderSize);
	}

	protected void doRender() {
		Graphics2D g2d = null;
		try {
			if (cfdgChanged) {
				cfdgHandle = null;
				cfdgChanged = false;
			}
			progress = 0;
			int width = getSize().width();
			int height = getSize().height();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			g2d = image.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			if (cfdgImage != null) {
				final SimpleCanvas canvas = new SimpleCanvas(g2d, buffer.getTile());
				cfdgImage.setListener(() -> didChanged(progress, pixels));
				cfdgHandle = cfdgImage.render(canvas, cfdgSeed, true);
				//TODO what errors are being collected?
				errors.addAll(cfdgHandle.errors());
			}
			if (!isInterrupted()) {
				progress = 1f;
				didChanged(progress, pixels);
			}
		} catch (Throwable e) {
			log.log(Level.WARNING, "Can't render image", e);
			errors.add(RendererErrors.makeError(0, 0, 0, 0, e.getMessage()));
		} finally {
			if (g2d != null) {
				g2d.dispose();
			}
		}
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

//	protected AffineTransform createTileTransform() {
//		final RendererSize tileSize = buffer.getTile().tileSize();
//		final RendererSize imageSize = buffer.getTile().imageSize();
//		final RendererSize borderSize = buffer.getTile().borderSize();
//		final RendererPoint tileOffset = buffer.getTile().tileOffset();
//		final int offsetX = borderSize.width();
//		final int offsetY = borderSize.height();
//		final AffineTransform affine = new AffineTransform();
//		affine.translate(-tileOffset.x() + offsetX, -tileOffset.y() + offsetY);
//		affine.scale(imageSize.width() / tileSize.width(), imageSize.height() / tileSize.height());
//		return affine;
//	}

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

	protected void free() {
		if (buffer != null) {
			buffer.dispose();
			buffer = null;
		}
	}

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

	public List<ScriptError> getErrors() {
		List<ScriptError> result = new ArrayList<>(errors);
		errors.clear();
		return result;
	}
}
