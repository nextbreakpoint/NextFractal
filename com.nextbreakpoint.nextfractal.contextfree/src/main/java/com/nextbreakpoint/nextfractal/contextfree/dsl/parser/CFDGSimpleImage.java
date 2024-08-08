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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFHandle;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFListener;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.Setter;

import java.util.List;

public class CFDGSimpleImage implements CFDGImage {
    private final CFDG cfdg;

    @Setter
    private CFListener listener;

    public CFDGSimpleImage(CFDG cfdg) {
        this.cfdg = cfdg;
    }

    public CFHandle render(CFCanvas canvas, String seed, boolean partialDraw) {
        cfdg.rulesLoaded();
        final CFDGRenderer renderer = cfdg.createRenderer(canvas.getWidth(), canvas.getHeight(), 1, seed.hashCode(), 0.1);
        if (renderer != null) {
            renderer.setListener(this::draw);
            renderer.run(canvas, partialDraw);
        }
        return new DefaultHandle(renderer, cfdg);
    }

    private void draw() {
        if (listener != null) {
            listener.draw();
        }
    }

    private static class DefaultHandle implements CFHandle {
        private final CFDGRenderer renderer;
        private final CFDG cfdg;

        public DefaultHandle(CFDGRenderer renderer, CFDG cfdg) {
            this.renderer = renderer;
            this.cfdg = cfdg;
        }

        @Override
        public void stop() {
            if (renderer != null) {
                renderer.setRequestStop(true);
            }
        }

        @Override
        public List<ScriptError> errors() {
            return cfdg.getSystem().getErrors();
        }
    }
}
