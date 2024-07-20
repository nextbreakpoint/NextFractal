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
package com.nextbreakpoint.nextfractal.core.export;

import com.nextbreakpoint.nextfractal.core.common.Metadata;

public class ExportProfileBuilder {
	private float quality;
	private float frameRate;
	private int frameWidth;
	private int frameHeight;
	private int tileWidth;
	private int tileHeight;
	private int tileOffsetX;
	private int tileOffsetY;
	private int borderWidth;
	private int borderHeight;
	private float startTime;
	private float stopTime;
	private String pluginId;
	private String script;
	private Metadata metadata;

	public static ExportProfileBuilder fromProfile(ExportProfile profile) {
		ExportProfileBuilder builder = new ExportProfileBuilder();
		builder.quality = profile.getQuality();
		builder.frameRate = profile.getQuality();
		builder.frameWidth = profile.getFrameWidth();
		builder.frameHeight = profile.getFrameHeight();
		builder.tileWidth = profile.getTileWidth();
		builder.tileHeight = profile.getTileHeight();
		builder.tileOffsetX = profile.getTileOffsetX();
		builder.tileOffsetY = profile.getTileOffsetY();
		builder.borderWidth = profile.getBorderWidth();
		builder.borderHeight = profile.getBorderHeight();
		builder.startTime = profile.getStartTime();
		builder.stopTime = profile.getStopTime();
		builder.pluginId = profile.getPluginId();
		builder.script = profile.getScript();
		builder.metadata = profile.getMetadata();
		return builder;
	}

	public void withQuality(float quality) {
		this.quality = quality;
	}

	public void withFrameRate(float frameRate) {
		this.frameRate = frameRate;
	}

	public void withFrameWidth(int frameWidth) {
		this.frameWidth = frameWidth;
	}

	public void withFrameHeight(int frameHeight) {
		this.frameHeight = frameHeight;
	}

	public void withTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public void withTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public void withTileOffsetX(int tileOffsetX) {
		this.tileOffsetX = tileOffsetX;
	}

	public void withTileOffsetY(int tileOffsetY) {
		this.tileOffsetY = tileOffsetY;
	}

	public void withBorderWidth(int borderWidth) {
		this.borderWidth = borderWidth;
	}

	public void withBorderHeight(int borderHeight) {
		this.borderHeight = borderHeight;
	}

	public void withStartTime(float startTime) {
		this.startTime = startTime;
	}

	public void withStopTime(float stopTime) {
		this.stopTime = stopTime;
	}

	public void withPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public void withScript(String script) {
		this.script = script;
	}

	public void withMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public ExportProfile build() {
		return new ExportProfile(quality, frameRate, frameWidth, frameHeight, tileWidth, tileHeight,
			tileOffsetX, tileOffsetY, borderWidth, borderHeight, startTime, stopTime, pluginId, script, metadata);
	}
}
