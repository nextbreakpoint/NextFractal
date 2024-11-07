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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTDefine;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModTerm;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReal;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRepContainer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReplacement;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRuleSpecifier;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTStartSpecifier;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CFG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Param;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.WeightType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere.DEFAULT_WHERE;

// cfdgImpl.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2005-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2005-2013 John Horigan - john@glyphic.com
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

@Log
public class CFDG {
	private final List<ShapeElement> shapeTypes = new ArrayList<>();
	private final List<String> fileNames = new ArrayList<>();

	private ASTReplacement initShape;
	private final Stack<ASTRule> rules = new Stack<>();
	private final Map<Integer, ASTDefine> functions = new HashMap<>();

	private final Map<CFG, Integer> paramDepth = new HashMap<>();
	private final Map<CFG, ASTExpression> paramExp = new HashMap<>();

	@Getter
    private final ASTRepContainer contents;
	private final ASTRule needle;

	@Getter
    private double[] backgroundColor = new double[] { 1, 1, 1, 1 };

	private Modification tileMod = new Modification();
	private Modification sizeMod = new Modification();
	private Modification timeMod = new Modification();
	private final Point2D tileOffset = new Point2D.Double(0, 0);

	private int parameters;
	private int stackSize;
	private int defaultShape = -1;

	@Getter
	@Setter
	private boolean impure;

	private boolean usesAlpha;
	private boolean usesColor;
	private boolean usesTime;
	private boolean usesBlendMode;
	private boolean usesFrameTime;
	private boolean uses16bitColor;

	@Getter
	private final CFDGSystem system;
	@Getter
	private final String version;

	@Setter
	private CFDGBuilder builder;

	public CFDG(CFDGSystem system, String version) {
		this.system = system;
		this.version = version;

        contents = new ASTRepContainer(system, DEFAULT_WHERE);
		contents.setGlobal(true);

		needle = new ASTRule(system, DEFAULT_WHERE, -1);

		PrimShape.getShapeNames()
				.forEach(s -> encodeShapeName(s, contents.getWhere()));

		initVariables();
	}

	public boolean usesAlpha() {
		return usesAlpha;
	}

	public boolean usesColor() {
		return usesColor;
	}

	public boolean usesTime() {
		return usesTime;
	}

	public boolean usesFrameTime() {
		return usesFrameTime;
	}

	private void initVariables() {
        final String piName = "\u03C0";
		final int piIndex = encodeShapeName(piName, ASTWhere.DEFAULT_WHERE);
		final ASTDefine piDef = new ASTDefine(system, ASTWhere.DEFAULT_WHERE, piName);
		piDef.setExp(new ASTReal(system, ASTWhere.DEFAULT_WHERE, Math.PI));
		piDef.getShapeSpecifier().setShapeType(piIndex);
		contents.addDefParameter(piIndex, piDef, ASTWhere.DEFAULT_WHERE);
		contents.getBody().add(piDef);

		final List<String> circleNames = List.of("\u2B24", "\u26AB", "\u25CF");
		circleNames.forEach(name -> {
			final int index = encodeShapeName(name, ASTWhere.DEFAULT_WHERE);
			final ASTDefine def = new ASTDefine(system, ASTWhere.DEFAULT_WHERE, name);
			def.setExp(new ASTRuleSpecifier(system, ASTWhere.DEFAULT_WHERE, PrimShapeType.circleType.getType(), "CIRCLE", null, null));
			def.getShapeSpecifier().setShapeType(index);
			contents.addDefParameter(index, def, ASTWhere.DEFAULT_WHERE);
			contents.getBody().add(def);
		});

		final List<String> squareNames = List.of("\u2B1B", "\u25FC", "\u25FE", "\uFFED");
		squareNames.forEach(name -> {
			final int index = encodeShapeName(name, ASTWhere.DEFAULT_WHERE);
			final ASTDefine def = new ASTDefine(system, ASTWhere.DEFAULT_WHERE, name);
			def.setExp(new ASTRuleSpecifier(system, ASTWhere.DEFAULT_WHERE, PrimShapeType.squareType.getType(), "SQUARE", null, null));
			def.getShapeSpecifier().setShapeType(index);
			contents.addDefParameter(index, def, ASTWhere.DEFAULT_WHERE);
			contents.getBody().add(def);
		});

		final List<String> triangleNames = List.of("\u25B2", "\u25B4");
		triangleNames.forEach(name -> {
			final int index = encodeShapeName(name, ASTWhere.DEFAULT_WHERE);
			final ASTDefine def = new ASTDefine(system, ASTWhere.DEFAULT_WHERE, name);
			def.setExp(new ASTRuleSpecifier(system, ASTWhere.DEFAULT_WHERE, PrimShapeType.triangleType.getType(), "TRIANGLE", null, null));
			def.getShapeSpecifier().setShapeType(index);
			contents.addDefParameter(index, def, ASTWhere.DEFAULT_WHERE);
			contents.getBody().add(def);
		});
	}

