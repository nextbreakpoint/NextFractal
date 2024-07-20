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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;

public class CFStackModification extends CFStackItem {
    private Modification modification;

    public CFStackModification(CFStack stack, Modification modification) {
        super(stack);
        this.modification = modification;
    }

    public Modification getModification() {
        return modification;
    }

    @Override
    public ExpType getType() {
        return ExpType.ModType;
    }

    @Override
    public int getTupleSize() {
        return 1;
    }
}
