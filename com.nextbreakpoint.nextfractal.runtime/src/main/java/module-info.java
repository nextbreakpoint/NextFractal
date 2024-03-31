/*
 * NextFractal 2.1.5
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
import com.nextbreakpoint.nextfractal.core.encode.Encoder;
import com.nextbreakpoint.nextfractal.runtime.encode.AVIVideoEncoder;
import com.nextbreakpoint.nextfractal.runtime.encode.PNGImageEncoder;
import com.nextbreakpoint.nextfractal.runtime.encode.QuicktimeVideoEncoder;

module com.nextbreakpoint.nextfractal.runtime {
    requires java.logging;
    requires com.nextbreakpoint.nextfractal.core;
    requires com.nextbreakpoint.nextfractal.mandelbrot;
    requires com.nextbreakpoint.nextfractal.contextfree;
    requires com.nextbreakpoint.try4java;
    requires com.nextbreakpoint.ffmpeg4java;
    requires com.nextbreakpoint.freeimage4java;
    exports com.nextbreakpoint.nextfractal.runtime.logging;
    exports com.nextbreakpoint.nextfractal.runtime.encode;
    exports com.nextbreakpoint.nextfractal.runtime.export;
    provides Encoder with PNGImageEncoder, AVIVideoEncoder, QuicktimeVideoEncoder;
}
