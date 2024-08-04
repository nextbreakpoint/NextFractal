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
package com.nextbreakpoint.nextfractal.core.javafx.editor;

import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpans;
import com.nextbreakpoint.nextfractal.core.editor.GenericStyleSpansBuilder;
import com.nextbreakpoint.nextfractal.core.event.EditorReportChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorSourceChanged;
import com.nextbreakpoint.nextfractal.core.javafx.BooleanObservableValue;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import lombok.extern.java.Log;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

@Log
public class ScriptArea extends CodeArea {
    private final PlatformEventBus eventBus;

    private final BooleanObservableValue internalSource = new BooleanObservableValue();

    public ScriptArea(PlatformEventBus eventBus) {
        this.eventBus = eventBus;

        internalSource.setValue(false);

        setParagraphGraphicFactory(LineNumberFactory.get(this));

        plainTextChanges()
                .suppressible()
                .suspendedWhen(internalSource)
                .successionEnds(Duration.ofMillis(500))
                .subscribe(textChange -> notifyEvent(getText()));

        eventBus.subscribe(EditorReportChanged.class.getSimpleName(), event -> handleEditorReportChanged((EditorReportChanged) event));
    }

    private void notifyEvent(String text) {
        eventBus.postEvent(EditorSourceChanged.builder().source(text).build());
    }

    private void handleEditorReportChanged(EditorReportChanged event) {
        if (!event.continuous()) {
            internalSource.setValue(true);
            replaceText(0, getLength(), event.session().script());
            getUndoManager().forgetHistory();
            internalSource.setValue(false);
        }
        updateTextStyles(event.result());
    }

    private void updateTextStyles(ParserResult result) {
        setStyleSpans(0, convertStyleSpans(result.highlighting()));
        final List<ParserError> errors = result.errors()
                .stream()
                .sorted(Comparator.comparing(ParserError::index))
                .toList();
        for (ParserError error : errors) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(error.toString());
            }
            if (error.type() != ParserErrorType.RUNTIME) {
                final int lineBegin = (int) error.index();
                final int lineEnd = lineBegin + 1;
                try {
                    if (lineBegin < getLength() && lineEnd <= getLength()) {
                        final GenericStyleSpansBuilder<Collection<String>> builder = new GenericStyleSpansBuilder<>();
                        builder.addSpan(List.of("error"), lineEnd - lineBegin);
                        final GenericStyleSpans<Collection<String>> spans = builder.build();
                        setStyleSpans(lineBegin, convertStyleSpans(spans));
                    } else {
                        if (log.isLoggable(Level.WARNING)) {
                            log.log(Level.WARNING, "begin " + lineBegin + ", length " + (lineEnd - lineBegin));
                        }
                    }
                } catch (Exception e) {
                    if (log.isLoggable(Level.WARNING)) {
                        log.log(Level.WARNING, "Something went wrong", e);
                    }
                }
            }
        }
    }

    private static StyleSpans<Collection<String>> convertStyleSpans(GenericStyleSpans<Collection<String>> spans) {
        return spans.styleSpans()
                .stream()
                .collect(() -> new StyleSpansBuilder<Collection<String>>(), (b, s) -> b.add(s.styles(), s.index()), (a, b) -> {
                })
                .create();
    }
}
