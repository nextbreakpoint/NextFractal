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
package com.nextbreakpoint.nextfractal.runtime.javafx.core;

import com.nextbreakpoint.nextfractal.core.common.AnimationClip;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.ParserResult;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.encoder.Encoder;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipAdded;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipMoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRemoved;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipRestored;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipsLoaded;
import com.nextbreakpoint.nextfractal.core.event.CaptureClipsMerged;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionActionFired;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStarted;
import com.nextbreakpoint.nextfractal.core.event.CaptureSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.CurrentFileChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorActionFired;
import com.nextbreakpoint.nextfractal.core.event.EditorDataChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorDeleteFilesRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorGrammarSelected;
import com.nextbreakpoint.nextfractal.core.event.EditorLoadFileRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorReportChanged;
import com.nextbreakpoint.nextfractal.core.event.EditorSaveFileRequested;
import com.nextbreakpoint.nextfractal.core.event.EditorStoreFileRequested;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionCreated;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionResumed;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionStopped;
import com.nextbreakpoint.nextfractal.core.event.ExportSessionSuspended;
import com.nextbreakpoint.nextfractal.core.event.HistorySessionAdded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataChanged;
import com.nextbreakpoint.nextfractal.core.event.PlaybackDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.PlaybackStopped;
import com.nextbreakpoint.nextfractal.core.event.RenderDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionBundleLoaded;
import com.nextbreakpoint.nextfractal.core.event.SessionDataChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionDataLoaded;
import com.nextbreakpoint.nextfractal.core.event.SessionErrorChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionExportRequested;
import com.nextbreakpoint.nextfractal.core.event.SessionReportChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionStatusChanged;
import com.nextbreakpoint.nextfractal.core.event.SessionTerminated;
import com.nextbreakpoint.nextfractal.core.event.TimeAnimationActionFired;
import com.nextbreakpoint.nextfractal.core.event.WorkspaceChanged;
import com.nextbreakpoint.nextfractal.core.export.ExportService;
import com.nextbreakpoint.nextfractal.core.export.ExportSession;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformEventBus;
import com.nextbreakpoint.nextfractal.runtime.javafx.utils.ApplicationUtils;
import com.nextbreakpoint.nextfractal.runtime.javafx.utils.FileUtils;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactoryByGrammar;
import static com.nextbreakpoint.nextfractal.runtime.javafx.utils.ApplicationUtils.PROPERTY_GRAMMAR_DEFAULT;
import static com.nextbreakpoint.nextfractal.runtime.javafx.utils.Constants.DEFAULT_PLUGIN_ID;
import static com.nextbreakpoint.nextfractal.runtime.javafx.utils.Constants.PROJECT_EXTENSION;

@Log
public class ApplicationHandler {
    private final PlatformEventBus eventBus;

    private final List<AnimationClip> clips = new ArrayList<>();
    private File workspace;
    private Session session;
    private AnimationClip clip;
    private boolean capture;
    private boolean edited;

    private FileChooser exportFileChooser;
    private FileChooser bundleFileChooser;
    private File exportCurrentFile;
    private File bundleCurrentFile;

    public ApplicationHandler(PlatformEventBus eventBus, ExportService exportService, Stage primaryStage) {
        this.eventBus = Objects.requireNonNull(eventBus);

        eventBus.subscribe(WorkspaceChanged.class.getSimpleName(), event -> workspace = ((WorkspaceChanged) event).file());

        eventBus.subscribe(CurrentFileChanged.class.getSimpleName(), event -> bundleCurrentFile = ((CurrentFileChanged) event).file());

        eventBus.subscribe(SessionDataLoaded.class.getSimpleName(), event -> handleSessionDataLoaded((SessionDataLoaded) event));
        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionDataChanged((SessionDataChanged) event));

        eventBus.subscribe(PlaybackDataLoaded.class.getSimpleName(), event -> session = ((PlaybackDataLoaded) event).session());
        eventBus.subscribe(PlaybackDataChanged.class.getSimpleName(), event -> session = ((PlaybackDataChanged) event).session());

        eventBus.subscribe(PlaybackStopped.class.getSimpleName(), _ -> handlePlaybackStopped());

        eventBus.subscribe(EditorActionFired.class.getSimpleName(), event -> handleEditorActionFired(((EditorActionFired) event).action()));

