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
package com.nextbreakpoint.nextfractal.core.javafx;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.nextfractal.core.common.Block;
import com.nextbreakpoint.nextfractal.core.common.Bundle;
import com.nextbreakpoint.nextfractal.core.common.DefaultThreadFactory;
import com.nextbreakpoint.nextfractal.core.common.FileManager;
import com.nextbreakpoint.nextfractal.core.common.SourceError;
import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BrowsePane extends BorderPane {
    private static final Logger logger = Logger.getLogger(BrowsePane.class.getName());
    private static final int FRAME_LENGTH_IN_MILLIS = 50;
    private static final int SCROLL_BOUNCE_DELAY = 500;
    private final ExecutorService browserExecutor;
    private final StringObservableValue sourcePathProperty;
    private final StringObservableValue importPathProperty;
    private final int numRows = 3;
    private final int numCols = 3;
    private final LinkedList<String> filter = new LinkedList<>();
    private final File workspace;
    private final File examples;
    private List<GridItem> items = new ArrayList<>();
    private BrowseDelegate delegate;
    private RendererTile tile;
    private AnimationTimer timer;
    private Thread thread;

    public BrowsePane(int width, int height, File workspace, File examples) {
        this.workspace = workspace;
        this.examples = examples;

        setMinWidth(width);
        setMaxWidth(width);
        setPrefWidth(width);
        setMinHeight(height);
        setMaxHeight(height);
        setPrefHeight(height);

        filter.add(".nf.zip");

        sourcePathProperty = new StringObservableValue();

        sourcePathProperty.setValue(null);

        importPathProperty = new StringObservableValue();

        importPathProperty.setValue(null);

        int size = width / numCols;

        int maxThreads = numCols;

        tile = createSingleTile(size, size);

        HBox toolbar1 = new HBox(2);
        toolbar1.setAlignment(Pos.CENTER_LEFT);

        HBox toolbar2 = new HBox(2);
        toolbar2.setAlignment(Pos.CENTER);

        HBox toolbar3 = new HBox(2);
        toolbar3.setAlignment(Pos.CENTER_RIGHT);

        Button closeButton = new Button("", Icons.createIconImage("/icon-close.png"));
        Button deleteButton = new Button("", Icons.createIconImage("/icon-delete.png"));
        Button reloadButton = new Button("", Icons.createIconImage("/icon-reload.png"));
        Button importButton = new Button("", Icons.createIconImage("/icon-import.png"));
        closeButton.setTooltip(new Tooltip("Hide projects"));
        deleteButton.setTooltip(new Tooltip("Delete projects"));
        reloadButton.setTooltip(new Tooltip("Reload projects"));
        importButton.setTooltip(new Tooltip("Import projects from directory"));

        Label statusLabel = new Label("");

        toolbar1.getChildren().add(statusLabel);
        toolbar3.getChildren().add(importButton);
        toolbar3.getChildren().add(deleteButton);
        toolbar3.getChildren().add(reloadButton);
        toolbar3.getChildren().add(closeButton);

        BorderPane toolbar = new BorderPane();
        toolbar.getStyleClass().add("toolbar");
        toolbar.getStyleClass().add("translucent");
        toolbar.setPrefHeight(height * 0.07);
        toolbar.setLeft(toolbar1);
        toolbar.setCenter(toolbar2);
        toolbar.setRight(toolbar3);

        deleteButton.setDisable(true);

        GridView grid = new GridView(numRows, numCols, size);

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
                int index = selectedRow * numCols + selectedCol;
                if (index >= 0 && index < items.size()) {
                    GridItem item = items.get(index);
                    File file = item.getFile();
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

        BorderPane box = new BorderPane();
        box.setCenter(grid);
        box.setBottom(toolbar);
        box.getStyleClass().add("browse");

        setCenter(box);

        closeButton.setOnMouseClicked(e -> doClose());

        importButton.setOnMouseClicked(e -> doChooseImportFolder());

        reloadButton.setOnMouseClicked(e -> {
            final File path = getCurrentSourceFolder();
            deleteButton.setDisable(true);
            loadFiles(statusLabel, grid, path);
        });

        deleteButton.setOnMouseClicked(e -> deleteSelected(items));

        sourcePathProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                reloadFiles(deleteButton, statusLabel, grid, path);
            }
        });

        importPathProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                File path = new File(newValue);
                importFiles(deleteButton, statusLabel, grid, path);
            }
        });

        widthProperty().addListener((observable, oldValue, newValue) -> {
            toolbar1.setPrefWidth(newValue.doubleValue() / 3);
            toolbar2.setPrefWidth(newValue.doubleValue() / 3);
            toolbar3.setPrefWidth(newValue.doubleValue() / 3);
        });

        browserExecutor = Executors.newFixedThreadPool(maxThreads, createThreadFactory("Browser"));

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

    private DefaultThreadFactory createThreadFactory(String name) {
        return new DefaultThreadFactory(name, true, Thread.MIN_PRIORITY);
    }

    public void reload() {
        if (listFiles(workspace).isEmpty()) {
            logger.log(Level.INFO, "Workspace is empty");
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
        List<File> files = items.stream().filter(GridItem::isSelected).map(GridItem::getFile).toList();
        delegate.didDeleteFiles(files);
    }

    public void setDelegate(BrowseDelegate delegate) {
        this.delegate = delegate;
    }

    public void dispose() {
        List<ExecutorService> executors = List.of(browserExecutor);
        executors.forEach(ExecutorService::shutdownNow);
        executors.forEach(this::await);
        stopWatching();
        removeItems();
    }

    private void await(ExecutorService executor) {
        Command.of(() -> executor.awaitTermination(5000, TimeUnit.MILLISECONDS))
                .execute()
                .observe()
                .onFailure(e -> logger.warning("Await termination timeout"))
                .get();
    }

    private void doClose() {
        delegate.didClose(this);
    }

    private void doChooseSourceFolder() {
        Block.begin(a -> doSelectSourceFolder(prepareSourceDirectoryChooser())).end().execute();
    }

    private void doChooseImportFolder() {
        Block.begin(a -> doSelectImportFolder(prepareImportDirectoryChooser())).end().execute();
    }

    private void doSelectSourceFolder(DirectoryChooser sourceDirectoryChooser) {
        Optional.ofNullable(sourceDirectoryChooser.showDialog(BrowsePane.this.getScene().getWindow()))
                .ifPresent(folder -> sourcePathProperty.setValue(folder.getAbsolutePath()));
    }

    private DirectoryChooser prepareSourceDirectoryChooser() {
        DirectoryChooser sourceDirectoryChooser = new DirectoryChooser();
        sourceDirectoryChooser.setInitialDirectory(getDefaultSourceFolder());
        sourceDirectoryChooser.setTitle("Choose source folder");
        return sourceDirectoryChooser;
    }

    private void doSelectImportFolder(DirectoryChooser importDirectoryChooser) {
        Optional.ofNullable(importDirectoryChooser.showDialog(BrowsePane.this.getScene().getWindow()))
                .ifPresent(folder -> importPathProperty.setValue(folder.getAbsolutePath()));
    }

    private DirectoryChooser prepareImportDirectoryChooser() {
        DirectoryChooser importDirectoryChooser = new DirectoryChooser();
        importDirectoryChooser.setInitialDirectory(getDefaultImportFolder());
        importDirectoryChooser.setTitle("Choose import folder");
        return importDirectoryChooser;
    }

    private RendererTile createSingleTile(int width, int height) {
        RendererSize imageSize = new RendererSize(width, height);
        RendererSize tileSize = new RendererSize(width, height);
        RendererSize tileBorder = new RendererSize(0, 0);
        RendererPoint tileOffset = new RendererPoint(0, 0);
        return new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
    }

    private void loadFiles(Label statusLabel, GridView grid, File folder) {
        removeItems();
        grid.setData(new GridItem[0]);
        List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            statusLabel.setText(files.size() + " project file" + (files.size() > 1 ? "s" : "") + " found");
            loadItems(grid, files);
        } else {
            statusLabel.setText("No project files found");
        }
    }

    private void importFiles(Button deleteButton, Label statusLabel, GridView grid, File folder) {
        List<File> files = listFiles(folder);
        if (!files.isEmpty()) {
            copyFilesAsync(deleteButton, statusLabel, grid, files, getCurrentSourceFolder());
        }
    }

    private Future<?> copyFilesAsync(Button deleteButton, Label statusLabel, GridView grid, List<File> files, File dest) {
        return browserExecutor.submit(() -> copyFiles(deleteButton, statusLabel, grid, files, dest));
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
                .onFailure(e -> logger.log(Level.WARNING, "Can't save file " + name, e))
                .get();
    }

    private File createFileName(File file, File location) {
        File tmpFile = new File(location, file.getName().substring(0, file.getName().indexOf(".")) + ".nf.zip");
        int i = 0;
        while (tmpFile.exists()) {
            tmpFile = new File(location, file.getName().substring(0, file.getName().indexOf(".")) + "-" + (i++) + ".nf.zip");
        }
        return tmpFile;
    }

    private void loadItems(GridView grid, List<File> files) {
        GridItem[] items = new GridItem[files.size()];
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
            GridItem item = items.get(index);
            if (item.getLoadItemFuture() != null && !item.getLoadItemFuture().isDone()) {
                item.getLoadItemFuture().cancel(true);
            }
        }

        for (int index = 0; index < items.size(); index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }

