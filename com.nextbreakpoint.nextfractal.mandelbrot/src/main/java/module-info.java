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
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.MetadataCodec;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.module.MandelbrotMetadataCodec;

module com.nextbreakpoint.nextfractal.mandelbrot {
    requires static lombok;
    requires transitive java.compiler;
    requires transitive jdk.compiler;
    requires transitive com.nextbreakpoint.nextfractal.core;
    requires transitive com.nextbreakpoint.convertedlibraries.antlr4.runtime;
    requires com.nextbreakpoint.convertedlibraries.commons.math3;
    exports com.nextbreakpoint.nextfractal.mandelbrot.module;
    exports com.nextbreakpoint.nextfractal.mandelbrot.core;
    exports com.nextbreakpoint.nextfractal.mandelbrot.dsl;
    exports com.nextbreakpoint.nextfractal.mandelbrot.graphics;
    provides CoreFactory with MandelbrotFactory;
    provides MetadataCodec with MandelbrotMetadataCodec;
    uses com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
    opens com.nextbreakpoint.nextfractal.mandelbrot.module to com.fasterxml.jackson.databind;
}
