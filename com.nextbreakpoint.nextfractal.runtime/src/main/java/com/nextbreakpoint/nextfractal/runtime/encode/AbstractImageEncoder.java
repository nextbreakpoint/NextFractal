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
package com.nextbreakpoint.nextfractal.runtime.encode;

import com.nextbreakpoint.freeimage4java.FIBITMAP;
import com.nextbreakpoint.freeimage4java.FREE_IMAGE_FORMAT;
import com.nextbreakpoint.freeimage4java.FreeImage4Java;
import com.nextbreakpoint.freeimage4java.FreeImage4JavaConstants;
import com.nextbreakpoint.freeimage4java.RGBQUAD;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.core.encode.EncoderContext;
import com.nextbreakpoint.nextfractal.core.encode.EncoderDelegate;
import com.nextbreakpoint.nextfractal.core.encode.EncoderException;
import com.nextbreakpoint.nextfractal.core.encode.EncoderHandle;
import lombok.extern.java.Log;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Andrea Medeghini
 */
@Log
public abstract class AbstractImageEncoder implements Encoder {
	private EncoderDelegate delegate;

	static {
		FreeImage4Java.FreeImage_Initialise(FreeImage4JavaConstants.TRUE);
	}

	/**
	 * @return
	 */
	public boolean isVideoSupported() {
		return false;
	}

	/**
	 * @see com.nextbreakpoint.nextfractal.core.encode.Encoder#setDelegate(com.nextbreakpoint.nextfractal.core.encode.EncoderDelegate)
	 */
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
	public void encode(EncoderHandle handle, int frame, int count) throws EncoderException {
		((ImageEncoderHandle) handle).encode();
	}

	/**
	 * @return
	 */
	protected boolean isAlphaSupported() {
		return false;
	}

	/**
	 * @param formatName
	 * @return
	 */
	protected FREE_IMAGE_FORMAT getFormat(String formatName) {
		switch (formatName) {
		case "PNG":
			return FREE_IMAGE_FORMAT.FIF_PNG;

		case "JPEG":
			return FREE_IMAGE_FORMAT.FIF_JPEG;
			
		default:
			return FREE_IMAGE_FORMAT.FIF_PNG;
		}
	}

	/**
	 * @return
	 */
	protected abstract String getFormatName();

	private class ImageEncoderHandle implements EncoderHandle {
		private final EncoderContext context;
		private final File path;

		public ImageEncoderHandle(EncoderContext context, File path) {
			this.context = Objects.requireNonNull(context);
			this.path = Objects.requireNonNull(path);
			if (delegate != null) {
				delegate.didProgressChanged(0f);
			}
		}

		public void encode() throws EncoderException {
			RGBQUAD value = null;
			FIBITMAP dib = null;
			try {
				if (log.isLoggable(Level.FINE)) {
					log.fine("Start encoding...");
				}
				long time = System.currentTimeMillis();
				int channels = isAlphaSupported() ? 4 : 3;
				value = new RGBQUAD();
				dib = FreeImage4Java.FreeImage_Allocate(context.getImageWidth(), context.getImageHeight(), channels * 8, 0x00FF0000, 0x0000FF00, 0x000000FF);
				final byte[] data = context.getPixelsAsByteArray(0, 0, 0, context.getImageWidth(), context.getImageHeight(), channels, false);
				for (int y = 0; y < context.getImageHeight(); y++) {
					int j = y * context.getImageWidth();
					for (int x = 0; x < context.getImageWidth(); x++) {
						int i = (j + x) * channels;
						value.setRgbRed(data[i + 0]);
						value.setRgbGreen(data[i + 1]);
						value.setRgbBlue(data[i + 2]);
						if (isAlphaSupported()) {
							value.setRgbReserved(data[i + 3]);
						}
						else {
							value.setRgbReserved((short) 255);
						}
						FreeImage4Java.FreeImage_SetPixelColor(dib, x, y, value);
						if (delegate != null && delegate.isInterrupted()) {
							break;
						}
					}
					Thread.yield();
				}
				if (delegate == null || !delegate.isInterrupted()) {
					FreeImage4Java.FreeImage_Save(getFormat(getFormatName()), dib, path.getAbsolutePath(), 0);
					time = System.currentTimeMillis() - time;
					if (log.isLoggable(Level.INFO)) {
						log.info("Image exported: elapsed time " + String.format("%3.2f", time / 1000.0d) + "s");
					}
					if (delegate != null) {
						delegate.didProgressChanged(100f);
					}
				}
			}
			catch (final Exception e) {
				throw new EncoderException(e);
			}
			finally {
				if (dib != null) {
					FreeImage4Java.FreeImage_Unload(dib);
					dib.delete();
				}
				if (value != null) {
					value.delete();
				}
			}
		}
	}
}
