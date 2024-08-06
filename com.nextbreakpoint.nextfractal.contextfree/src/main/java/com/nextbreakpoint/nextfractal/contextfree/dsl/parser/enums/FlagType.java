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
	CF_P6M(22L << 15);
	
	private final long mask;

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
		String normalizedName = name.replace(":","_").toUpperCase();
		for (FlagType value : FlagType.values()) {
			if (value.name().equals(normalizedName)) {
				return value;
			}
		}
		return CF_NONE;
	}
}
