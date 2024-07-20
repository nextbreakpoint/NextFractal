/*
 * NextFractal 2.3.1
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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.util.Objects;

public class ExportJob {
	private final ExportSession session;
	private final ExportProfile profile;

	public ExportJob(ExportSession session, ExportProfile profile) {
		this.session = Objects.requireNonNull(session);
		this.profile = Objects.requireNonNull(profile);
	}

	public ExportProfile getProfile() {
		return profile;
	}

	public RendererTile getTile() {
		return profile.createTile();
	}

	public File getFile() {
		return session.getFile();
	}

	public File getTmpFile() {
		return session.getTmpFile();
	}

	public void writePixels(RendererSize size, IntBuffer pixels) throws IOException {
		try (RandomAccessFile raf = new RandomAccessFile(this.getTmpFile(), "rw")) {
			writeFrame(raf, size, convertToBytes(size, pixels));
		}
	}
	
	@Override
	public String toString() {
		return "[sessionId = " + session.getSessionId() + ", profile=" + profile + "]";
	}

	private void writeFrame(RandomAccessFile raf, RendererSize size, byte[] data) throws IOException {
		final int sw = size.getWidth();
		final int sh = size.getHeight();
		final int tx = this.getProfile().getTileOffsetX();
		final int ty = this.getProfile().getTileOffsetY();
		final int tw = this.getProfile().getTileWidth();
		final int th = this.getProfile().getTileHeight();
		final int iw = this.getProfile().getFrameWidth();
		final int ih = this.getProfile().getFrameHeight();
		final int ly = Math.min(th, ih - ty);
		final int lx = Math.min(tw, iw - tx);
		long pos = (ty * iw + tx) * 4;
		for (int j = ((sw * (sh - th) + (sw - tw)) / 2) * 4, k = 0; k < ly; k++) {
			raf.seek(pos);
			raf.write(data, j, lx * 4);
			j += sw * 4;
			pos += iw * 4;
			Thread.yield();
		}
	}

	private byte[] convertToBytes(RendererSize size, IntBuffer pixels) {
		byte[] data = new byte[size.getWidth() * size.getHeight() * 4];
		for (int j = 0, i = 0; i < data.length; i += 4) {
			int pixel = pixels.get(j++);
			data[i + 3] = (byte)((pixel >> 24) & 0xFF);
			data[i + 0] = (byte)((pixel >> 16) & 0xFF);
			data[i + 1] = (byte)((pixel >> 8) & 0xFF);
			data[i + 2] = (byte)((pixel >> 0) & 0xFF);
		}
		return data;
	}
}
