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

public class ExportJobHandle {
    private final ExportJob job;
    private volatile ExportProfile profile;
    private volatile ExportJobState state;
    private volatile Throwable error;

    public ExportJobHandle(ExportJob job) {
        this.job = job;
        this.state = ExportJobState.READY;
    }

    public ExportProfile getProfile() {
        return profile;
    }

    public ExportJobState getState() {
        return state;
    }

    public Throwable getError() {
        return error;
    }

    public ExportJob getJob() {
        return job;
    }

    public void setProfile(ExportProfile profile) {
        this.profile = profile;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void setState(ExportJobState state) {
        this.state = state;
    }

    public boolean isInterrupted() {
        return state == ExportJobState.INTERRUPTED;
    }

    public boolean isCompleted() {
        return state == ExportJobState.COMPLETED;
    }

    public boolean isReady() {
        return state == ExportJobState.READY;
    }
}
