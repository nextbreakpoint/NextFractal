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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransform1D;
import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.AssignmentType;
import lombok.Getter;
import lombok.Setter;

import java.awt.geom.AffineTransform;

// shape.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2005-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2005-2015 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA
//
// Mark Lentczner can be contacted at markl@glyphic.com or at
// Mark Lentczner, 1209 Villa St., Mountain View, CA 94041-1123, USA

@Setter
public class Modification implements Cloneable {
	@Getter
    private Rand64 rand64Seed = new Rand64();
	@Getter
    private AffineTransform transform = new AffineTransform();
	@Getter
    private AffineTransform1D transformZ = new AffineTransform1D();
	@Getter
    private AffineTransformTime transformTime = new AffineTransformTime();
	@Getter
	private long blendMode;
	private HSBColor color = new HSBColor(0, 0, 0, 0);
	private HSBColor colorTarget = new HSBColor(0, 0, 0, 0);
	private int colorAssignment;

    public HSBColor color() {
		return color;
	}

    public HSBColor colorTarget() {
		return colorTarget;
	}

    public int colorAssignment() {
		return colorAssignment;
	}

    public double getZ() {
		return transformZ.getTz();
	}

	public double area() {
		return Math.abs(transform.getDeterminant());
	}

	public boolean isFinite() {
		return Double.isFinite(transform.getScaleX()) && Double.isFinite(transform.getScaleY()) && Double.isFinite(transform.getShearX()) && Double.isFinite(transform.getShearY()) && Double.isFinite(transform.getTranslateX()) && Double.isFinite(transform.getTranslateY());
	}

	public Modification concat(Modification modification) {
		transform.concatenate(modification.getTransform());
		transformZ.concatenate(modification.getTransformZ());
		transformTime.concatenate(modification.getTransformTime());
		HSBColor.adjust(color, colorTarget, modification.color(), modification.colorTarget(), modification.colorAssignment());
		rand64Seed.add(modification.getRand64Seed());
		if (modification.getBlendMode() != 0) {
			blendMode = modification.getBlendMode();
		}
		return this;
	}

	public boolean merge(Modification modification) {
		boolean conflict =
				(colorAssignment & modification.colorAssignment) != 0 ||
				((modification.colorAssignment & AssignmentType.HueMask.getType()) != 0 &&              color.hue() != 0.0) ||
				((             colorAssignment & AssignmentType.HueMask.getType()) != 0 && modification.color.hue() != 0.0) ||
				(color.bright() != 0.0 && modification.color.bright() != 0.0) ||
				(color.sat() != 0.0 && modification.color.sat() != 0.0) ||
				(color.alpha() != 0.0 && modification.color.alpha() != 0.0);

		if (conflict) {
			return true;
		}

		transform.concatenate(modification.getTransform());
		transformZ.concatenate(modification.getTransformZ());
		transformTime.concatenate(modification.getTransformTime());
		rand64Seed.add(modification.getRand64Seed());
		if (modification.getBlendMode() != 0) {
			blendMode = modification.getBlendMode();
		}

		colorTarget.addHue(modification.colorTarget.hue());
		colorTarget.addBright(modification.colorTarget.bright());
		colorTarget.addSat(modification.colorTarget.sat());
		colorTarget.addAlpha(modification.colorTarget.alpha());

		color.addHue(modification.color.hue());
		color.addBright(modification.color.bright());
		color.addSat(modification.color.sat());
		color.addAlpha(modification.color.alpha());

		colorAssignment |= modification.colorAssignment;

		return false;
	}

	public Object clone() {
		Modification modification = new Modification();
		modification.rand64Seed = (Rand64)rand64Seed.clone();
		modification.transform = (AffineTransform)transform.clone();
		modification.transformZ = (AffineTransform1D)transformZ.clone();
		modification.transformTime = (AffineTransformTime)transformTime.clone();
		modification.color = (HSBColor)color.clone();
		modification.colorTarget = (HSBColor)colorTarget.clone();
		modification.colorAssignment = colorAssignment;
		modification.blendMode = blendMode;
		return modification;
	}
}
