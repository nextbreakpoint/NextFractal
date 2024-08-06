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

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTDefine;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModTerm;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRepContainer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReplacement;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTStartSpecifier;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTUtils;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CFG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Param;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.WeightType;
import lombok.Getter;
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

@Log
public class CFDG {
	private final ASTRule needle;
	@Getter
    private final CFDGDriver driver;
	private final ASTRepContainer cfdgContents;
	private final List<ShapeElement> shapeTypes = new ArrayList<>();
	private final Stack<ASTRule> rules = new Stack<>();
	private final Map<Integer, ASTDefine> functions = new HashMap<>();
	private final Map<CFG, Integer> paramDepth = new HashMap<>();
	private final Map<CFG, ASTExpression> paramExp = new HashMap<>();
	@Getter
    private double[] backgroundColor = new double[] { 1, 1, 1, 1 };
	private ASTReplacement initShape;
	private Modification tileMod = new Modification();
	private Modification sizeMod = new Modification();
	private Modification timeMod = new Modification();
	private final Point2D tileOffset = new Point2D.Double(0, 0);
	private int parameters;
	private int stackSize;
	private boolean usesAlpha;
	private boolean usesColor;
	private boolean usesTime;
	private boolean usesFrameTime;

	public CFDG(CFDGDriver cfdgDriver) {
		this.driver = cfdgDriver;
		cfdgContents = new ASTRepContainer(null, cfdgDriver);
		needle = new ASTRule(null, cfdgDriver, -1);
		PrimShape.getShapeNames().forEach(this::encodeShapeName);
	}

    public Shape getInitialShape(CFDGRenderer renderer) {
		Shape shape = new Shape();
		shape.worldState.setColor(new HSBColor(0,0,0,1));
		shape.worldState.setColorTarget(new HSBColor(0,0,0,1));
		shape.worldState.getTransformTime().setEnd(1);
		initShape.replace(shape, renderer);
		shape.worldState.getTransform().translate(tileOffset.getX(), tileOffset.getY());
		return shape;
	}

