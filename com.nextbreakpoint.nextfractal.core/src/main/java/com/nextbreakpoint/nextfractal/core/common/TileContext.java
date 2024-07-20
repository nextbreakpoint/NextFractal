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
package com.nextbreakpoint.nextfractal.core.common;

import com.nextbreakpoint.nextfractal.core.render.RendererPoint;
import com.nextbreakpoint.nextfractal.core.render.RendererSize;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.util.UUID;

public class TileContext {
	private UUID taskId;
	private Session session;
	private float quality;
	private int imageWidth;
	private int imageHeight;
	private int tileWidth;
	private int tileHeight;
	private int tileOffsetX;
	private int tileOffsetY;
	private int borderWidth;
	private int borderHeight;
	private int row;
	private int col;
	private int rows;
	private int cols;

	private TileContext() {}
	
	public RendererTile getTile() {
		return createTile();
	}

	public Session getSession() {
		return session;
	}

	public UUID getTaskId() {
		return taskId;
	}

	public int getImageWidth() {
		return imageWidth;
	}

	public int getImageHeight() {
		return imageHeight;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public int getTileOffsetX() {
		return tileOffsetX;
	}

	public int getTileOffsetY() {
		return tileOffsetY;
	}

	public int getBorderWidth() {
		return borderWidth;
	}

	public int getBorderHeight() {
		return borderHeight;
	}

	public float getQuality() {
		return quality;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public static Builder builder() {
		return new Builder();
	}

	public RendererTile createTile() {
		RendererSize imageSize = new RendererSize(imageWidth, imageHeight);
		RendererSize tileSize = new RendererSize(tileWidth, tileHeight);
		RendererSize tileBorder = new RendererSize(borderWidth, borderHeight);
		RendererPoint tileOffset = new RendererPoint(tileOffsetX, tileOffsetY);
		RendererTile tile = new RendererTile(imageSize, tileSize, tileOffset, tileBorder);
		return tile;
	}

	public static class Builder {
		private UUID taskId;
		private Session session;
		private float quality;
		private int imageWidth;
		private int imageHeight;
		private int tileWidth;
		private int tileHeight;
		private int tileOffsetX;
		private int tileOffsetY;
		private int borderWidth;
		private int borderHeight;
		private int row;
		private int col;
		private int rows;
		private int cols;

		public Builder withTaskId(UUID taskId) {
			this.taskId = taskId;
			return this;
		}

		public Builder withSession(Session session) {
			this.session = session;
			return this;
		}

		public Builder withQuality(float quality) {
			this.quality = quality;
			return this;
		}

		public Builder withImageWidth(int imageWidth) {
			this.imageWidth = imageWidth;
			return this;
		}

		public Builder withImageHeight(int imageHeight) {
			this.imageHeight = imageHeight;
			return this;
		}

		public Builder withTileWidth(int tileWidth) {
			this.tileWidth = tileWidth;
			return this;
		}

		public Builder withTileHeight(int tileHeight) {
			this.tileHeight = tileHeight;
			return this;
		}

		public Builder withTileOffsetX(int tileOffsetX) {
			this.tileOffsetX = tileOffsetX;
			return this;
		}

		public Builder withTileOffsetY(int tileOffsetY) {
			this.tileOffsetY = tileOffsetY;
			return this;
		}

		public Builder withBorderWidth(int borderWidth) {
			this.borderWidth = borderWidth;
			return this;
		}

		public Builder withBorderHeight(int borderHeight) {
			this.borderHeight = borderHeight;
			return this;
		}

		public Builder withRow(int row) {
			this.row = row;
			return this;
		}

		public Builder withCol(int col) {
			this.col = col;
			return this;
		}

		public Builder withRows(int rows) {
			this.rows = rows;
			return this;
		}

		public Builder withCols(int cols) {
			this.cols = cols;
			return this;
		}

		public TileContext build() {
			TileContext tileJob = new TileContext();
			tileJob.taskId = taskId;
			tileJob.session = session;
			tileJob.quality = quality;
			tileJob.imageWidth = imageWidth;
			tileJob.imageHeight = imageHeight;
			tileJob.tileWidth = tileWidth;
			tileJob.tileHeight = tileHeight;
			tileJob.tileOffsetX = tileOffsetX;
			tileJob.tileOffsetY = tileOffsetY;
			tileJob.borderWidth = borderWidth;
			tileJob.borderHeight = borderHeight;
			tileJob.row = row;
			tileJob.col = col;
			tileJob.rows = rows;
			tileJob.cols = cols;
			return tileJob;
		}
	}
}
