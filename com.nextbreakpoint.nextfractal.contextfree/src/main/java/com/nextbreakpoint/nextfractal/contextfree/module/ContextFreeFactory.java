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
package com.nextbreakpoint.nextfractal.contextfree.module;

import com.nextbreakpoint.common.command.Command;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.nextfractal.core.common.CoreFactory;
import com.nextbreakpoint.nextfractal.core.common.ImageComposer;
import com.nextbreakpoint.nextfractal.core.common.ImageGenerator;
import com.nextbreakpoint.nextfractal.core.common.Metadata;
import com.nextbreakpoint.nextfractal.core.common.MetadataCodec;
import com.nextbreakpoint.nextfractal.core.common.Session;
import com.nextbreakpoint.nextfractal.core.render.RendererFactory;
import com.nextbreakpoint.nextfractal.core.render.RendererTile;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;

public class ContextFreeFactory implements CoreFactory {
	public static final String PLUGIN_ID = "ContextFree";
	public static final String GRAMMAR = "ContextFree";

	/**
	 * @see CoreFactory#getId()
	 */
	public String getId() {
		return PLUGIN_ID;
	}

	public String getGrammar() {
		return GRAMMAR;
	}

	/**
	 * @see CoreFactory#createSession()
	 */
	@Override
	public Session createSession() {
		return new ContextFreeSession();
	}

	@Override
	public Session createSession(String script, Metadata metadata) {
		return new ContextFreeSession(script, (ContextFreeMetadata) metadata);
	}

	/**
	 * @see CoreFactory#createImageGenerator(java.util.concurrent.ThreadFactory, com.nextbreakpoint.nextfractal.core.render.RendererFactory, com.nextbreakpoint.nextfractal.core.render.RendererTile, boolean)
	 */
	@Override
	public ImageGenerator createImageGenerator(ThreadFactory threadFactory, RendererFactory renderFactory, RendererTile tile, boolean opaque) {
		return new ContextFreeImageGenerator(threadFactory, renderFactory, tile, opaque);
	}

	@Override
	public ImageComposer createImageComposer(ThreadFactory threadFactory, RendererTile tile, boolean opaque) {
		return new ContextFreeImageComposer(threadFactory, tile, opaque);
	}

	@Override
	public MetadataCodec createMetadataCodec() {
		return new ContextFreeMetadataCodec();
	}

	@Override
	public Either<String> loadResource(String resourceName) {
		return Command.of(() -> Objects.requireNonNull(getClass().getResource(resourceName)).toExternalForm()).execute();
	}
}