	public ASTRepContainer getContents() {
		return cfdgContents;
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

	public ASTRule findRule(int nameIndex) {
		for (ASTRule rule: rules) {
			if (rule.getNameIndex() == nameIndex) {
				return rule;
			}
		}
		return null;
	}

	public ASTRule findRule(int nameIndex, double weight) {
		needle.setNameIndex(nameIndex);
		needle.setWeight(weight);
		int first = lowerBound(rules, 0, rules.size(), needle);
		if (first == rules.size() || rules.get(first).getNameIndex() != nameIndex) {
			driver.fail("Can't find a rule for a shape (very helpful I know)", null);
		}
		return rules.get(first);
	}

	private int lowerBound(List<ASTRule> rules, int first, int last, ASTRule val) {
		// TODO controllare
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

	public boolean addRule(ASTRule rule) {
		rules.push(rule);
		ShapeElement type = shapeTypes.get(rule.getNameIndex());
		if (type.getShapeType() == ShapeType.NewShape) {
			type.setShapeType(rule.isPath() ? ShapeType.PathType : ShapeType.RuleType);
		}
		if (type.getParameters() != null && !type.getParameters().isEmpty()) {
			rule.getRuleBody().getParameters().clear();
			rule.getRuleBody().getParameters().addAll(type.getParameters());
		}
		type.setHasRules(true);
		return type.isShape();
	}

	public void addParameter(Param param) {
		parameters |= param.getType();
		usesColor = (parameters & Param.Color.getType()) != 0;
		usesTime = (parameters & Param.Time.getType()) != 0;
		usesFrameTime = (parameters & Param.FrameTime.getType()) != 0;
	}

//	public double[] getColor(HSBColor hsb) {
//		return hsb.getRGBA();
//		//TODO completare uses16bitColor
////		if (uses16bitColor) {
////			return c;
////		} else {
////			return c;
////		}
//	}

	public boolean isTiled(AffineTransform transform, double[] point) {
		if (!hasParameter(CFG.Tile, ExpType.Mod)) {
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
				driver.fail("Tile must be aligned with the X or Y axis", null);
			}

			if ((u.x - o.x) < 0.0 || (v.y - o.y) < 0.0) {
				driver.fail("Tile must be in the positive X/Y quadrant", null);
			}

			point[0] = u.x - o.x;
			point[1] = v.y - o.y;
		}

		return true;
	}

	public FriezeType isFrieze(AffineTransform transform, double[] point) {
		if (!hasParameter(CFG.Tile, ExpType.Mod)) {
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

			//TODO controllare
//			Point2D.Double o = new Point2D.Double(o_x, o_y);
//			Point2D.Double u = new Point2D.Double(u_x, u_y);
//			Point2D.Double v = new Point2D.Double(v_x, v_y);
//
//			tileMod.getTransform().transform(o, o);
//			tileMod.getTransform().transform(u, u);
//			tileMod.getTransform().transform(v, v);
//
//			if (Math.abs(u.y - o.y) >= 0.0001 && Math.abs(v.x - o.x) >= 0.0001) {
//				driver.fail("Frieze must be aligned with the X or Y axis", null);
//			}
//
//			if ((u.x - o.x) < 0.0 || (v.y - o.y) < 0.0) {
//				driver.fail("Frieze must be in the positive X/Y quadrant", null);
//			}
//
//			point[0] = u.x - o.x;
//			point[1] = v.y - o.y;

			if (Math.abs(u_y - o_y) >= 0.0001 || Math.abs(v_x - o_x) >= 0.0001) {
				driver.fail("Frieze must be aligned with the X and Y axis", null);
			}

			if ((u_x - o_x) < 0.0 || (v_y - o_y) < 0.0) {
				driver.fail("Frieze must be in the positive X/Y quadrant", null);
			}

			point[0] = u_x - o_x;
			point[1] = u_y - o_y;
		}

		return tileMod.getTransform().getScaleX() == 0.0 ? FriezeType.FriezeY : FriezeType.FriezeX;
	}

	public boolean isSized(double[] point) {
		if (!hasParameter(CFG.Size, ExpType.Mod)) {
			return false;
		}

		if (point != null) {
			point[0] = sizeMod.getTransform().getScaleX();
			point[1] = sizeMod.getTransform().getScaleY();
		}

		if (sizeMod.getTransform().getShearX() != 0.0 || sizeMod.getTransform().getShearY() != 0.0) {
			driver.fail("Size specification must not be rotated or skewed", null);
		}

		return true;
	}

	public boolean isTimed(AffineTransformTime transform) {
		if (!hasParameter(CFG.Time, ExpType.Mod)) {
			return false;
		}

		if (transform != null) {
			transform.setBegin(timeMod.getTransformTime().getBegin());
			transform.setEnd(timeMod.getTransformTime().getEnd());
			transform.setStep(timeMod.getTransformTime().getStep());
		}

		if (sizeMod.getTransformTime().getBegin() >= sizeMod.getTransformTime().getEnd()) {
			driver.fail("Time specification must have positive duration", null);
		}

		return true;
	}

	public void getSymmetry(List<AffineTransform> syms, CFDGRenderer renderer) {
		syms.clear();
		ASTExpression exp = hasParameter(CFG.Symmetry);
		List<ASTModification> left = ASTUtils.getTransforms(driver, exp, syms, renderer, isTiled(null, null), tileMod.getTransform());
		if (!left.isEmpty()) {
			driver.fail("At least one term was invalid", exp.getToken());
		}
	}

	public boolean hasParameter(CFG p, double[] value, CFDGRenderer renderer) {
		ASTExpression exp = hasParameter(p);
		if (exp == null || exp.getType() != ExpType.Numeric) {
			return false;
		}
		if (!exp.isConstant() && renderer != null) {
			driver.fail("This expression must be constant", exp.getToken());
			return false;
		} else {
			exp.evaluate(value, 1, renderer);
		}
		return true;
	}

	public boolean hasParameter(CFG p, Modification value, CFDGRenderer renderer) {
		ASTExpression exp = hasParameter(p);
		if (exp == null || exp.getType() != ExpType.Mod) {
			return false;
		}
		if (!exp.isConstant() && renderer != null) {
			driver.fail("This expression must be constant", exp.getToken());
			return false;
		} else {
			exp.evaluate(value, true, renderer);
		}
		return true;
	}

	public boolean hasParameter(CFG p, ExpType type) {
		ASTExpression exp = hasParameter(p);
        return exp != null && exp.getType() == type;
    }

	public ASTExpression hasParameter(CFG p) {
		return paramExp.get(p);
	}

	public boolean addParameter(String name, ASTExpression exp, int depth) {
		CFG p = CFG.byName(name);
		Integer oldDepth = paramDepth.get(p);
		if (oldDepth == null || depth < oldDepth) {
			paramDepth.put(p, depth);
			paramExp.put(p, exp);
		}
		return true;
	}

	public void rulesLoaded() {
		double[] weightsums = new double[shapeTypes.size()];
		double[] percentweightsums = new double[shapeTypes.size()];
		double[] unitweightsums = new double[shapeTypes.size()];
		int[] rulecounts = new int[shapeTypes.size()];
		int[] weightTypes = new int[shapeTypes.size()];

		for (ASTRule rule : rules) {
			if (rule.getWeightType() == WeightType.PercentWeight) {
				percentweightsums[rule.getNameIndex()] += rule.getWeight();
				if (percentweightsums[rule.getNameIndex()] > 1.0001) {
					driver.fail("Percentages exceed 100%", rule.getToken());
				}
			} else {
				weightsums[rule.getNameIndex()] += rule.getWeight();
			}
			rulecounts[rule.getNameIndex()] += 1;
			weightTypes[rule.getNameIndex()] |= rule.getWeightType().getType();
		}

		for (ASTRule rule : rules) {
			double weight = rule.getWeight() / weightsums[rule.getNameIndex()];
			if ((weightTypes[rule.getNameIndex()] & WeightType.PercentWeight.getType()) != 0) {
				if (rule.getWeightType() == WeightType.PercentWeight) {
					weight = rule.getWeight();
				} else {
					weight *= 1.0 - percentweightsums[rule.getNameIndex()];
					if (percentweightsums[rule.getNameIndex()] > 0.9999) {
						driver.warning("Percentages sum to 100%, this rule has no weight", rule.getToken());
					}
				}
			}
			if (weightTypes[rule.getNameIndex()] == WeightType.PercentWeight.getType() && Math.abs(percentweightsums[rule.getNameIndex()] - 1.0) > 0.0001) {
				driver.warning("Percentages do not sum to 100%", rule.getToken());
			}
			if (!Double.isFinite(weight)) {
				weight = 0;
			}
			unitweightsums[rule.getNameIndex()] += weight;
			if (rulecounts[rule.getNameIndex()] - 1 > 0) {
				rule.setWeight(unitweightsums[rule.getNameIndex()]);
			} else {
				rule.setWeight(1.1);
			}
		}

		Collections.sort(rules);

		driver.setLocalStackDepth(0);

		cfdgContents.compile(CompilePhase.TypeCheck, null, null);

		if (!driver.isErrorOccurred()) {
			cfdgContents.compile(CompilePhase.Simplify, null, null);
		}

		double[] value = new double[1];
		boolean uses16bitColor = hasParameter(CFG.ColorDepth, value, null) && Math.floor(value[0]) == 16;

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
                    break;
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

	public int tryEncodeShapeName(String s) {
	    for (int i = 0; i < shapeTypes.size(); i++) {
	        if (s.equals(shapeTypes.get(i).getName())) {
	            return i;
	        }
	    }
	    return -1;
	}

	public int encodeShapeName(String s) {
		int i = tryEncodeShapeName(s);
		if (i >= 0) return i;
		shapeTypes.add(new ShapeElement(s));
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
		if (nameIndex < shapeTypes.size() && args != null) {
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
			if (p.getParameters().isEmpty()) {
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
		type.getParameters().clear();
		type.getParameters().addAll(p.getParameters());
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
		try {
			ASTExpression startExp = paramExp.get(CFG.StartShape);

			if (startExp == null) {
				driver.fail("No startshape found", null);
				return null;
			}

			if (startExp instanceof ASTStartSpecifier specStart) {
                initShape = new ASTReplacement(startExp.getToken(), driver, specStart, specStart.getModification(), RepElemType.empty);
				initShape.getChildChange().addEntropy(initShape.getShapeSpecifier().getEntropy());
			} else {
				driver.fail("Type error in startshape", startExp.getToken());
				return null;
			}

			CFDGRenderer renderer = new CFDGRenderer(this, width, height, minSize, variation, border);

			Modification tiled = new Modification();
			Modification sized = new Modification();
			Modification timed = new Modification();

			double[] maxShape = new double[0];

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
				timeMod = timed;
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
		}

		return null;
	}

	public int getRulesCount() {
		return rules.size();
	}

	public ASTRule getRule(int index) {
		return rules.get(index);
	}
}
