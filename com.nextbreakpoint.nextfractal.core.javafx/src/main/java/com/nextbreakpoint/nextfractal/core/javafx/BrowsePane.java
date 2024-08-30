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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.nextfractal.core.common.Block;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.graphics.Point;
import com.nextbreakpoint.nextfractal.core.graphics.Size;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.EXECUTE;

@Log
public class BrowsePane extends BorderPane {
    private static final String FILE_EXTENSION = ".nf.zip";
    private static final int FRAME_LENGTH_IN_MILLIS = 50;
    private static final int SCROLL_BOUNCE_DELAY = 500;

    private final List<GridItem> items = new ArrayList<>();
    private final List<String> filter = new LinkedList<>();
    private final StringObservableValue sourcePathProperty;
    private final StringObservableValue importPathProperty;
    private final ExecutorService executor;
    private final int numRows = 3;
    private final int numCols = 3;
    private final File workspace;
    private final File examples;
    private final Tile tile;
    private Thread thread;
    private AnimationTimer timer;
    @Setter
    private BrowseDelegate delegate;

    public BrowsePane(int width, int height, File workspace, File examples) {
        this.workspace = workspace;
        this.examples = examples;

        setMinWidth(width);
        setMaxWidth(width);
        setPrefWidth(width);
        setMinHeight(height);
        setMaxHeight(height);
        setPrefHeight(height);

        filter.add(FILE_EXTENSION);

        sourcePathProperty = new StringObservableValue();

        sourcePathProperty.setValue(null);

        importPathProperty = new StringObservableValue();

        importPathProperty.setValue(null);

        final int size = width / numCols;

        tile = createSingleTile(size, size);

        final HBox toolbar1 = new HBox(2);
        toolbar1.setAlignment(Pos.CENTER_LEFT);

        final HBox toolbar2 = new HBox(2);
        toolbar2.setAlignment(Pos.CENTER);

        final HBox toolbar3 = new HBox(2);
        toolbar3.setAlignment(Pos.CENTER_RIGHT);

        final Button closeButton = new Button("", Icons.createIconImage("/icon-close.png"));
        final Button deleteButton = new Button("", Icons.createIconImage("/icon-delete.png"));
        final Button reloadButton = new Button("", Icons.createIconImage("/icon-reload.png"));
        final Button importButton = new Button("", Icons.createIconImage("/icon-import.png"));
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
        toolbar.getStyleClass().add("translucent");
        toolbar.setPrefHeight(height * 0.07);
        toolbar.setLeft(toolbar1);
        toolbar.setCenter(toolbar2);
        toolbar.setRight(toolbar3);

        deleteButton.setDisable(true);

        final GridView grid = new GridView(numRows, numCols, size);

        grid.setDelegate(new GridViewDelegate() {
            @Override
            public void didRangeChange(GridView source, int firstRow, int lastRow) {
//				source.updateCells();
            }

            @Override
            public void didCellChange(GridView source, int row, int col) {
                source.updateCell(row * numCols + col);
            }

            @Override
            public void didSelectionChange(GridView source, int selectedRow, int selectedCol, int clicks) {
                final int index = selectedRow * numCols + selectedCol;
                if (index >= 0 && index < items.size()) {
                    final GridItem item = items.get(index);
                    final File file = item.getFile();
                    if (file != null) {
                        if (clicks == 1) {
                            item.setSelected(!item.isSelected());
                            if (items.stream().anyMatch(GridItem::isSelected)) {
                                deleteButton.setDisable(false);
                            } else {
                                deleteButton.setDisable(true);
                            }
                        } else {
                            item.setSelected(false);
                            if (delegate != null) {
                                delegate.didSelectFile(BrowsePane.this, file);
                            }
                        }
                    }
                }
            }
        });

        final BorderPane box = new BorderPane();
        box.setCenter(grid);
        box.setBottom(toolbar);
        box.getStyleClass().add("browse");

        setCenter(box);

        closeButton.setOnMouseClicked(_ -> doClose());

        importButton.setOnMouseClicked(_ -> doChooseImportFolder());

        reloadButton.setOnMouseClicked(_ -> {
            final File path = getCurrentSourceFolder();
            deleteButton.setDisable(true);
            loadFiles(statusLabel, grid, path);
        });

        deleteButton.setOnMouseClicked(_ -> deleteSelected(items));

        sourcePathProperty.addListener((_, _, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                reloadFiles(deleteButton, statusLabel, grid, path);
            }
        });

