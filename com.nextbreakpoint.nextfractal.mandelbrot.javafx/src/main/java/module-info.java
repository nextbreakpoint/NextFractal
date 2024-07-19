/*
 * NextFractal 2.3.0
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
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.editors.AlgorithmAttributeEditorFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.javafx.MandelbrotUIFactory;

module com.nextbreakpoint.nextfractal.mandelbrot.javafx {
    requires static lombok;
    requires transitive com.nextbreakpoint.nextfractal.mandelbrot;
    requires transitive com.nextbreakpoint.nextfractal.core.javafx;
    requires com.nextbreakpoint.libraries.richtextfx;
    provides com.nextbreakpoint.nextfractal.core.javafx.UIFactory with MandelbrotUIFactory;
    provides com.nextbreakpoint.nextfractal.core.javafx.AttributeEditorFactory with AlgorithmAttributeEditorFactory;
}
