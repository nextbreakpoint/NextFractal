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
package com.nextbreakpoint.nextfractal.core.javafx.grid;

import com.nextbreakpoint.nextfractal.core.common.ExecutorUtils;
import com.nextbreakpoint.nextfractal.core.common.ThreadUtils;
import com.nextbreakpoint.nextfractal.core.javafx.grid.internal.GridViewFocusModel;
import com.nextbreakpoint.nextfractal.core.javafx.grid.internal.GridViewSelectionModel;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
public class GridView<T extends GridViewItem> extends Pane {
    private static final int CACHED = 4;
    private static final int EXTRA = 1;
    private static final int SCROLL_DIRECTION_UP = 0;
    private static final int SCROLL_DIRECTION_DOWN = 1;
    private static final int SCROLL_DIRECTION_LEFT = 2;
    private static final int SCROLL_DIRECTION_RIGHT = 3;
    private final GridViewCellFactory factory;
    private final boolean horizontal;
    private final int numCells;
    private final ScheduledExecutorService executor;
    private final ObjectProperty<ObservableList<T>> items;
    private final ObjectProperty<GridViewFocusModel<T>> focusModel;
    private final ObjectProperty<GridViewSelectionModel<T>> selectionModel;
    private final InvalidationListener itemsObserver;
    private final ListChangeListener<T> itemsContentListener = _ -> reset();
    private final WeakListChangeListener<T> weakItemsContentListener = new WeakListChangeListener<>(itemsContentListener);
    private ScheduledFuture<?> scheduledFuture;
    private GridViewCell[] cells;
    private volatile int scrollDirection;
    private int cellSize;
    private int numRows;
    private int numCols;
    private double offsetX;
    private double offsetY;
    private double prevOffsetX;
    private double prevOffsetY;
    @Getter
    @Setter
    private GridViewDelegate<T> delegate;

    public GridView(GridViewCellFactory factory, boolean horizontal, int numCells) {
        this.factory = factory;
        this.horizontal = horizontal;
        this.numCells = numCells;

        executor = ExecutorUtils.newSingleThreadScheduledExecutor(ThreadUtils.createVirtualThreadFactory("Grid View"));

        getStyleClass().add("grid-view");

        setFocusTraversable(true);

        items = new SimpleObjectProperty<>(this, "items");
        focusModel = new SimpleObjectProperty<>(this, "focusModel");
        selectionModel = new SimpleObjectProperty<>(this, "selectionModel");

        items.set(FXCollections.observableArrayList());

        focusModel.set(new GridViewFocusModel<>(this));

        selectionModel.set(new GridViewSelectionModel<T>(this) {
            @Override
            protected T getModelItem(int index) {
                return items.get().get(index);
            }

            @Override
            protected Integer getItemCount() {
                return items.get().size();
            }
        });

        itemsObserver = new InvalidationListener() {
            private WeakReference<ObservableList<T>> weakItemsRef = new WeakReference<>(items.get());

            @Override
            public void invalidated(Observable observable) {
                ObservableList<T> oldItems = weakItemsRef.get();
                weakItemsRef = new WeakReference<>(items.get());
                updateItemsObserver(oldItems, items.get());
            }
        };
        items.addListener(new WeakInvalidationListener(itemsObserver));

        if (items.get() != null) {
            items.get().addListener(weakItemsContentListener);
        }

        focusModel.addListener((_, _, _) -> refreshCells());

        selectionModel.get().getSelectedIndices()
                .addListener((ListChangeListener<? super Integer>) _ -> refreshCells());

        addEventHandler(ScrollEvent.SCROLL_STARTED, this::onScroll);
        addEventHandler(ScrollEvent.SCROLL_FINISHED, this::onScroll);
        addEventHandler(ScrollEvent.SCROLL, this::onScroll);

        addEventHandler(MouseEvent.MOUSE_CLICKED, this::onMouseClicked);

        addEventHandler(KeyEvent.KEY_PRESSED, this::onKeyPressed);
        addEventHandler(KeyEvent.KEY_RELEASED, this::onKeyReleased);
        setOnMouseEntered(_ -> requestFocus());

        widthProperty().addListener((_, _, _) -> initialize());
        heightProperty().addListener((_, _, _) -> initialize());

        initialize();
    }

    public void dispose() {
        ExecutorUtils.shutdown(executor);
    }

    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    public final FocusModel<T> getFocusModel() {
        return focusModel == null ? null : focusModel.get();
    }