        importPathProperty.addListener((_, _, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                importFiles(deleteButton, statusLabel, grid, path);
            }
        });

        widthProperty().addListener((_, _, newValue) -> {
            toolbar1.setPrefWidth(newValue.doubleValue() / 3);
            toolbar2.setPrefWidth(newValue.doubleValue() / 3);
            toolbar3.setPrefWidth(newValue.doubleValue() / 3);
        });

        executor = ExecutorUtils.newSingleThreadExecutor(ThreadUtils.createVirtualThreadFactory("Browser Panel"));

        runTimer(grid);
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

    public void reload() {
        if (listFiles(workspace).isEmpty()) {
            log.log(Level.INFO, "Workspace is empty");
            importPathProperty.setValue(null);
            Platform.runLater(this::doChooseImportFolder);
        } else {
            Platform.runLater(() -> sourcePathProperty.setValue(getCurrentSourceFolder().getAbsolutePath()));
        }
    }

    public void reload(Button deleteButton, Label statusLabel, GridView grid) {
        final File path = getCurrentSourceFolder();
        deleteButton.setDisable(true);
        loadFiles(statusLabel, grid, path);
    }

    private void deleteSelected(List<GridItem> items) {
        final List<File> files = items.stream().filter(GridItem::isSelected).map(GridItem::getFile).toList();
        delegate.didDeleteFiles(files);
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

    private void loadFiles(Label statusLabel, GridView grid, File folder) {
        removeItems();
        grid.setData(new GridItem[0]);
        final List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            statusLabel.setText(files.size() + " project file" + (files.size() > 1 ? "s" : "") + " found");
            loadItems(grid, files);
        } else {
            statusLabel.setText("No project files found");
        }
    }

    private void importFiles(Button deleteButton, Label statusLabel, GridView grid, File folder) {
        final List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            copyFilesAsync(deleteButton, statusLabel, grid, files, getCurrentSourceFolder());
        }
    }

    private Future<?> copyFilesAsync(Button deleteButton, Label statusLabel, GridView grid, List<File> files, File dest) {
        return executor.submit(() -> copyFiles(deleteButton, statusLabel, grid, files, dest));
    }

    private void copyFiles(Button deleteButton, Label statusLabel, GridView grid, List<File> files, File dest) {
        Platform.runLater(() -> grid.setDisable(true));

        for (File file : files) {
            Platform.runLater(() -> statusLabel.setText("Importing " + files.size() + " files..."));

            copyFile(file, dest);
        }

        Platform.runLater(() -> grid.setDisable(false));

        Platform.runLater(() -> reloadFiles(deleteButton, statusLabel, grid, dest));
    }

    private void reloadFiles(Button deleteButton, Label statusLabel, GridView grid, File path) {
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

    private void loadItems(GridView grid, List<File> files) {
        final GridItem[] items = new GridItem[files.size()];
        for (int i = 0; i < files.size(); i++) {
            items[i] = new GridItem();
            items[i].setFile(files.get(i));
            this.items.add(items[i]);
        }
        grid.setData(items);
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
        for (int index = 0; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getLoadItemFuture() != null && !item.getLoadItemFuture().isDone()) {
                item.getLoadItemFuture().cancel(true);
            }
        }

        for (int index = 0; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }

//		for (int index = 0; index < items.size(); index++) {
//			final GridItem item = items.get(index);
//			if (item.getRenderer() != null) {
//				item.getRenderer().waitFor();
//			}
//		}

        for (int index = 0; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
//				final GridItemRenderer renderer = item.getRenderer();
                item.setRenderer(null);
//				renderer.dispose();
            }
        }

        items.clear();
    }

    private void runTimer(GridView grid) {
        timer = new AnimationTimer() {
            private long last;

            @Override
            public void handle(long now) {
                final long time = now / 1000000;
                if (time - last > FRAME_LENGTH_IN_MILLIS) {
                    try {
                        updateCells(grid);
                    } catch (ExecutionException | InterruptedException e) {
                        log.log(Level.WARNING, "Can't update cells", e);
                    }
                    last = time;
                }
            }
        };
        timer.start();
    }

    private void updateCells(GridView grid) throws ExecutionException, InterruptedException {
        if (grid.getData() == null) {
            return;
        }
        int firstRow = grid.getFirstRow();
        int lastRow = grid.getLastRow();
        if (firstRow > 0) {
            firstRow -= 1;
        }
        if (lastRow < grid.getData().length / numCols - 1) {
            lastRow += 1;
        }
        final int firstIndex = Math.min(firstRow * numCols, items.size());
        final int lastIndex = lastRow * numCols + numCols;
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            item.setAborted(true);
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            item.setAborted(true);
        }
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            if (item.getLoadItemFuture() != null) {
                item.getLoadItemFuture().get();
                item.setLoadItemFuture(null);
            }
            if (item.getInitItemFuture() != null) {
                item.getInitItemFuture().get();
                item.setInitItemFuture(null);
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getLoadItemFuture() != null) {
                item.getLoadItemFuture().get();
                item.setLoadItemFuture(null);
            }
            if (item.getInitItemFuture() != null) {
                item.getInitItemFuture().get();
                item.setInitItemFuture(null);
            }
        }
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            item.setAborted(false);
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            item.setAborted(false);
        }
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().waitFor();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().waitFor();
            }
        }
        for (int index = 0; index < firstIndex; index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                final GridItemRenderer renderer = item.getRenderer();
                item.setRenderer(null);
                item.setBitmap(null);
                renderer.dispose();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            final GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                final GridItemRenderer renderer = item.getRenderer();
                item.setRenderer(null);
                item.setBitmap(null);
                renderer.dispose();
            }
        }
        for (int index = firstIndex; index < Math.min(lastIndex, items.size()); index++) {
            final GridItem item = items.get(index);
            final BrowseBitmap bitmap = item.getBitmap();
            final GridItemRenderer renderer = item.getRenderer();
            final long time = System.currentTimeMillis();
            if (bitmap == null && time - item.getLastChanged() > SCROLL_BOUNCE_DELAY && item.getLoadItemFuture() == null) {
                loadItemAsync(item);
            }
            if (bitmap != null && renderer == null && time - item.getLastChanged() > SCROLL_BOUNCE_DELAY && item.getInitItemFuture() == null) {
                initItemAsync(item);
            }
        }
        grid.updateCells();
    }

    private void loadItemAsync(GridItem item) {
        final File file = item.getFile();
        item.setLoadItemFuture(executor.submit(() -> {
            loadItem(item, file);
            return null;
        }));
    }

    private void loadItem(GridItem item, File file) {
        try {
            if (!item.isAborted() && delegate != null) {
                final BrowseBitmap bitmap = delegate.createBitmap(file, tile.tileSize());
                Platform.runLater(() -> item.setBitmap(bitmap));
            }
        } catch (Exception e) {
            item.setErrors(List.of(new ScriptError(EXECUTE, 0, 0, 0, 0, e.getMessage())));
            log.log(Level.WARNING, "Can't create bitmap: " + e.getMessage());
        }
    }

    private void initItemAsync(GridItem item) {
        final BrowseBitmap bitmap = item.getBitmap();
        item.setInitItemFuture(executor.submit(() -> {
            initItem(item, bitmap);
            return null;
        }));
    }

    private void initItem(GridItem item, BrowseBitmap bitmap) {
        try {
            if (!item.isAborted() && delegate != null) {
                final GridItemRenderer renderer = delegate.createRenderer(bitmap);
                Platform.runLater(() -> item.setRenderer(renderer));
            }
        } catch (Exception e) {
            item.setErrors(List.of(new ScriptError(EXECUTE, 0, 0, 0, 0, e.getMessage())));
            log.log(Level.WARNING, "Can't initialize renderer", e);
        }
    }

    private void stopWatching() {
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
            thread = null;
        }
    }

    private void startWatching(Button deleteButton, Label statusLabel, GridView grid) {
        if (thread == null) {
            final ThreadFactory threadFactory = ThreadUtils.createPlatformThreadFactory("Watcher");
            final Path path = getCurrentSourceFolder().toPath();
            thread = threadFactory.newThread(() -> watchLoop(path, _ -> reload(deleteButton, statusLabel, grid)));
            thread.start();
        }
    }

    private void watchLoop(Path dir, Consumer<Void> consumer) {
        try {
            for (; ; ) {
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
