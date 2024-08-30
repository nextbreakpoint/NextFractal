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
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.SimpleCanvas;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;

@Log
public class Renderer {
	private final Lock lock = new Lock();
	private final GraphicsFactory renderFactory;
	private final ExecutorService executor;
	private final Tile tile;
	private Surface buffer;
	private int[] pixels;
	private BufferedImage image;
	private CFRenderer renderer;
	private Future<?> future;
	@Getter
	private volatile boolean aborted;
	@Getter
	private volatile boolean interrupted;
	@Getter
	private boolean initialized;
	@Getter
	private Size size;
	@Setter
	private boolean opaque;
	@Setter
	private RendererDelegate delegate;

	public Renderer(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile) {
		this.renderFactory = renderFactory;
		this.tile = tile;
		opaque = true;
		executor = ExecutorUtils.newSingleThreadExecutor(threadFactory);
		ensureBufferAndSize();
	}

	public void init() {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		ensureBufferAndSize();
		initialized = true;
	}

	public void dispose() {
		ExecutorUtils.shutdown(executor);
		image = null;
		pixels = null;
		future = null;
		renderer = null;
		if (buffer != null) {
			buffer.dispose();
			buffer = null;
		}
		initialized = false;
	}

	public void runTask() {
		if (!initialized) {
			throw new IllegalStateException("Operation not permitted");
		}
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		interrupted = false;
		future = executor.submit(this::render);
	}

    public void abortTasks() {
		interrupted = true;
		if (future != null) {
			if (renderer != null) {
				renderer.stop();
			}
		}
	}

	public void waitForTasks() throws InterruptedException {
		try {
			if (future != null) {
				future.get();
				future = null;
			}
		} catch (InterruptedException e) {
			log.warning("Interrupted while awaiting for task");
			throw e;
		} catch (ExecutionException e) {
			log.log(Level.WARNING, "Cannot execute task", e);
			aborted = true;
		}
	}

	public void setImage(CFDGImage image, String seed) {
		if (future != null) {
			throw new IllegalStateException("Operation not permitted");
		}
		renderer = image.open(size.width(), size.height(), seed);
		//TODO propagate progress from CF renderer
		renderer.setListener(() -> update(0, pixels));
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
				// final Size borderSize = buffer.getTile().borderSize();
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

	private void ensureBufferAndSize() {
		final Tile newTile = computeOptimalBufferSize(tile, 0);
		final int width = newTile.tileSize().width() + newTile.borderSize().width() * 2;
		final int height = newTile.tileSize().height() + newTile.borderSize().height() * 2;
		if (buffer != null) {
			buffer.dispose();
		}
		size = new Size(width, height);
		buffer = new Surface();
		buffer.setSize(size);
		buffer.setTile(computeOptimalBufferSize(tile, 0));
		buffer.setBuffer(renderFactory.createBuffer(size.width(), size.height()));
		buffer.setAffine(createTransform(0));
		image = new BufferedImage(size.width(), size.height(), BufferedImage.TYPE_INT_ARGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}

	// this method is executed in a worker thread.
	private void render() {
		final List<ScriptError> errors = new ArrayList<>();
		Graphics2D g2d = null;
		try {
			aborted = false;
			g2d = image.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			if (renderer != null) {
                renderer.run(new SimpleCanvas(g2d, buffer.getTile()), true);
				//TODO what errors are being collected?
				errors.addAll(renderer.errors());
			}
			if (interrupted) {
				aborted = true;
				update(1, pixels);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't render image", e);
			errors.add(RendererErrors.makeError(0, 0, 0, 0, e.getMessage()));
			aborted = true;
		} finally {
			if (g2d != null) {
				g2d.dispose();
			}
		}
		if (!errors.isEmpty()) {
			update(1, errors);
		}
	}

	private static Tile computeOptimalBufferSize(Tile tile, double rotation) {
		final Size tileSize = tile.tileSize();
		final Size imageSize = tile.imageSize();
		final Size borderSize = tile.borderSize();
		final Point tileOffset = tile.tileOffset();
		return new Tile(imageSize, tileSize, tileOffset, borderSize);
	}

	private AffineTransform createTransform(double rotation) {
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

//	private AffineTransform createTransform() {
//		final Size tileSize = buffer.getTile().tileSize();
//		final Size imageSize = buffer.getTile().imageSize();
//		final Size borderSize = buffer.getTile().borderSize();
//		final Point tileOffset = buffer.getTile().tileOffset();
//		final int offsetX = borderSize.width();
//		final int offsetY = borderSize.height();
//		final AffineTransform affine = renderFactory.createAffineTransform();
//		affine.append(renderFactory.createTranslateAffineTransform(-tileOffset.x() + offsetX, -tileOffset.y() + offsetY));
//		affine.append(renderFactory.createScaleAffineTransform((double) imageSize.width() / tileSize.width(), (double) imageSize.height() / tileSize.height()));
//		return affine;
//	}

	private void update(float progress, int[] pixels) {
		lock.lock();
		try {
			if (buffer != null) {
				buffer.getBuffer().update(pixels);
			}
		} finally {
			lock.unlock();
		}
		if (delegate != null) {
			delegate.onImageUpdated(progress, List.of());
		}
	}

	private void update(float progress, List<ScriptError> errors) {
		if (delegate != null) {
			delegate.onImageUpdated(progress, errors);
		}
	}
}
