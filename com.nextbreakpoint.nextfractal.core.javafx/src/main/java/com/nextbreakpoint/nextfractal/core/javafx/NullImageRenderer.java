package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;

public class NullImageRenderer implements ImageRenderer {
    @Override
    public void abort() {
    }

    @Override
    public void waitFor() {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean hasImageChanged() {
        return false;
    }

    @Override
    public void drawImage(GraphicsContext gc, int x, int y) {
    }
}
