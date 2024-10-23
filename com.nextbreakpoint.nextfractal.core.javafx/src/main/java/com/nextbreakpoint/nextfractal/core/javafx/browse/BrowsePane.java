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
package com.nextbreakpoint.nextfractal.core.javafx.browse;

import com.nextbreakpoint.nextfractal.core.common.Block;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import com.nextbreakpoint.nextfractal.core.javafx.Icons;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformImageLoader;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridView;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewDelegate;
import com.nextbreakpoint.nextfractal.core.javafx.observable.StringObservableValue;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import lombok.Setter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log
public class BrowsePane extends BorderPane {
    private static final String FILE_EXTENSION = ".nf.zip";

    private final List<BrowseGridViewItem> items = new ArrayList<>();
    private final List<String> filter = new LinkedList<>();
    private final StringObservableValue sourcePathProperty;
    private final StringObservableValue importPathProperty;
    private final ExecutorService executor;
    private final File workspace;
    private final File examples;
    private final GridView<BrowseGridViewItem> gridView;
    private Thread thread;
    @Setter
    private BrowseDelegate delegate;

    public BrowsePane(File workspace, File examples) {
        this.workspace = workspace;
        this.examples = examples;

        filter.add(FILE_EXTENSION);

        sourcePathProperty = new StringObservableValue();

        sourcePathProperty.setValue(null);

        importPathProperty = new StringObservableValue();

        importPathProperty.setValue(null);

        final HBox toolbar1 = new HBox(2);
        toolbar1.setAlignment(Pos.CENTER_LEFT);

        final HBox toolbar2 = new HBox(2);
        toolbar2.setAlignment(Pos.CENTER);

        final HBox toolbar3 = new HBox(2);
        toolbar3.setAlignment(Pos.CENTER_RIGHT);

        final double percentage = Icons.computeOptimalIconPercentage();
        final int buttonSize = (int) Math.rint(Screen.getPrimary().getVisualBounds().getWidth() * percentage);

        final Button closeButton = new Button("", Icons.createSVGIcon("/close.svg", buttonSize));
        final Button deleteButton = new Button("", Icons.createSVGIcon("/remove.svg", buttonSize));
        final Button reloadButton = new Button("", Icons.createSVGIcon("/reload.svg", buttonSize));
        final Button importButton = new Button("", Icons.createSVGIcon("/import.svg", buttonSize));

        closeButton.setTooltip(new Tooltip("Hide projects"));
        deleteButton.setTooltip(new Tooltip("Delete projects"));
        reloadButton.setTooltip(new Tooltip("Reload projects"));
        importButton.setTooltip(new Tooltip("Import projects from directory"));

        final Label statusLabel = new Label("");

        toolbar1.getChildren().add(statusLabel);
        toolbar3.getChildren().add(importButton);
        toolbar3.getChildren().add(deleteButton);
        toolbar3.getChildren().add(reloadButton);
        toolbar3.getChildren().add(closeButton);

        final BorderPane toolbar = new BorderPane();
        toolbar.getStyleClass().add("toolbar");
        toolbar.setLeft(toolbar1);
        toolbar.setCenter(toolbar2);
        toolbar.setRight(toolbar3);

        deleteButton.setDisable(true);

        executor = ExecutorUtils.newFixedThreadPool(15, ThreadUtils.createVirtualThreadFactory("Browser"));

        gridView = new GridView<>(new BrowseGridViewCellFactory(), false, 3);

        gridView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        gridView.getSelectionModel().getSelectedIndices()
                .addListener((ListChangeListener<? super Integer>) change -> deleteButton.setDisable(change.getList().isEmpty()));

        gridView.setDelegate(new GridViewDelegate<>() {
            @Override
            public void onCellSelected(GridView<BrowseGridViewItem> source, int selectedRow, int selectedCol, int itemIndex) {
                if (delegate != null) {
                    if (gridView.getItems() != null) {
                        if (itemIndex >= 0 && itemIndex < gridView.getItems().size()) {
                            final BrowseGridViewItem item = gridView.getItems().get(itemIndex);
                            final File file = item.getFile();
                            if (file != null) {
                                delegate.didSelectFile(BrowsePane.this, file);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCellsUpdated(GridView<BrowseGridViewItem> source) {
            }
        });

        final StackPane box = new StackPane();
        box.getChildren().add(gridView);
        box.getChildren().add(toolbar);
        box.getStyleClass().add("browse");

        setCenter(box);

        closeButton.setOnMouseClicked(_ -> doClose());

        importButton.setOnMouseClicked(_ -> doChooseImportFolder());

        reloadButton.setOnMouseClicked(_ -> {
            final File path = getCurrentSourceFolder();
            deleteButton.setDisable(true);
            loadFiles(statusLabel, gridView, path);
        });

        deleteButton.setOnMouseClicked(_ -> deleteSelected(gridView));

        sourcePathProperty.addListener((_, _, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                reloadFiles(deleteButton, statusLabel, gridView, path);
            }
        });

        importPathProperty.addListener((_, _, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                importFiles(deleteButton, statusLabel, gridView, path);
            }
        });

        widthProperty().addListener((_, _, newValue) -> {
            toolbar1.setPrefWidth(newValue.doubleValue() / 3);
            toolbar2.setPrefWidth(newValue.doubleValue() / 3);
            toolbar3.setPrefWidth(newValue.doubleValue() / 3);
        });

        heightProperty().addListener((_, _, newValue) -> {
            toolbar.setMinHeight(newValue.doubleValue() * 0.07);
            toolbar.setMaxHeight(newValue.doubleValue() * 0.07);
            toolbar.setPrefHeight(newValue.doubleValue() * 0.07);
            toolbar.setTranslateY((newValue.doubleValue() - newValue.doubleValue() * 0.07) / 2);
        });
    }

    public File getCurrentSourceFolder() {
        return workspace;
    }

    private File getDefaultSourceFolder() {
        return new File(System.getProperty("user.home"));
    }

    private File getDefaultImportFolder() {
        return examples;
    }

    public void enable() {
        log.info("Browser enabled");
    }

    public void disable() {
        log.info("Browser disabled");
    }

    public void reload() {
        if (listFiles(workspace).isEmpty()) {
            log.log(Level.INFO, "Workspace is empty");
            importPathProperty.setValue(null);
            Platform.runLater(this::doChooseImportFolder);
        } else {
            Platform.runLater(() -> sourcePathProperty.setValue(getCurrentSourceFolder().getAbsolutePath()));
        }
    }

    public void reload(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid) {
        final File path = getCurrentSourceFolder();
        deleteButton.setDisable(true);
        loadFiles(statusLabel, grid, path);
    }

    private void deleteSelected(GridView<BrowseGridViewItem> gridView) {
        if (gridView.getSelectionModel() != null && gridView.getSelectionModel().getSelectedItems() != null) {
            final List<File> files = gridView.getSelectionModel().getSelectedItems().stream()
                    .map(BrowseGridViewItem::getFile)
                    .toList();
            delegate.didDeleteFiles(files);
        }
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
        stopWatching();
        removeItems();
    }

    private void doClose() {
        delegate.didClose(this);
    }

    private void doChooseSourceFolder() {
        Block.begin(_ -> doSelectSourceFolder(prepareSourceDirectoryChooser())).end().execute();
    }

    private void doChooseImportFolder() {
        Block.begin(_ -> doSelectImportFolder(prepareImportDirectoryChooser())).end().execute();
    }

    private void doSelectSourceFolder(DirectoryChooser sourceDirectoryChooser) {
        Optional.ofNullable(sourceDirectoryChooser.showDialog(BrowsePane.this.getScene().getWindow()))
                .ifPresent(folder -> sourcePathProperty.setValue(folder.getAbsolutePath()));
    }

    private DirectoryChooser prepareSourceDirectoryChooser() {
        final DirectoryChooser sourceDirectoryChooser = new DirectoryChooser();
        sourceDirectoryChooser.setInitialDirectory(getDefaultSourceFolder());
        sourceDirectoryChooser.setTitle("Choose source folder");
        return sourceDirectoryChooser;
    }

    private void doSelectImportFolder(DirectoryChooser importDirectoryChooser) {
        Optional.ofNullable(importDirectoryChooser.showDialog(BrowsePane.this.getScene().getWindow()))
                .ifPresent(folder -> importPathProperty.setValue(folder.getAbsolutePath()));
    }

    private DirectoryChooser prepareImportDirectoryChooser() {
        final DirectoryChooser importDirectoryChooser = new DirectoryChooser();
        importDirectoryChooser.setInitialDirectory(getDefaultImportFolder());
        importDirectoryChooser.setTitle("Choose import folder");
        return importDirectoryChooser;
    }

    private Tile createSingleTile(int width, int height) {
        final Size imageSize = new Size(width, height);
        final Size tileSize = new Size(width, height);
        final Size tileBorder = new Size(0, 0);
        final Point tileOffset = new Point(0, 0);
        return new Tile(imageSize, tileSize, tileOffset, tileBorder);
    }

    private void loadFiles(Label statusLabel, GridView<BrowseGridViewItem> grid, File folder) {
        removeItems();
        grid.setData(List.of());
        final List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            statusLabel.setText(files.size() + " project file" + (files.size() > 1 ? "s" : "") + " found");
            loadItems(grid, files);
        } else {
            statusLabel.setText("No project files found");
        }
    }

    private void importFiles(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid, File folder) {
        final List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            copyFilesAsync(deleteButton, statusLabel, grid, files, getCurrentSourceFolder());
        }
    }

    private Future<?> copyFilesAsync(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid, List<File> files, File dest) {
        return executor.submit(() -> copyFiles(deleteButton, statusLabel, grid, files, dest));
    }

    private void copyFiles(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid, List<File> files, File dest) {
        Platform.runLater(() -> grid.setDisable(true));

        for (File file : files) {
            Platform.runLater(() -> statusLabel.setText("Importing " + files.size() + " files..."));

            copyFile(file, dest);
        }

        Platform.runLater(() -> grid.setDisable(false));

        Platform.runLater(() -> reloadFiles(deleteButton, statusLabel, grid, dest));
    }

    private void reloadFiles(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid, File path) {
        deleteButton.setDisable(true);
        stopWatching();
        startWatching(deleteButton, statusLabel, grid);
        loadFiles(statusLabel, grid, path);
    }

    private void copyFile(File file, File location) {
        FileManager.loadBundle(file)
                .optional()
                .ifPresent(session -> saveFile(session, createFileName(file, location)));
    }

    private void saveFile(Bundle session, File name) {
        FileManager.saveBundle(name, session)
                .observe()
                .onFailure(e -> log.log(Level.WARNING, "Can't save file " + name, e))
                .get();
    }

    private File createFileName(File file, File location) {
        File tmpFile = new File(location, file.getName().substring(0, file.getName().indexOf(".")) + FILE_EXTENSION);
        int i = 0;
        while (tmpFile.exists()) {
            tmpFile = new File(location, file.getName().substring(0, file.getName().indexOf(".")) + "-" + (i++) + FILE_EXTENSION);
        }
        return tmpFile;
    }

    private void loadItems(GridView<BrowseGridViewItem> grid, List<File> files) {
        items.clear();
        for (File file : files) {
            final var imageLoader = new PlatformImageLoader(executor, () -> FileManager.loadBundle(file), this::computeSize);
            items.add(new BrowseGridViewItem(file, imageLoader));
        }
        grid.setData(items);
    }

    private Size computeSize() {
        return new Size((int) Math.rint(getWidth() / 3), (int) Math.rint(getWidth() / 3));
    }

    private List<File> listFiles(File folder) {
        final File[] files = folder.listFiles((dir, name) -> hasSuffix(name));
        if (files == null) {
            return List.of();
        } else {
            return Stream.of(Objects.requireNonNull(files))
                    .sorted().collect(Collectors.toList());
        }
    }

    private boolean hasSuffix(String name) {
        return filter.stream().anyMatch(name::endsWith);
    }

    private void removeItems() {
        gridView.setData(List.of());
        items.clear();
    }

    private void stopWatching() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
    }

