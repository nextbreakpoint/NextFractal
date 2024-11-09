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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.model;

import lombok.Getter;

public class DSLToken {
    private final String code;
    @Getter
    private final int line;
    @Getter
    private final int charPositionInLine;
    @Getter
    private final int startIndex;
    @Getter
    private final int stopIndex;
    @Getter
    private final String text;

    public DSLToken(String code, int line, int charPositionInLine, int startIndex, int stopIndex, String text) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.text = text;
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
