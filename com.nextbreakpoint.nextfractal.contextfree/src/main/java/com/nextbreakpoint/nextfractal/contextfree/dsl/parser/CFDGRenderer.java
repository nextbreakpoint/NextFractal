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
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFCanvas;
import com.nextbreakpoint.nextfractal.contextfree.dsl.CFListener;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTCompiledPath;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTDefine;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReplacement;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CFG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FriezeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PrimShapeType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;

// renderimpl.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2006-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2006-2013 John Horigan - john@glyphic.com
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
public class CFDGRenderer {
	private static final double FIXED_BORDER = 8.0;
	private static final double SHAPE_BORDER = 1.0;
	public static final int DRAW_AT = 1000;
	public static final double MIN_AREA = 0.3;
	public static final double MIN_SIZE = 0.3;

	private int width;
	private int height;

	@Setter
    @Getter
    private Point2D.Double lastPoint;
	@Setter
    @Getter
    private boolean stop;
	@Setter
    @Getter
    private boolean closed;
	@Setter
    @Getter
    private boolean wantMoveTo;
	@Setter
    @Getter
    private boolean wantCommand;
	@Setter
    @Getter
    private boolean opsOnly;
	@Setter
    @Getter
    private int index;
	@Setter
    @Getter
    private int nextIndex;

	@Setter
    @Getter
    private ASTCompiledPath currentPath;
	@Setter
    @Getter
    private CommandIterator currentCommand;
	@Setter
    @Getter
    private double currentTime;
	@Setter
    @Getter
    private double currentFrame;
	@Setter
    @Getter
    private Rand64 currentSeed;
	@Setter
    @Getter
    private boolean randUsed;
	@Setter
    @Getter
    private double maxNatural;
	@Setter
    @Getter
    private double maxSteps;
	@Setter
    @Getter
    private volatile boolean requestStop;
	@Setter
    @Getter
    private volatile boolean requestFinishUp;
	@Setter
    @Getter
    private volatile boolean requestUpdate;

	@Getter
	private final CFDGBuilder builder;
	private final CFDG cfdg;
	private CFCanvas canvas;
	private TiledCanvas tiledCanvas;
	private boolean colorConflict;
	@Getter
    private int maxShapes = 2000000;
	private boolean tiled;
	private boolean sized;
	private boolean timed;
	private FriezeType frieze;
	private double friezeSize;
	private boolean drawingMode;
	private boolean finalStep;

