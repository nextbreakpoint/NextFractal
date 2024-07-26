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
package com.nextbreakpoint.nextfractal.core.encode;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

public class RAFEncoderContext implements EncoderContext {
	private final String sessionId;
	private final RandomAccessFile raf;
	private final int imageWidth;
	private final int imageHeight;
	private final int frameRate;

	/**
	 *
	 * @param sessionId
	 * @param raf
	 * @param imageWidth
	 * @param imageHeight
	 * @param frameRate
     */
	public RAFEncoderContext(final String sessionId, final RandomAccessFile raf, final int imageWidth, final int imageHeight, final int frameRate) {
		this.sessionId = Objects.requireNonNull(sessionId);
		this.raf = Objects.requireNonNull(raf);
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.frameRate = frameRate;
	}

	@Override
	public byte[] getPixelsAsByteArray(final int n, final int x, final int y, final int w, final int h, final int s) throws IOException {
		final byte[] data = new byte[w * h * s];
		final byte[] row = new byte[w * 4];
		long pos = (n * (long) getImageWidth() * (long) getImageHeight() + y * (long) getImageWidth() + x) * 4L;
		int t = 0;
		for (int k = 0; k < h; k++) {
			raf.seek(pos);
			raf.readFully(row);
			for (int j = 0, i = 0; i < row.length; j += s, i += 4) {
				if (s == 3) {
					data[t + j] = row[i];
					data[t + j + 1] = row[i + 1];
					data[t + j + 2] = row[i + 2];
				}
				else if (s == 4) {
					data[t + j] = row[i];
					data[t + j + 1] = row[i + 1];
					data[t + j + 2] = row[i + 2];
					data[t + j + 3] = row[i + 3];
				}
			}
			t += w * s;
			pos += getImageWidth() * 4L;
		}
		return data;
	}

	@Override
	public byte[] getPixelsAsByteArray(final int n, final int x, final int y, final int w, final int h, final int s, final boolean flip) throws IOException {
		final byte[] data = new byte[w * h * s];
		final byte[] row = new byte[w * 4];
		long pos = (n * (long) getImageWidth() * (long) getImageHeight() + y * (long) getImageWidth() + x) * 4L;
		if (flip) {
			int t = (h - 1) * w * s;
			for (int k = 0; k < h; k++) {
				raf.seek(pos);
				raf.readFully(row);
				for (int j = 0, i = 0; i < row.length; j += s, i += 4) {
					if (s == 3) {
						data[t + j] = row[i];
						data[t + j + 1] = row[i + 1];
						data[t + j + 2] = row[i + 2];
					}
					else if (s == 4) {
						data[t + j] = row[i];
						data[t + j + 1] = row[i + 1];
						data[t + j + 2] = row[i + 2];
						data[t + j + 3] = row[i + 3];
					}
				}
				t -= w * s;
				pos += getImageWidth() * 4L;
			}
		} else {
			int t = 0;
			for (int k = 0; k < h; k++) {
				raf.seek(pos);
				raf.readFully(row);
				for (int j = 0, i = 0; i < row.length; j += s, i += 4) {
					if (s == 3) {
						data[t + j] = row[i];
						data[t + j + 1] = row[i + 1];
						data[t + j + 2] = row[i + 2];
					}
					else if (s == 4) {
						data[t + j] = row[i];
						data[t + j + 1] = row[i + 1];
						data[t + j + 2] = row[i + 2];
						data[t + j + 3] = row[i + 3];
					}
				}
				t += w * s;
				pos += getImageWidth() * 4L;
			}
		}
		return data;
	}

	@Override
	public int getImageWidth() {
		return imageWidth;
	}

	@Override
	public int getImageHeight() {
		return imageHeight;
	}

	@Override
	public int getFrameRate() {
		return frameRate;
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}
}
