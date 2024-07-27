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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.common.Animation;
import com.nextbreakpoint.nextfractal.core.common.Constants;
import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.encoder.Encoder;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ExportSession {
	private static final int BORDER_SIZE = 0;

	@Getter
    private final String sessionId;
	@Getter
	//TODO the encoder has mutable state. perhaps we should use an encoder factory instead
    private final Encoder encoder;
	@Getter
    private final Size frameSize;
	@Getter
    private final File tmpFile;
	@Getter
    private final File file;
	@Getter
	private final int tileSize;
	@Getter
	private final int frameRate;
	@Getter
	private final float quality;

	private final List<ExportJob> jobs = new ArrayList<>();
	private final List<AnimationFrame> frames = new ArrayList<>();

	public ExportSession(String sessionId, Session session, List<AnimationClip> clips, File file, File tmpFile, Size frameSize, int tileSize, Encoder encoder) {
		this.sessionId = Objects.requireNonNull(sessionId);
		this.tmpFile = Objects.requireNonNull(tmpFile);
		this.file = Objects.requireNonNull(file);
		this.frameSize = Objects.requireNonNull(frameSize);
		this.encoder = Objects.requireNonNull(encoder);
		this.tileSize = tileSize;
		this.quality = 1;
		this.frameRate = Constants.FRAMES_PER_SECOND;
		createFrames(session, clips);
		jobs.addAll(createJobs());
	}

    public int getFrameCount() {
		return frames.size();
	}

	public List<ExportJob> getJobs() {
		return Collections.unmodifiableList(jobs);
	}

	public List<AnimationFrame> getFrames() {
		return Collections.unmodifiableList(frames);
	}

	@Override
	public String toString() {
		return "[sessionId = " + sessionId + "]";
	}

	//TODO extract code to separate class
	private void createFrames(Session session, List<AnimationClip> clips) {
		if (!clips.isEmpty() && clips.getFirst().events().size() > 1) {
			final Animation animation = new Animation(clips, frameRate);
			this.frames.addAll(animation.generateFrames());
		} else {
			frames.add(new AnimationFrame(session.pluginId(), session.script(), session.metadata(), true, true));
		}
	}

	//TODO extract code to separate class
	private List<ExportJob> createJobs() {
		final List<ExportJob> jobs = new ArrayList<>();
		final int frameWidth = frameSize.width();
		final int frameHeight = frameSize.height();
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

	//TODO extract code to separate class
	private ExportProfile createProfile(final int frameWidth, final int frameHeight, int tileOffsetX, int tileOffsetY) {
		return ExportProfile.builder()
				.withFrameWidth(frameWidth)
				.withFrameHeight(frameHeight)
				.withTileWidth(tileSize)
				.withTileHeight(tileSize)
				.withTileOffsetX(tileOffsetX)
				.withTileOffsetY(tileOffsetY)
				.withBorderWidth(BORDER_SIZE)
				.withBorderHeight(BORDER_SIZE)
				.build();
	}

	private ExportJob createJob(ExportProfile profile) {
		return new ExportJob(this, profile);
	}
}
