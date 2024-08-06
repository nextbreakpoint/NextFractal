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

import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGHandle;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGImage;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFDGListener;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import lombok.Setter;

import java.util.List;

public class CFDGSimpleImage implements CFDGImage {
    private final CFDG cfdg;
    @Setter
    private CFDGListener listener;

    public CFDGSimpleImage(CFDG cfdg) {
        this.cfdg = cfdg;
    }

    public CFDGHandle render(CFCanvas canvas, String seed, boolean partialDraw) {
        CollectingLogger logger = new CollectingLogger();
        cfdg.getDriver().setLogger(logger);
        cfdg.rulesLoaded();
        final CFDGRenderer renderer = cfdg.createRenderer(canvas.getWidth(), canvas.getHeight(), 1, seed.hashCode(), 0.1);
        renderer.setRenderListener(this::partialDraw);
        renderer.run(canvas, partialDraw);
        return new DefaultHandle(renderer, logger);
    }

    private void partialDraw() {
        if (listener != null) {
            listener.partialDraw();
        }
    }

    private static class DefaultHandle implements CFDGHandle {
        private final CFDGRenderer renderer;
        private final CollectingLogger logger;

        public DefaultHandle(CFDGRenderer renderer, CollectingLogger logger) {
            this.renderer = renderer;
            this.logger = logger;
        }

        @Override
        public void stop() {
            renderer.setRequestStop(true);
        }

        @Override
        public List<ScriptError> errors() {
            return logger.getErrors();
        }
    }
}
