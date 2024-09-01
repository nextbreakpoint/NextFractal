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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum FlagType {
	CF_NONE(0L),
	CF_MITER_JOIN(1L),
	CF_ROUND_JOIN(2L),
	CF_BEVEL_JOIN(3L),
	CF_JOIN_MASK(0x7L),
	CF_JOIN_PRESENT(8L),
	CF_BUTT_CAP(1L << 4),
	CF_ROUND_CAP(2L << 4),
	CF_SQUARE_CAP(3L << 4),
	CF_CAP_MASK(0x7L << 4),
	CF_CAP_PRESENT(8L << 4),
	CF_ARC_CW(1L << 8),
	CF_ARC_LARGE(1L << 9),
	CF_CONTINUOUS(1L << 10),
	CF_ALIGN(1L << 11),
	CF_EVEN_ODD(1L << 12),
	CF_ISO_WIDTH(1L << 13),
	CF_FILL(1L << 14),
	CF_CYCLIC(31L << 15),
	CF_DIHEDRAL(1L << 15),
	CF_P11G(2L << 15),
	CF_P11M(3L << 15),
	CF_P1M1(4L << 15),
	CF_P2(30L << 15),
	CF_P2MG(6L << 15),
	CF_P2MM(7L << 15),
	CF_PM(8L << 15),
	CF_PG(9L << 15),
	CF_CM(10L << 15),
	CF_PMM(11L << 15),
	CF_PMG(12L << 15),
	CF_PGG(13L << 15),
	CF_CMM(14L << 15),
	CF_P4(15L << 15),
	CF_P4M(16L << 15),
	CF_P4G(17L << 15),
	CF_P3(18L << 15),
	CF_P3M1(19L << 15),
	CF_P31M(20L << 15),
	CF_P6(21L << 15),
	CF_P6M(22L << 15),
	CF_SRC_OVER((1 << 20) | (1 << 21)),
	CF_CLEAR((1 << 20) | (2 << 21)),
	CF_XOR((1 << 20) | (3 << 21)),
	CF_PLUS((1 << 20) | (4 << 21)),
	CF_MULTIPLY((1 << 20) | (5 << 21)),
	CF_SCREEN((1 << 20) | (6 << 21)),
	CF_OVERLAY((1 << 20) | (6 << 21)),
	CF_DARKEN((1 << 20) | (7 << 21)),
	CF_LIGHTEN((1 << 20) | (8 << 21)),
	CF_COLOR_DODGE((1 << 20) | (9 << 21)),
	CF_COLOR_BURN((1 << 20) | (10 << 21)),
	CF_HARD_LIGHT((1 << 20) | (11 << 21)),
	CF_SOFT_LIGHT((1 << 20) | (12 << 21)),
	CF_DIFFERENCE((1 << 20) | (13 << 21)),
	CF_EXCLUSION((1 << 20) | (14 << 21));
	
	private final long mask;

	private static final Map<String, Long> flagNames = new HashMap<>();

	static {
		flagNames.put("CF::None",        FlagType.CF_NONE.getMask());
		flagNames.put("CF::MiterJoin",   FlagType.CF_MITER_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::RoundJoin",   FlagType.CF_ROUND_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::BevelJoin",   FlagType.CF_BEVEL_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::ButtCap",     FlagType.CF_BUTT_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::RoundCap",    FlagType.CF_ROUND_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::SquareCap",   FlagType.CF_SQUARE_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::ArcCW",       FlagType.CF_ARC_CW.getMask());
		flagNames.put("CF::ArcLarge",    FlagType.CF_ARC_LARGE.getMask());
		flagNames.put("CF::Continuous",  FlagType.CF_CONTINUOUS.getMask());
		flagNames.put("CF::Align",       FlagType.CF_ALIGN.getMask());
		flagNames.put("CF::EvenOdd",     FlagType.CF_EVEN_ODD.getMask());
		flagNames.put("CF::IsoWidth",    FlagType.CF_ISO_WIDTH.getMask());
		flagNames.put("~~CF_FILL~~",     FlagType.CF_FILL.getMask());
		flagNames.put("CF::Cyclic",      FlagType.CF_CYCLIC.getMask());
		flagNames.put("CF::Dihedral",    FlagType.CF_DIHEDRAL.getMask());
		flagNames.put("CF::p11g",        FlagType.CF_P11G.getMask());
		flagNames.put("CF::p11m",        FlagType.CF_P11M.getMask());
		flagNames.put("CF::p1m1",        FlagType.CF_P1M1.getMask());
		flagNames.put("CF::p2",          FlagType.CF_P2.getMask());
		flagNames.put("CF::p2mg",        FlagType.CF_P2MG.getMask());
		flagNames.put("CF::p2mm",        FlagType.CF_P2MM.getMask());
		flagNames.put("CF::pm",          FlagType.CF_PM.getMask());
		flagNames.put("CF::pg",          FlagType.CF_PG.getMask());
		flagNames.put("CF::cm",          FlagType.CF_CM.getMask());
		flagNames.put("CF::pmm",         FlagType.CF_PMM.getMask());
		flagNames.put("CF::pmg",         FlagType.CF_PMG.getMask());
		flagNames.put("CF::pgg",         FlagType.CF_PGG.getMask());
		flagNames.put("CF::cmm",         FlagType.CF_CMM.getMask());
		flagNames.put("CF::p4",          FlagType.CF_P4.getMask());
		flagNames.put("CF::p4m",         FlagType.CF_P4M.getMask());
		flagNames.put("CF::p4g",         FlagType.CF_P4G.getMask());
		flagNames.put("CF::p3",          FlagType.CF_P3.getMask());
		flagNames.put("CF::p3m1",        FlagType.CF_P3M1.getMask());
		flagNames.put("CF::p31m",        FlagType.CF_P31M.getMask());
		flagNames.put("CF::p6",          FlagType.CF_P6.getMask());
		flagNames.put("CF::p6m",         FlagType.CF_P6M.getMask());
		flagNames.put("CF::Normal",      FlagType.CF_SRC_OVER.getMask());
		flagNames.put("CF::Clear",       FlagType.CF_CLEAR.getMask());
		flagNames.put("CF::Xor",         FlagType.CF_XOR.getMask());
		flagNames.put("CF::Plus",        FlagType.CF_PLUS.getMask());
		flagNames.put("CF::Multiply",    FlagType.CF_MULTIPLY.getMask());
		flagNames.put("CF::Screen",      FlagType.CF_SCREEN.getMask());
		flagNames.put("CF::Overlay",     FlagType.CF_OVERLAY.getMask());
		flagNames.put("CF::Darken",      FlagType.CF_DARKEN.getMask());
		flagNames.put("CF::Lighten",     FlagType.CF_LIGHTEN.getMask());
		flagNames.put("CF::ColorDodge",  FlagType.CF_COLOR_DODGE.getMask());
		flagNames.put("CF::ColorBurn",   FlagType.CF_COLOR_BURN.getMask());
		flagNames.put("CF::HardLight",   FlagType.CF_HARD_LIGHT.getMask());
		flagNames.put("CF::SoftLight",   FlagType.CF_SOFT_LIGHT.getMask());
		flagNames.put("CF::Difference",  FlagType.CF_DIFFERENCE.getMask());
		flagNames.put("CF::Exclusion",   FlagType.CF_EXCLUSION.getMask());
	}

	FlagType(long mask) {
		this.mask = mask;
	}

    public static FlagType fromMask(long mask) {
		for (FlagType value : FlagType.values()) {
			if (value.getMask() == mask) {
				return value;
			}
		}
		return CF_NONE;
	}

	public static FlagType byName(String name) {
		for (FlagType value : FlagType.values()) {
			if (value.name().equals(name)) {
				return value;
			}
		}
		return CF_NONE;
	}

	public static String flagToString(int flag) {
		return flagNames
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == flag)
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse("Unknown flag");
	}

	public static Long findFlag(String name) {
		return flagNames.get(name);
	}
}
