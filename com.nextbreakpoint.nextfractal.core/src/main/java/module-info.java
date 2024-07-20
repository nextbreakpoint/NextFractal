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
module com.nextbreakpoint.nextfractal.core {
    requires static lombok;
    requires transitive java.desktop;
    requires transitive java.logging;
    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.annotation;
    requires transitive com.nextbreakpoint.common;
    exports com.nextbreakpoint.nextfractal.core.common;
    exports com.nextbreakpoint.nextfractal.core.editor;
    exports com.nextbreakpoint.nextfractal.core.params;
    exports com.nextbreakpoint.nextfractal.core.encode;
    exports com.nextbreakpoint.nextfractal.core.export;
    exports com.nextbreakpoint.nextfractal.core.render;
    exports com.nextbreakpoint.nextfractal.core.event;
    uses com.nextbreakpoint.nextfractal.core.common.CoreFactory;
    uses com.nextbreakpoint.nextfractal.core.encode.Encoder;
    opens com.nextbreakpoint.nextfractal.core.common to com.fasterxml.jackson.databind;
    opens com.nextbreakpoint.nextfractal.core.editor to com.fasterxml.jackson.databind;
}