        eventBus.subscribe(EditorDataChanged.class.getSimpleName(), event -> {
            final Session session = ((EditorDataChanged) event).session();
            final boolean continuous = ((EditorDataChanged)event).continuous();
            final boolean appendToHistory = ((EditorDataChanged)event).appendToHistory();
            notifySessionChanged(eventBus, session, continuous, appendToHistory && !continuous);
        });

        eventBus.subscribe(RenderDataChanged.class.getSimpleName(), event -> {
            final Session session = ((RenderDataChanged) event).session();
            final boolean continuous = ((RenderDataChanged) event).continuous();
            final boolean appendToHistory = ((RenderDataChanged) event).appendToHistory();
            notifySessionChanged(eventBus, session, continuous, appendToHistory && !continuous);
        });

        eventBus.subscribe(EditorReportChanged.class.getSimpleName(), event -> handleEditorReportChanged((EditorReportChanged) event));

        eventBus.subscribe(SessionReportChanged.class.getSimpleName(), event -> {
            final ParserResult report = ((SessionReportChanged) event).result();
            final String message = formatErrors(report.errors());
            eventBus.postEvent(SessionErrorChanged.builder().error(message).build());
            eventBus.postEvent(SessionStatusChanged.builder().status(message).build());
            if (report.errors().isEmpty()) {
                notifySessionChanged(eventBus, ((SessionReportChanged) event).session(), ((SessionReportChanged) event).continuous(), ((SessionReportChanged) event).appendToHistory());
            }
        });

        eventBus.subscribe(EditorGrammarSelected.class.getSimpleName(), event -> handleGrammarSelected(((EditorGrammarSelected) event).grammar()));

        eventBus.subscribe(SessionDataChanged.class.getSimpleName(), event -> handleSessionChanged(((SessionDataChanged) event).session()));

        eventBus.subscribe(SessionTerminated.class.getSimpleName(), _ -> handleSessionTerminate(exportService));

        eventBus.subscribe(ExportSessionCreated.class.getSimpleName(), event -> handleExportSessionCreated(exportService, ((ExportSessionCreated) event).session()));

        eventBus.subscribe(ExportSessionStopped.class.getSimpleName(), event -> handleExportSessionStopped(exportService, ((ExportSessionStopped) event).session()));

        eventBus.subscribe(ExportSessionResumed.class.getSimpleName(), event -> handleExportSessionResumed(exportService, ((ExportSessionResumed) event).session()));

        eventBus.subscribe(ExportSessionSuspended.class.getSimpleName(), event -> handleExportSessionSuspended(exportService, ((ExportSessionSuspended) event).session()));

        eventBus.subscribe(EditorLoadFileRequested.class.getSimpleName(), event -> handleLoadFile(((EditorLoadFileRequested) event).file()));

        eventBus.subscribe(EditorSaveFileRequested.class.getSimpleName(), event -> handleSaveFile(((EditorSaveFileRequested) event).file()));

        eventBus.subscribe(EditorStoreFileRequested.class.getSimpleName(), event -> handleStoreFile(((EditorStoreFileRequested) event).file()));

        eventBus.subscribe(EditorDeleteFilesRequested.class.getSimpleName(), event -> handleDeleteFiles(((EditorDeleteFilesRequested) event).files()));

        eventBus.subscribe(SessionErrorChanged.class.getSimpleName(), event -> handleErrorChanged(((SessionErrorChanged) event).error()));

        eventBus.subscribe(CaptureSessionActionFired.class.getSimpleName(), event -> handleCaptureSession(((CaptureSessionActionFired) event).action()));

        eventBus.subscribe(CaptureClipRestored.class.getSimpleName(), event -> handleClipRestored(((CaptureClipRestored) event).clip()));

        eventBus.subscribe(CaptureClipRemoved.class.getSimpleName(), event -> handleClipRemoved(((CaptureClipRemoved) event).clip()));

        eventBus.subscribe(CaptureClipAdded.class.getSimpleName(), event -> handleClipAdded(((CaptureClipAdded) event).clip()));

        eventBus.subscribe(CaptureClipMoved.class.getSimpleName(), event -> handleClipMoved(((CaptureClipMoved) event).fromIndex(), ((CaptureClipMoved) event).toIndex()));

        eventBus.subscribe(SessionBundleLoaded.class.getSimpleName(), event -> handleBundleLoaded((SessionBundleLoaded) event));

        eventBus.subscribe(SessionExportRequested.class.getSimpleName(), event -> handleExportSession(primaryStage, (SessionExportRequested) event));

