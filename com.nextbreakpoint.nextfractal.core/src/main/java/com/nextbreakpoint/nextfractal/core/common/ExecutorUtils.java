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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecutorUtils {
    public static ExecutorService newVirtualThreadPerTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return Executors.newSingleThreadExecutor(threadFactory);
    }

    public static ExecutorService newFixedThreadPool(int poolSize, ThreadFactory threadFactory) {
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public static ExecutorService newThreadPerTaskExecutor(ThreadFactory threadFactory) {
        return Executors.newThreadPerTaskExecutor(threadFactory);
    }

    public static void shutdown(ExecutorService executor) {
        if (executor != null) {
            try {
                executor.shutdownNow();
                if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    log.warning("Timeout occurred while terminating executor");
                }
            } catch (InterruptedException e) {
                log.warning("Interrupted while terminating executor");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot terminate executor", e);
            }
        }
    }
}
