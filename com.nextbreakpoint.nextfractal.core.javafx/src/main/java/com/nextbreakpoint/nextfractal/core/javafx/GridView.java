/*
 * NextFractal 2.2.0
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

import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;

public class GridView extends Pane {
	private final GridViewCell[] cells;
	private GridViewDelegate delegate;
	protected final int cellSize;
	private Object[] data;
	private int numExtraRows = 2;
	protected int selectedRow = -1;
	protected int selectedCol = -1;
	protected int numRows;
	protected int numCols;
	private double offsetX;
	private double offsetY;
	private double prevOffsetY;
	private double prevOffsetX;

	public GridView(int numRows, int numCols, int cellSize) {
		this.numRows = numRows;
		this.numCols = numCols;
		this.cellSize = cellSize;
		getStyleClass().add("grid-view");

		cells = new GridViewCell[(numRows + numExtraRows) * numCols];

		for (int i = 0; i < cells.length; i++) {
			GridViewCell cell = createCell(i);
			cell.setMaxWidth(cellSize);
			cell.setMaxHeight(cellSize);
			cell.setMinWidth(cellSize);
			cell.setMinHeight(cellSize);
			cell.setPrefWidth(cellSize);
			cell.setPrefHeight(cellSize);
			getChildren().add(cell);
			cells[i] = cell;
		}

		addEventFilter(ScrollEvent.SCROLL_STARTED,
				scrollEvent -> scrollCells(scrollEvent.getDeltaX(), scrollEvent.getDeltaY()));

		addEventFilter(ScrollEvent.SCROLL_FINISHED,
				scrollEvent -> scrollCells(scrollEvent.getDeltaX(), scrollEvent.getDeltaY()));

		addEventFilter(ScrollEvent.SCROLL,
				scrollEvent -> scrollCells(scrollEvent.getDeltaX(), scrollEvent.getDeltaY()));

		addEventFilter(MouseEvent.MOUSE_CLICKED,
				mouseEvent -> {
                    selectedCol = (int)Math.abs((mouseEvent.getX() - offsetX) / cellSize);
                    selectedRow = (int)Math.abs((mouseEvent.getY() - offsetY) / cellSize);
                    if (delegate != null) {
                        delegate.didSelectionChange(GridView.this, selectedRow, selectedCol, mouseEvent.getClickCount());
                    }
                });

		widthProperty().addListener((observable, oldValue, newValue) -> {
            resetScroll();
            updateRows();
        });

		heightProperty().addListener((observable, oldValue, newValue) -> {
            resetScroll();
            updateRows();
        });
	}

	private void updateRows() {
		int firstRow = (int)Math.abs(offsetY / cellSize);
		int lastRow = firstRow + numRows;
		for (int row = 0; row < numRows + numExtraRows; row++) {
			for (int col = 0; col < numCols; col++) {
				GridViewCell cell = cells[row * numCols + col];
				cell.setLayoutY(row * cellSize + (offsetY - ((int)(offsetY / cellSize)) * cellSize));
				cell.setLayoutX(col * cellSize + (offsetX - ((int)(offsetX / cellSize)) * cellSize));
				int index = (firstRow + row) * numCols + col;
				if (data != null && index < data.length) {
					cell.setData(data[index]);
				} else {
					cell.setData(null);
				}
				if (delegate != null) {
					delegate.didCellChange(this, row, col);
				}
			}
		}
		if (delegate != null) {
			delegate.didRangeChange(this, firstRow, lastRow);
		}
	}

//	private void layoutCells() {
//		for (int row = 0; row < numRows + numExtraRows; row++) {
//			for (int col = 0; col < numCols; col++) {
//				GridViewCell cell = cells[row * numCols + col];
//				cell.setLayoutY(row * cellSize + (offsetY - ((int)(offsetY / cellSize)) * cellSize));
//				cell.setLayoutX(col * cellSize + (offsetX - ((int)(offsetX / cellSize)) * cellSize));
//			}
//		}
//	}

	private void resetScroll() {
		offsetX = 0;
		offsetY = 0;
		prevOffsetX = 0;
		prevOffsetY = 0;
	}

	private void scrollCells(double deltaX, double deltaY) {
		if (data != null) {
			double y = (data.length / numCols) * cellSize - Math.min(numRows * cellSize, getHeight()) + (data.length % numCols > 0 ? cellSize : 0);
			double x = numCols * cellSize - Math.min(numCols * cellSize, getWidth());
			if (y > 0) {
				offsetY += deltaY;
				if (offsetY < -y) {
					offsetY = -y;
				}
				if (offsetY > 0) {
					offsetY = 0;
				}
			}
			if (x > 0) {
				offsetX += deltaX;
				if (offsetX < -x) {
					offsetX = -x;
				}
				if (offsetX > 0) {
					offsetX = 0;
				}
			}
			if (prevOffsetX != offsetX || prevOffsetY != offsetY) {
				prevOffsetY = offsetY;
				prevOffsetX = offsetX;
				updateRows();
			}
		}
	}

	protected GridViewCell createCell(int index) {
		return new GridViewCell(index, cellSize, cellSize);
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
		resetScroll();
		updateRows();
	}

	public GridViewDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(GridViewDelegate delegate) {
		this.delegate = delegate;
	}

	public int getFirstRow() {
		int firstRow = (int)Math.abs(offsetY / cellSize);
		return firstRow;
	}

	public int getLastRow() {
		int firstRow = (int)Math.abs(offsetY / cellSize);
		int lastRow = firstRow + numRows;
		return lastRow;
	}

	public void updateCells() {
		for (int i = 0; i < cells.length; i++) {
			cells[i].update();
		}
	}
	
	public void updateCell(int index) {
		cells[index].update();
	}
	
	public int getSelectedRow() {
		return selectedRow;
	}
	
	public int getSelectedCol() {
		return selectedCol;
	}
}
