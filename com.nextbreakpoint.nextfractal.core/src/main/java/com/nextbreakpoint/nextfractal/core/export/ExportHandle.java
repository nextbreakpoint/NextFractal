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

import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ExportHandle {
	private final ExportSession session;
	private final Set<ExportJobHandle> jobs = new HashSet<>();
	private volatile int frameNumber;
	private volatile float progress;
	private volatile boolean cancelled;
	private volatile long timestamp;
	private volatile ExportState state;

	public ExportHandle(ExportSession session) {
		this.session = session;
		this.frameNumber = 0;
		this.state = ExportState.SUSPENDED;
		this.timestamp = System.currentTimeMillis();
		this.jobs.addAll(session.getJobs().stream().map(job -> new ExportJobHandle(job)).collect(Collectors.toSet()));
	}

	public int getFrameNumber() {
		return frameNumber;
	}

	public float getProgress() {
		return progress;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isReady() {
		return state == ExportState.READY;
	}

	public boolean isDispatched() {
		return state == ExportState.DISPATCHED;
	}

	public boolean isSuspended() {
		return state == ExportState.SUSPENDED;
	}

	public boolean isInterrupted() {
		return state == ExportState.INTERRUPTED;
	}

	public boolean isCompleted() {
		return state == ExportState.COMPLETED;
	}

	public boolean isFinished() {
		return state == ExportState.FINISHED;
	}

	public boolean isFailed() {
		return state == ExportState.FAILED;
	}

	public ExportState getState() {
		return state;
	}

	public ExportSession getSession() {
		return session;
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

	public File getTmpFile() {
		return session.getTmpFile();
	}

	public RendererSize getSize() {
		return session.getSize();
	}

	public File getFile() {
		return session.getFile();
	}

	public int getJobsCount() {
		return session.getJobs().size();
	}

	public Collection<ExportJobHandle> getJobs() {
		return Collections.unmodifiableSet(jobs);
	}

	public void updateProgress() {
		setProgress(session.getFrameCount() > 0 ? ((getFrameNumber() + 1f) / (float)session.getFrameCount()) : (getCompletedJobsCount() / (float)session.getJobs().size()));
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void setState(ExportState state) {
		timestamp = System.currentTimeMillis();
		this.state = state;
	}

	public String getCurrentPluginId() {
		return session.getFrames().get(frameNumber).getPluginId();
	}

	public String getCurrentScript() {
		return session.getFrames().get(frameNumber).getScript();
	}

	public Metadata getCurrentMetadata() {
		return session.getFrames().get(frameNumber).getMetadata();
	}

	public boolean nextFrame() {
		if (frameNumber < session.getFrameCount() - 1) {
			frameNumber += 1;
			return true;
		}
		return false;
	}

	public boolean isFrameCompleted() {
		return getCompletedJobsCount() == getJobsCount();
	}

	private int getCompletedJobsCount() {
		return jobs.stream().filter(job -> job.isCompleted()).mapToInt(job -> 1).sum();
	}

    public boolean isSessionCompleted() {
        return (getFrameCount() == 0 || getFrameNumber() == getFrameCount() - 1) && isFrameCompleted();
    }

	public long getTimestamp() {
		return timestamp;
	}
}
