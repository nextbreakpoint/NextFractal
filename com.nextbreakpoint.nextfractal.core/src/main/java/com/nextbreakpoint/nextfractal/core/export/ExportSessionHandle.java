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

import com.nextbreakpoint.nextfractal.core.common.AnimationFrame;
import com.nextbreakpoint.nextfractal.core.encoder.Encoder;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import lombok.Getter;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class ExportSessionHandle {
	private final Set<ExportJobHandle> jobs = new HashSet<>();

	@Getter
    private final ExportSession session;

	private volatile int frameNumber;
	private volatile float progress;
	private volatile boolean cancelled;
	private volatile long timestamp;
	private volatile ExportSessionState state;

	public ExportSessionHandle(ExportSession session) {
		this.session = Objects.requireNonNull(session);
		this.frameNumber = 0;
		this.state = ExportSessionState.SUSPENDED;
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

	public synchronized boolean isReady() {
		return state == ExportSessionState.READY;
	}

	public synchronized boolean isDispatched() {
		return state == ExportSessionState.DISPATCHED;
	}

	public synchronized boolean isSuspended() {
		return state == ExportSessionState.SUSPENDED;
	}

	public synchronized boolean isInterrupted() {
		return state == ExportSessionState.INTERRUPTED;
	}

	public synchronized boolean isCompleted() {
		return state == ExportSessionState.COMPLETED;
	}

	public synchronized boolean isTerminated() {
		return state == ExportSessionState.TERMINATED;
	}

	public synchronized boolean isFailed() {
		return state == ExportSessionState.FAILED;
	}

	public synchronized boolean isExpired() {
		return System.currentTimeMillis() - timestamp > 5000;
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

	public Encoder getEncoder() {
		return session.getEncoder();
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

	public Collection<ExportJobHandle> getJobs() {
		return Collections.unmodifiableSet(jobs);
	}

	public boolean isFrameCompleted() {
		return getCompletedJobsCount() == getJobsCount();
	}

    public boolean isSessionCompleted() {
        return (getFrameCount() == 0 || getFrameNumber() == getFrameCount() - 1) && isFrameCompleted();
    }

	private int getCompletedJobsCount() {
		return jobs.stream().filter(ExportJobHandle::isCompleted).mapToInt(_ -> 1).sum();
	}
}
