package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import java.awt.geom.Point2D;

public record Vertex(Point2D.Double point, int command) implements Cloneable {
    @Override
    public Vertex clone() {
        return new Vertex((Point2D.Double) point.clone(), command);
    }
}
