/*
 * NextFractal 2.4.0
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

import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.graphics.GraphicsFactory;
import com.nextbreakpoint.nextfractal.core.graphics.Tile;

import java.util.concurrent.ThreadFactory;

public interface CoreFactory {
	/**
	 * @return
	 */
    String getId();

	/**
	 * @return
	 */
    String getGrammar();

	/**
	 * @return
	 */
    Session createSession();

	/**
	 * @return
	 */
    Session createSession(String script, Metadata metadata);

	/**
	 * @param threadFactory
	 * @param renderFactory
	 * @param tile
	 * @param opaque
	 * @return
	 */
    ImageGenerator createImageGenerator(ThreadFactory threadFactory, GraphicsFactory renderFactory, Tile tile, boolean opaque);

	/**
	 * @param threadFactory
	 * @param tile
	 * @param opaque
	 * @return
	 */
    ImageComposer createImageComposer(ThreadFactory threadFactory, Tile tile, boolean opaque);

	/**
	 * @return
	 */
    MetadataCodec createMetadataCodec();

	/**
	 * @param resourceName
	 * @return
	 */
    Either<String> loadResource(String resourceName);
}
