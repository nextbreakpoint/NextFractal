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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTCompiledPath;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTPathCommand;

import java.util.concurrent.atomic.AtomicLong;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_BUTT_CAP;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_FILL;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_MITER_JOIN;

public class CommandInfo {
    private static AtomicLong lastPathUID = new AtomicLong(0);
    private Long pathUID;
    private int index;
    private long flags;
    private double miterLimit;
    private double strokeWidth;
    private PathStorage pathStorage;

//    public static final CommandInfo DEFAULT_COMMAND_INFO = new CommandInfo(0, null);
    public static final Long DEFAULT_PATH_UID = Long.MAX_VALUE;

    public CommandInfo() {
        index = 0;
        flags = 0;
        miterLimit = 1.0;
        strokeWidth = 0.1;
        pathUID = DEFAULT_PATH_UID;
        pathStorage = null;
    }

//    public CommandInfo(CommandInfo info) {
//        this.index = info.index;
//        this.flags = info.flags;
//        this.miterLimit = info.miterLimit;
//        this.strokeWidth = info.strokeWidth;
//        this.pathUID = info.pathUID;
//        this.pathStorage = info.pathStorage;
//    }

    public CommandInfo(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        init(index, path, width, pathCommand);
    }

    public CommandInfo(PathStorage pathStorage) {
        this(0, pathStorage);
    }

    private CommandInfo(int index, PathStorage pathStorage) {
        flags = CF_MITER_JOIN.getMask() | CF_BUTT_CAP.getMask() | CF_FILL.getMask();
        miterLimit = 1.0;
        strokeWidth = 0.1;
        pathUID = 0L;
        this.index = index;
        this.pathStorage = pathStorage;
    }

    public int getIndex() {
        return index;
    }

    public long getFlags() {
        return flags;
    }

    public double getMiterLimit() {
        return miterLimit;
    }

    public double getStrokeWidth() {
        return strokeWidth;
    }

    public Long getPathUID() {
        return pathUID;
    }

    public PathStorage getPathStorage() {
        return pathStorage;
    }

    private void init(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        if (pathUID != path.getPathUID() || this.index != index) {
            if (pathCommand != null) {
                flags = pathCommand.getFlags();
                miterLimit = pathCommand.getMiterLimit();
            } else {
                flags =  CF_MITER_JOIN.getMask() | CF_BUTT_CAP.getMask() | CF_FILL.getMask();
                miterLimit = 1.0;
            }
            this.index = index;
            pathStorage = (PathStorage) path.getPathStorage().clone();
            pathUID = path.getPathUID();
            strokeWidth = width;
        }
    }

    public void tryInit(int index, ASTCompiledPath path, double width, ASTPathCommand pathCommand) {
        // Try to change the path UID from the default value to a value that is
        // guaranteed to not be in use. If successful then perform initialization
        pathUID = lastPathUID.incrementAndGet();
        if (pathUID < DEFAULT_PATH_UID) {
            init(index, path, width, pathCommand);
        }
    }
}
