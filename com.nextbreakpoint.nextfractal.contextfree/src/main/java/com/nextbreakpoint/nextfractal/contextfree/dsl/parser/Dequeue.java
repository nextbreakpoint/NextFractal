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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import java.util.ArrayList;
import java.util.List;

public class Dequeue {
    private final List<CommandInfo> commands = new ArrayList<>();

    public void pushBack(CommandInfo commandInfo) {
        commands.add(commandInfo);
    }

    public CommandInfo back() {
        return commands.getLast();
    }

    public CommandIterator iterator() {
        return new CommandIterator(commands);
    }

    public void clear() {
        commands.clear();
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }
}