	public Shape getInitialShape(CFDGRenderer renderer) {
		Shape shape = new Shape();
		shape.worldState.setColor(new HSBColor(0,0,0,1));
		shape.worldState.setColorTarget(new HSBColor(0,0,0,1));
		shape.worldState.getTransformTime().setEnd(1);
		initShape.replace(builder, renderer, shape);
		shape.worldState.getTransform().translate(tileOffset.getX(), tileOffset.getY());
		return shape;
	}

    public void initBackgroundColor(CFDGRenderer renderer) {
		Modification white = new Modification();
		white.setColor(new HSBColor(0.0, 0.0, 1.0, 1.0));
		if (hasParameter(CFG.Background, white, renderer)) {
			backgroundColor = white.color().getRGBA();
			if (!usesAlpha) {
				backgroundColor[3] = 1.0;
			}
		}
	}

	public ASTRule findRule(int nameIndex, double weight) {
		needle.setNameIndex(nameIndex);
		needle.setWeight(weight);
		int first = lowerBound(rules, 0, rules.size(), needle);
		if (first == rules.size() || rules.get(first).getNameIndex() != nameIndex) {
			throw new CFDGException("Can't find a rule for a shape (very helpful I know)", DEFAULT_WHERE);
		}
		return rules.get(first);
	}

	// Search for a rule in the mRules list even before it is sorted
	public ASTRule findRule(int nameIndex) {
		for (ASTRule rule: rules) {
			if (rule.getNameIndex() == nameIndex) {
				return rule;
			}
		}
		return null;
	}

	public boolean addRule(ASTRule rule) {
		rules.push(rule);
		ShapeElement type = shapeTypes.get(rule.getNameIndex());
		if (type.getShapeType() == ShapeType.NewShape) {
			type.setShapeType(rule.isPath() ? ShapeType.PathType : ShapeType.RuleType);
		}
		if (type.getParameters() != null && !type.getParameters().isEmpty()) {
			rule.getRuleBody().setParameters(type.getParameters());
		}
		type.setHasRules(true);
		return type.isShape();
	}

	public void addParameter(Param param) {
		parameters |= param.getType();
		usesColor = (parameters & Param.Color.getType()) != 0;
		usesTime = (parameters & Param.Time.getType()) != 0;
		usesFrameTime = (parameters & Param.FrameTime.getType()) != 0;
		usesBlendMode = (parameters & Param.Blend.getType()) != 0;
	}

	public double[] getColor(HSBColor hsb) {
        return hsb.getRGBA();
	}

	public boolean isTiled(AffineTransform transform, double[] point) {
		ASTWhere[] where = new ASTWhere[1];
		if (!hasParameter(CFG.Tile, ExpType.Mod, where)) {
			return false;
		}

		if (tileMod.getTransform().getScaleX() == 0 || tileMod.getTransform().getScaleY() == 0) {
			return false;
		}

		if (transform != null) {
			transform.setTransform(new AffineTransform(tileMod.getTransform().getScaleX(), tileMod.getTransform().getShearY(), tileMod.getTransform().getShearX(), tileMod.getTransform().getScaleY(), 0, 0));
		}

		if (point != null && point.length == 2) {
			double o_x = 0.0;
			double o_y = 0.0;
			double u_x = 1.0;
			double u_y = 0.0;
			double v_x = 0.0;
			double v_y = 1.0;

			Point2D.Double o = new Point2D.Double(o_x, o_y);
			Point2D.Double u = new Point2D.Double(u_x, u_y);
			Point2D.Double v = new Point2D.Double(v_x, v_y);

			tileMod.getTransform().transform(o, o);
			tileMod.getTransform().transform(u, u);
			tileMod.getTransform().transform(v, v);

			if (Math.abs(u.y - o.y) >= 0.0001 && Math.abs(v.x - o.x) >= 0.0001) {
				system.error("Tile must be aligned with the X or Y axis", where[0]);
			}

			if ((u.x - o.x) < 0.0 || (v.y - o.y) < 0.0) {
				system.error("Tile must be in the positive X/Y quadrant", where[0]);
			}

			point[0] = u.x - o.x;
			point[1] = v.y - o.y;
		}

		return true;
	}