//		for (int index = 0; index < items.size(); index++) {
//			GridItem item = items.get(index);
//			if (item.getRenderer() != null) {
//				item.getRenderer().waitFor();
//			}
//		}

        for (int index = 0; index < items.size(); index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
//				GridItemRenderer renderer = item.getRenderer();
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
                long time = now / 1000000;
                if (time - last > FRAME_LENGTH_IN_MILLIS) {
                    try {
                        updateCells(grid);
                    } catch (ExecutionException | InterruptedException e) {
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
        int firstIndex = Math.min(firstRow * numCols, items.size());
        int lastIndex = lastRow * numCols + numCols;
        for (int index = 0; index < firstIndex; index++) {
            GridItem item = items.get(index);
            item.setAborted(true);
        }
        for (int index = lastIndex; index < items.size(); index++) {
            GridItem item = items.get(index);
            item.setAborted(true);
        }
        for (int index = 0; index < firstIndex; index++) {
            GridItem item = items.get(index);
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
            GridItem item = items.get(index);
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
            GridItem item = items.get(index);
            item.setAborted(false);
        }
        for (int index = lastIndex; index < items.size(); index++) {
            GridItem item = items.get(index);
            item.setAborted(false);
        }
        for (int index = 0; index < firstIndex; index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().abort();
            }
        }
        for (int index = 0; index < firstIndex; index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().waitFor();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                item.getRenderer().waitFor();
            }
        }
        for (int index = 0; index < firstIndex; index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                GridItemRenderer renderer = item.getRenderer();
                item.setRenderer(null);
                item.setBitmap(null);
                renderer.dispose();
            }
        }
        for (int index = lastIndex; index < items.size(); index++) {
            GridItem item = items.get(index);
            if (item.getRenderer() != null) {
                GridItemRenderer renderer = item.getRenderer();
                item.setRenderer(null);
                item.setBitmap(null);
                renderer.dispose();
            }
        }
        for (int index = firstIndex; index < Math.min(lastIndex, items.size()); index++) {
            GridItem item = items.get(index);
            BrowseBitmap bitmap = item.getBitmap();
            GridItemRenderer renderer = item.getRenderer();
            long time = System.currentTimeMillis();
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
        item.setLoadItemFuture(browserExecutor.submit(() -> {
            loadItem(item, file);
            return null;
        }));
    }

    private void loadItem(GridItem item, File file) {
        try {
            if (!item.isAborted() && delegate != null) {
                final BrowseBitmap bitmap = delegate.createBitmap(file, tile.getTileSize());

                Platform.runLater(() -> item.setBitmap(bitmap));
            }
        } catch (Exception e) {
            item.setErrors(Arrays.asList(new SourceError(SourceError.ErrorType.RUNTIME, 0, 0, 0, 0, e.getMessage())));
            logger.log(Level.WARNING, "Can't create bitmap: " + e.getMessage());
        }
    }

    private void initItemAsync(GridItem item) {
        final BrowseBitmap bitmap = item.getBitmap();
        item.setInitItemFuture(browserExecutor.submit(() -> {
            initItem(item, bitmap);
            return null;
        }));
    }

    private void initItem(GridItem item, BrowseBitmap bitmap) {
        try {
            if (!item.isAborted() && delegate != null) {
                GridItemRenderer renderer = delegate.createRenderer(bitmap);

                Platform.runLater(() -> item.setRenderer(renderer));
            }
        } catch (Exception e) {
            item.setErrors(List.of(new SourceError(SourceError.ErrorType.RUNTIME, 0, 0, 0, 0, e.getMessage())));
            logger.log(Level.WARNING, "Can't initialize renderer", e);
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
            thread = createThreadFactory("Watcher").newThread(() -> watchLoop(getCurrentSourceFolder().toPath(), a -> Platform.runLater(() -> this.reload(deleteButton, statusLabel, grid))));
            thread.start();
        }
    }

    private void watchLoop(Path dir, Consumer<Void> consumer) {
        try {
            for (; ; ) {
                WatchService watcher = FileSystems.getDefault().newWatchService();

                WatchKey watchKey = null;

                logger.log(Level.INFO, "Watch loop starting...");

                try {
                    watchKey = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

                    logger.log(Level.INFO, "Watch loop started");

                    for (; ; ) {
                        WatchKey key = watcher.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            logger.log(Level.INFO, "Watch loop events " + event.count());

                            consumer.accept(null);
                        }

                        boolean valid = key.reset();

                        if (!valid) {
                            break;
                        }
                    }

                    logger.log(Level.INFO, "Watch loop exited");
                } finally {
                    if (watchKey != null) {
                        watchKey.cancel();
                    }

                    watcher.close();
                }

                consumer.accept(null);
            }
        } catch (InterruptedException x) {
            logger.log(Level.INFO, "Watch loop interrupted");
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Can't watch directory " + getCurrentSourceFolder().getAbsolutePath(), e);
        }
    }
}
