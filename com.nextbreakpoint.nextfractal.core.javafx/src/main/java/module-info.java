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
import com.nextbreakpoint.nextfractal.core.javafx.graphics.internal.JavaFXGraphicsFactory;
import com.nextbreakpoint.nextfractal.core.javafx.parameter.AttributeEditorFactory;
import com.nextbreakpoint.nextfractal.core.javafx.parameter.DoubleAttributeEditorFactory;

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
module com.nextbreakpoint.nextfractal.core.javafx {
    requires static lombok;
    requires transitive java.prefs;
    requires transitive javafx.controls;
    requires transitive com.nextbreakpoint.nextfractal.core;
    requires com.nextbreakpoint.convertedlibraries.richtextfx;
    exports com.nextbreakpoint.nextfractal.core.javafx;
    exports com.nextbreakpoint.nextfractal.core.javafx.event;
    exports com.nextbreakpoint.nextfractal.core.javafx.editor;
    exports com.nextbreakpoint.nextfractal.core.javafx.viewer;
    exports com.nextbreakpoint.nextfractal.core.javafx.parameter;
    exports com.nextbreakpoint.nextfractal.core.javafx.observable;
    exports com.nextbreakpoint.nextfractal.core.javafx.history;
    exports com.nextbreakpoint.nextfractal.core.javafx.jobs;
    exports com.nextbreakpoint.nextfractal.core.javafx.export;
    exports com.nextbreakpoint.nextfractal.core.javafx.browse;
    exports com.nextbreakpoint.nextfractal.core.javafx.grid;
    exports com.nextbreakpoint.nextfractal.core.javafx.playback;
    exports com.nextbreakpoint.nextfractal.core.javafx.misc;
    uses com.nextbreakpoint.nextfractal.core.encoder.Encoder;
    uses com.nextbreakpoint.nextfractal.core.javafx.UIFactory;
    uses AttributeEditorFactory;
    provides AttributeEditorFactory with DoubleAttributeEditorFactory;
    provides com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory with JavaFXGraphicsFactory;
}
