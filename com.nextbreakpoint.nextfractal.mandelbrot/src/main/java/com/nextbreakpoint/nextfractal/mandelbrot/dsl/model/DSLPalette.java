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

import com.nextbreakpoint.nextfractal.mandelbrot.core.Palette;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class DSLPalette extends DSLObject {
    private final String name;
    private final List<DSLPaletteElement> elements;

    public DSLPalette(DSLToken token, String name, List<DSLPaletteElement> elements) {
        super(token);
        this.name = name;
        this.elements = elements;
    }

    public Palette evaluate(DSLInterpreterContext context, Map<String, Variable> scope) {
        final Palette palette = new Palette();
        for (DSLPaletteElement element : elements) {
            palette.add(element.evaluate(context, scope));
        }
        return palette;
    }

    public void compile(DSLCompilerContext context, Map<String, VariableDeclaration> scope) {
        context.append("palette");
        context.append(name.toUpperCase().substring(0, 1));
        context.append(name.substring(1));
        context.append(" = palette()");
        for (DSLPaletteElement element : elements) {
            context.append(".add(");
            element.compile(context, scope);
            context.append(")");
        }
        context.append(".build();\n");
    }
}
