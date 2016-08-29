package com.nextbreakpoint.nextfractal.contextfree.grammar;

import com.nextbreakpoint.nextfractal.contextfree.core.Bounds;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.FriezeType;

import java.awt.geom.AffineTransform;

public class TiledCanvas extends SimpleCanvas {
    private SimpleCanvas canvas;
    private AffineTransform transform;
    private FriezeType frieze;
    private double scale;

    public TiledCanvas(SimpleCanvas canvas, AffineTransform transform, FriezeType frieze) {
        //TODO rivedere
        super(canvas.getWidth(), canvas.getHeight());
        this.canvas = canvas;
        this.transform = transform;
        this.frieze = frieze;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getScale() {
        return scale;
    }

    public void tileTransform(Bounds bounds) {
        //TODO completare
    }
}
