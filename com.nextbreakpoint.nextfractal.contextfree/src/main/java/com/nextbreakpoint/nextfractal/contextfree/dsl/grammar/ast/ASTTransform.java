/*
 * NextFractal 2.3.0
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
package com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.core.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import org.antlr.v4.runtime.Token;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class ASTTransform extends ASTReplacement {
	private ASTRepContainer body;
	private ASTExpression expHolder;
	private boolean clone;
	
	public ASTTransform(CFDGDriver driver, ASTExpression exp, Token location) {
		super(driver, null, RepElemType.empty, location);
		body = new ASTRepContainer(driver);
		this.expHolder = exp;
		this.clone = false;
	}
	
	public ASTRepContainer getBody() {
		return body;
	}
	
	public boolean isClone() {
		return clone;
	}

	public void setClone(boolean clone) {
		this.clone = clone;
	}

	public ASTExpression getExpHolder() {
		return expHolder;
	}

	public void setExpHolder(ASTExpression expHolder) {
		this.expHolder = expHolder;
	}

	@Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		ASTExpression ret = null;
		if (expHolder != null) {
			ret = expHolder.compile(ph);
		}
		if (ret != null) {
			driver.error("Error analyzing transform list", location);
		}
		body.compile(ph, null, null);
		
		switch (ph) {
			case TypeCheck: 
				if (clone && !ASTParameter.Impure) {
					driver.error("Shape cloning only permitted in impure mode", location);
				}
				break;
	
			case Simplify:
				if (expHolder != null) {
					expHolder = expHolder.simplify();
				}
				break;
	
			default:
				break;
		}
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		AffineTransform dummy = new AffineTransform();
		@SuppressWarnings("unchecked")
		List<AffineTransform> transforms = new ArrayList<>();
		List<ASTModification> mods = AST.getTransforms(driver, expHolder, transforms, renderer, false, dummy);
		Rand64 cloneSeed = renderer.getCurrentSeed();
		Shape transChild = (Shape)parent.clone();
		boolean opsOnly = body.getRepType() == RepElemType.op.getType();
		if (opsOnly && !tr) {
			transChild.getWorldState().getTransform().setToIdentity();
		}
		int modsLength = mods.size();
		int totalLength = modsLength + transforms.size();
		for (int i = 0; i < totalLength; i++) {
			Shape child = transChild;
			if (i < modsLength) {
				mods.get(i).evaluate(child.getWorldState(), true, renderer);
			} else {
				child.getWorldState().getTransform().concatenate(transforms.get(i - modsLength));
			}
			int size = renderer.getStackSize();
			for (ASTReplacement rep : body.getBody()) {
				if (clone) {
					renderer.setCurrentSeed(cloneSeed);
				}
				rep.traverse(child, tr || opsOnly, renderer);
			}
			renderer.unwindStack(size, body.getParameters());
		}
	}
}