	public FriezeType isFrieze(AffineTransform transform, double[] point) {
		ASTWhere[] where = new ASTWhere[1];
		if (!hasParameter(CFG.Tile, ExpType.Mod, where)) {
			return FriezeType.NoFrieze;
		}

		if (tileMod.getTransform().getScaleX() != 0 && tileMod.getTransform().getScaleY() != 0) {
			return FriezeType.NoFrieze;
		}

		if (tileMod.getTransform().getScaleX() == 0 && tileMod.getTransform().getScaleY() == 0) {
			return FriezeType.NoFrieze;
		}

		if (transform != null) {
			transform.setTransform(new AffineTransform(tileMod.getTransform().getScaleX(), tileMod.getTransform().getShearY(), tileMod.getTransform().getShearX(), tileMod.getTransform().getScaleY(), 0, 0));
		}

		if (point != null && point.length == 2) {
			double o_x = 0.0;
			double o_y = 0.0;
			double u_x = 1.0;
			double u_y = 0.0;
			double v_x = 0.0;
			double v_y = 1.0;

			Point2D.Double o = new Point2D.Double(o_x, o_y);
			Point2D.Double u = new Point2D.Double(u_x, u_y);
			Point2D.Double v = new Point2D.Double(v_x, v_y);

			tileMod.getTransform().transform(o, o);
			tileMod.getTransform().transform(u, u);
			tileMod.getTransform().transform(v, v);

			if (Math.abs(u.y - o.y) >= 0.0001 && Math.abs(v.x - o.x) >= 0.0001) {
				system.error("Frieze must be aligned with the X or Y axis", where[0]);
			}

			if ((u.x - o.x) < 0.0 || (v.y - o.y) < 0.0) {
				system.error("Frieze must be in the positive X/Y quadrant", where[0]);
			}

			point[0] = u.x - o.x;
			point[1] = v.y - o.y;
		}

		return tileMod.getTransform().getScaleX() == 0.0 ? FriezeType.FriezeY : FriezeType.FriezeX;
	}

	public boolean isTiledOrFrieze() {
        return hasParameter(CFG.Tile, ExpType.Mod, new ASTWhere[1]);
	}

	public boolean isSized(double[] point) {
		ASTWhere[] where = new ASTWhere[1];
		if (!hasParameter(CFG.Size, ExpType.Mod, where)) {
			return false;
		}

		if (point != null && point.length == 2) {
			point[0] = sizeMod.getTransform().getScaleX();
			point[1] = sizeMod.getTransform().getScaleY();
		}

		if (sizeMod.getTransform().getShearX() != 0.0 || sizeMod.getTransform().getShearY() != 0.0) {
			system.error("Size specification must not be rotated or skewed", where[0]);
		}

		return true;
	}

	public boolean isTimed(AffineTransformTime transform) {
		ASTWhere[] where = new ASTWhere[1];
		if (!hasParameter(CFG.Time, ExpType.Mod, where)) {
			return false;
		}

		if (transform != null) {
			transform.setBegin(timeMod.getTransformTime().getBegin());
			transform.setEnd(timeMod.getTransformTime().getEnd());
			transform.setStep(timeMod.getTransformTime().getStep());
		}

		if (sizeMod.getTransformTime().getBegin() >= sizeMod.getTransformTime().getEnd()) {
			system.error("Time specification must have positive duration", where[0]);
		}

		return true;
	}

	public void getSymmetry(CFDGBuilder builder, CFDGRenderer renderer, List<AffineTransform> syms) {
		syms.clear();
		ASTExpression exp = hasParameter(CFG.Symmetry);
		List<ASTModification> left = AST.getTransforms(builder, exp, syms, renderer, isTiled(null, null), tileMod.getTransform());
		if (!left.isEmpty()) {
			system.error("At least one term was invalid", exp.getWhere());
		}
	}

	public boolean hasParameter(CFG param, double[] value, CFDGRenderer renderer) {
		ASTExpression exp = hasParameter(param);
		if (exp == null || exp.getType() != ExpType.Numeric) {
			return false;
		}
		if (!exp.isConstant() && renderer != null) {
			system.error("This expression must be constant", exp.getWhere());
			return false;
		} else {
			exp.evaluate(builder, renderer, value, 1);
		}
		return true;
	}

