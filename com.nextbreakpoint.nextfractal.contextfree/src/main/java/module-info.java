/*
 * NextFractal 2.1.4
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
import com.nextbreakpoint.nextfractal.contextfree.module.ContextFreeFactory;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;

module com.nextbreakpoint.nextfractal.contextfree {
    requires java.logging;
    requires java.desktop;
    requires commons.math3;
    requires org.antlr.antlr4.runtime;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.nextbreakpoint.nextfractal.core;
    requires com.nextbreakpoint.try4java;
    exports com.nextbreakpoint.nextfractal.contextfree.module;
    exports com.nextbreakpoint.nextfractal.contextfree.core;
    exports com.nextbreakpoint.nextfractal.contextfree.dsl;
    exports com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;
    exports com.nextbreakpoint.nextfractal.contextfree.renderer;
    provides CoreFactory with ContextFreeFactory;
    opens com.nextbreakpoint.nextfractal.contextfree.module to com.fasterxml.jackson.databind;
}
