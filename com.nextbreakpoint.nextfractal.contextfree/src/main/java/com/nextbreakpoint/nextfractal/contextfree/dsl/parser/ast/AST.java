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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModClass;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

// ast.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2009-2013 John Horigan - john@glyphic.com
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

public class AST {
    public static final double MAX_NATURAL = 9007199254740992.0;
    public static final int MAX_VECTOR_SIZE = 99;
    public static final double M_PI = Math.PI;
    public static final double M_PI_2 = Math.PI / 2;
    public static final double M_PI_4 = Math.PI / 4;
    public static final double M_SQRT1_2 = 1 / Math.sqrt(2);
    public static final double M_SQRT2 = Math.sqrt(2);
    public static final int ModificationSize = 6;

    public static ExpType decodeType(CFDGSystem system, String typeName, int[] tupleSize, boolean[] isNatural, ASTWhere where) {
        ExpType type = ExpType.None;
        tupleSize[0] = 1;
        isNatural[0] = false;

        if (typeName.equals("number")) {
            type = ExpType.Numeric;
        } else if (typeName.equals("natural")) {
            type = ExpType.Numeric;
            isNatural[0] = true;
        } else if (typeName.equals("adjustment")) {
            type = ExpType.Mod;
            tupleSize[0] = ModificationSize;
        } else if (typeName.equals("shape")) {
            type = ExpType.Rule;
        } else if (typeName.startsWith("vector")) {
            if (!typeName.matches("vector[0-9]+")) {
                system.error("Illegal vector type specification", where);
            }
            int size = Integer.parseInt(typeName.substring(ModificationSize));
            if (size <= 1 || size > 99) {
                system.error("Illegal vector size (<=1 or >99)", where);
            }
            type = ExpType.Numeric;
            tupleSize[0] = size;
        } else {
            system.error("Unrecognized type name", where);
        }

        return type;
    }

    public static void processDihedral(CFDGSystem system, List<AffineTransform> syms, double order, double x, double y, boolean dihedral, double angle, ASTWhere where) {
        if (order < 1.0) {
            system.error("Rotational symmetry order must be one or larger", where);
        }
        AffineTransform reg = new AffineTransform();
        //TODO are coordinates inverted?
        reg.translate(x, y);
        AffineTransform mirror = getMirrorTransform(angle);
        int num = (int)order;
        order = 2.0 * Math.PI / order;
        for (int i = 0; i < num; ++i) {
            AffineTransform t = new AffineTransform(reg);
            if (i != 0) t.rotate(i * order);
            AffineTransform t2 = new AffineTransform(t);
            t2.concatenate(mirror);
            //TODO are coordinates inverted?
            t.translate(-x, -y);
            t2.translate(-x, -y);
            addUnique(syms, t);
            if (dihedral) addUnique(syms, t2);
        }
    }