	private final int variation;
	private final double border;

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
	private final Bounds pathBounds = new Bounds();
	private AffineTransform currTransform = new AffineTransform();
	private AffineTransformTime timeBounds = new AffineTransformTime();
	private AffineTransformTime frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.MIN_VALUE, Double.MAX_VALUE);
	private int outputSoFar;
	private int shapeCount;
	private int todoCount;
	private boolean animating;

	private final List<AffineTransform> symmetryOps = new ArrayList<>();

	private final List<CommandInfo> shapeMap = new ArrayList<>();
	private final List<Shape> unfinishedShapes = new ArrayList<>();
	private final List<FinishedShape> finishedShapes = new ArrayList<>();

	private final ASTRule[] primitivePaths;

	private CFStack cfStack;
	private AffineTransform tileTransform;
	@Getter
    @Setter
    private CFListener listener;
	@Getter
	@Setter
    private boolean impure;

	public CFDGRenderer(CFDGBuilder builder, int width, int height, double minSize, int variation, double border) {
		this.builder = builder;
		this.width = width;
		this.height = height;
		this.minSize = minSize;
		this.variation = variation;
		this.border = border;

		this.cfdg = builder.getCfdg();

		primitivePaths = new ASTRule[PrimShape.getShapeNames().size()];
		for (int i = 0; i < primitivePaths.length; i++) {
			primitivePaths[i] = new ASTRule(cfdg.getSystem(), i);
		}

		for (PrimShape primShape : PrimShape.getShapeMap().values()) {
			shapeMap.add(new CommandInfo(primShape));
		}

		final double[] value = new double[1];
		value[0] = 0;
		cfdg.hasParameter(CFG.FrameTime, value, null);
		currentTime = value[0];
		value[0] = 0;
		cfdg.hasParameter(CFG.Frame, value, null);
		currentFrame = value[0];
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

	public static boolean isNatural(CFDGBuilder builder, CFDGRenderer renderer, double n) {
		if (renderer != null && renderer.isImpure()) {
			return true;
		}
		if (builder == null || builder.isImpure()) {
			return true;
		}
		return n >= 0 && n <= (renderer != null ? renderer.getMaxNatural() : builder.getMaxNatural()) && n == Math.floor(n);
	}

	public void initStack(CFStackRule stackRule) {
		if (stackRule != null && stackRule.getParamCount() > 0) {
			if (getStackSize() + stackRule.getParamCount() > cfStack.getMaxStackSize()) {
				throw new CFDGException("Maximum stack getMaxStackSize exceeded", ASTWhere.DEFAULT_WHERE);
			}
			final int oldSize = getStackSize();
			setStackSize(getStackSize() + stackRule.getParamCount());
			stackRule.copyParams(getStack().getStackItems(), oldSize);
		}
		setLogicalStackTop(getStackSize());
	}

	public void unwindStack(int oldSize, List<ASTParameter> parameters) {
		if (oldSize == getStackSize()) {
			return;
		}
		int pos = oldSize;
		for (final ASTParameter parameter : parameters) {
			if (pos >= getStackSize()) {
				break;  // no guarantee entire frame was computed
			}
			if (parameter.isLoopIndex() || parameter.getStackIndex() < 0) {
				// loop indices are unwound in ASTloop::traverse()
				// and <0 stack index indicates that the param isn't on the stack
				// (i.e., function, constant, or config var)
				continue;
			}
			if (parameter.getType() == ExpType.Rule) {
				setStackItem(pos, null);
			}
			pos += parameter.getTupleSize();
		}
		setStackSize(oldSize);
		setLogicalStackTop(getStackSize());
	}

	public void colorConflict(ASTWhere where) {
		if (colorConflict) {
			return;
		}
		colorConflict = true;
		cfdg.getSystem().warning("Conflicting color change", where);
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

	public void init() {
		initTraverse();

		Rand64.initRandomSeed(variation);

		currentSeed = new Rand64();

		cfStack = new CFStack(new CFStackItem[8192]);

		final Shape dummy = new Shape();
		for (ASTReplacement rep : cfdg.getContents().getBody()) {
			if (rep instanceof ASTDefine def) {
				def.traverse(builder, this, dummy, false);
			}
		}

		fixedBorderX = 0;
		fixedBorderY = 0;
		shapeBorder = 1;
		totalArea = 0;

		outputSoFar = 0;

		final double[] value = new double[1];

		minArea = MIN_AREA;

		value[0] = minSize;
		cfdg.hasParameter(CFG.MinimumSize, value, this);
		minSize = value[0] <= 0 ? MIN_SIZE : minSize;
		minArea = minSize * minSize;

		fixedBorderX = FIXED_BORDER * Math.min(border, 1.0);
		shapeBorder = SHAPE_BORDER * Math.max(border, 1.0);

		value[0] = fixedBorderX;
		cfdg.hasParameter(CFG.BorderFixed, value, this);
		fixedBorderX = value[0];

		value[0] = shapeBorder;
		cfdg.hasParameter(CFG.BorderDynamic, value, this);
		shapeBorder = value[0];

		if (2 * (int) Math.abs(fixedBorderX) >= Math.min(width, height)) {
			fixedBorderX = 0;
		}

		if (shapeBorder <= 0.0) {
			shapeBorder = 1.0;
		}

		if (cfdg.hasParameter(CFG.MaxNatural, value, this) && (value[0] < 1.0 || value[0] >= AST.MAX_NATURAL)) {
			final ASTExpression max = cfdg.hasParameter(CFG.MaxNatural);
			maxNatural = value[0];
			throw new CFDGException((value[0] < 1.0) ? "CF::MaxNatural must be >= 1" : "CF::MaxNatural must be < " + AST.MAX_NATURAL, max.getWhere());
		}

		maxNatural = value[0];

		currentPath = new ASTCompiledPath(cfdg.getSystem(), null);

		cfdg.getSymmetry(builder, this, symmetryOps);

		cfdg.initBackgroundColor(this);
	}

	public void initBounds() {
		init();

		final double[] vector = new double[2];

		tiled = cfdg.isTiled(null, vector);
		frieze = cfdg.isFrieze(null, vector);
		sized = cfdg.isSized(vector);
		timed = cfdg.isTimed(timeBounds);

		final double tileX = vector[0];
		final double tileY = vector[1];

		if (tiled || sized) {
			fixedBorderX = shapeBorder = 0.0;
			bounds.setMinX(-tileX / 2.0);
			bounds.setMinY(-tileY / 2.0);
			bounds.setMaxX(tileX / 2.0);
			bounds.setMaxY(tileY / 2.0);
			final int[] currWidth = new int[] { width };
			final int[] currHeight = new int[] { height };
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
			final int[] currWidth = new int[] { width };
			final int[] currHeight = new int[] { height };
			rescaleOutput(currWidth, currHeight, true);
			width = currWidth[0];
			height = currHeight[0];
			scaleArea = currArea;
		}
	}

	private void cleanup() {
		finishedShapes.clear();
		unfinishedShapes.clear();

		unwindStack(0, cfdg.getContents().getParameters());

		currentPath = null;

		cfdg.resetCachedPaths();
	}

	public void setMaxShapes(int maxShapes) {
		this.maxShapes = (maxShapes != 0) ? maxShapes : 400000000;
	}

	public void resetBounds() {
		bounds = new Bounds();
	}

	public void setLogicalStack(CFStack logicalStack) {
		this.cfStack = logicalStack;
	}

	private void outputPrep(CFCanvas canvas) {
		this.canvas = canvas;

		if (canvas != null) {
			width = canvas.getWidth();
			height = canvas.getHeight();

			if (tiled || frieze != FriezeType.NoFrieze) {
				final AffineTransform transform = new AffineTransform();
				cfdg.isTiled(transform, null);
				cfdg.isFrieze(transform, null);
				tiledCanvas = new TiledCanvas(canvas, transform, frieze);
				tiledCanvas.setScale(currScale);
				this.canvas = tiledCanvas;
			}

			frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}

		requestStop = false;
		requestFinishUp = false;
		requestUpdate = false;

		animating = false;
		//TODO implement stats
//		m_stats.inOutput = false;
//		m_stats.animating = false;
//		m_stats.finalOutput = false;
	}

	public double run(CFCanvas canvas, boolean partialDraw) {
		if (canvas != null) {
			canvas.clear(cfdg.getBackgroundColor());
		}

		notifyDraw();

		if (!animating) {
			outputPrep(canvas);
		}

		int reportAt = 250;

		{
			final Shape initShape = cfdg.getInitialShape(this);

			initShape.getWorldState().setRand64Seed(currentSeed);

			if (!timed) {
				timeBounds = initShape.getWorldState().getTransformTime();
			}

			try {
				processShape(initShape);
			} catch (CFDGException e) {
				log.log(Level.WARNING, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getSystem().error(e.getMessage(), e.getWhere());
			} catch (Exception e) {
				log.log(Level.SEVERE, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getSystem().fail(e.getMessage());
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

			final Shape shape = (Shape) unfinishedShapes.removeFirst().clone();

			todoCount -= 1;

			try {
				final ASTRule rule = cfdg.findRule(shape.getShapeType(), shape.getWorldState().getRand64Seed().getDouble());
				drawingMode = false;
				rule.traverseRule(builder, this, shape, false);
			} catch (CFDGDeferUntilRuntimeException | CFDGStopException e) {
				log.log(Level.INFO, "Stop rendering...", e);
				break;
			} catch (CFDGException e) {
				log.log(Level.WARNING, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getSystem().error(e.getMessage(), e.getWhere());
				break;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Can't render CFDG image", e);
				requestStop = true;
				cfdg.getSystem().fail(e.getMessage());
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

		outputStats();

		if (canvas != null) {
			cfdg.getSystem().info("Done.", null);
		}

		if (canvas != null && frieze != FriezeType.NoFrieze) {
			final int[] currWidth = new int[] { width };
			final int[] currHeight = new int[] { height };
			rescaleOutput(currWidth, currHeight, true);
			width = currWidth[0];
			height = currHeight[0];
		}

		long totalTime = System.currentTimeMillis() - time;
		cfdg.getSystem().info("Rendering of " + outputSoFar + " shapes took " + totalTime / 1000.0 + "s", null);

		return currScale;
	}

	public void draw(CFCanvas canvas) {
		frameTimeBounds = AffineTransformTime.getTranslateInstance(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		outputPrep(canvas);
		outputFinal();
		outputStats();
	}

	public void animate(CFCanvas canvas, int frames, int frame, boolean zoom) {
		if (canvas != null) {
			canvas.clear(cfdg.getBackgroundColor());
		}

		notifyDraw();

		final boolean ftime = cfdg.usesFrameTime();

		zoom = zoom && !ftime;

		if (!ftime) {
			cfdg.getSystem().info("Precomputing time/space bounds", null);
			cleanup();
		}

		final int[] currWidth = new int[] { width };
		final int[] currHeight = new int[] { height };
		rescaleOutput(currWidth, currHeight, true);

		outputPrep(canvas);

		final double framInc = (timeBounds.getEnd() - timeBounds.getBegin()) / frames;

		final OutputBounds outputBounds = new OutputBounds(frames, timeBounds, currWidth[0], currHeight[0], this);

		if (!ftime) {
			cfdg.getSystem().info("Computing zoom", null);

			try {
				forEachShape(outputBounds::apply);
				outputSoFar = 0;
				outputBounds.backwardFilter(10);
			} catch (CFDGStopException e) {
				log.log(Level.INFO, "Stop rendering...", e);
				animating = false;
				return;
			} catch (CFDGException e) {
				log.log(Level.WARNING, "Can't render CFDG image", e);
				cfdg.getSystem().error(e.getMessage(), e.getWhere());
				return;
			} catch (Exception e) {
				log.log(Level.SEVERE, "Can't render CFDG image", e);
				cfdg.getSystem().error(e.getMessage(), null);
				return;
			}
		}

		shapeCount = 0;
		animating = true;

		frameTimeBounds.setEnd(timeBounds.getBegin());

		final Bounds savedBounds = bounds;

		for (int frameCount = 1; frameCount <= frames; frameCount++) {
			if (frame != 0 && frameCount != frame) continue;

			cfdg.getSystem().info(String.format("Generating frame %d of %d", frameCount, frames), null);

			if (zoom) {
				bounds = outputBounds.frameBounds(frameCount - 1);
			}

			shapeCount += outputBounds.frameCount(frameCount - 1);

			frameTimeBounds.setBegin(frameTimeBounds.getEnd());
			frameTimeBounds.setEnd(timeBounds.getBegin() + framInc * frameCount);

			if (ftime) {
				currentTime = (frameTimeBounds.getBegin() + frameTimeBounds.getEnd()) * 0.5;
				currentFrame = (frameCount - 1.0) / (frames - 1.0);

				try {
					initBounds();
				} catch (CFDGException e) {
					cfdg.getSystem().error(e.getMessage(), e.getWhere());
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

			if (ftime) {
				cleanup();
			}

			if (canvas.hasError()) {
				cfdg.getSystem().error(String.format("An error occurred generating frame %d", frame), null);
			}

			if (requestStop || requestFinishUp) {
				break;
			}
		}

		bounds = savedBounds;
		animating = false;
		outputStats();

		if (frame == 0) {
			cfdg.getSystem().info(String.format("Animation of %d frames complete", frames), null);
		}
	}

	public void processShape(Shape shape) {
		double area = shape.getAreaCache();
		if (!Double.isFinite(area)) {
			requestStop = true;
			cfdg.getSystem().error("A shape has undefined or infinite state: %s".formatted(cfdg.decodeShapeName(shape.getShapeType())), null);
			return;
		}

		if (shape.getWorldState().getTransformTime().getBegin() > shape.getWorldState().getTransformTime().getEnd()) {
			return;
		}

		if (cfdg.getShapeType(shape.getShapeType()) == ShapeType.RuleType && cfdg.shapeHasRules(shape.getShapeType())) {
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
			requestStop = true;
			cfdg.getSystem().error(String.format("Shape with no rules encountered: %s", cfdg.decodeShapeName(shape.getShapeType())), null);
		}
	}

	public void processPrimShape(Shape shape, ASTRule rule) {
		if (symmetryOps.isEmpty() || shape.getShapeType() == PrimShapeType.fillType.getType()) {
			processPrimShapeSiblings(shape, rule);
		} else {
            for (AffineTransform symmetryOp : symmetryOps) {
                Shape sym = (Shape) shape.clone();
                sym.getWorldState().getTransform().concatenate(symmetryOp);
                processPrimShapeSiblings(sym, rule);
            }
		}
	}

	private void processPrimShapeSiblings(Shape shape, ASTRule path) {
		if (scale == 0.0) {
			// If we don't know the approximate scale yet then just
			// make an educated guess.
			scale = (width + height) / Math.sqrt(Math.abs(shape.getWorldState().getTransform().getDeterminant()));
		}

		if (path != null || shape.getShapeType() != PrimShapeType.fillType.getType()) {
			currentArea = 0.0;
			pathBounds.invalidate();
			drawingMode = false;

			if (path != null) {
				opsOnly = false;
				path.traversePath(builder, this, shape);
			} else {
				if (shape.getShapeType() < 3) {
					CommandInfo attr = shapeMap.get(shape.getShapeType());
					processPathCommand(shape, attr);
				}
			}

			// Drop off-canvas shapes if CF::Size is specified, or any shape where
			// something weird happened while determining its bounds
			if (!pathBounds.valid() || (sized && !pathBounds.overlaps(bounds)))
				return;

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

				final int[] currWidth = new int[] { width };
				final int[] currHeight = new int[] { height };
				scale = bounds.computeScale(currWidth, currHeight, fixedBorderX, fixedBorderY, false, null, false);
				width = currWidth[0];
				height = currHeight[0];
				scaleArea = scale * scale;
			}
		} else {
			currentArea = 1.0;
		}

		shapeCount += 1;

		final FinishedShape finishedShape = new FinishedShape((Shape)shape.clone(), shapeCount, new Bounds(pathBounds));
		finishedShape.getWorldState().getTransformZ().setSz(currentArea);

		if (!cfdg.usesTime()) {
			finishedShape.getWorldState().getTransformTime().setBegin(totalArea);
			finishedShape.getWorldState().getTransformTime().setEnd(Double.POSITIVE_INFINITY);
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
			cfdg.getSystem().error("A shape has undefined or infinite state: %s".formatted(cfdg.decodeShapeName(finishedShape.shapeType)), null);
			return;
		}

		// Drop shapes outside the current frame if we are animating and rerunning
		// the cfdg file for every frame.
		if (!cfdg.usesFrameTime() || finishedShape.getWorldState().getTransformTime().overlaps(frameTimeBounds)) {
			finishedShapes.add(finishedShape);
		}
	}

	public void processSubpath(Shape shape, boolean tr, RepElemType expectedType) {
		ASTRule rule;
		if (cfdg.getShapeType(shape.getShapeType()) != ShapeType.PathType && PrimShape.isPrimShape(shape.getShapeType()) && expectedType == RepElemType.op) {
			rule = primitivePaths[shape.getShapeType()];
		} else {
			rule = cfdg.findRule(shape.getShapeType(), 0.0);
		}
		if (rule.getRuleBody().getRepType() != expectedType.getType()) {
			throw new CFDGException("Subpath is not of the expected type (path ops/commands)", rule.getWhere());
		}
		boolean saveOpsOnly = opsOnly;
		opsOnly = opsOnly || (expectedType == RepElemType.op);
		rule.getRuleBody().traverse(builder, this, shape, tr, true);
		opsOnly = saveOpsOnly;
	}

	public void processPathCommand(Shape shape, CommandInfo info) {
		if (drawingMode) {
			if (canvas != null && info != null) {
				double[] color = shape.getWorldState().color().getRGBA();
				AffineTransform tr = shape.getWorldState().getTransform();
				//TODO implement blend
				final int blend = 0;
//				final int blend = (info.getFlags() & (1 << 20)) != 0 ? ((info.getFlags() >> 21) & 31) : SOURCE_OVER;
				canvas.path(color, tr, info.getPath().getCurrentPath(), info.getFlags(), info.getStrokeWidth(), info.getMiterLimit(), blend);
			}
		} else {
			if (info != null) {
				pathBounds.update(shape.getWorldState().getTransform(), info.getPath().getCurrentPath(), scale, info.getFlags(), info.getStrokeWidth());
				currentArea = Math.abs((pathBounds.getMaxX() - pathBounds.getMinX()) * (pathBounds.getMaxY() - pathBounds.getMinY()));
			}
		}
	}

	private void rescaleOutput(int[] width, int[] height, boolean finalStep) {
		if (!bounds.valid()) {
			return;
		}

		final AffineTransform transform = new AffineTransform();

		final double scale = bounds.computeScale(width, height, fixedBorderX, fixedBorderY, true, transform, tiled || sized || frieze != FriezeType.NoFrieze);

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

	private void forEachShape(Consumer<FinishedShape> shapeFunction) {
		int shapeIdx = 0;
		int drawAt = DRAW_AT;
		for (FinishedShape shape : finishedShapes) {
			shapeFunction.accept(shape);
			shapeIdx += 1;
			if (shapeIdx == drawAt) {
				notifyDraw();
				drawAt *= 2;
			}
			if (shapeIdx % 100 == 0) {
				Thread.yield();
			}
		}
		outputSoFar += finishedShapes.size();
	}

	private void drawShape(FinishedShape shape) {
		if (requestStop) {
			throw new CFDGStopException("Stopping", ASTWhere.DEFAULT_WHERE);
		}

		if (!finalStep && requestFinishUp) {
			throw new CFDGStopException("Stopping", ASTWhere.DEFAULT_WHERE);
		}

		if (requestUpdate) {
			outputStats();
		}

		if (!shape.getWorldState().getTransformTime().overlaps(frameTimeBounds)) {
			return;
		}

		final AffineTransform transform = shape.getWorldState().getTransform();
		transform.preConcatenate(currTransform);

		final double a = shape.getWorldState().getTransformZ().getSz() * currArea;

		if (shape.getShapeType() != PrimShapeType.fillType.getType() && (!Double.isFinite(a) || a < minArea)) {
			return;
		}

		if (tiledCanvas != null && shape.getShapeType() != PrimShapeType.fillType.getType()) {
			final Bounds b = new Bounds(shape.bounds());
			final Point2D.Double p1 = new Point2D.Double(b.getMinX(), b.getMinY());
			final Point2D.Double p2 = new Point2D.Double(b.getMaxX(), b.getMaxY());
			currTransform.transform(p1, p1);
			currTransform.transform(p2, p2);
			b.setMinX(p1.getX());
			b.setMinY(p1.getY());
			b.setMaxX(p2.getX());
			b.setMaxY(p2.getY());
			tiledCanvas.tileTransform(b);
		}

		if (cfdg.getShapeType(shape.getShapeType()) == ShapeType.PathType) {
			final ASTRule rule = cfdg.findRule(shape.getShapeType());
			rule.traversePath(builder, this, shape);
		} else {
			final double[] color = shape.getWorldState().color().getRGBA();
			//TODO implement blend
			/*
			//==============================================================comp_op_e
			enum comp_op_e
			{
				comp_op_clear,         //----comp_op_clear
				comp_op_src,           //----comp_op_src
				comp_op_dst,           //----comp_op_dst
				comp_op_src_over,      //----comp_op_src_over
				comp_op_dst_over,      //----comp_op_dst_over
				comp_op_src_in,        //----comp_op_src_in
				comp_op_dst_in,        //----comp_op_dst_in
				comp_op_src_out,       //----comp_op_src_out
				comp_op_dst_out,       //----comp_op_dst_out
				comp_op_src_atop,      //----comp_op_src_atop
				comp_op_dst_atop,      //----comp_op_dst_atop
				comp_op_xor,           //----comp_op_xor
				comp_op_plus,          //----comp_op_plus
				//comp_op_minus,         //----comp_op_minus
				comp_op_multiply,      //----comp_op_multiply
				comp_op_screen,        //----comp_op_screen
				comp_op_overlay,       //----comp_op_overlay
				comp_op_darken,        //----comp_op_darken
				comp_op_lighten,       //----comp_op_lighten
				comp_op_color_dodge,   //----comp_op_color_dodge
				comp_op_color_burn,    //----comp_op_color_burn
				comp_op_hard_light,    //----comp_op_hard_light
				comp_op_soft_light,    //----comp_op_soft_light
				comp_op_difference,    //----comp_op_difference
				comp_op_exclusion,     //----comp_op_exclusion
				//comp_op_contrast,      //----comp_op_contrast
				//comp_op_invert,        //----comp_op_invert
				//comp_op_invert_rgb,    //----comp_op_invert_rgb

				end_of_comp_op_e
			};*/
			final int blend = 0;
//			final int blend = (shape.getWorldState().getBlendMode() & (1 << 20)) != 0 ?
//					((shape.getWorldState().getBlendMode() >> 21) & 31) : SOURCE_OVER;
			if (PrimShape.isPrimShape(shape.getShapeType())) {
				canvas.primitive(shape.getShapeType(), color, transform, blend);
			} else {
				requestStop = true;
				cfdg.getSystem().error("Non drawable shape with no rules: %s".formatted(cfdg.decodeShapeName(shape.getShapeType())), null);
				throw new CFDGStopException("Stopping", ASTWhere.DEFAULT_WHERE);
			}
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

		final int[] currWidth = new int[] { width };
		final int[] currHeight = new int[] { height };
		rescaleOutput(currWidth, currHeight, true);

		final long time = System.currentTimeMillis();
		Collections.sort(finishedShapes);
		final long totalTime = System.currentTimeMillis() - time;
		cfdg.getSystem().info("Sorting of " + finishedShapes.size() + " shapes took " + totalTime / 1000.0 + "s", null);

		canvas.start(outputSoFar == 0, cfdg.getBackgroundColor(), currWidth[0], currHeight[0]);

		drawingMode = true;

		drawFinishedShapes();

		canvas.end();

		notifyDraw();
	}

	private void drawFinishedShapes() {
		try {
			final long time = System.currentTimeMillis();
			forEachShape(this::drawShape);
			final long totalTime = System.currentTimeMillis() - time;
			cfdg.getSystem().info("Drawing of " + finishedShapes.size() + " shapes took " + totalTime / 1000.0 + "s", null);
		} catch (CFDGStopException e) {
			log.log(Level.INFO, "Stop rendering...", e);
		} catch (CFDGException e) {
			log.log(Level.WARNING, "Can't render CFDG image", e);
			cfdg.getSystem().error(e.getMessage(), e.getWhere());
		} catch (Exception e) {
			log.log(Level.SEVERE, "Can't render CFDG image", e);
			cfdg.getSystem().fail(e.getMessage());
		}
	}

	private void notifyDraw() {
		final CFListener renderListener = getListener();
		if (renderListener != null) {
			renderListener.draw();
		}
	}

	private void outputFinal() {
		output(true);
	}

	private void outputPartial() {
		output(false);
	}

	private void outputStats() {
		//TODO implement stats
		requestUpdate = false;
	}

	public static boolean abortEverything() {
		//TODO is abortEverything required?
		return false;
	}
}
