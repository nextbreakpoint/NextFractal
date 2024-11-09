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
package com.nextbreakpoint.nextfractal.core.javafx;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.stage.Screen;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.logging.Level;

@Log
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Icons {
    public static final Color DEFAULT_COLOR = Color.web("020202");

    public static ImageView createIconImage(String name, double size) {
        return createIconImage(Icons.class.getResourceAsStream(name), size);
    }

    public static ImageView createIconImage(InputStream stream, double size) {
        if (stream != null) {
            final ImageView image = new ImageView(new Image(stream));
            image.setSmooth(true);
            image.setFitWidth(size);
            image.setFitHeight(size);
            return image;
        } else {
            final ImageView image = new ImageView();
            image.setSmooth(true);
            image.setFitWidth(size);
            image.setFitHeight(size);
            return image;
        }
    }

    public static Node createSVGIcon(String name, double size) {
        return createSVGIcon(name, size, DEFAULT_COLOR);
    }

    public static Node createSVGIcon(InputStream stream, double size) {
        return createSVGIcon(stream, size, DEFAULT_COLOR);
    }

    public static Node createSVGIcon(String name, double size, Color color) {
        return createSVGIcon(Icons.class.getResourceAsStream(name), size, color);
    }

    public static Node createSVGIcon(InputStream stream, double size, Color color) {
        if (stream != null) {
            final SVGPath path = new SVGPath();
            path.setSmooth(true);
            path.setStrokeWidth(3);
            path.setFill(Color.TRANSPARENT);
            path.setStroke(color);
            path.setStrokeType(StrokeType.CENTERED);
            path.setStrokeLineCap(StrokeLineCap.ROUND);
            path.setStrokeLineJoin(StrokeLineJoin.ROUND);
            path.setStrokeMiterLimit(4);
            path.setContent(extractPath(stream));
            final double originalWidth = path.prefWidth(-1);
            final double originalHeight = path.prefHeight(-1);
            final double scaleX = size / Math.max(originalWidth, originalHeight);
            path.setScaleX(scaleX);
            path.setScaleY(scaleX);
            final BorderPane pane = new BorderPane();
            pane.setCenter(path);
            pane.setMinSize(size, size);
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            return pane;
        } else {
            final BorderPane pane = new BorderPane();
            pane.setMinSize(size, size);
            pane.setMaxSize(size, size);
            pane.setPrefSize(size, size);
            return pane;
        }
    }

    private static String extractPath(InputStream stream) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(stream);
            final NodeList pathElements = document.getElementsByTagName("path");
            if (pathElements.getLength() > 0) {
                final org.w3c.dom.Node pathNode = pathElements.item(0).getAttributes().getNamedItem("d");
                if (pathNode != null && pathNode.getNodeValue() != null) {
                    return pathNode.getNodeValue();
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Can't load icon", e);
        }
        return null;
    }

    public static double computeOptimalIconPercentage() {
        double size = 0.019;

        final Screen screen = Screen.getPrimary();

        if (screen.getDpi() > 100 || screen.getVisualBounds().getWidth() > 1200) {
            size = 0.020;
        }

        if (screen.getDpi() > 200 || screen.getVisualBounds().getWidth() > 2400) {
            size = 0.022;
        }

        return size;
    }

    public static double computeOptimalLargeIconPercentage() {
        double size = 0.021;

        final Screen screen = Screen.getPrimary();

        if (screen.getDpi() > 100 || screen.getVisualBounds().getWidth() > 1200) {
            size = 0.022;
        }

        if (screen.getDpi() > 200 || screen.getVisualBounds().getWidth() > 2400) {
            size = 0.024;
        }

        return size;
    }
}
