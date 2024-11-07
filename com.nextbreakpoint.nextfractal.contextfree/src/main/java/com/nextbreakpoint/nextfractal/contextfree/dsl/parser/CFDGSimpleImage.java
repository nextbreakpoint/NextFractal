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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFListener;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFRenderer;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.Setter;

import java.util.List;

public class CFDGSimpleImage implements CFDGImage {
    private final CFDG cfdg;

    public CFDGSimpleImage(CFDG cfdg) {
        this.cfdg = cfdg;
    }

    @Override
    public CFRenderer createRenderer(int width, int height, String seed) {
        cfdg.rulesLoaded();
        final CFDGRenderer renderer = cfdg.createRenderer(width, height, 1, seed.hashCode(), 0.1);
        return new DefaultRenderer(cfdg, renderer);
    }

    private static class DefaultRenderer implements CFRenderer {
        private final CFDG cfdg;
        private final CFDGRenderer renderer;

        @Setter
        private CFListener listener;

        public DefaultRenderer(CFDG cfdg, CFDGRenderer renderer) {
            this.renderer = renderer;
            this.cfdg = cfdg;
            renderer.setListener(this::draw);
        }

        @Override
        public void run(CFCanvas canvas, boolean partialDraw) {
            if (renderer != null) {
                renderer.run(canvas, partialDraw);
            }
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

        private void draw() {
            if (listener != null) {
                listener.draw();
            }
        }
    }
}
