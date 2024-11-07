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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderException;
import com.nextbreakpoint.nextfractal.core.encoder.EncoderHandle;
import com.nextbreakpoint.nextfractal.core.encoder.RAFEncoderContext;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public final class ExportSessionHandle {
	private final Set<ExportJobHandle> jobs = new HashSet<>();

	@Getter
    private final ExportSession session;

	private int frameNumber;
	private float progress;
	private boolean cancelled;
	private boolean suspended;
	private long timestamp;
	private ExportSessionState state;

	@Getter
	@Setter
	private EncoderHandle encoderHandle;

	public ExportSessionHandle(ExportSession session) {
		this.session = Objects.requireNonNull(session);
		this.frameNumber = 0;
		this.state = ExportSessionState.READY;
		this.timestamp = System.currentTimeMillis();
		this.jobs.addAll(session.getJobs().stream().map(ExportJobHandle::new).collect(Collectors.toSet()));
	}

	public synchronized int getFrameNumber() {
		return frameNumber;
	}

	public synchronized float getProgress() {
		return progress;
	}

	public synchronized boolean isCancelled() {
		return cancelled;
	}

	public synchronized void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public synchronized boolean isSuspended() {
		return suspended;
	}

	public synchronized void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public synchronized long getTimestamp() {
		return timestamp;
	}

	public synchronized ExportSessionState getState() {
		return state;
	}

	public synchronized void setState(ExportSessionState state) {
		timestamp = System.currentTimeMillis();
		this.state = Objects.requireNonNull(state);
	}

	public synchronized void updateProgress() {
		progress = getFrameCount() > 1 ? ((getFrameNumber() + 1f) / (float)getFrameCount()) : (getCompletedJobsCount() / (float)getJobsCount());
	}

	public synchronized AnimationFrame getCurrentFrame() {
		return session.getFrames().get(frameNumber);
	}

	public synchronized boolean nextFrame() {
		if (frameNumber < session.getFrameCount() - 1) {
			frameNumber += 1;
			return true;
		}
		return false;
	}

	public String getSessionId() {
		return session.getSessionId();
	}

	public int getFrameCount() {
		return session.getFrameCount();
	}

	public int getFrameRate() {
		return session.getFrameRate();
	}

	public Size getSize() {
		return session.getFrameSize();
	}

	public File getFile() {
		return session.getFile();
	}

	public File getTmpFile() {
		return session.getTmpFile();
	}

	public int getJobsCount() {
		return session.getJobs().size();
	}

	public int getCompletedJobsCount() {
		return jobs.stream().filter(job -> job.getState() == ExportJobState.COMPLETED).mapToInt(_ -> 1).sum();
	}

	public boolean isFrameCompleted() {
		return getCompletedJobsCount() == getJobsCount();
	}

    public boolean isSessionCompleted() {
        return (getFrameCount() == 0 || getFrameNumber() == getFrameCount() - 1) && isFrameCompleted();
    }

	public Collection<ExportJobHandle> getJobs() {
		return Collections.unmodifiableSet(jobs);
	}

	public synchronized void openEncoder() throws IOException, EncoderException {
		if (encoderHandle == null) {
			final RandomAccessFile raf = new RandomAccessFile(session.getTmpFile(), "r");
			final String sessionId = session.getSessionId();
			final int frameRate = session.getFrameRate();
			final int imageWidth = session.getFrameSize().width();
			final int imageHeight = session.getFrameSize().height();
			final RAFEncoderContext context = new RAFEncoderContext(sessionId, raf, imageWidth, imageHeight, frameRate);
			encoderHandle = session.getEncoder().open(context, session.getFile());
		}
	}

	public synchronized void closeEncoder() throws EncoderException {
		if (encoderHandle != null) {
			try {
				session.getEncoder().close(encoderHandle);
			} finally {
				if (!session.getTmpFile().delete()) {
					log.log(Level.WARNING, "Cannot delete temporary file: " + session.getTmpFile());
				}
				encoderHandle = null;
			}
		}
	}

	public synchronized void encode(int frameNumber, int repeatFrameCount, int frameCount) throws EncoderException {
		session.getEncoder().encode(encoderHandle, frameNumber, repeatFrameCount, frameCount);
	}
}