        eventBus.subscribe(EditorActionFired.class.getSimpleName(), event -> handleEditorAction(primaryStage, ((EditorActionFired) event).action()));

        eventBus.subscribe(TimeAnimationActionFired.class.getSimpleName(), event -> handleTimeAnimationAction(((TimeAnimationActionFired)event).action()));

        primaryStage.setOnCloseRequest(e -> {
            if (!handleConfirmTerminate(exportService)) {
                e.consume();
            }
        });

        // the following events are required to initialise the application
        Platform.runLater(() -> handleGrammarSelected(System.getProperty(PROPERTY_GRAMMAR_DEFAULT, DEFAULT_PLUGIN_ID)));
    }

	private String formatErrors(List<ScriptError> errors) {
        final StringBuilder builder = new StringBuilder();
        if (errors != null) {
            for (ScriptError error : errors) {
                builder.append("Line ");
                builder.append(error.line());
                builder.append(": ");
                builder.append(error.message());
                builder.append("\n");
            }
        }
        return builder.toString();
	}

    private void notifySessionChanged(PlatformEventBus eventBus, Session session, boolean continuous, boolean historyAppend) {
        eventBus.postEvent(SessionDataChanged.builder().session(session).continuous(continuous).appendToHistory(historyAppend).build());
    }

    private void handleEditorReportChanged(EditorReportChanged event) {
        eventBus.postEvent(SessionReportChanged.builder().session(event.session()).continuous(event.continuous()).appendToHistory(event.appendToHistory()).result(event.result()).build());
    }

    private void handleTimeAnimationAction(String action) {
        if ("stop".equals(action)) {
            eventBus.postEvent(SessionDataChanged.builder().session(session).continuous(false).appendToHistory(true).build());
        }
    }

    private void handleSessionDataLoaded(SessionDataLoaded event) {
        session = event.session();

        if (event.appendToHistory()) {
            eventBus.postEvent(HistorySessionAdded.builder().session(event.session()).build());
        }
    }

    private void handleSessionDataChanged(SessionDataChanged event) {
        session = event.session();

        if (event.appendToHistory()) {
            eventBus.postEvent(HistorySessionAdded.builder().session(event.session()).build());
        }
    }

    private void handleEditorActionFired(String action) {
        if (session != null && action.equals("reload"))
            eventBus.postEvent(SessionDataLoaded.builder().session(session).continuous(false).appendToHistory(false).build());
    }

    private void handlePlaybackStopped() {
        eventBus.postEvent(SessionDataLoaded.builder().session(session).continuous(false).appendToHistory(true).build());
    }

    private void handleGrammarSelected(String grammar) {
        tryFindFactoryByGrammar(grammar)
                .optional()
                .ifPresent(this::createSession);
    }

    private void handleDeleteFiles(List<File> files) {
        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setContentText("Do you want to delete the selected %s?".formatted(files.size() > 1 ? files.size() + " projects" : "project"));
        dialog.setTitle("Action required");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
        final Optional<ButtonType> response = dialog.showAndWait();
        if (response.isPresent() && response.get().equals(ButtonType.YES)) {
            files.forEach(file -> {
                try {
                    log.info("Deleting file " + file);
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    log.warning("Can't delete file " + file);
                }
            });
        }
    }

    private void handleEditorAction(Window window, String action) {
        if (action.equals("load")) Optional.ofNullable(showLoadBundleFileChooser())
                .map(fileChooser -> fileChooser.showOpenDialog(window))
                .ifPresent(file -> eventBus.postEvent(EditorLoadFileRequested.builder().file(file).build()));
        if (action.equals("save")) Optional.ofNullable(showSaveBundleFileChooser())
                .map(fileChooser -> fileChooser.showSaveDialog(window))
                .ifPresent(file -> eventBus.postEvent(EditorSaveFileRequested.builder().file(file).build()));
        if (action.equals("store")) Optional.ofNullable(FileUtils.newProjectFile(workspace))
                .ifPresent(file -> eventBus.postEvent(EditorStoreFileRequested.builder().file(file).build()));
    }

    private void handleBundleLoaded(SessionBundleLoaded sessionBundleLoaded) {
        final Bundle bundle = sessionBundleLoaded.bundle();
        if (edited && !clips.isEmpty() && !bundle.clips().isEmpty()) {
            final Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("There are new or modified clips. Do you want to discard them?");
            dialog.setTitle("Action required");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
            final Optional<ButtonType> response = dialog.showAndWait();
            if (response.isPresent() && response.get().equals(ButtonType.NO)) {
                eventBus.postEvent(CaptureClipsMerged.builder().clips(bundle.clips()).build());
            } else {
                eventBus.postEvent(CaptureClipsLoaded.builder().clips(bundle.clips()).build());
            }
        } else {
            if (edited && !clips.isEmpty()) {
                eventBus.postEvent(CaptureClipsMerged.builder().clips(bundle.clips()).build());
            } else {
                eventBus.postEvent(CaptureClipsLoaded.builder().clips(bundle.clips()).build());
            }
        }
        eventBus.postEvent(SessionDataLoaded.builder().session(bundle.session()).continuous(sessionBundleLoaded.continuous()).appendToHistory(sessionBundleLoaded.appendToHistory()).build());
    }

    private void handleClipAdded(AnimationClip clip) {
        clips.add(clip);
        edited = true;
    }

    private void handleClipRemoved(AnimationClip clip) {
        clips.remove(clip);
        edited = true;
    }

    private void handleClipRestored(AnimationClip clip) {
        clips.add(clip);
        edited = false;
    }

    private void handleClipMoved(int fromIndex, int toIndex) {
        final AnimationClip clip = clips.get(fromIndex);
        clips.remove(fromIndex);
        clips.add(toIndex, clip);
        edited = true;
    }

    private void handleCaptureSession(String action) {
        if (action.equals("start")) startCapture();
        if (action.equals("stop")) stopCapture();
    }

    private void handleSessionChanged(Session session) {
        if (capture) {
            clip = clip.appendEvent(new Date(), session.pluginId(), session.script(), session.metadata());
        }
    }

    private void handleErrorChanged(String error) {
    }

    private void handleLoadFile(File file) {
        FileManager.loadBundle(file)
                .observe()
                .onSuccess(bundle -> eventBus.postEvent(CurrentFileChanged.builder().file(file).build()))
                .onFailure(e -> showLoadError(file, e))
                .get()
                .optional()
                .ifPresent(bundle -> eventBus.postEvent(SessionBundleLoaded.builder().bundle(bundle).continuous(false).appendToHistory(true).build()));
    }

    private void handleSaveFile(File file) {
        FileManager.saveBundle(file, new Bundle(session, clips))
                .observe()
                .onFailure(e -> showSaveError(file, e))
                .onSuccess(v -> edited = false)
                .get()
                .optional()
                .ifPresent(bundle -> eventBus.postEvent(CurrentFileChanged.builder().file(file).build()));
    }

    private void handleStoreFile(File file) {
        FileManager.saveBundle(file, new Bundle(session, clips))
                .observe()
                .onFailure(e -> showSaveError(file, e))
                .onSuccess(v -> edited = false)
                .get();
    }

    private void handleExportSession(Window window, SessionExportRequested request) {
        ApplicationUtils.createEncoder(request.format()).ifPresent(encoder -> {
            final Consumer<File> fileConsumer = file -> createExportSession(encoder, request.size(), file);
            Optional.ofNullable(prepareExportFileChooser(encoder.getSuffix()).showSaveDialog(window))
                    .ifPresent(fileConsumer.andThen(file -> exportCurrentFile = file));
        });
    }

    private void handleSessionTerminate(ExportService exportService) {
        log.info("Terminating application...");
        exportService.shutdown();
    }

    private void handleExportSessionCreated(ExportService exportService, ExportSession exportSession) {
        exportService.startSession(exportSession);
    }

    private void handleExportSessionStopped(ExportService exportService, ExportSession exportSession) {
        exportService.stopSession(exportSession);
    }

    private void handleExportSessionResumed(ExportService exportService, ExportSession exportSession) {
        exportService.resumeSession(exportSession);
    }

    private void handleExportSessionSuspended(ExportService exportService, ExportSession exportSession) {
        exportService.suspendSession(exportSession);
    }

    private boolean handleConfirmTerminate(ExportService exportService) {
        if (confirmTerminate(exportService.getSessionCount())) {
            eventBus.postEvent(SessionTerminated.builder().build());
            return true;
        } else {
            return false;
        }
    }

    private void createSession(CoreFactory factory) {
        ApplicationUtils.createSession(factory)
                .optional()
                .ifPresent(session -> eventBus.postEvent(SessionDataLoaded.builder().session(session).continuous(false).appendToHistory(true).build()));
    }

    private void createExportSession(Encoder encoder, Size size, File file) {
        try {
            final ExportSession exportSession = ApplicationUtils.createExportSession(encoder, session, clips, size, file);
            eventBus.postEvent(ExportSessionCreated.builder().session(exportSession).build());
            log.info("Export session created: " + exportSession.getSessionId());
        } catch (Exception e) {
            log.log(Level.WARNING, "Can't export clip to file " + file.getName(), e);
            eventBus.postEvent(SessionStatusChanged.builder().status("Can't export clip to file " + file.getName()).build());
        }
    }

    private boolean confirmTerminate(int sessionCount) {
        if (sessionCount > 0) {
            final Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("There are jobs still running. Do you want to terminate them and exit?");
            dialog.setTitle("Action required");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
            final Optional<ButtonType> response = dialog.showAndWait();
            return response.isEmpty() || !response.get().equals(ButtonType.NO);
        } else if (edited && !clips.isEmpty()) {
            final Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setContentText("There are new or modified clips. Do you want to discard them and exit?");
            dialog.setTitle("Action required");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.NO);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.YES);
            final Optional<ButtonType> response = dialog.showAndWait();
            return response.isEmpty() || !response.get().equals(ButtonType.NO);
        }
        return true;
    }

    private void showLoadError(File file, Exception e) {
        log.log(Level.WARNING, "Can't load file " + file.getAbsolutePath(), e);
        eventBus.postEvent(SessionStatusChanged.builder().status("Can't load file " + file.getName()).build());
    }

    private void showSaveError(File file, Exception e) {
        log.log(Level.WARNING, "Can't save file " + file.getAbsolutePath(), e);
        eventBus.postEvent(SessionStatusChanged.builder().status("Can't save file " + file.getName()).build());
    }

    private void startCapture() {
        capture = true;
        clip = new AnimationClip();
        if (session != null) {
            clip = clip.appendEvent(new Date(), session.pluginId(), session.script(), session.metadata());
        }
        eventBus.postEvent(CaptureSessionStarted.builder().clip(clip).build());
    }

    private void stopCapture() {
        capture = false;
        if (session != null) {
            clip = clip.appendEvent(new Date(), session.pluginId(), session.script(), session.metadata());
        }
        eventBus.postEvent(CaptureSessionStopped.builder().clip(clip).build());
    }

    private void ensureExportFileChooser(String suffix) {
        if (exportFileChooser == null) {
            exportFileChooser = new FileChooser();
            exportFileChooser.setInitialFileName(FileUtils.createFileName(suffix));
            exportFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        }
    }

    private FileChooser prepareExportFileChooser(String suffix) {
        ensureExportFileChooser(suffix);
        exportFileChooser.setTitle("Export");
        if (exportCurrentFile != null) {
            exportFileChooser.setInitialDirectory(exportCurrentFile.getParentFile());
            exportFileChooser.setInitialFileName(FileUtils.createFileName(suffix));
        }
        return exportFileChooser;
    }

    private void ensureBundleFileChooser(String suffix) {
        if (bundleFileChooser == null) {
            bundleFileChooser = new FileChooser();
            bundleFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            bundleFileChooser.setInitialFileName(FileUtils.createFileName(suffix));
        }
    }

    private FileChooser showSaveBundleFileChooser() {
        ensureBundleFileChooser(PROJECT_EXTENSION);
        bundleFileChooser.setTitle("Save");
        if (bundleCurrentFile != null) {
            bundleFileChooser.setInitialDirectory(bundleCurrentFile.getParentFile());
            bundleFileChooser.setInitialFileName(bundleCurrentFile.getName());
//            if (bundleCurrentFile.getName().endsWith(PROJECT_EXTENSION)) {
//                bundleFileChooser.setInitialFileName(bundleCurrentFile.getName());
//            } else {
//                bundleFileChooser.setInitialFileName(FileUtils.createFileName(PROJECT_EXTENSION));
//            }
        }
        return bundleFileChooser;
    }

    private FileChooser showLoadBundleFileChooser() {
        ensureBundleFileChooser(PROJECT_EXTENSION);
        bundleFileChooser.setTitle("Load");
        if (bundleCurrentFile != null) {
            bundleFileChooser.setInitialDirectory(bundleCurrentFile.getParentFile());
            bundleFileChooser.setInitialFileName(bundleCurrentFile.getName());
        }
        return bundleFileChooser;
    }
}