	public boolean hasParameter(CFG param, Modification value, CFDGRenderer renderer) {
		ASTExpression exp = hasParameter(param);
		if (exp == null || exp.getType() != ExpType.Mod) {
			return false;
		}
		if (!exp.isConstant() && renderer != null) {
			system.error("This expression must be constant", exp.getWhere());
			return false;
		} else {
			exp.evaluate(builder, renderer, value, true);
		}
		return true;
	}

	public boolean hasParameter(CFG param, ExpType type, ASTWhere[] where) {
		ASTExpression exp = hasParameter(param);
        if (exp == null || exp.getType() != type) {
			return false;
		}
		if (where != null) {
			where[0] = exp.getWhere();
		}
		return true;
    }

	public ASTExpression hasParameter(CFG param) {
		return paramExp.get(param);
	}

	public void addParameter(CFG param, ASTExpression exp, int depth) {
		Integer oldDepth = paramDepth.get(param);
		if (oldDepth == null || depth < oldDepth) {
			paramDepth.put(param, depth);
			paramExp.put(param, exp);
		}
	}

	public void rulesLoaded() {
		double[] weightSums = new double[shapeTypes.size()];
		double[] percentWeightSums = new double[shapeTypes.size()];
		double[] unitWeightSums = new double[shapeTypes.size()];
		int[] ruleCounts = new int[shapeTypes.size()];
		int[] weightTypes = new int[shapeTypes.size()];

		for (ASTRule rule : rules) {
			if (rule.getWeightType() == WeightType.PercentWeight) {
				percentWeightSums[rule.getNameIndex()] += rule.getWeight();
				if (percentWeightSums[rule.getNameIndex()] > 1.0001) {
					system.error("Percentages exceed 100%", rule.getWhere());
				}
			} else {
				weightSums[rule.getNameIndex()] += rule.getWeight();
			}
			ruleCounts[rule.getNameIndex()] += 1;
			weightTypes[rule.getNameIndex()] |= rule.getWeightType().getType();
		}

		for (ASTRule rule : rules) {
			double weight = rule.getWeight() / weightSums[rule.getNameIndex()];
			if ((weightTypes[rule.getNameIndex()] & WeightType.PercentWeight.getType()) != 0) {
				if (rule.getWeightType() == WeightType.PercentWeight) {
					weight = rule.getWeight();
				} else {
					weight *= 1.0 - percentWeightSums[rule.getNameIndex()];
					if (percentWeightSums[rule.getNameIndex()] > 0.9999) {
						system.error("Percentages sum to 100%, this rule has no weight", rule.getWhere());
					}
				}
			}
			if (weightTypes[rule.getNameIndex()] == WeightType.PercentWeight.getType() && Math.abs(percentWeightSums[rule.getNameIndex()] - 1.0) > 0.0001) {
				system.error("Percentages do not sum to 100%", rule.getWhere());
			}
			if (!Double.isFinite(weight)) {
				weight = 0;
			}
			unitWeightSums[rule.getNameIndex()] += weight;
			if (ruleCounts[rule.getNameIndex()] - 1 > 0) {
				rule.setWeight(unitWeightSums[rule.getNameIndex()]);
			} else {
				rule.setWeight(1.1);
			}
		}

		Collections.sort(rules);

		builder.setLocalStackDepth(0);
		builder.setInPathContainer(false);
		contents.compile(builder, CompilePhase.TypeCheck, null, null);
		builder.setInPathContainer(false);
		if (!system.isErrorOccurred()) {
			contents.compile(builder, CompilePhase.Simplify, null, null);
		}

		double[] value = new double[1];
		uses16bitColor = hasParameter(CFG.ColorDepth, value, null) && Math.floor(value[0]) == 16;

		if (hasParameter(CFG.Color, value, null)) {
			usesColor = value[0] != 0;
		}

		if (hasParameter(CFG.Alpha, value, null)) {
			usesColor = value[0] != 0;
		}

		ASTExpression e = hasParameter(CFG.Background);
		if (e instanceof ASTModification m) {
            usesAlpha = m.getModData().color().alpha() != 1.0;
			for (ASTModTerm term : m.getModExp()) {
                if (term.getModType() == ModType.alpha || term.getModType() == ModType.alphaTarg) {
                    usesAlpha = true;
                }
			}
		}
	}

	public int numRules() {
		return rules.size();
	}

