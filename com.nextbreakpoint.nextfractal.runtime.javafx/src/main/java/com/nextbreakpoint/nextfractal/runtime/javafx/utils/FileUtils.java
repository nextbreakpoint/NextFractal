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
package com.nextbreakpoint.nextfractal.runtime.javafx.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.nextbreakpoint.nextfractal.runtime.javafx.utils.Constants.PROJECT_EXTENSION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmssSSS");

    public static File newProjectFile(File workspace) {
        if (!workspace.exists() || !workspace.canWrite()) {
            throw new IllegalStateException("Can't create file");
        }
        File file = new File(workspace, createFileName(PROJECT_EXTENSION));
        while (file.exists()) {
            file = new File(workspace, createFileName(PROJECT_EXTENSION));
        }
        return file;
    }

    public static String createFileName(String extension) {
        return FORMATTER.format(LocalDateTime.now(Clock.systemUTC())) + extension;
    }
}
