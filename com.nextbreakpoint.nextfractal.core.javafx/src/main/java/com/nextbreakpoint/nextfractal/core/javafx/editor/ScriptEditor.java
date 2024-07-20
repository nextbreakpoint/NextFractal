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
package com.nextbreakpoint.nextfractal.core.javafx.editor;

import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

public class ScriptEditor extends BorderPane {
    private final ScriptArea codeArea;

    public ScriptEditor(PlatformEventBus eventBus) {
        codeArea = new ScriptArea(eventBus);
        getStyleClass().add("source-editor");

        final ScrollPane codePane = new ScrollPane();
        codePane.setContent(codeArea);
        codePane.setFitToWidth(true);
        codePane.setFitToHeight(true);

        setCenter(codePane);

        codeArea.setOnDragDropped(e -> handleDragDropped(e, eventBus));
        codeArea.setOnDragOver(this::handleDragOver);
    }

    private static void handleDragDropped(DragEvent e, PlatformEventBus eventBus) {
        e.getDragboard().getFiles().stream().findFirst()
            .ifPresent(file -> eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build()));
    }

    private void handleDragOver(DragEvent e) {
        if (e.getGestureSource() != codeArea && e.getDragboard().hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
    }
}