	public String decodeShapeName(int shape) {
		if (shape < shapeTypes.size()) {
			return shapeTypes.get(shape).getName();
		} else {
			return "**unnamed shape**";
		}
	}

	public ASTWhere decodeShapeLocation(int shapeType) {
		if (shapeType < shapeTypes.size()) {
			return shapeTypes.get(shapeType).getFirstUse();
		} else {
			return DEFAULT_WHERE;
		}
	}

	public int tryEncodeShapeName(String s) {
	    for (int i = 0; i < shapeTypes.size(); i++) {
	        if (s.equals(shapeTypes.get(i).getName())) {
	            return i;
	        }
	    }
	    return -1;
	}

	public int encodeShapeName(String s, ASTWhere where) {
		int i = tryEncodeShapeName(s);
		if (i >= 0) return i;
		shapeTypes.add(new ShapeElement(where, s));
		return shapeTypes.size() - 1;
	}

	public ShapeType getShapeType(int nameIndex) {
		return shapeTypes.get(nameIndex).getShapeType();
	}

	public boolean shapeHasRules(int nameIndex) {
		if (nameIndex < shapeTypes.size()) {
			return shapeTypes.get(nameIndex).hasRules();
		}
		return false;
	}

	public void setShapeHasNoParam(int nameIndex, ASTExpression args) {
		if (nameIndex < shapeTypes.size() && args == null) {
			shapeTypes.get(nameIndex).setShouldHaveNoParams(true);
		}
	}

	public boolean getShapeHasNoParam(int nameIndex) {
		if (nameIndex < shapeTypes.size()) {
			return shapeTypes.get(nameIndex).isShouldHaveNoParams();
		}
		return false;
	}

	public String setShapeParams(int nameIndex, ASTRepContainer p, int argSize, boolean isPath) {
		ShapeElement type = shapeTypes.get(nameIndex);
		if (type.isShape()) {
			if (!p.getParameters().isEmpty()) {
				return "Shape has already been declared. Parameter declaration must be on the first shape declaration only";
			}
			if (type.getShapeType() == ShapeType.PathType && !isPath) {
				return "Shape name already in use by another rule or path";
			}
			if (isPath) {
				return "Path name already in use by another rule or path";
			}
			return null;
		}
		if (type.getShapeType() != ShapeType.NewShape) {
			return "Shape name already in use by another rule or path";
		}
		if (defaultShape == -1 && p.getParameters().isEmpty())
			defaultShape = nameIndex;
		type.setParameters(new ArrayList<>(p.getParameters()));
		type.setShape(true);
		type.setArgSize(argSize);
		type.setShapeType(isPath ? ShapeType.PathType : ShapeType.NewShape);
		return null;
	}

	public List<ASTParameter> getShapeParams(int nameIndex) {
		if (nameIndex < 0 || nameIndex >= shapeTypes.size() || !shapeTypes.get(nameIndex).isShape()) {
			return null;
		}
		return shapeTypes.get(nameIndex).getParameters();
	}

	public int getShapeParamsSize(int nameIndex) {
		if (nameIndex < 0 || nameIndex >= shapeTypes.size()) {
			return 0;
		}
		return shapeTypes.get(nameIndex).getArgSize();
	}

	public int reportStackDepth(int size) {
		if (size > stackSize) {
			stackSize = size;
		}
		return stackSize;
	}

	public void resetCachedPaths() {
		for (ASTRule rule : rules) {
			rule.resetCachedPath();
		}
	}

	public ASTDefine declareFunction(int nameIndex, ASTDefine def) {
		ASTDefine prev = findFunction(nameIndex);
		if (prev != null) {
			return prev;
		}
		functions.put(nameIndex, def);
		return def;
	}

	public ASTDefine findFunction(int index) {
        return functions.get(index);
    }

