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

import com.nextbreakpoint.nextfractal.core.common.RendererDelegate;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsContext;
import com.nextbreakpoint.nextfractal.core.javafx.PlatformImageLoader;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewCellRenderer;
import com.nextbreakpoint.nextfractal.core.javafx.grid.GridViewItem;
import lombok.Getter;

import java.io.File;
import java.util.Objects;

@Getter
public class BrowseGridViewItem extends GridViewItem {
	private final File file;

	public BrowseGridViewItem(File file, PlatformImageLoader imageLoader) {
		super(new GridViewCellRendererAdapter(imageLoader));
		this.file = Objects.requireNonNull(file);
	}

	private static class GridViewCellRendererAdapter implements GridViewCellRenderer {
		private final PlatformImageLoader imageLoader;

        private GridViewCellRendererAdapter(PlatformImageLoader imageLoader) {
            this.imageLoader = Objects.requireNonNull(imageLoader);
        }

        @Override
		public void run() {
			imageLoader.run();
		}

		@Override
		public void cancel() {
			imageLoader.cancel();
		}

		@Override
		public void waitFor() throws InterruptedException {
			imageLoader.waitFor();
		}

		@Override
		public void draw(GraphicsContext gc, int x, int y) {
			imageLoader.drawImage(gc, x, y);
		}

		@Override
		public void setDelegate(RendererDelegate delegate) {
			imageLoader.setDelegate(delegate);
		}
	}
}
