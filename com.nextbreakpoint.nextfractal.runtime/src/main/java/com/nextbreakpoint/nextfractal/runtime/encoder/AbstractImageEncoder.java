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
package com.nextbreakpoint.nextfractal.runtime.encoder;

import com.nextbreakpoint.freeimage4java.tagRGBQUAD;
import com.nextbreakpoint.nextfractal.core.encoder.Encoder;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderContext;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderDelegate;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderException;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderHandle;
import lombok.extern.java.Log;

import java.io.File;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import java.util.logging.Level;

import static com.nextbreakpoint.freeimage4java.Libfreeimage.FreeImage_Allocate;
import static com.nextbreakpoint.freeimage4java.Libfreeimage.FreeImage_Initialise;
import static com.nextbreakpoint.freeimage4java.Libfreeimage.FreeImage_Save;
import static com.nextbreakpoint.freeimage4java.Libfreeimage.FreeImage_SetPixelColor;
import static com.nextbreakpoint.freeimage4java.Libfreeimage.FreeImage_Unload;
import static com.nextbreakpoint.freeimage4java.Libfreeimage.TRUE;
import static java.lang.foreign.MemorySegment.NULL;

@Log
public abstract class AbstractImageEncoder implements Encoder {
	private EncoderDelegate delegate;

	static {
		FreeImage_Initialise(TRUE());
	}

	public boolean isVideoSupported() {
		return false;
	}

	@Override
	public void setDelegate(EncoderDelegate delegate) {
		this.delegate = delegate;
	}

	@Override
	public EncoderHandle open(EncoderContext context, File path) {
		return new ImageEncoderHandle(context, path);
	}

	@Override
	public void close(EncoderHandle handle) {
	}

	@Override
	public void encode(EncoderHandle handle, int frameIndex, int repeatFrameCount, int totalFrameCount) throws EncoderException {
		((ImageEncoderHandle) handle).encode();
	}

	protected abstract int getFormat();

	protected boolean isAlphaSupported() {
		return false;
	}

	private class ImageEncoderHandle implements EncoderHandle {
		private final EncoderContext context;
		private final File path;

		public ImageEncoderHandle(EncoderContext context, File path) {
			this.context = Objects.requireNonNull(context);
			this.path = Objects.requireNonNull(path);
			if (delegate != null) {
				delegate.didProgressChanged(0f);
			}
			log.info("Session %s: Encoding image...".formatted(context.getSessionId()));
		}

		public void encode() throws EncoderException {
			try (var arena = Arena.ofConfined()) {
				final int channels = isAlphaSupported() ? 4 : 3;
				long time = System.currentTimeMillis();
				var pBitmap = NULL;
				try {
					pBitmap = FreeImage_Allocate(context.getImageWidth(), context.getImageHeight(), channels * 8, 0x00FF0000, 0x0000FF00, 0x000000FF);
					final byte[] data = context.getPixelsAsByteArray(0, 0, 0, context.getImageWidth(), context.getImageHeight(), channels, false);
					final MemorySegment value = arena.allocate(tagRGBQUAD.layout());
					for (int y = 0; y < context.getImageHeight(); y++) {
						int j = y * context.getImageWidth();
						for (int x = 0; x < context.getImageWidth(); x++) {
							int i = (j + x) * channels;
							tagRGBQUAD.rgbRed(value, (data[i]));
							tagRGBQUAD.rgbGreen(value, (data[i + 1]));
							tagRGBQUAD.rgbBlue(value, (data[i + 2]));
							if (isAlphaSupported()) {
								tagRGBQUAD.rgbReserved(value, (data[i + 3]));
							} else {
								tagRGBQUAD.rgbReserved(value, (byte) 255);
							}
							FreeImage_SetPixelColor(pBitmap, x, y, value);
							if (delegate != null && delegate.isInterrupted()) {
								break;
							}
						}
						if (y % 100 == 0) {
							log.info("Session %s: Completed %.0f%%".formatted(context.getSessionId(), ((y + 1f) / context.getImageHeight()) * 100f));
						}
						if (delegate != null && (y % 10 == 0)) {
							delegate.didProgressChanged(((y + 1f) / context.getImageHeight()) * 100f);
						}
						Thread.yield();
					}
					if (delegate == null || !delegate.isInterrupted()) {
						final var fileName = arena.allocateFrom(path.getAbsolutePath());
						FreeImage_Save(getFormat(), pBitmap, fileName, 0);
						time = System.currentTimeMillis() - time;
						log.info("Session %s: Completed %.0f%%".formatted(context.getSessionId(), 100f));
						if (delegate != null) {
							delegate.didProgressChanged(100f);
						}
						if (log.isLoggable(Level.INFO)) {
							log.info("Session %s: Image exported (total time %3.2fs)".formatted(context.getSessionId(), time / 1000.0d));
						}
					}
				}
				catch (final Exception e) {
					log.log(Level.WARNING, "Session %s: Failed to encode image".formatted(context.getSessionId()), e);
					throw new EncoderException("Failed to encode image", e);
				}
				finally {
					if (!pBitmap.equals(NULL)) {
						FreeImage_Unload(pBitmap);
					}
				}
			}
		}
	}
}