	public CFDGRenderer createRenderer(int width, int height, double minSize, int variation, double border) {
		ASTWhere where = new ASTWhere(0, 0, 0, 0, "");
		try {
			ASTExpression startExp = paramExp.get(CFG.StartShape);
			if (startExp == null) {
				if (defaultShape != -1) {
					// Synthesize a valid, type-checked startshape based on the default shape
					ASTStartSpecifier start = new ASTStartSpecifier(system, shapeTypes.get(defaultShape).getFirstUse(), defaultShape, shapeTypes.get(defaultShape).getName(), new ASTModification(system, where));
					start.setArgSource(ArgSource.NoArgs);
					start.setSimpleRule(CFStack.createStackRule(defaultShape, 0, null));
					start.setConstant(true);
					start.setLocality(Locality.PureLocal);
					startExp = start;
					system.info("Using %s as the startshape".formatted(shapeTypes.get(defaultShape).getName()), where);
				} else {
					system.error("No startshape found", where);
					return null;
				}
			}

			if (startExp instanceof ASTStartSpecifier specStart) {
                initShape = new ASTReplacement(system, startExp.getWhere(), specStart, specStart.getModification(), RepElemType.empty);
				initShape.getChildChange().addEntropy(initShape.getShapeSpecifier().getEntropy());
			} else {
				system.error("Type error in startshape", startExp.getWhere());
				return null;
			}

			CFDGRenderer renderer = new CFDGRenderer(builder, width, height, minSize, variation, border);

			renderer.setImpure(impure);

			Modification tiled = new Modification();
			Modification sized = new Modification();
			Modification timed = new Modification();

			double[] maxShape = new double[1];

			if (hasParameter(CFG.Tile, tiled, null)) {
				tileMod = tiled;
				AffineTransform transform = tileMod.getTransform();
				tileOffset.setLocation(transform.getTranslateX(), transform.getTranslateY());
				AffineTransform t = new AffineTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), 0, 0);
				tileMod.setTransform(t);
			}

			if (hasParameter(CFG.Size, sized, null)) {
				sizeMod = sized;
				AffineTransform transform = sizeMod.getTransform();
				tileOffset.setLocation(transform.getTranslateX(), transform.getTranslateY());
				AffineTransform t = new AffineTransform(transform.getScaleX(), transform.getShearY(), transform.getShearX(), transform.getScaleY(), 0, 0);
				tileMod.setTransform(t);
			}

			if (hasParameter(CFG.Time, timed, null)) {
				if (timed.getTransformTime().getEnd() <= timed.getTransformTime().getBegin() ||
						!Double.isFinite(timed.getTransformTime().getBegin()) ||
						!Double.isFinite(timed.getTransformTime().getEnd()) ||
						!Double.isFinite(timed.getTransformTime().getStep()))
				{
					ASTWhere[] paramWhere = new ASTWhere[1];
					hasParameter(CFG.Time, ExpType.Mod, paramWhere);
					system.error("Illegal CF::Time specification", paramWhere[0]);
					return null;
				}
				timeMod = timed;
				double[] frameVal = new double[1];
				double[] ftimeVal = new double[1];
				boolean frame = hasParameter(CFG.Frame, frameVal, null);
				boolean ftime = hasParameter(CFG.FrameTime, ftimeVal, null);
				if (frame || ftime) {
					if (frame && ftime) {
						system.error("It is not necessary to specify both CF::Frame and CF::FrameTime", where);
					} else if (frame) {
						ftimeVal[0] = (timed.getTransformTime().getEnd() - timed.getTransformTime().getBegin()) * frameVal[0] + timed.getTransformTime().getBegin();
						ASTExpression e = new ASTReal(system, where, ftimeVal[0]);
						addParameter(CFG.FrameTime, e, 0);
						system.error("Setting CF::FrameTime to %f from CF::Frame", where);
					} else /* if (ftime) */ {
						frameVal[0] = (ftimeVal[0] - timed.getTransformTime().getBegin()) / (timed.getTransformTime().getEnd() - timed.getTransformTime().getBegin());
						ASTExpression e = new ASTReal(system, where, frameVal[0]);
						addParameter(CFG.Frame, e, 0);
						system.error("Setting CF::Frame to %f from CF::FrameTime", where);
					}
				}
			}

			if (hasParameter(CFG.MaxShapes, maxShape, null)) {
				if (maxShape[0] > 1) {
					renderer.setMaxShapes((int)maxShape[0]);
				}
			}

			renderer.initBounds();

			return renderer;
		} catch (Exception e) {
			log.log(Level.WARNING, "Can't create CFDG renderer", e);
			system.error("Can't create CFDG renderer", where);
		}

		return null;
	}

	// only for testing
	public ASTRule getRule(int index) {
		return rules.get(index);
	}

	private int lowerBound(List<ASTRule> rules, int first, int last, ASTRule val) {
		// TODO verify lowerBound
		int count = last - first;
		while (count > 0) {
			int step = count / 2;
			int offset = first + step;
			if (rules.get(offset).compareTo(val) < 0) {
				first = first + 1;
				count = step + 1;
			} else {
				count = step;
			}
		}
		return first;
	}
}
