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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar;

import com.nextbreakpoint.nextfractal.contextfree.core.AffineTransformTime;
import com.nextbreakpoint.nextfractal.contextfree.core.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTCompiledPath;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTDefine;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTReplacement;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast.ASTRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CFG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.CFDGException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.exceptions.StopException;
import com.nextbreakpoint.nextfractal.contextfree.graphics.RenderListener;
import org.antlr.v4.runtime.Token;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.IntStream;

public class CFDGRenderer {
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CFDG.class.getName());

	private static final double FIXED_BORDER = 8.0;
	private static final double SHAPE_BORDER = 1.0;
	public static final int DRAW_AT = 1000;
	public static final int MIN_AREA = 1;

	private int width;
	private int height;

	private Point2D.Double lastPoint;
	private boolean stop;
	private boolean closed;
	private boolean wantMoveTo;
	private boolean wantCommand;
	private boolean opsOnly;
	private int index;
	private int nextIndex;

	private ASTCompiledPath currentPath;
	private CommandIterator currentCommand;
	private double currentTime;
	private double currentFrame;
	private Rand64 currentSeed = new Rand64();
	private boolean randUsed;
	private double maxNatural;
	private double maxSteps;
	private volatile boolean requestStop;
	private volatile boolean requestFinishUp;
	private volatile boolean requestUpdate;

	private CFDG cfdg;
	private CFCanvas canvas;
	private TiledCanvas tiledCanvas;
	private boolean colorConflict;
	private int maxShapes = 2000000;
	private boolean tiled;
	private boolean sized;
	private boolean timed;
	private FriezeType frieze;
	private double friezeSize;
	private boolean drawingMode;
	private boolean finalStep;

	private int variation;
	private double border;

	private double scaleArea;
	private double scale;
	private double fixedBorderX;
	private double fixedBorderY;
	private double shapeBorder;
	private double totalArea;
	private double currentArea;
	private double currScale;
	private double currArea;
	private double minArea;
	private double minSize;
	private Bounds bounds = new Bounds();
	private Bounds pathBounds = new Bounds();
	private AffineTransform currTransform = new AffineTransform();
	private AffineTransformTime timeBounds = new AffineTransformTime();
	private AffineTransformTime frameTimeBounds = new AffineTransformTime();
	private int outputSoFar;
	private int shapeCount;
	private int todoCount;
	private boolean animating;

	private List<AffineTransform> symmetryOps = new ArrayList<>();

	private List<CommandInfo> shapeMap = new ArrayList<>();
	private List<Shape> unfinishedShapes = new ArrayList<>();
	private List<FinishedShape> finishedShapes = new ArrayList<>();

	private ASTRule[] primitivePaths;

	private CFStack cfStack;
	private AffineTransform tileTransform;
	private RenderListener renderListener;

	public CFDGRenderer(CFDG cfdg, int width, int height, double minSize, int variation, double border) {
		this.cfdg = cfdg;
		this.width = width;
		this.height = height;
		this.minSize = minSize;
		this.variation = variation;
		this.border = border;

		frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.MIN_VALUE, Double.MAX_VALUE);

		primitivePaths = new ASTRule[PrimShape.getShapeNames().size()];

		for (int i = 0; i < primitivePaths.length; i++) {
			primitivePaths[i] = new ASTRule(cfdg.getDriver(), i, null);
		}

		for (PrimShape primShape : PrimShape.getShapeMap().values()) {
			shapeMap.add(new CommandInfo(primShape));
		}

		double[] value = new double[1];
		value[0] = 0;
		cfdg.hasParameter(CFG.FrameTime, value, null);
		currentTime = value[0];
		value[0] = 0;
		cfdg.hasParameter(CFG.Frame, value, null);
		currentFrame = value[0];
	}

	public ASTCompiledPath getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(ASTCompiledPath currentPath) {
		this.currentPath = currentPath;
	}

	public CommandIterator getCurrentCommand() {
		return currentCommand;
	}

	public void setCurrentCommand(CommandIterator iterator) {
		this.currentCommand = iterator;
	}

	public double getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(double currentTime) {
		this.currentTime = currentTime;
	}

	public double getCurrentFrame() {
		return currentFrame;
	}

	public void setCurrentFrame(double currentFrame) {
		this.currentFrame = currentFrame;
	}

	public Rand64 getCurrentSeed() {
		return currentSeed;
	}

	public void setCurrentSeed(Rand64 currentSeed) {
		this.currentSeed = currentSeed;
	}

	public boolean isRandUsed() {
		return randUsed;
	}

	public void setRandUsed(boolean randUsed) {
		this.randUsed = randUsed;
	}

	public double getMaxNatural() {
		return maxNatural;
	}

	public void setMaxNatural(double maxNatural) {
		this.maxNatural = maxNatural;
	}

	public boolean isRequestStop() {
		return requestStop;
	}

	public void setRequestStop(boolean requestStop) {
		this.requestStop = requestStop;
	}

	public int getMaxShapes() {
		return maxShapes;
	}

	public void setMaxShapes(int maxShapes) {
		this.maxShapes = (maxShapes != 0) ? maxShapes : 400000000;
	}

	public Point2D.Double getLastPoint() {
		return lastPoint;
	}

	public void setLastPoint(Point2D.Double lastPoint) {
		this.lastPoint = lastPoint;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}

	public boolean isWantMoveTo() {
		return wantMoveTo;
	}

	public void setWantMoveTo(boolean wantMoveTo) {
		this.wantMoveTo = wantMoveTo;
	}

	public boolean isWantCommand() {
		return wantCommand;
	}

	public void setWantCommand(boolean wantCommand) {
		this.wantCommand = wantCommand;
	}

	public boolean isOpsOnly() {
		return opsOnly;
	}

	public void setOpsOnly(boolean opsOnly) {
		this.opsOnly = opsOnly;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getNextIndex() {
		return nextIndex;
	}

	public void setNextIndex(int nextIndex) {
		this.nextIndex = nextIndex;
	}

	public double getMaxSteps() {
		return maxSteps;
	}

	public void setMaxSteps(double maxSteps) {
		this.maxSteps = maxSteps;
	}

	public boolean isRequestFinishUp() {
		return requestFinishUp;
	}

	public void setRequestFinishUp(boolean requestFinishUp) {
		this.requestFinishUp = requestFinishUp;
	}

	public boolean isRequestUpdate() {
		return requestUpdate;
	}

	public void setRequestUpdate(boolean requestUpdate) {
		this.requestUpdate = requestUpdate;
	}

	public CFStackItem getStackItem(int stackIndex) {
		return getStack().getStackItem(stackIndex);
	}

	public void setStackItem(int stackIndex, CFStackItem item) {
		getStack().setStackItem(stackIndex, item);
	}

	public void addStackItem(CFStackItem stackType) {
		getStack().addStackItem(stackType);
	}

	public void removeStackItem() {
		getStack().removeStackItem();
	}

	public int getStackSize() {
		return getStack().getStackSize();
	}

	public int getMaxStackSize() {
		return getStack().getMaxStackSize();
	}

	public void setStackSize(int stackSize) {
		this.getStack().setStackSize(stackSize);
	}

	public int getLogicalStackTop() {
		return getStack().getStackTop();
	}

	public void setLogicalStackTop(int cfLogicalStackTop) {
		this.getStack().setStackTop(cfLogicalStackTop);
	}

	public CFStack getStack() {
		return cfStack;
	}

	public void init() {
		initTraverse();

		Rand64.initRandomSeed(variation);

		currentSeed = new Rand64();

		cfStack = new CFStack(new CFStackItem[8192]);

		for (ASTReplacement rep : cfdg.getContents().getBody()) {
			if (rep instanceof ASTDefine) {
				ASTDefine def = (ASTDefine) rep;
				def.traverse(new Shape(), false, this);
			}
		}

		fixedBorderX = 0;
		fixedBorderY = 0;
		shapeBorder = 1;
		totalArea = 0;
		minArea = MIN_AREA;
		outputSoFar = 0;
		double[] value = new double[] { minArea };
		cfdg.hasParameter(CFG.MinimumSize, value, this);
		value[0] = Math.max(value[0], Math.sqrt(MIN_AREA));
		minArea = value[0] * value[0];
		fixedBorderX = FIXED_BORDER * Math.min(border, 1.0);
		shapeBorder = SHAPE_BORDER * Math.max(border, 1.0);

		value[0] = fixedBorderX;
		cfdg.hasParameter(CFG.BorderFixed, value, this);
		fixedBorderX = value[0];

		value[0] = shapeBorder;
		cfdg.hasParameter(CFG.BorderDynamic, value, this);
		shapeBorder = value[0];

		if (2 * (int)Math.abs(fixedBorderX) >= Math.min(width, height)) {
			fixedBorderX = 0;
		}

		if (shapeBorder <= 0.0) {
			shapeBorder = 1.0;
		}

		if (cfdg.hasParameter(CFG.MaxNatural, value, this) && (value[0] < 1.0 || value[0] > 9007199254740992.0)) {
        	maxNatural = value[0];
			//TODO rivedere
			throw new RuntimeException((value[0] < 1.0) ? "CF::MaxNatural must be >= 1" : "CF::MaxNatural must be < 9007199254740992");
		}

		currentPath = new ASTCompiledPath(cfdg.getDriver(), null);

		cfdg.getSummetry(symmetryOps, this);

		cfdg.initBackgroundColor(this);
	}

	public void initTraverse() {
		lastPoint = new Point2D.Double(0, 0);
		stop = false;
		closed = false;
		wantMoveTo = true;
		wantCommand = true;
		opsOnly = false;
		index = 0;
		nextIndex = 0;
	}

	public boolean isNatual(double n) {
		//TODO rivedere
		return n >= 0 && n <= getMaxNatural() && n == Math.floor(n);
	}

	public void initStack(CFStackRule stackRule) {
		if (stackRule != null && stackRule.getParamCount() > 0) {
			if (cfStack.getStackSize() + stackRule.getParamCount() > cfStack.getMaxStackSize()) {
				throw new RuntimeException("Maximum stack getMaxStackSize exceeded");
			}
			int oldSize = cfStack.getStackSize();
			cfStack.setStackSize(cfStack.getStackSize() + stackRule.getParamCount());
			stackRule.copyTo(cfStack.getStackItems(), oldSize);
		}
		setLogicalStackTop(cfStack.getStackSize());
	}

	public void unwindStack(int oldSize, List<ASTParameter> parameters) {
		if (oldSize == cfStack.getStackSize()) {
			return;
		}
		int pos = oldSize;
		for (ASTParameter parameter : parameters) {
			if (pos >= cfStack.getStackSize()) {
				break;
			}
			if (parameter.isLoopIndex() || parameter.getStackIndex() < 0) {
				continue;
			}
			if (parameter.getType() == ExpType.RuleType) {
				//TODO rivedere
				((CFStackRule)cfStack.getStackItem(pos)).setParamCount(0);
			}
			IntStream.range(0, parameter.getTupleSize()).forEach(i -> cfStack.setStackItem(i, null));
			pos += parameter.getTupleSize();
		}
		cfStack.setStackSize(oldSize);
		cfStack.setStackTop(oldSize);
	}

	public void colorConflict(Token location) {
		//TODO rivedere
		if (colorConflict) {
			return;
		}
		colorConflict = true;
		cfdg.getDriver().warning("Conflicting color change", location);
	}

	public void processShape(Shape shape) {
		double area = shape.getAreaCache();
		if (!Double.isFinite(area)) {
			requestStop = true;
			cfdg.getDriver().error("A shape got too big", null);
			return;
		}

		if (shape.getWorldState().getTransformTime().getBegin() > shape.getWorldState().getTransformTime().getEnd()) {
			return;
		}

		if (cfdg.getShapeType(shape.getShapeType()) == ShapeType.RuleType && cfdg.shapeHasRules(shape.getShapeType())) {
//			if (shapeCount > 7990) {
//				logger.info("area " + area);
//				logger.info("scaleArea " + scaleArea);
//				logger.info("todoCount " + todoCount);
//			}
			if (!bounds.valid() || area * scaleArea >= minArea) {
				todoCount += 1;
				unfinishedShapes.add(shape);
			}
		} else if (cfdg.getShapeType(shape.getShapeType()) == ShapeType.PathType) {
			ASTRule rule = cfdg.findRule(shape.getShapeType(), 0.0);
			processPrimShape(shape, rule);
		} else if (PrimShape.isPrimShape(shape.getShapeType())) {
			processPrimShape(shape, null);
		} else {
			//TODO rivedere
			requestStop = true;
			cfdg.getDriver().error(String.format("Shape with no rules encountered: %s", cfdg.decodeShapeName(shape.getShapeType())), null);
		}
	}

	public void processPrimShape(Shape shape, ASTRule rule) {
		int num = symmetryOps.size();
		if (num == 0 || cfdg.getShapeType(shape.getShapeType()) == ShapeType.FillType) {
			Shape copy = (Shape)shape.clone();
			processPrimShapeSiblings(copy, rule);
		} else {
			for (int i = 0; i < num; i++) {
				Shape sym = (Shape)shape.clone();
				sym.getWorldState().getTransform().concatenate(symmetryOps.get(i));
				processPrimShapeSiblings(sym, rule);
			}
		}
	}

	public void processPrimShapeSiblings(Shape shape, ASTRule rule) {
		shapeCount += 1;

		if (scale == 0.0) {
			scale = (width + height) / Math.sqrt(Math.abs(shape.getWorldState().getTransform().getDeterminant()));
		}

		if (rule != null || cfdg.getShapeType(shape.getShapeType()) != ShapeType.FillType) {
			currentArea = 0.0;
			pathBounds.invalidate();
			drawingMode = false;
			if (rule != null) {
				opsOnly = false;
				rule.traversePath(shape, this);
			} else {
				CommandInfo attr = null;
				if (shape.getShapeType() < 3) {
					attr = shapeMap.get(shape.getShapeType());
					processPathCommand(shape, attr);
				}
			}
			totalArea += currentArea;
			if (!tiled && !sized) {
				bounds.merge(pathBounds.dilate(shapeBorder));
				if (frieze == FriezeType.FriezeX) {
					bounds.setMinX(-friezeSize);
					bounds.setMaxX(+friezeSize);
				}
				if (frieze == FriezeType.FriezeY) {
					bounds.setMinY(-friezeSize);
					bounds.setMaxY(+friezeSize);
				}

				int[] currWidth = new int[] { width };
				int[] curreHeight = new int[] { height };
				scale = bounds.computeScale(currWidth, curreHeight, fixedBorderX, fixedBorderY, false, null, false);
				width = currWidth[0];
				height = curreHeight[0];
				scaleArea = scale * scale;
			}
		} else {
			currentArea = 1.0;
		}

		FinishedShape finishedShape = new FinishedShape((Shape)shape.clone(), shapeCount, new Bounds(pathBounds));
		finishedShape.getWorldState().getTransformZ().setSz(currentArea);

		if (!cfdg.usesTime()) {
			finishedShape.getWorldState().getTransformTime().setBegin(totalArea);
			finishedShape.getWorldState().getTransformTime().setEnd(Double.MAX_VALUE);
		}

		if (finishedShape.getWorldState().getTransformTime().getBegin() < timeBounds.getBegin() && Double.isFinite(finishedShape.getWorldState().getTransformTime().getBegin()) && !timed) {
			timeBounds.setBegin(finishedShape.getWorldState().getTransformTime().getBegin());
		}
		if (finishedShape.getWorldState().getTransformTime().getBegin() > timeBounds.getEnd() && Double.isFinite(finishedShape.getWorldState().getTransformTime().getBegin()) && !timed) {
			timeBounds.setEnd(finishedShape.getWorldState().getTransformTime().getBegin());
		}
		if (finishedShape.getWorldState().getTransformTime().getEnd() > timeBounds.getEnd() && Double.isFinite(finishedShape.getWorldState().getTransformTime().getEnd()) && !timed) {
			timeBounds.setEnd(finishedShape.getWorldState().getTransformTime().getEnd());
		}
		if (finishedShape.getWorldState().getTransformTime().getEnd() < timeBounds.getBegin() && Double.isFinite(finishedShape.getWorldState().getTransformTime().getEnd()) && !timed) {
			timeBounds.setBegin(finishedShape.getWorldState().getTransformTime().getEnd());
		}

		if (!finishedShape.getWorldState().isFinite()) {
			requestStop = true;
			cfdg.getDriver().error("A shape got too big.", null);
			return;
		}

		finishedShapes.add(finishedShape);
	}

	public void processSubpath(Shape shape, boolean tr, RepElemType expectedType) {
		ASTRule rule = null;
		if (cfdg.getShapeType(shape.getShapeType()) != ShapeType.PathType && PrimShape.isPrimShape(shape.getShapeType()) && expectedType == RepElemType.op) {
			rule = primitivePaths[shape.getShapeType()];
		} else {
			rule = cfdg.findRule(shape.getShapeType(), 0.0);
		}
		if (rule.getRuleBody().getRepType() != expectedType.getType()) {
			throw new CFDGException("Subpath is not of the expected type (path ops/commands)", rule.getLocation());
		}
		boolean saveOpsOnly = opsOnly;
		opsOnly = opsOnly || (expectedType == RepElemType.op);
		rule.getRuleBody().traverse(shape, tr, this, true);
		opsOnly = saveOpsOnly;
	}

	public void processPathCommand(Shape shape, CommandInfo info) {
		if (drawingMode) {
			if (canvas != null && info != null) {
				double[] color = shape.getWorldState().color().getRGBA();
				AffineTransform tr = shape.getWorldState().getTransform();
				canvas.path(color, tr, info);
			}
		} else {
			if (info != null) {
				pathBounds.update(shape.getWorldState().getTransform(), scale, info);
				currentArea = Math.abs((pathBounds.getMaxX() - pathBounds.getMinX()) * (pathBounds.getMaxY() - pathBounds.getMinY()));
			}
		}
	}

	public void initBounds() {
		init();

		double[] vector = new double[2];

		tiled = cfdg.isTiled(null, vector);
		frieze = cfdg.isFrieze(null, vector);
		sized = cfdg.isSized(vector);
		timed = cfdg.isTimed(timeBounds);

		double tileX = vector[0];
		double tileY = vector[1];

		if (tiled || sized) {
			fixedBorderX = shapeBorder = 0.0;
			bounds.setMinX(-tileX / 2.0);
			bounds.setMinY(-tileY / 2.0);
			bounds.setMaxX(tileX / 2.0);
			bounds.setMaxY(tileY / 2.0);
			int[] currWidth = new int[] { width };
			int[] currHeight = new int[] { height };
			rescaleOutput(currWidth, currHeight, true);
			width = currWidth[0];
			height = currHeight[0];
			scaleArea = currArea;
		}

		if (frieze == FriezeType.FriezeX)
			friezeSize = tileX / 2.0;
		if (frieze == FriezeType.FriezeY)
			friezeSize = tileY / 2.0;
		if (frieze != FriezeType.FriezeY)
			fixedBorderY = fixedBorderX;
		if (frieze == FriezeType.FriezeX)
			fixedBorderX = 0;
	}

	public void resetSize(int x, int y) {
		this.width = x;
		this.height = y;
		if (tiled || sized) {
			currScale = currArea = 0.0;
			int[] currWidth = new int[] { width };
			int[] currHeight = new int[] { height };
			rescaleOutput(currWidth, currHeight, true);
			width = currWidth[0];
			height = currHeight[0];
			scaleArea = currArea;
		}
	}

	public void cleanup() {
		//TODO rivedere

//		for (FinishedShape shape : finishedShapes) {
//			if (shape.isAbortEverything()) {
//				break;
//			}
//		}

		finishedShapes.clear();
		unfinishedShapes.clear();

		unwindStack(0, cfdg.getContents().getParameters());

		currentPath = null;

		cfdg.resetCachedPaths();
	}

	public void resetBounds() {
		bounds = new Bounds();
	}

	public double run(CFCanvas canvas, boolean partialDraw) {
		canvas.clear(cfdg.getBackgroundColor());

		notifyRenderListener();

		if (!animating) {
			outputPrep(canvas);
		}

		int reportAt = 250;

		{
			Shape initShape = cfdg.getInitialShape(this);

			initShape.getWorldState().setRand64Seed(currentSeed);

			if (!timed) {
				timeBounds = initShape.getWorldState().getTransformTime();
			}

			try {
				processShape(initShape);
			} catch (CFDGException e) {
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), e.getLocation());
			} catch (Exception e) {
				//TODO rivedere
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), null);
			}
		}

		long time = System.currentTimeMillis();

		for (;;) {
			if (requestStop) {
				break;
			}

			if (requestFinishUp) {
				break;
			}

			if (unfinishedShapes.isEmpty()) {
				break;
			}

			if (shapeCount + todoCount > maxShapes) {
				break;
			}

			Shape shape = (Shape)unfinishedShapes.remove(0).clone();

			todoCount -= 1;

			try {
				ASTRule rule = cfdg.findRule(shape.getShapeType(), shape.getWorldState().getRand64Seed().getDouble());
				drawingMode = false;
				rule.traverse(shape, false, this);
			} catch (StopException e) {
				break;
			} catch (CFDGException e) {
				logger.log(Level.WARNING, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), e.getLocation());
				break;
			} catch (Exception e) {
				logger.log(Level.WARNING, "Can't render CFDG image", e);
				//TODO rivedere
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), null);
				break;
			}

			if (requestUpdate || shapeCount > reportAt) {
				if (partialDraw) {
					outputPartial();
				}
				outputStats();
				reportAt = 2 * shapeCount;
			}

			Thread.yield();
		}

		if (cfdg.usesTime() || !timed) {
			timeBounds = AffineTransformTime.getTranslateInstance(0.0, totalArea);
		}

		if (!requestStop) {
			outputFinal();
		}

		if (!requestStop) {
			outputStats();
			if (canvas != null) {
				cfdg.getDriver().info("Done.", null);
			}
		}

		if (canvas != null && frieze != FriezeType.NoFrieze) {
			int[] currWidth = new int[] { width };
			int[] currHeight = new int[] { height };
			rescaleOutput(currWidth, currHeight, true);
			width = currWidth[0];
			height = currHeight[0];
		}

		long totalTime = System.currentTimeMillis() - time;
		cfdg.getDriver().info("Rendering of " + outputSoFar + " shapes took " + totalTime / 1000.0 + "s", null);

		return currScale;
	}

	public void draw(SimpleCanvas canvas) {
		frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.MIN_VALUE, Double.MAX_VALUE);
		outputPrep(canvas);
		outputFinal();
		outputStats();
	}

	public void animate(SimpleCanvas canvas, int frames, boolean zoom) {
		canvas.clear(cfdg.getBackgroundColor());

		notifyRenderListener();

		outputPrep(canvas);

		boolean ftime = cfdg.usesFrameTime();

		zoom = zoom && !ftime;

		if (ftime) {
			cleanup();
		}

		int[] currWidth = new int[] { width };
		int[] currHeight = new int[] { height };
		if (outputSoFar < DRAW_AT) {
			rescaleOutput(currWidth, currHeight, true);
		}

		canvas.start(true, cfdg.getBackgroundColor(), currWidth[0], currHeight[0]);

		canvas.end();

		double framInc = (timeBounds.getEnd() - timeBounds.getBegin()) / frames;

		OutputBounds outputBounds = new OutputBounds(frames, timeBounds, currWidth[0], currHeight[0], this);

		if (zoom) {
			cfdg.getDriver().info("Computing zoom", null);

			try {
				forEachShape(true, shape -> {
					outputBounds.apply(shape);
					return null;
				});
			} catch (StopException e) {
				return;
			} catch (CFDGException e) {
				logger.log(Level.WARNING, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), e.getLocation());
				return;
			} catch (Exception e) {
				logger.log(Level.WARNING, "Can't render CFDG image", e);
				//TODO rivedere
				requestStop = true;
				cfdg.getDriver().error(e.getMessage(), null);
				return;
			}
		}

		shapeCount = 0;
		animating = true;

		frameTimeBounds.setEnd(timeBounds.getBegin());

		Bounds savedBounds = bounds;

		for (int frameCount = 1; frameCount <= frames; frameCount++) {
			cfdg.getDriver().info(String.format("Generating frame %d of %d", frameCount, frames), null);

			if (zoom) {
				bounds = outputBounds.frameBounds(frameCount - 1);
			}

			shapeCount += outputBounds.frameCount(frameCount - 1);

			frameTimeBounds.setBegin(frameTimeBounds.getEnd());
			frameTimeBounds.setEnd(timeBounds.getBegin() + framInc * frameCount);

			if (ftime) {
				currentTime = (frameTimeBounds.getBegin() + frameTimeBounds.getEnd()) * 0.5;
				currentFrame = (frameCount - 1.0) / (frames - 1.0);

				//TODO rivedere

				try {
					init();
				} catch (Exception e) {
					cfdg.getDriver().error(e.getMessage(), null);
					cleanup();
					bounds = savedBounds;
					animating = false;
					outputStats();
					return;
				}
				run(canvas, false);
				this.canvas = canvas;
			} else {
				outputFinal();
				outputStats();
			}

			if (!ftime) {
				cleanup();
			}

			if (requestStop || requestFinishUp) {
				break;
			}
		}

		bounds = savedBounds;
		animating = false;
		outputStats();

		cfdg.getDriver().info(String.format("Animation of %d frames complete", frames), null);
	}

	private void outputPrep(CFCanvas canvas) {
		this.canvas = canvas;

		if (canvas != null) {
//			width = canvas.getWidth();
//			height = canvas.getHeight();

			if (tiled || frieze != FriezeType.NoFrieze) {
				AffineTransform transform = new AffineTransform();
				cfdg.isTiled(transform, null);
				cfdg.isFrieze(transform, null);
				tiledCanvas = new TiledCanvas(canvas, transform, frieze);
				tiledCanvas.setScale(currScale);
				this.canvas = tiledCanvas;
			}

			frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.MIN_VALUE, Double.MAX_VALUE);
		}

		requestStop = false;
		requestFinishUp = false;
		requestUpdate = false;

		animating = false;
		// TODO aggiungere stats?
	}

	private void outputFinal() {
		output(true);
	}

	private void outputPartial() {
		output(false);
	}

	private void outputStats() {
		//TODO rivedere
		requestUpdate = false;
	}

	private void rescaleOutput(int[] width, int[] height, boolean finalStep) {
		if (!bounds.valid()) {
			return;
		}

		AffineTransform transform = new AffineTransform();

		double scale = bounds.computeScale(width, height, fixedBorderX, fixedBorderY, true, transform, tiled || sized || frieze != FriezeType.NoFrieze);

		//TODO rivedere

		if (finalStep || currScale == 0.0 || currScale * 0.9 > scale) {
			currScale = scale;
			currArea = scale * scale;
			if (tiledCanvas != null) {
				tiledCanvas.setScale(scale);
			}
			currTransform = transform;
			outputSoFar = 0;
		}
	}

	private void output(boolean finalStep) {
		if (canvas == null) {
			return;
		}

		if (!finalStep) {
			return;
		}

		this.finalStep = true;

		int[] currWidth = new int[] { width };
		int[] currHeight = new int[] { height };
		if (outputSoFar < DRAW_AT) {
			rescaleOutput(currWidth, currHeight, true);
		}

		long time = System.currentTimeMillis();
		Collections.sort(finishedShapes);
		long totalTime = System.currentTimeMillis() - time;
		cfdg.getDriver().info("Sorting of " + finishedShapes.size() + " shapes took " + totalTime / 1000.0 + "s", null);

		//TODO rivedere

		canvas.start(outputSoFar == 0, cfdg.getBackgroundColor(), currWidth[0], currHeight[0]);

		drawingMode = true;

		drawFinishedShapes();

		canvas.end();

		notifyRenderListener();
	}

	private void drawFinishedShapes() {
		try {
			long time = System.currentTimeMillis();
			forEachShape(true, this::drawShape);
			long totalTime = System.currentTimeMillis() - time;
			cfdg.getDriver().info("Drawing of " + finishedShapes.size() + " shapes took " + totalTime / 1000.0 + "s", null);
			finishedShapes.clear();
		} catch (StopException e) {
		} catch (Exception e) {
			cfdg.getDriver().error(e.getMessage(), null);
		}
	}

	private void notifyRenderListener() {
		RenderListener renderListener = getRenderListener();
		if (renderListener != null) {
			renderListener.partialDraw();
		}
	}

	private Object forEachShape(boolean finalStep, Function<FinishedShape, Object> shapeFunction) {
		int shapeIdx = 0;
		int drawAt = DRAW_AT;
		for (FinishedShape shape : finishedShapes) {
			shapeFunction.apply(shape);
			shapeIdx += 1;
			if (shapeIdx == drawAt) {
				notifyRenderListener();
				drawAt *= 2;
			}
			if (shapeIdx % 100 == 0) {
				Thread.yield();
			}
		}
		outputSoFar += finishedShapes.size();
		return null;
	}

	private Object drawShape(FinishedShape shape) {
		if (requestStop) {
			throw new StopException();
		}

		if (!finalStep && requestFinishUp) {
			throw new StopException();
		}

		if (requestUpdate) {
			outputStats();
		}

		if (!shape.getWorldState().getTransformTime().overlaps(frameTimeBounds)) {
			return null;
		}

		AffineTransform transform = shape.getWorldState().getTransform();

		transform.preConcatenate(currTransform);

		double a = shape.getWorldState().getTransformZ().sz() * currArea;

		if ((!Double.isFinite(a) && shape.getShapeType() != PrimShapeType.fillType.getType()) || a < minArea) {
			return null;
		}

		if (tiledCanvas != null && shape.getShapeType() != PrimShapeType.fillType.getType()) {
			Bounds b = shape.bounds();
			Point2D.Double p1 = new Point2D.Double(b.getMinX(), b.getMinY());
			Point2D.Double p2 = new Point2D.Double(b.getMaxX(), b.getMaxY());
			currTransform.transform(p1, p1);
			currTransform.transform(p2, p2);
			b = new Bounds();
			b.setMinX(p1.getX());
			b.setMinY(p1.getY());
			b.setMaxX(p2.getX());
			b.setMaxY(p2.getY());
			canvas.tileTransform(b);
		}

		if (cfdg.getShapeType(shape.getShapeType()) == ShapeType.PathType) {
			ASTRule rule = cfdg.findRule(shape.getShapeType());
			rule.traversePath(shape, this);
		} else {
			double[] color = shape.getWorldState().color().getRGBA();
			if (PrimShape.isPrimShape(shape.getShapeType())) {
				canvas.primitive(shape.getShapeType(), color, transform);
			} else {
				cfdg.getDriver().error("Non drawable shape with no rules: " + cfdg.decodeShapeName(shape.getShapeType()), null);
				requestStop = true;
				throw new StopException();
			}
		}

		return null;
	}

	public static boolean abortEverything() {
		//TODO completare abortEverything
		return false;
	}

	public void setLogicalStack(CFStack logicalStack) {
		this.cfStack = logicalStack;
	}

    public CFDGDriver getDriver() {
        return cfdg.getDriver();
    }

	public void setRenderListener(RenderListener renderListener) {
		this.renderListener = renderListener;
	}

	public RenderListener getRenderListener() {
		return renderListener;
	}
}
