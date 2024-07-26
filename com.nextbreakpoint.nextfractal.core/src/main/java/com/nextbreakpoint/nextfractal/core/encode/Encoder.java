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
package com.nextbreakpoint.nextfractal.core.encode;

import java.io.File;

public interface Encoder {
	/**
	 * @return
	 */
	String getId();

	/**
	 * @return
	 */
	String getName();

	/**
	 * @return
	 */
	boolean isVideoSupported();

	/**
	 * @param delegate
	 */
	void setDelegate(EncoderDelegate delegate);

	/**
	 * @param context
	 * @param path
	 * @return
	 */
	EncoderHandle open(EncoderContext context, File path) throws EncoderException;

	/**
	 * @param handle
	 */
	void close(EncoderHandle handle) throws EncoderException;

	/**
	 * @param handle
	 * @param frameIndex
	 * @param repeatFrameCount
	 * @param totalFrameCount
	 * @throws EncoderException
	 */
	void encode(EncoderHandle handle, int frameIndex, int repeatFrameCount, int totalFrameCount) throws EncoderException;

	/**
	 * @return
	 */
	String getSuffix();
}