    public final MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel == null ? null : selectionModel.get();
    }

    public void setData(List<T> data) {
        items.get().clear();
        items.get().addAll(data);
        reset();
    }

    private void onMouseClicked(MouseEvent mouseEvent) {
        mouseEvent.consume();

        final int clicks = mouseEvent.getClickCount();

        final int selectedCol = (int) Math.abs((mouseEvent.getX() - offsetX - getInsets().getLeft()) / cellSize);
        final int selectedRow = (int) Math.abs((mouseEvent.getY() - offsetY - getInsets().getTop()) / cellSize);

        selectCell(selectedRow, selectedCol, clicks, !mouseEvent.isShiftDown());
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        keyEvent.consume();

        switch (keyEvent.getCode()) {
            case UP -> scrollDirection = SCROLL_DIRECTION_UP;
            case DOWN -> scrollDirection = SCROLL_DIRECTION_DOWN;
            case LEFT -> scrollDirection = SCROLL_DIRECTION_LEFT;
            case RIGHT -> scrollDirection = SCROLL_DIRECTION_RIGHT;
            default -> scrollDirection = -1;
        }

        if (scrollDirection < 0) {
            return;
        }

        final boolean clearSelection = !keyEvent.isShiftDown();

        scroll(clearSelection);

        if (scheduledFuture == null) {
            scheduledFuture = executor.scheduleAtFixedRate(() -> scroll(clearSelection), 200, 50, TimeUnit.MILLISECONDS);
        }
    }

    private void onKeyReleased(KeyEvent keyEvent) {
        keyEvent.consume();

        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            try {
                scheduledFuture.get();
            } catch (CancellationException e) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.warning("Can't complete task");
            } finally {
                scheduledFuture = null;
            }
        }
    }

    private void onScroll(ScrollEvent scrollEvent) {
        scrollEvent.consume();

        final double deltaX = scrollEvent.getDeltaX();
        final double deltaY = scrollEvent.getDeltaY();

        scrollCells(deltaX, deltaY);
    }

    private void updateItemsObserver(ObservableList<T> oldList, ObservableList<T> newList) {
        if (oldList != null) oldList.removeListener(weakItemsContentListener);
        if (newList != null) newList.addListener(weakItemsContentListener);
    }

    private String getDirection() {
        return switch (scrollDirection) {
            case SCROLL_DIRECTION_UP -> "UP";
            case SCROLL_DIRECTION_DOWN -> "DOWN";
            case SCROLL_DIRECTION_LEFT -> "LEFT";
            case SCROLL_DIRECTION_RIGHT -> "RIGHT";
            default -> throw new IllegalStateException("Not valid direction");
        };
    }

    private int getFirstRow() {
        return (int) Math.abs(offsetY / cellSize);
    }

    private int getLastRow() {
        return getFirstRow() + numRows;
    }

    private int getFirstCol() {
        return (int) Math.abs(offsetX / cellSize);
    }

    private int getLastCol() {
        return getFirstCol() + numCols;
    }

    private int getSize() {
        return items.get().size();
    }

    private boolean isEmpty() {
        return getSize() == 0;
    }

    private double getInnerHeight() {
        return getHeight() - getInsets().getTop() - getInsets().getBottom();
    }

    private double getInnerWidth() {
        return getWidth() - getInsets().getLeft() - getInsets().getRight();
    }

    private void initialize() {
        final double left = getInsets().getLeft();
        final double top = getInsets().getTop();
        final double innerWidth = getInnerWidth();
        final double innerHeight = getInnerHeight();

        if (innerWidth > 0 && innerHeight > 0) {
            final int oldCellSize = cellSize;
            final int oldNumRows = numRows;
            final int oldNumCols = numCols;

            if (horizontal) {
                numRows = numCells;
                cellSize = (int) Math.rint(innerHeight / numRows);
                numCols = (int) Math.ceil(innerWidth / cellSize);
            } else {
                numCols = numCells;
                cellSize = (int) Math.rint(innerWidth / numCols);
                numRows = (int) Math.ceil(innerHeight / cellSize);
            }

            if (oldCellSize != cellSize || oldNumRows != numRows || oldNumCols != numCols) {
                cells = createCells();
            }

            setClip(new Rectangle(left, top, innerWidth, innerHeight));

            scrollCells(0, 0);
        }
    }

    private void reset() {
        resetSelection();
        resetScroll();
        updateCells();
    }

    private void resetSelection() {
        selectionModel.get().clearSelection();
    }

    private void resetScroll() {
        offsetX = 0;
        offsetY = 0;
        prevOffsetX = 0;
        prevOffsetY = 0;
    }

    private int getItemIndex(int row, int col) {
        if (horizontal) {
            return col * numRows + row;
        } else {
            return row * numCols + col;
        }
    }

    private int getCellIndex(int row, int col) {
        return getItemIndex(row - getFirstRow(), col - getFirstCol());
    }

    private int getFirstIndex() {
        final ObservableList<T> itemsList = items.get();
        if (horizontal) {
            int firstCol = getFirstCol();
            for (int k = 0; k < CACHED; k++) {
                if (firstCol > 0) {
                    firstCol -= 1;
                } else {
                    break;
                }
            }
            return Math.min(firstCol * numRows, itemsList.size());
        } else {
            int firstRow = getFirstRow();
            for (int k = 0; k < CACHED; k++) {
                if (firstRow > 0) {
                    firstRow -= 1;
                } else {
                    break;
                }
            }
            return Math.min(firstRow * numCols, itemsList.size());
        }
    }

    private int getLastIndex() {
        final ObservableList<T> itemsList = items.get();
        if (horizontal) {
            int lastCol = getLastCol();
            for (int k = 0; k < CACHED; k++) {
                if (lastCol < getSize() / numRows - 1) {
                    lastCol += 1;
                } else {
                    break;
                }
            }
            return Math.min(lastCol * numRows + numRows, itemsList.size());
        } else {
            int lastRow = getLastRow();
            for (int k = 0; k < CACHED; k++) {
                if (lastRow < getSize() / numCols - 1) {
                    lastRow += 1;
                } else {
                    break;
                }
            }
            return Math.min(lastRow * numCols + numCols, itemsList.size());
        }
    }

    private GridViewCell[] createCells() {
        log.info("Recreate cells");

        final GridViewCell[] oldCells = cells;

        final GridViewCell[] cells = new GridViewCell[horizontal ? (numCols + EXTRA) * numRows : (numRows + EXTRA) * numCols];

        if (oldCells == null) {
            for (int i = 0; i < cells.length; i++) {
                cells[i] = factory.createCell(i, cellSize, cellSize);
            }
        } else {
            if (cells.length > oldCells.length) {
                log.info("Increase size of cells array");

                System.arraycopy(oldCells, 0, cells, 0, oldCells.length);
                for (int i = oldCells.length; i < cells.length; i++) {
                    cells[i] = factory.createCell(i, cellSize, cellSize);
                }
            }

            if (cells.length < oldCells.length) {
                log.info("Decrease size of cells array");

                System.arraycopy(oldCells, 0, cells, 0, cells.length);
            }
        }

        getChildren().clear();

        for (GridViewCell cell : cells) {
            getChildren().add(cell);
        }

        return cells;
    }

    private void updateCells() {
        try {
            if (items != null && !items.get().isEmpty()) {
                final int firstIndex = getFirstIndex();
                final int lastIndex = getLastIndex();
                final ObservableList<T> itemsList = items.get();
                if (offsetX > prevOffsetX) {
                    for (int index = lastIndex; index < itemsList.size(); index++) {
                        final GridViewItem item = itemsList.get(index);
                        item.cancel();
                    }
                    for (int index = firstIndex - 1; index >= 0; index--) {
                        final GridViewItem item = itemsList.get(index);
                        item.cancel();
                    }
                    for (int index = lastIndex; index < itemsList.size(); index++) {
                        final GridViewItem item = itemsList.get(index);
                        item.waitFor();
                    }
                    for (int index = firstIndex - 1; index >= 0; index--) {
                        final GridViewItem item = itemsList.get(index);
                        item.waitFor();
                    }
                } else {
                    for (int index = firstIndex - 1; index >= 0; index--) {
                        final GridViewItem item = itemsList.get(index);
                        item.cancel();
                    }
                    for (int index = lastIndex; index < itemsList.size(); index++) {
                        final GridViewItem item = itemsList.get(index);
                        item.cancel();
                    }
                    for (int index = firstIndex - 1; index >= 0; index--) {
                        final GridViewItem item = itemsList.get(index);
                        item.waitFor();
                    }
                    for (int index = lastIndex; index < itemsList.size(); index++) {
                        final GridViewItem item = itemsList.get(index);
                        item.waitFor();
                    }
                }

                if (horizontal) {
                    final int firstCol = getFirstCol();
                    if (offsetX > prevOffsetX) {
                        for (int col = 0; col < numCols + EXTRA; col++) {
                            for (int row = 0; row < numRows; row++) {
                                resetCell(row, col);
                            }
                        }
                        for (int col = 0; col < numCols + EXTRA; col++) {
                            for (int row = 0; row < numRows; row++) {
                                updateCell(row, col, firstCol);
                            }
                        }
                    } else {
                        for (int col = numCols + EXTRA - 1; col >= 0; col--) {
                            for (int row = 0; row < numRows; row++) {
                                resetCell(row, col);
                            }
                        }
                        for (int col = numCols + EXTRA - 1; col >= 0; col--) {
                            for (int row = 0; row < numRows; row++) {
                                updateCell(row, col, firstCol);
                            }
                        }
                    }
                } else {
                    final int firstRow = getFirstRow();
                    if (offsetY > prevOffsetY) {
                        for (int row = numRows + EXTRA - 1; row >= 0; row--) {
                            for (int col = 0; col < numCols; col++) {
                                resetCell(row, col);
                            }
                        }
                        for (int row = numRows + EXTRA - 1; row >= 0; row--) {
                            for (int col = 0; col < numCols; col++) {
                                updateCell(row, col, firstRow);
                            }
                        }
                    } else {
                        for (int row = 0; row < numRows + EXTRA; row++) {
                            for (int col = 0; col < numCols; col++) {
                                resetCell(row, col);
                            }
                        }
                        for (int row = 0; row < numRows + EXTRA; row++) {
                            for (int col = 0; col < numCols; col++) {
                                updateCell(row, col, firstRow);
                            }
                        }
                    }
                }
            } else {
                clearCells();
            }

            if (delegate != null) {
                delegate.onCellsUpdated(this);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.log(Level.WARNING, "Can't render grid", e);
        }
    }

    private void clearCells() {
        if (horizontal) {
            if (offsetX > prevOffsetX) {
                for (int col = 0; col < numCols + EXTRA; col++) {
                    for (int row = 0; row < numRows; row++) {
                        resetCell(row, col);
                    }
                }
            } else {
                for (int col = numCols + EXTRA - 1; col >= 0; col--) {
                    for (int row = 0; row < numRows; row++) {
                        resetCell(row, col);
                    }
                }
            }
        } else {
            if (offsetY > prevOffsetY) {
                for (int row = numRows + EXTRA - 1; row >= 0; row--) {
                    for (int col = 0; col < numCols; col++) {
                        resetCell(row, col);
                    }
                }
            } else {
                for (int row = 0; row < numRows + EXTRA; row++) {
                    for (int col = 0; col < numCols; col++) {
                        resetCell(row, col);
                    }
                }
            }
        }
    }

    private void refreshCells() {
        if (horizontal) {
            final int firstCol = getFirstCol();
            if (offsetX > prevOffsetX) {
                for (int col = 0; col < numCols + EXTRA; col++) {
                    for (int row = 0; row < numRows; row++) {
                        refreshCell(row, col, firstCol);
                    }
                }
            } else {
                for (int col = numCols + EXTRA - 1; col >= 0; col--) {
                    for (int row = 0; row < numRows; row++) {
                        refreshCell(row, col, firstCol);
                    }
                }
            }
        } else {
            final int firstRow = getFirstRow();
            if (offsetY > prevOffsetY) {
                for (int row = numRows + EXTRA - 1; row >= 0; row--) {
                    for (int col = 0; col < numCols; col++) {
                        refreshCell(row, col, firstRow);
                    }
                }
            } else {
                for (int row = 0; row < numRows + EXTRA; row++) {
                    for (int col = 0; col < numCols; col++) {
                        refreshCell(row, col, firstRow);
                    }
                }
            }
        }
    }

    private void resetCell(int row, int col) {
        if (horizontal) {
            final GridViewCell cell = cells[col * numRows + row];
            cell.unbindItem();
            cell.setSelected(false);
            cell.setFocus(false);
            cell.setDirty(true);
            cell.update();
        } else {
            final GridViewCell cell = cells[row * numCols + col];
            cell.unbindItem();
            cell.setSelected(false);
            cell.setFocus(false);
            cell.setDirty(true);
            cell.update();
        }
    }

    private void updateCell(int row, int col, int first) {
        final ObservableList<T> itemsList = items.get();
        if (horizontal) {
            final GridViewCell cell = cells[col * numRows + row];
            cell.setLayoutX(getInsets().getLeft() + col * cellSize + (offsetX - ((int) (offsetX / (cellSize))) * cellSize));
            cell.setLayoutY(getInsets().getTop() + row * cellSize + (offsetY - ((int) (offsetY / (cellSize))) * cellSize));
            final int index = (first + col) * numRows + row;
            if (!itemsList.isEmpty() && index < itemsList.size()) {
                final GridViewItem item = itemsList.get(index);
                cell.bindItem(item);
                cell.setSelected(selectionModel.get().isSelected(index));
                cell.setFocus(focusModel.get().isFocused(index));
                cell.setDirty(true);
                item.run();
                cell.update();
            }
        } else {
            final GridViewCell cell = cells[row * numCols + col];
            cell.setLayoutX(getInsets().getLeft() + col * cellSize + (offsetX - ((int) (offsetX / (cellSize))) * cellSize));
            cell.setLayoutY(getInsets().getTop() + row * cellSize + (offsetY - ((int) (offsetY / (cellSize))) * cellSize));
            final int index = (first + row) * numCols + col;
            if (!itemsList.isEmpty() && index < itemsList.size()) {
                final GridViewItem item = itemsList.get(index);
                cell.bindItem(item);
                cell.setSelected(selectionModel.get().isSelected(index));
                cell.setFocus(focusModel.get().isFocused(index));
                cell.setDirty(true);
                item.run();
                cell.update();
            }
        }
    }

    private void refreshCell(int row, int col, int first) {
        if (horizontal) {
            final int index = (first + col) * numRows + row;
            final GridViewCell cell = cells[col * numRows + row];
            cell.setSelected(selectionModel.get().isSelected(index));
            cell.setFocus(focusModel.get().isFocused(index));
            cell.setDirty(true);
            cell.update();
        } else {
            final int index = (first + row) * numCols + col;
            final GridViewCell cell = cells[row * numCols + col];
            cell.setSelected(selectionModel.get().isSelected(index));
            cell.setFocus(focusModel.get().isFocused(index));
            cell.setDirty(true);
            cell.update();
        }
    }

    private void scroll(boolean clearSelection) {
        Platform.runLater(() -> {
            log.log(Level.INFO, "scroll direction {0}", getDirection());

            final int index = getFocusModel().getFocusedIndex();

            final int newIndex = moveIndex(index);

            if (newIndex < 0) {
                return;
            }

            scrollToIndex(newIndex);

            getFocusModel().focus(newIndex);

            if (clearSelection) {
                selectionModel.get().clearSelection();
            }

            getSelectionModel().selectIndices(newIndex);

            updateCells();
            prevOffsetX = offsetX;
            prevOffsetY = offsetY;
        });
    }

    private int moveIndex(int index) {
        final ObservableList<T> itemsList = items.get();

        log.log(Level.INFO, "current index {0}", index);

        if (index < 0) {
            if (horizontal) {
                switch (scrollDirection) {
                    case SCROLL_DIRECTION_UP, SCROLL_DIRECTION_LEFT -> index = itemsList.size() - 1;
                    case SCROLL_DIRECTION_DOWN, SCROLL_DIRECTION_RIGHT -> index = 0;
                }
            } else {
                switch (scrollDirection) {
                    case SCROLL_DIRECTION_UP, SCROLL_DIRECTION_LEFT -> index = itemsList.size() - 1;
                    case SCROLL_DIRECTION_DOWN, SCROLL_DIRECTION_RIGHT -> index = 0;
                }
            }
        } else {
            if (horizontal) {
                switch (scrollDirection) {
                    case SCROLL_DIRECTION_UP -> index = Math.max(index - 1, 0);
                    case SCROLL_DIRECTION_DOWN -> index = Math.min(index + 1, itemsList.size() - 1);
                    case SCROLL_DIRECTION_LEFT -> index = Math.max(index - numRows, index % numRows);
                    case SCROLL_DIRECTION_RIGHT -> index = Math.min(index + numRows, (itemsList.size() / numRows) * numRows + itemsList.size() % numRows - 1);
                }
            } else {
                switch (scrollDirection) {
                    case SCROLL_DIRECTION_UP -> index = Math.max(index - numCols, index % numCols);
                    case SCROLL_DIRECTION_DOWN -> index = Math.min(index + numCols, (itemsList.size() / numCols) * numCols + itemsList.size() % numCols - 1);
                    case SCROLL_DIRECTION_LEFT -> index = Math.max(index - 1, 0);
                    case SCROLL_DIRECTION_RIGHT -> index = Math.min(index + 1, itemsList.size() - 1);
                }
            }
        }

        return index;
    }

    private void scrollToIndex(int index) {
        final ObservableList<T> itemsList = items.get();

        log.log(Level.INFO, "scroll to index {0}", index);

        if (horizontal) {
            int col = index / numRows;
            if (col * cellSize < Math.abs(offsetX)) {
                offsetX = -col * cellSize;
                offsetX = Math.min(offsetX, 0);
            } else if (col * cellSize > cellSize * (numCols - 1) + Math.abs(offsetX)) {
                final int x = computeMaximumScrollX(itemsList);
                offsetX = -(col - 2) * cellSize;
                offsetX = Math.max(offsetX, -x);
            }
        } else {
            int row = index / numCols;
            if (row * cellSize < Math.abs(offsetY)) {
                offsetY = -row * cellSize;
                offsetY = Math.min(offsetY, 0);
            } else if (row * cellSize > cellSize * (numRows - 1) + Math.abs(offsetY)) {
                final int y = computeMaximumScrollY(itemsList);
                offsetY = -(row - 2) * cellSize;
                offsetY = Math.max(offsetY, -y);
            }
        }
    }

    private void scrollCells(double deltaX, double deltaY) {
        if (items == null) {
            return;
        }

        final ObservableList<T> itemsList = items.get();

        if (itemsList.isEmpty()) {
            return;
        }

        if (horizontal) {
            offsetY = 0;
            final int x = computeMaximumScrollX(itemsList);
            if (x > 0) {
                offsetX += deltaX;
                offsetX = Math.min(Math.max(offsetX, -x), 0);
            }
        } else {
            offsetX = 0;
            final int y = computeMaximumScrollY(itemsList);
            if (y > 0) {
                offsetY += deltaY;
                offsetY = Math.min(Math.max(offsetY, -y), 0);
            }
        }

        updateCells();
        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
    }

    private int computeMaximumScrollY(ObservableList<T> itemsList) {
        return (itemsList.size() / numCols) * cellSize + (itemsList.size() % numCols > 0 ? cellSize : 0) - (int) getInnerHeight();
    }

    private int computeMaximumScrollX(ObservableList<T> itemsList) {
        return (itemsList.size() / numRows) * cellSize + (itemsList.size() % numRows > 0 ? cellSize : 0) - (int) getInnerWidth();
    }

    private void selectCell(int selectedRow, int selectedCol, int clicks, boolean clearSelection) {
        log.log(Level.INFO, "selectedRow = {0}, selectedCol = {1}", new Object[] {selectedRow, selectedCol});

        final int cellIndex = getCellIndex(selectedRow, selectedCol);

        log.log(Level.INFO, "cellIndex = {0}", cellIndex);

        if (cellIndex >= 0 && cellIndex < cells.length) {
            final int itemIndex = getItemIndex(selectedRow, selectedCol);

            log.log(Level.INFO, "itemIndex = {0}", itemIndex);

            if (itemIndex < items.get().size()) {
                if (clearSelection) {
                    selectionModel.get().clearSelection();
                }

                if (clicks == 1) {
                    if (selectionModel.get().isSelected(itemIndex)) {
                        log.log(Level.INFO, "clear index = {0}", itemIndex);

                        selectionModel.get().clearSelection(itemIndex);
                    } else {
                        log.log(Level.INFO, "select index = {0}", itemIndex);

                        selectionModel.get().selectIndices(itemIndex);
                    }
                } else {
                    log.log(Level.INFO, "clear index = {0}", itemIndex);

                    selectionModel.get().clearSelection(itemIndex);
                }

                refreshCells();

                if (delegate != null && clicks > 1) {
                    delegate.onCellSelected(GridView.this, selectedRow, selectedCol, itemIndex);
                }
            }
        }
    }
}
