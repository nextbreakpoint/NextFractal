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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums;

import lombok.Getter;

import java.util.HashMap;

@Getter
public enum ModClass {
    InvalidClass(-1), 
    NotAClass(0), 
    GeomClass(1), 
    ZClass(2), 
    TimeClass(4),
    HueClass(8), 
    SatClass(16), 
    BrightClass(32), 
    AlphaClass(64),
    HueTargetClass(128), 
    SatTargetClass(256), 
    BrightTargetClass(512), 
    AlphaTargetClass(1024),
    StrokeClass(2048), 
    ParamClass(4096), 
    PathOpClass(8192);
    
	private final int type;

    private static final HashMap<ModType, ModClass> evalMap = new HashMap<>();

    static {
        evalMap.put(ModType.unknown, ModClass.fromType(ModClass.NotAClass.getType()));
        evalMap.put(ModType.x, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
        evalMap.put(ModType.y, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
        evalMap.put(ModType.z, ModClass.ZClass);
        evalMap.put(ModType.xyz, ModClass.fromType(ModClass.NotAClass.getType()));
        evalMap.put(ModType.transform, ModClass.GeomClass);
        evalMap.put(ModType.size, ModClass.GeomClass);
        evalMap.put(ModType.sizexyz, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.ZClass.getType()));
        evalMap.put(ModType.rotate, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
        evalMap.put(ModType.skew, ModClass.GeomClass);
        evalMap.put(ModType.flip, ModClass.GeomClass);
        evalMap.put(ModType.zsize, ModClass.ZClass);
        evalMap.put(ModType.blend, ModClass.fromType(ModClass.NotAClass.getType()));
        evalMap.put(ModType.hue, ModClass.HueClass);
        evalMap.put(ModType.sat, ModClass.SatClass);
        evalMap.put(ModType.bright, ModClass.BrightClass);
        evalMap.put(ModType.alpha, ModClass.AlphaClass);
        evalMap.put(ModType.hueTarg, ModClass.HueClass);
        evalMap.put(ModType.satTarg, ModClass.SatClass);
        evalMap.put(ModType.brightTarg, ModClass.BrightClass);
        evalMap.put(ModType.alphaTarg, ModClass.AlphaClass);
        evalMap.put(ModType.targHue, ModClass.HueTargetClass);
        evalMap.put(ModType.targSat, ModClass.SatTargetClass);
        evalMap.put(ModType.targBright, ModClass.BrightTargetClass);
        evalMap.put(ModType.targAlpha, ModClass.AlphaTargetClass);
        evalMap.put(ModType.time, ModClass.TimeClass);
        evalMap.put(ModType.timescale, ModClass.TimeClass);
        evalMap.put(ModType.param, ModClass.ParamClass);
        evalMap.put(ModType.x1, ModClass.PathOpClass);
        evalMap.put(ModType.y1, ModClass.PathOpClass);
        evalMap.put(ModType.x2, ModClass.PathOpClass);
        evalMap.put(ModType.y2, ModClass.PathOpClass);
        evalMap.put(ModType.xrad, ModClass.PathOpClass);
        evalMap.put(ModType.yrad, ModClass.PathOpClass);
        evalMap.put(ModType.modification, ModClass.InvalidClass);
    }

    ModClass(int type) {
		this.type = type;
	}

    public static ModClass fromType(int type) {
		for (ModClass value : ModClass.values()) {
			if (value.getType() == type) {
				return value;
			}
		}
		return NotAClass;
	}

    public static ModClass byModType(ModType modType) {
        return evalMap.get(modType);
    }
}