    private void startWatching(Button deleteButton, Label statusLabel, GridView<BrowseGridViewItem> grid) {
        if (thread == null) {
            final ThreadFactory threadFactory = ThreadUtils.createPlatformThreadFactory("Watcher");
            final Path path = getCurrentSourceFolder().toPath();
            thread = threadFactory.newThread(() -> watchLoop(path, _ -> reload(deleteButton, statusLabel, grid)));
            thread.start();
        }
    }

    private void watchLoop(Path dir, Consumer<Void> consumer) {
        try {
            for (;;) {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                WatchKey watchKey = null;
                log.log(Level.INFO, "Watch loop starting...");
                try {
                    watchKey = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                    log.log(Level.INFO, "Watch loop started");
                    for (; ; ) {
                        WatchKey key = watcher.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            log.log(Level.INFO, "Watch loop events " + event.count());
                            Platform.runLater(() -> consumer.accept(null));
                        }
                        boolean valid = key.reset();
                        if (!valid) {
                            break;
                        }
                    }
                    log.log(Level.INFO, "Watch loop exited");
                } finally {
                    if (watchKey != null) {
                        watchKey.cancel();
                    }
                    watcher.close();
                }
                Platform.runLater(() -> consumer.accept(null));
            }
        } catch (InterruptedException x) {
            log.log(Level.INFO, "Watch loop interrupted");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            log.log(Level.WARNING, "Can't watch directory " + getCurrentSourceFolder().getAbsolutePath(), e);
        }
    }
}
