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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.common.Clip;
import com.nextbreakpoint.nextfractal.core.common.ClipProcessor;
import com.nextbreakpoint.nextfractal.core.common.Frame;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nextbreakpoint.nextfractal.core.common.ClipProcessor.FRAMES_PER_SECOND;

public final class ExportSession {
	private static final int BORDER_SIZE = 0;

	private final List<ExportJob> jobs = new ArrayList<>();
	private final List<Frame> frames = new ArrayList<>();
	private final String sessionId;
	private final Encoder encoder;
	private final RendererSize size;
	private final File tmpFile;
	private final File file;
	private final int tileSize;
	private final float quality;
	private final int frameRate;
	private final Session session;

	public ExportSession(String sessionId, Session session, List<Clip> clips, File file, File tmpFile, RendererSize size, int tileSize, Encoder encoder) {
		this.sessionId = sessionId;
		this.session = session;
		this.tmpFile = tmpFile;
		this.file = file;
		this.size = size;
		this.encoder = encoder;
		this.tileSize = tileSize;
		this.quality = 1;
		this.frameRate = FRAMES_PER_SECOND;
		if (clips.size() > 0 && clips.get(0).getEvents().size() > 1) {
			this.frames.addAll(new ClipProcessor(clips, frameRate).generateFrames());
		} else {
			frames.add(new Frame(session.getPluginId(), session.getMetadata(), session.getScript(), true, true));
		}
		jobs.addAll(createJobs());
	}

	public String getSessionId() {
		return sessionId;
	}

	public RendererSize getSize() {
		return size;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public File getFile() {
		return file;
	}

	public File getTmpFile() {
		return tmpFile;
	}

	public int getFrameRate() {
		return frameRate;
	}

	public int getFrameCount() {
		return frames.size();
	}

	public List<ExportJob> getJobs() {
		return Collections.unmodifiableList(jobs);
	}

	public List<Frame> getFrames() {
		return Collections.unmodifiableList(frames);
	}

	public void dispose() {
		jobs.clear();
	}

	private List<ExportJob> createJobs() {
		final List<ExportJob> jobs = new ArrayList<ExportJob>();
		final int frameWidth = size.getWidth();
		final int frameHeight = size.getHeight();
		final int nx = frameWidth / tileSize;
		final int ny = frameHeight / tileSize;
		final int rx = frameWidth - tileSize * nx;
		final int ry = frameHeight - tileSize * ny;
		if ((nx > 0) && (ny > 0)) {
			for (int tx = 0; tx < nx; tx++) {
				for (int ty = 0; ty < ny; ty++) {
					int tileOffsetX = tileSize * tx;
					int tileOffsetY = tileSize * ty;
					jobs.add(createJob(createProfile(frameWidth, frameHeight, tileOffsetX, tileOffsetY)));
				}
			}
		}
		if (rx > 0) {
			for (int ty = 0; ty < ny; ty++) {
				int tileOffsetX = tileSize * nx;
				int tileOffsetY = tileSize * ty;
				jobs.add(createJob(createProfile(frameWidth, frameHeight, tileOffsetX, tileOffsetY)));
			}
		}
		if (ry > 0) {
			for (int tx = 0; tx < nx; tx++) {
				int tileOffsetX = tileSize * tx;
				int tileOffsetY = tileSize * ny;
				jobs.add(createJob(createProfile(frameWidth, frameHeight, tileOffsetX, tileOffsetY)));
			}
		}
		if (rx > 0 && ry > 0) {
			int tileOffsetX = tileSize * nx;
			int tileOffsetY = tileSize * ny;
			jobs.add(createJob(createProfile(frameWidth, frameHeight, tileOffsetX, tileOffsetY)));
		}
		return jobs;
	}

	private ExportProfile createProfile(final int frameWidth, final int frameHeight, int tileOffsetX, int tileOffsetY) {
		ExportProfileBuilder builder = new ExportProfileBuilder();
		builder.withQuality(quality);
		builder.withFrameRate(frameRate);
		builder.withFrameWidth(frameWidth);
		builder.withFrameHeight(frameHeight);
		builder.withTileWidth(tileSize);
		builder.withTileHeight(tileSize);
		builder.withTileOffsetX(tileOffsetX);
		builder.withTileOffsetY(tileOffsetY);
		builder.withBorderWidth(BORDER_SIZE);
		builder.withBorderHeight(BORDER_SIZE);
		return builder.build();
	}

	private ExportJob createJob(ExportProfile profile) {
		return new ExportJob(this, profile);
	}
}
