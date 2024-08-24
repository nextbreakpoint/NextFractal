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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTCompiledPath;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTPathCommand;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_BUTT_CAP;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_FILL;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_MITER_JOIN;

// CmdInfo.cpp
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA

@Getter
public class CommandInfo {
    private static final CommandInfo DEFAULT_COMMAND_INFO = new CommandInfo(0, null);
    private static final Long DEFAULT_PATH_UID = Long.MAX_VALUE;

    private AtomicLong pathUID;
    private int index;
    @Getter
    @Setter
    private long flags;
    private double miterLimit;
    private double strokeWidth;
    private PathStorage path;

    public CommandInfo() {
        index = 0;
        flags = 0;
        miterLimit = 1.0;
        strokeWidth = 0.1;
        pathUID = new AtomicLong(DEFAULT_PATH_UID);
        path = null;
    }

    public CommandInfo(CommandInfo info) {
        this.index = info.index;
        this.flags = info.flags;
        this.miterLimit = info.miterLimit;
        this.strokeWidth = info.strokeWidth;
        this.pathUID = info.pathUID;
        this.path = info.path;
    }

    public CommandInfo(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        init(index, path, width, pathCommand);
    }

    public CommandInfo(PathStorage path) {
        this(0, path);
    }

    private CommandInfo(int index, PathStorage path) {
        flags = CF_MITER_JOIN.getMask() | CF_BUTT_CAP.getMask() | CF_FILL.getMask();
        miterLimit = 1.0;
        strokeWidth = 0.1;
        pathUID = new AtomicLong(0L);
        this.index = index;
        this.path = path;
    }

    private void init(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        if (pathUID == null || !Objects.equals(pathUID.get(), path.getPathUID()) || this.index != index) {
            if (pathCommand != null) {
                flags = pathCommand.getFlags();
                miterLimit = pathCommand.getMiterLimit();
            } else {
                flags = CF_MITER_JOIN.getMask() | CF_BUTT_CAP.getMask() | CF_FILL.getMask();
                miterLimit = 1.0;
            }
            this.index = index;
            this.path = (PathStorage) path.getPath().clone();
            pathUID = new AtomicLong(path.getPathUID());
            strokeWidth = width;
        }
    }

    public void tryInit(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        // Try to change the path UID from the default value to a value that is
        // guaranteed to not be in use. If successful then perform initialization
        if (pathUID.compareAndExchange(DEFAULT_PATH_UID, 0L) != 0) {
            init(index, path, width, pathCommand);
        }
    }
}
