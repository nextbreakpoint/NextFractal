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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.concurrent.ThreadFactory;

public class PlatformThreadFactory implements ThreadFactory {
	private final boolean isDaemon;
	private final int priority;
	private final String name;
	private int ordinal;

	public PlatformThreadFactory(final String name, final boolean isDaemon, final int priority) {
		this.name = name;
		this.isDaemon = isDaemon;
		this.priority = priority;
	}

	@Override
	public Thread newThread(final Runnable runnable) {
		final Thread thread = Thread.ofPlatform().factory().newThread(runnable);
		thread.setPriority(priority);
		thread.setDaemon(isDaemon);
		thread.setName(name + "-" + (ordinal++));
		return thread;
	}
}
