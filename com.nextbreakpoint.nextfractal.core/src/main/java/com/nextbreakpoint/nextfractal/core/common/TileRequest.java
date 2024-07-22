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
package com.nextbreakpoint.nextfractal.core.common;

import java.util.UUID;

public class TileRequest {
	private Session session;
	private int size;
	private int cols;
	private int rows;
	private int col;
	private int row;
	private UUID taskId;

	private TileRequest() {}

	public int getSize() {
		return size;
	}

	public int getCols() {
		return cols;
	}

	public int getRows() {
		return rows;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public Session getSession() {
		return session;
	}

	public UUID getTaskId() {
		return taskId;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private UUID taskId;
		private Session session;
		private int size;
		private int cols;
		private int rows;
		private int col;
		private int row;

		public Builder withSession(Session session) {
			this.session = session;
			return this;
		}

		public Builder withTaskId(UUID taskId) {
			this.taskId = taskId;
			return this;
		}

		public Builder withSize(int size) {
			this.size = size;
			return this;
		}

		public Builder withCols(int cols) {
			this.cols = cols;
			return this;
		}

		public Builder withRows(int rows) {
			this.rows = rows;
			return this;
		}

		public Builder withCol(int col) {
			this.col = col;
			return this;
		}

		public Builder withRow(int row) {
			this.row = row;
			return this;
		}

		public TileRequest build() {
			TileRequest tileRequest = new TileRequest();
			tileRequest.taskId = taskId;
			tileRequest.session = session;
			tileRequest.size = size;
			tileRequest.cols = cols;
			tileRequest.rows = rows;
			tileRequest.col = col;
			tileRequest.row = row;
			return tileRequest;
		}
	}
}
