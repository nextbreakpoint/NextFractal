/*
 * NextFractal 7.0 
 * http://www.nextbreakpoint.com
 *
 * Copyright 2001, 2015 Andrea Medeghini
 * andrea@nextbreakpoint.com
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
package com.nextbreakpoint.nextfractal.twister.ui.javafx.extensions.editor;

import com.nextbreakpoint.nextfractal.core.tree.NodeValue;
import com.nextbreakpoint.nextfractal.core.ui.javafx.editor.NumberEditorRuntime;
import com.nextbreakpoint.nextfractal.twister.common.PercentageElementNodeValue;

/**
 * @author Andrea Medeghini
 */
public class PercentageElementEditorRuntime extends NumberEditorRuntime {
	/**
	 * @see com.nextbreakpoint.nextfractal.core.ui.javafx.editor.NumberEditorRuntime#createNodeValue(java.lang.Object)
	 */
	@Override
	protected NodeValue<?> createNodeValue(final Number value) {
		return new PercentageElementNodeValue(value.intValue());
	}

	/**
	 * @see com.nextbreakpoint.nextfractal.core.ui.javafx.editor.NumberEditorRuntime#parseValue(java.lang.String)
	 */
	@Override
	protected Number parseValue(final String text) {
		return Integer.parseInt(text);
	}
}