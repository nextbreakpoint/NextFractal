/*
 * NextFractal 2.1.3
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2022 Andrea Medeghini
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
module com.nextbreakpoint.nextfractal.runtime.javafx {
    requires java.logging;
    requires java.desktop;
    requires java.compiler;
    requires javafx.controls;
    requires com.nextbreakpoint.try4java;
    requires com.nextbreakpoint.nextfractal.core;
    requires com.nextbreakpoint.nextfractal.runtime;
    requires com.nextbreakpoint.nextfractal.core.javafx;
    requires com.nextbreakpoint.nextfractal.mandelbrot.javafx;
    requires com.nextbreakpoint.nextfractal.contextfree.javafx;
    exports com.nextbreakpoint.nextfractal.runtime.javafx;
    uses com.nextbreakpoint.nextfractal.core.encode.Encoder;
}
