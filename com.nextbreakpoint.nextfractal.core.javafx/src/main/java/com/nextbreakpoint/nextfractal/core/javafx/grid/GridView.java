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

import com.nextbreakpoint.nextfractal.core.javafx.browse.BrowseGridViewItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.util.List;
import java.util.logging.Level;

@Log
public class GridView extends Pane {
	private final int EXTRA = 1;
	private final int cellSize;
	private final int numRows;
	private final int numCols;
	private final GridViewCell[] cells;
	private GridViewItem[] data;
	private double offsetX;
	private double offsetY;
	private double prevOffsetX;
	private double prevOffsetY;
	@Getter
	private int selectedRow = -1;
	@Getter
	private int selectedCol = -1;
	@Getter
	@Setter
	private GridViewDelegate delegate;
	@Getter
	@Setter
	private boolean horizontal;

	public GridView(GridViewCellFactory factory, int numRows, int numCols, int cellSize) {
		this.numRows = numRows;
		this.numCols = numCols;
		this.cellSize = cellSize;

		getStyleClass().add("grid-view");

		cells = new GridViewCell[horizontal ? (numCols + EXTRA) * numRows : (numRows + EXTRA) * numCols];

		for (int i = 0; i < cells.length; i++) {
			final GridViewCell cell = factory.createCell(i, cellSize, cellSize);
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
                        delegate.onSelectionChanged(GridView.this, selectedRow, selectedCol, mouseEvent.getClickCount());
                    }
					final int index = getCellIndex(selectedRow - getFirstRow(), selectedCol - getFirstCol());
					if (index >= 0 && index < cells.length) {
						cells[index].update();
					}
                });

		widthProperty().addListener((_, _, _) -> {
            resetScroll();
            update();
        });

		heightProperty().addListener((_, _, _) -> {
            resetScroll();
            update();
        });
	}

	public void setData(List<BrowseGridViewItem> data) {
		final BrowseGridViewItem[] items = new BrowseGridViewItem[data.size()];
		for (int i = 0; i < data.size(); i++) {
			items[i] = data.get(i);
		}
		this.data = items;
		resetScroll();
		update();
	}

	public int getFirstRow() {
		return (int) Math.abs(offsetY / cellSize);
	}

	public int getLastRow() {
		return getFirstRow() + numRows;
	}

	public int getFirstCol() {
		return (int) Math.abs(offsetX / cellSize);
	}

	public int getLastCol() {
		return getFirstCol() + numCols;
	}

	public int getSize() {
		return data != null ? data.length : 0;
	}

	public boolean isEmpty() {
		return getSize() == 0;
	}

	private void update() {
		updateCells();
	}

	private void updateCells() {
		try {
			if (data != null) {
				final int firstIndex = getFirstIndex();
				final int lastIndex = getLastIndex();
				for (int index = 0; index < firstIndex; index++) {
					final GridViewItem item = data[index];
					item.cancel();
				}
				for (int index = lastIndex; index < data.length; index++) {
					final GridViewItem item = data[index];
					item.cancel();
				}
				for (int index = 0; index < firstIndex; index++) {
					final GridViewItem item = data[index];
					item.waitFor();
				}
				for (int index = lastIndex; index < data.length; index++) {
					final GridViewItem item = data[index];
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

//			for (int col = 0; col < numCols + EXTRA; col++) {
//				for (int row = 0; row < numRows; row++) {
//					final GridViewCell cell = cells[col * numRows + row];
//					cell.update();
//				}
//			}

			if (delegate != null) {
				delegate.onGridUpdated();
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.log(Level.WARNING, "Can't render grid", e);
		}
	}

	private void resetCell(int row, int col) {
		if (horizontal) {
			final GridViewCell cell = cells[col * numRows + row];
			cell.unbindItem();
			cell.update();
		} else {
			final GridViewCell cell = cells[row * numCols + col];
			cell.unbindItem();
			cell.update();
		}
	}

	private void updateCell(int row, int col, int first) {
		if (horizontal) {
			final GridViewCell cell = cells[col * numRows + row];
			cell.setLayoutX(col * cellSize + (offsetX - ((int) (offsetX / (cellSize))) * cellSize));
			cell.setLayoutY(row * cellSize + (offsetY - ((int) (offsetY / (cellSize))) * cellSize));
			final int index = (first + col) * numRows + row;
			if (data != null && index < data.length) {
				final GridViewItem item = data[index];
				cell.bindItem(item);
				item.run();
				cell.update();
			}
		} else {
			final GridViewCell cell = cells[row * numCols + col];
			cell.setLayoutX(col * cellSize + (offsetX - ((int) (offsetX / (cellSize))) * cellSize));
			cell.setLayoutY(row * cellSize + (offsetY - ((int) (offsetY / (cellSize))) * cellSize));
			final int index = (first + row) * numCols + col;
			if (data != null && index < data.length) {
				final GridViewItem item = data[index];
				cell.bindItem(item);
				item.run();
				cell.update();
			}
		}
	}

	private int getFirstIndex() {
		if (horizontal) {
			int firstCol = getFirstCol();
			if (firstCol > 0) {
				firstCol -= 1;
			}
			return Math.min(firstCol * numRows, data.length);
		} else {
			int firstRow = getFirstRow();
			if (firstRow > 0) {
				firstRow -= 1;
			}
			return Math.min(firstRow * numCols, data.length);
		}
	}

	private int getLastIndex() {
		if (horizontal) {
			int lastCol = getLastCol();
			if (lastCol < getSize() / numRows - 1) {
				lastCol += 1;
			}
			return Math.min(lastCol * numRows + numRows, data.length);
		} else {
			int lastRow = getLastRow();
			if (lastRow < getSize() / numCols - 1) {
				lastRow += 1;
			}
			return Math.min(lastRow * numCols + numCols, data.length);
		}
	}

	public int getCellIndex(int row, int col) {
		if (horizontal) {
			return col * numRows + row;
		} else {
			return row * numCols + col;
		}
	}

	private void resetScroll() {
		offsetX = 0;
		offsetY = 0;
		prevOffsetX = 0;
		prevOffsetY = 0;
	}

	private void scrollCells(double deltaX, double deltaY) {
		if (data == null) {
			return;
		}

		if (horizontal) {
			offsetY = 0;
			final int x = ((data.length / numRows) * cellSize + (data.length % numRows > 0 ? cellSize : 0) - numCols * cellSize);
			if (x > 0) {
				offsetX += deltaX;
				if (offsetX < -x) {
					offsetX = -x;
				}
				if (offsetX > 0) {
					offsetX = 0;
				}
			}
        } else {
			offsetX = 0;
			final int y = ((data.length / numCols) * cellSize + (data.length % numCols > 0 ? cellSize : 0) - numRows * cellSize);
			if (y > 0) {
				offsetY += deltaY;
				if (offsetY < -y) {
					offsetY = -y;
				}
				if (offsetY > 0) {
					offsetY = 0;
				}
			}
        }

		update();
		prevOffsetX = offsetX;
		prevOffsetY = offsetY;
    }
}