    // Analyze the symmetry spec accumulated in the data vector and add the
    // appropriate affine transforms to the SymmList. Avoid adding the identity
    // transform if it is already present in the SymmList.
    //TODO test symmetry
    public static void processSymmSpec(CFDGSystem system, List<AffineTransform> syms, AffineTransform tile, boolean tiled, List<Double> data, ASTWhere where) {
        if (data == null || data.isEmpty()) {
            return;
        }

        final int type = data.getFirst().intValue();
        final FlagType flag = FlagType.fromMask(type);

        final boolean frieze = (tile.getScaleX() != 0.0 || tile.getScaleY() != 0.0) && tile.getScaleX() * tile.getScaleY() == 0.0;
        final boolean rhombic = tiled && ((Math.abs(tile.getShearY()) <= 0.01 && Math.abs(tile.getShearX() / tile.getScaleX() - 0.5) < 0.01) || (Math.abs(tile.getShearX()) <= 0.01 && Math.abs(tile.getShearY() / tile.getScaleY() - 0.5) < 0.01));
        final boolean rectangular = tiled && tile.getShearX() == 0.0 && tile.getShearY() == 0.0;
        final boolean square = rectangular && tile.getScaleX() == tile.getScaleY();

        boolean hexagonal = false;
        boolean square45 = false;
        double size45 = tile.getScaleX();

        if (rhombic) {
            double x1 = 1;
            double y1 = 0;
            Point2D.Double p1 = new Point2D.Double(x1, y1);
            tile.transform(p1, p1);
            x1 = p1.getX();
            y1 = p1.getY();
            double dist10 = Math.hypot(x1, y1);
            double x2 = 0;
            double y2 = 1;
            Point2D.Double p2 = new Point2D.Double(x2, y2);
            tile.transform(p2, p2);
            x2 = p2.getX();
            y2 = p2.getY();
            double dist01 = Math.hypot(x2, y2);
            hexagonal = Math.abs(dist10 / dist01 - 1.0) < 0.01;
            square45 = Math.abs(dist10 / dist01 - M_SQRT2) < 0.01 || Math.abs(dist01 / dist10 - M_SQRT2) < 0.01;
            size45 = Math.min(dist01, dist10);
        }

        if (type >= FlagType.CF_P11G.getMask() && type <= FlagType.CF_P2MM.getMask() && !frieze) {
            system.error("Frieze symmetry only works in frieze designs", where);
        }

        if (type >= FlagType.CF_PM.getMask() && type <= FlagType.CF_P6M.getMask() && !tiled) {
            system.error("Wallpaper symmetry only works in tiled designs", where);
        }

        if (type == FlagType.CF_P2.getMask() && !frieze && !tiled) {
            system.error("p2 symmetry only works in frieze or tiled designs", where);
        }

        final AffineTransform ref45 = getMirrorTransform(M_PI_4);
        final AffineTransform ref135 = getMirrorTransform(-M_PI_4);

        switch (flag) {
            case CF_CYCLIC -> {
                double order, x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 4:
                        x = data.get(2);
                        y = data.get(3);
                    case 2:
                        order = data.get(1);
                        break;
                    default:
                        system.error("Cyclic symmetry requires an order argument and an optional center of rotation", where);
                        order = 1.0;
                        break;
                }
                processDihedral(system, syms, order, x, y, false, 0.0, where);
            }
            case CF_DIHEDRAL -> {
                double order, angle = 0.0, x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 5:
                        x = data.get(3);
                        y = data.get(4);
                    case 3:
                        order = data.get(1);
                        angle = data.get(2) * M_PI / 180.0;
                        break;
                    case 4:
                        x = data.get(2);
                        y = data.get(3);
                    case 2:
                        order = data.get(1);
                        break;
                    default:
                        system.error("Dihedral symmetry requires an order argument, an optional mirror angle, and an optional center of rotation", where);
                        order = 1.0;
                        break;
                }
                processDihedral(system, syms, order, x, y, true, angle, where);
            }
            case CF_P11G -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 2) {
                    if (tile.getScaleX() != 0.0)
                        mirrory = data.get(1);
                    else
                        mirrorx = data.get(1);
                } else if (data.size() > 2) {
                    system.error("p11g symmetry takes no arguments or an optional glide axis position argument", where);
                }
                final AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                tr.translate(-mirrorx, -mirrory);
                if (tile.getScaleX() != 0.0)
                    tr.scale(1, -1);
                else
                    tr.scale(-1, 1);
                tr.translate(tile.getScaleX() * 0.5 + mirrorx, tile.getScaleY() * 0.5 + mirrory);
                addUnique(syms, tr);
            }
            case CF_P11M -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 2) {
                    if (tile.getScaleX() != 0.0)
                        mirrory = data.get(1);
                    else
                        mirrorx = data.get(1);
                } else if (data.size() > 2) {
                    system.error("p11m symmetry takes no arguments or an optional mirror axis position argument", where);
                }
                AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                tr.translate(-mirrorx, -mirrory);
                if (tile.getScaleX() != 0.0)
                    tr.scale(1, -1);
                else
                    tr.scale(-1, 1);
                tr.translate(mirrorx, mirrory);
                addUnique(syms, tr);
            }
            case CF_P1M1 -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 2) {
                    if (tile.getScaleX() != 0.0)
                        mirrorx = data.get(1);
                    else
                        mirrory = data.get(1);
                } else if (data.size() > 2) {
                    system.error("p1m1 symmetry takes no arguments or an optional mirror axis position argument", where);
                }
                AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                tr.translate(-mirrorx, -mirrory);
                if (tile.getScaleX() != 0.0)
                    tr.scale(-1, 1);
                else
                    tr.scale(1, -1);
                tr.translate(mirrorx, mirrory);
                addUnique(syms, tr);
            }
            case CF_P2 -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 3) {
                    mirrorx = data.get(1);
                    mirrory = data.get(2);
                } else if (data.size() != 1) {
                    system.error("p2 symmetry takes no arguments or a center of rotation", where);
                }
                processDihedral(system, syms, 2.0, mirrorx, mirrory, false, 0.0, where);
            }
            case CF_P2MG -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 3) {
                    mirrorx = data.get(1);
                    mirrory = data.get(2);
                } else if (data.size() != 1) {
                    system.error("p2mg symmetry takes no arguments or a center of rotation", where);
                }
                AffineTransform tr1 = new AffineTransform();
                AffineTransform tr2 = AffineTransform.getTranslateInstance(-mirrorx, -mirrory);
                AffineTransform tr3 = AffineTransform.getTranslateInstance(-mirrorx, -mirrory);
                AffineTransform tr4 = AffineTransform.getTranslateInstance(-mirrorx, -mirrory);
                tr2.setToScale(-1, 1);
                tr3.setToScale(-1, -1);
                tr4.setToScale(1, -1);
                tr2.translate(tile.getScaleX() * 0.5 + mirrorx, tile.getScaleY() * 0.5 + mirrory);
                tr3.translate(mirrorx, mirrory);
                tr4.translate(tile.getScaleX() * 0.5 + mirrorx, tile.getScaleY() * 0.5 + mirrory);
                addUnique(syms, tr1);
                addUnique(syms, tr2);
                addUnique(syms, tr3);
                addUnique(syms, tr4);
            }
            case CF_P2MM -> {
                double mirrorx = 0.0, mirrory = 0.0;
                if (data.size() == 3) {
                    mirrorx = data.get(1);
                    mirrory = data.get(2);
                } else if (data.size() != 1) {
                    system.error("p2mm symmetry takes no arguments or a center of relection", where);
                }
                processDihedral(system, syms, 2.0, mirrorx, mirrory, true, 0.0, where);
            }
            case CF_PM -> {
                if (!rectangular && !square45) {
                    system.error("pm symmetry requires rectangular tiling", where);
                }
                double offset = 0.0;
                switch (data.size()) {
                    case 2:
                        break;
                    case 3:
                        offset = data.get(2);
                        break;
                    default:
                        system.error("pm symmetry takes a mirror axis argument and an optional axis position argument", where);
                }
                AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                int axis = data.get(1).intValue();
                if (rectangular && (axis < 0 || axis > 1))
                    system.error("pm symmetry mirror axis argument must be 0 or 1", where);
                else if (square45 && (axis < 2 || axis > 3))
                    system.error("pm symmetry mirror axis argument must be 2 or 3", where);
                switch (axis) {
                    case 0:         // mirror on x axis
                        tr.translate(0, -offset);
                        tr.setToScale(1, -1);
                        tr.translate(0, offset);
                        break;
                    case 1:         // mirror on y axis
                        tr.translate(-offset, 0);
                        tr.setToScale(-1, 1);
                        tr.translate(offset, 0);
                        break;
                    case 2:         // mirror on x=y axis
                        tr.translate(-offset * M_SQRT1_2, offset * M_SQRT1_2);
                        tr.concatenate(ref45);
                        tr.translate(offset * M_SQRT1_2, -offset * M_SQRT1_2);
                        break;
                    case 3:         // mirror on x=-y axis
                        tr.translate(-offset * M_SQRT1_2, -offset * M_SQRT1_2);
                        tr.concatenate(ref135);
                        tr.translate(offset * M_SQRT1_2, offset * M_SQRT1_2);
                        break;
                    default:
                        system.error("pm symmetry mirror axis argument must be 0, 1, 2, or 3", where);
                }
                addUnique(syms, tr);
            }
            case CF_PG -> {
                if (!rectangular && !square45) {
                    system.error("pg symmetry requires rectangular tiling", where);
                }
                double offset = 0.0;
                switch (data.size()) {
                    case 2:
                        break;
                    case 3:
                        offset = data.get(2);
                        break;
                    default:
                        system.error("pg symmetry takes a glide axis argument and an optional axis position argument", where);
                }
                AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                int axis = data.get(1).intValue();
                if (rectangular && (axis < 0 || axis > 1))
                    system.error("pg symmetry mirror axis argument must be 0 or 1", where);
                else if (square45 && (axis < 2 || axis > 3))
                    system.error("pg symmetry mirror axis argument must be 2 or 3", where);
                switch (axis) {
                    case 0:         // mirror on x axis
                        tr.translate(0, -offset);
                        tr.setToScale(1, -1);
                        tr.translate(tile.getScaleX() * 0.5, offset);
                        break;
                    case 1:         // mirror on y axis
                        tr.translate(-offset, 0);
                        tr.setToScale(-1, 1);
                        tr.translate(offset, tile.getScaleY() * 0.5);
                        break;
                    case 2:         // mirror on x=y axis
                        tr.translate(-offset * M_SQRT1_2, offset * M_SQRT1_2);
                        tr.concatenate(ref45);
                        tr.translate((offset + size45 * 0.5) * M_SQRT1_2, (-offset + size45 * 0.5) * M_SQRT1_2);
                        break;
                    case 3:         // mirror on x=-y axis
                        tr.translate(-offset * M_SQRT1_2, -offset * M_SQRT1_2);
                        tr.concatenate(ref135);
                        tr.translate((offset - size45 * 0.5) * M_SQRT1_2, (offset + size45 * 0.5) * M_SQRT1_2);
                        break;
                    default:
                        system.error("pg symmetry glide axis argument must be 0, 1, 2, or 3", where);
                }
                addUnique(syms, tr);
            }
            case CF_CM -> {
                if (!rhombic && !square) {
                    system.error("cm symmetry requires diamond tiling", where);
                }
                double offset = 0.0;
                switch (data.size()) {
                    case 2:
                        break;
                    case 3:
                        offset = data.get(2);
                        break;
                    default:
                        system.error("cm symmetry takes a mirror axis argument and an optional axis position argument", where);
                }
                AffineTransform tr = new AffineTransform();
                addUnique(syms, tr);
                int axis = data.get(1).intValue();
                if (rhombic && (axis < 0 || axis > 1))
                    system.error("cm symmetry mirror axis argument must be 0 or 1", where);
                else if (square && (axis < 2 || axis > 3))
                    system.error("cm symmetry mirror axis argument must be 2 or 3", where);
                switch (axis) {
                    case 0:         // mirror on x axis
                        tr.translate(0, -offset);
                        tr.setToScale(1, -1);
                        tr.translate(0, offset);
                        break;
                    case 1:         // mirror on y axis
                        tr.translate(-offset, 0);
                        tr.setToScale(-1, 1);
                        tr.translate(offset, 0);
                        break;
                    case 2:         // mirror on x=y axis
                        tr.translate(offset * M_SQRT1_2, -offset * M_SQRT1_2);
                        tr.concatenate(ref45);
                        tr.translate(-offset * M_SQRT1_2, offset * M_SQRT1_2);
                        break;
                    case 3:         // mirror on x=-y axis
                        tr.translate(-offset * M_SQRT1_2, -offset * M_SQRT1_2);
                        tr.concatenate(ref135);
                        tr.translate(offset * M_SQRT1_2, offset * M_SQRT1_2);
                        break;
                    default:
                        system.error("cm symmetry mirror axis argument must be 0, 1, 2, or 3", where);
                }
                addUnique(syms, tr);
            }
            case CF_PMM -> {
                if (!rectangular && !square45) {
                    system.error("pmm symmetry requires rectangular tiling", where);
                }
                double centerx = 0.0, centery = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        centerx = data.get(1);
                        centery = data.get(2);
                        break;
                    default:
                        system.error("pmm symmetry takes no arguments or a center of reflection", where);
                }
                processDihedral(system, syms, 2.0, centerx, centery, true, square45 ? M_PI_4 : 0.0, where);
            }
            case CF_PMG -> {
                if (!rectangular && !square45) {
                    system.error("pmg symmetry requires rectangular tiling", where);
                }
                double centerx = 0.0, centery = 0.0;
                switch (data.size()) {
                    case 2:
                        break;
                    case 4:
                        centerx = data.get(2);
                        centery = data.get(3);
                        break;
                    default:
                        system.error("pmg symmetry takes a mirror axis argument and an optional center of reflection", where);
                }
                AffineTransform tr = new AffineTransform();
                AffineTransform tr2 = new AffineTransform();
                int axis = data.get(1).intValue();
                if (rectangular && (axis < 0 || axis > 1))
                    system.error("pmg symmetry mirror axis argument must be 0 or 1", where);
                else if (square45 && (axis < 2 || axis > 3))
                    system.error("pmg symmetry mirror axis argument must be 2 or 3", where);
                switch (axis) {
                    case 0: {       // mirror on x axis
                        double cy = Math.abs(centery + 0.25 * tile.getScaleY()) < Math.abs(centery - 0.25 * tile.getScaleY()) ?
                                centery + 0.25 * tile.getScaleY() : centery - 0.25 * tile.getScaleY();
                        processDihedral(system, syms, 2.0, centerx, cy, false, 0.0, where);
                        tr.translate(-centerx, 0.0);
                        tr.scale(-1, 1);
                        tr.translate(centerx, 0.5 * tile.getScaleY());
                        addUnique(syms, tr);
                        tr2.translate(0.0, -centery);
                        tr2.scale(1, -1);
                        tr2.translate(0.0, centery);
                        addUnique(syms, tr2);
                        break;
                    }
                    case 1: {       // mirror on y axis
                        double cx = Math.abs(centerx + 0.25 * tile.getScaleX()) < Math.abs(centerx - 0.25 * tile.getScaleX()) ?
                                centerx + 0.25 * tile.getScaleX() : centerx - 0.25 * tile.getScaleX();
                        processDihedral(system, syms, 2.0, cx, centery, false, 0.0, where);
                        tr.translate(-centerx, 0.0);
                        tr.scale(-1, 1);
                        tr.translate(centerx, 0.0);
                        addUnique(syms, tr);
                        tr2.translate(0.0, -centery);
                        tr2.scale(1, -1);
                        tr2.translate(0.5 * tile.getScaleX(), centery);
                        addUnique(syms, tr2);
                        break;
                    }
                    case 2: {       // mirror on x=y axis
                        double cx = centerx - 0.25 * M_SQRT1_2 * size45;
                        double cy = centery + 0.25 * M_SQRT1_2 * size45;
                        double cx2 = centerx + 0.25 * M_SQRT1_2 * size45;
                        double cy2 = centery - 0.25 * M_SQRT1_2 * size45;
                        if (cx2 * cx2 + cy2 * cy2 < cx * cx + cy * cy) {
                            cx = cx2;
                            cy = cy2;
                        }
                        processDihedral(system, syms, 2.0, cx, cy, false, 0.0, where);
                        tr.translate(-centerx, -centery);   // mirror on x=y
                        tr.concatenate(ref45);
                        tr.translate(centerx, centery);
                        addUnique(syms, tr);
                        tr2.translate(-centerx, -centery);   // glide on x=-y
                        tr2.concatenate(ref135);
                        tr2.translate(centerx - size45 * M_SQRT1_2 * 0.5, centery + size45 * M_SQRT1_2 * 0.5);
                        addUnique(syms, tr2);
                        break;
                    }
                    case 3: {       // mirror on x=-y axis
                        double cx = centerx + 0.25 * M_SQRT1_2 * size45;
                        double cy = centery + 0.25 * M_SQRT1_2 * size45;
                        double cx2 = centerx - 0.25 * M_SQRT1_2 * size45;
                        double cy2 = centery - 0.25 * M_SQRT1_2 * size45;
                        if (cx2 * cx2 + cy2 * cy2 < cx * cx + cy * cy) {
                            cx = cx2;
                            cy = cy2;
                        }
                        processDihedral(system, syms, 2.0, cx, cy, false, 0.0, where);
                        tr.translate(-centerx, -centery);   // mirror on x=-y
                        tr.concatenate(ref135);
                        tr.translate(centerx, centery);
                        addUnique(syms, tr);
                        tr2.translate(-centerx, -centery);   // glide on x=y
                        tr2.concatenate(ref45);
                        tr2.translate(centerx + size45 * M_SQRT1_2 * 0.5, centery + size45 * M_SQRT1_2 * 0.5);
                        addUnique(syms, tr2);
                        break;
                    }
                    default:
                        system.error("pmg symmetry mirror axis argument must be 0, 1, 2, or 3", where);
                }
            }
            case CF_PGG -> {
                if (!rectangular && !square45) {
                    system.error("pgg symmetry requires rectangular tiling", where);
                }
                double centerx = 0.0, centery = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        centerx = data.get(1);
                        centery = data.get(2);
                        break;
                    default:
                        system.error("pgg symmetry takes no arguments or a center of glide axis intersection", where);
                }
                if (square45) {
                    double cx = centerx + 0.25 * M_SQRT2 * size45;
                    double cy = centery;
                    double cx2 = centerx - 0.25 * M_SQRT2 * size45;
                    double cy2 = centery;
                    if (cx2 * cx2 + cy2 * cy2 < cx * cx + cy * cy) {
                        cx = cx2;
                        cy = cy2;
                    }
                    cx2 = centerx;
                    cy2 = centery + 0.25 * M_SQRT2 * size45;
                    if (cx2 * cx2 + cy2 * cy2 < cx * cx + cy * cy) {
                        cx = cx2;
                        cy = cy2;
                    }
                    cx2 = centerx;
                    cy2 = centery - 0.25 * M_SQRT2 * size45;
                    if (cx2 * cx2 + cy2 * cy2 < cx * cx + cy * cy) {
                        cx = cx2;
                        cy = cy2;
                    }
                    processDihedral(system, syms, 2.0, cx, cy, false, 0.0, where);
                    AffineTransform tr = new AffineTransform();
                    AffineTransform tr2 = new AffineTransform();
                    //TODO are coordinates inverted?
                    tr.translate(centerx, centery);   // glide on x=y
                    tr.concatenate(ref45);
                    tr.translate(centerx + size45 * M_SQRT1_2 * 0.5, centery + size45 * M_SQRT1_2 * 0.5);
                    addUnique(syms, tr);
                    //TODO are coordinates inverted?
                    tr2.translate(centerx, centery);   // glide on x=-y
                    tr.concatenate(ref135);
                    tr2.translate(centerx - size45 * M_SQRT1_2 * 0.5, centery + size45 * M_SQRT1_2 * 0.5);
                    addUnique(syms, tr2);
                    break;
                }
                double cx = Math.abs(centerx + 0.25 * tile.getScaleX()) < Math.abs(centerx - 0.25 * tile.getScaleX()) ?
                        centerx + 0.25 * tile.getScaleX() : centerx - 0.25 * tile.getScaleX();
                double cy = Math.abs(centery + 0.25 * tile.getScaleY()) < Math.abs(centery - 0.25 * tile.getScaleY()) ?
                        centery + 0.25 * tile.getScaleY() : centery - 0.25 * tile.getScaleY();
                processDihedral(system, syms, 2.0, cx, cy, false, 0.0, where);
                AffineTransform tr = new AffineTransform();
                AffineTransform tr2 = new AffineTransform();
                //TODO are coordinates inverted?
                tr.translate(centerx, 0.0);
                tr.scale(-1, 1);
                //TODO are coordinates inverted?
                tr.translate(-centerx, -0.5 * tile.getScaleY());
                addUnique(syms, tr);
                //TODO are coordinates inverted?
                tr2.translate(0.0, centery);
                tr2.scale(1, -1);
                //TODO are coordinates inverted?
                tr2.translate(-0.5 * tile.getScaleX(), -centery);
                addUnique(syms, tr2);
            }
            case CF_CMM -> {
                if (!rhombic && !square) {
                    system.error("cmm symmetry requires diamond tiling", where);
                }
                double centerx = 0.0, centery = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        centerx = data.get(1);
                        centery = data.get(2);
                        break;
                    default:
                        system.error("cmm symmetry takes no arguments or a center of reflection", where);
                }
                processDihedral(system, syms, 2.0, centerx, centery, true, square45 ? M_PI_4 : 0.0, where);
            }
            case CF_P4, CF_P4M -> {
                if (!square && !square45) {
                    system.error("p4 & p4m symmetry requires square tiling", where);
                }
                double x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        x = data.get(1);
                        y = data.get(2);
                        break;
                    default:
                        system.error("p4 & p4m symmetry takes no arguments or a center of rotation", where);
                }
                processDihedral(system, syms, 4.0, x, y, type == FlagType.CF_P4M.getMask(), square ? M_PI_4 : 0.0, where);
            }
            case CF_P4G -> {
                if (!square && !square45) {
                    system.error("p4g symmetry requires square tiling", where);
                }
                double centerx = 0.0, centery = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        centerx = data.get(1);
                        centery = data.get(2);
                        break;
                    default:
                        system.error("p4g symmetry takes no arguments or a center of rotation", where);
                }
                AffineTransform reg = new AffineTransform();
                //TODO are coordinates inverted?
                reg.translate(centerx, centery);
                AffineTransform glide = new AffineTransform(reg);
                if (square45) {
                    glide.translate(-size45 * 0.25 * M_SQRT1_2, -size45 * 0.25 * M_SQRT1_2);
                    glide.concatenate(ref135);
                    glide.translate(-size45 * 0.25 * M_SQRT1_2, size45 * 0.75 * M_SQRT1_2);
                } else {
                    glide.translate(tile.getScaleX() * 0.25, 0.0);
                    glide.scale(-1, 1);
                    glide.translate(-tile.getScaleX() * 0.25, tile.getScaleY() * 0.5);
                }
                for (int i = 0; i < 4; ++i) {
                    AffineTransform tr = new AffineTransform(reg);
                    AffineTransform tr2 = new AffineTransform(glide);
                    if (i != 0) {
                        tr.rotate(i * M_PI_2);
                        tr2.rotate(i * M_PI_2);
                    }
                    //TODO are coordinates inverted?
                    tr.translate(-centerx, -centery);
                    //TODO are coordinates inverted?
                    tr2.translate(-centerx, -centery);
                    addUnique(syms, tr);
                    addUnique(syms, tr2);
                }
            }
            case CF_P3 -> {
                if (!hexagonal) {
                    system.error("p3 symmetry requires hexagonal tiling", where);
                }
                double x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        x = data.get(1);
                        y = data.get(2);
                        break;
                    default:
                        system.error("p3 symmetry takes no arguments or a center of rotation", where);
                }
                processDihedral(system, syms, 3.0, x, y, false, 0.0, where);
            }
            case CF_P3M1, CF_P31M -> {
                if (!hexagonal) {
                    system.error("p3m1 & p31m symmetry requires hexagonal tiling", where);
                }
                double x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        x = data.get(1);
                        y = data.get(2);
                        break;
                    default:
                        system.error("p3m1 & p31m symmetry takes no arguments or a center of rotation", where);
                }
                boolean deg30 = (Math.abs(tile.getShearX()) <= 0.000001) != (type == FlagType.CF_P3M1.getMask());
                double angle = M_PI / (deg30 ? 6.0 : 3.0);
                processDihedral(system, syms, 3.0, x, y, true, angle, where);
            }
            case CF_P6, CF_P6M -> {
                if (!hexagonal) {
                    system.error("p6 & p6m symmetry requires hexagonal tiling", where);
                }
                double x = 0.0, y = 0.0;
                switch (data.size()) {
                    case 1:
                        break;
                    case 3:
                        x = data.get(1);
                        y = data.get(2);
                        break;
                    default:
                        system.error("p6 & p6m symmetry takes no arguments or a center of rotation", where);
                }
                processDihedral(system, syms, 6.0, x, y, type == FlagType.CF_P6M.getMask(), 0.0, where);
            }
            default -> system.error("Unknown symmetry type", where);
            // never gets here
        }

        data.clear();
    }

    public static ASTExpression getFlagsAndStroke(CFDGSystem system, List<ASTModTerm> terms, long[] flags) {
        List<ASTModTerm> temp = new ArrayList<>(terms);
        terms.clear();
        ASTExpression ret = null;
        for (ASTModTerm term : temp) {
            switch (term.getModType()) {
                case param -> flags[0] |= term.getArgCountOrFlags();
                case stroke -> {
                    if (ret != null) {
                        system.error("Only one stroke width term is allowed", term.getWhere());
                    }
                    ret = term.getArguments();
                    term.setArguments(null);
                }
                default -> terms.add(term);
            }
        }
        return ret;
    }

    public static List<ASTModification> getTransforms(CFDGBuilder builder, ASTExpression expression, List<AffineTransform> syms, CFDGRenderer renderer, boolean tiled, AffineTransform transform) {
        List<ASTModification> result = new ArrayList<>();

        syms.clear();

        if (expression == null) {
            return result;
        }

        List<Double> symmSpec = new ArrayList<>();

        boolean snarfFlagOpts = false;

        ASTWhere where = null;

        for (int i = 0; i < expression.size(); i++) {
            ASTExpression cit = expression.getChild(i);
            switch (cit.getType()) {
                case Flag -> {
                    if (snarfFlagOpts) {
                        processSymmSpec(builder.getSystem(), syms, transform, tiled, symmSpec, where);
                    }
                    snarfFlagOpts = true;
                    where = cit.getWhere();
                    int sz = cit.evaluate(builder, null, 0);
                    if (sz < 1) {
                        builder.error("Could not evaluate this", cit.getWhere());
                    } else {
                        double[] values = new double[sz];
                        if (cit.evaluate(builder, values, values.length) != sz) {
                            builder.error("Could not evaluate this", cit.getWhere());
                        } else {
                            for (double value : values) {
                                symmSpec.add(value);
                            }
                        }
                    }
                }
                case Numeric -> {
                    where = cit.getWhere();
                    if (snarfFlagOpts) {
                        int sz = cit.evaluate(builder, null, 0);
                        if (sz < 1) {
                            builder.error("Could not evaluate this", cit.getWhere());
                        } else {
                            double[] values = new double[sz];
                            if (cit.evaluate(builder, values, values.length) != sz) {
                                builder.error("Could not evaluate this", cit.getWhere());
                            } else {
                                for (double value : values) {
                                    symmSpec.add(value);
                                }
                            }
                        }
                    } else {
                        builder.error("Symmetry flag expected here", cit.getWhere());
                    }
                }
                case Mod -> {
                    if (snarfFlagOpts) {
                        processSymmSpec(builder.getSystem(), syms, transform, tiled, symmSpec, where);
                    }
                    snarfFlagOpts = false;
                    if (cit instanceof ASTModification m) {
                        if (m.getModClass() != null && m.getModClass().getType() == (ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()) && (renderer != null || m.isConstant())) {
                            Modification mod = new Modification();
                            cit.evaluate(builder, renderer, mod, false);
                            addUnique(syms, mod.getTransform());
                        } else {
                            result.add(m);
                        }
                    } else {
                        builder.error("Wrong type", cit.getWhere());
                    }
                }
                default -> builder.error("Wrong type", cit.getWhere());
            }
        }

        if (snarfFlagOpts) {
            processSymmSpec(builder.getSystem(), syms, transform, tiled, symmSpec, where);
        }

        return result;
    }

    public static void addUnique(List<AffineTransform> syms, AffineTransform transform) {
        if (!syms.contains(transform)) {
            syms.add((AffineTransform) transform.clone());
        }
    }

    public static Locality combineLocality(Locality locality1, Locality locality2) {
        return Locality.fromType(locality1.getType() | locality2.getType());
    }

    public static List<ASTExpression> extract(ASTExpression exp) {
        if (exp instanceof ASTCons) {
            return ((ASTCons)exp).getChildren();
        } else {
            List<ASTExpression> ret = new ArrayList<>();
            ret.add(exp);
            return ret;
        }
    }

    public static ASTExpression makeResult(double[] result, int length, ASTExpression from) {
        ASTExpression ret = null;
        for (int i = 0; i < length; i++) {
            final ASTReal r = new ASTReal(from.getSystem(), from.getWhere(), result[i]);
            r.setType(from.getType());
            r.setNatural(from.isNatural());
            ret = ret != null ? ret.append(r) : r;
        }
        return ret;
    }

    private static AffineTransform getMirrorTransform(double angle) {
        return getMirrorTransform(Math.cos(angle), Math.sin(angle));
    }

    private static AffineTransform getMirrorTransform(double ux, double uy) {
        return new AffineTransform(
                2.0 * ux * ux - 1.0,
                2.0 * ux * uy,
                2.0 * ux * uy,
                2.0 * uy * uy - 1.0,
                0.0, 0.0);
    }
}
