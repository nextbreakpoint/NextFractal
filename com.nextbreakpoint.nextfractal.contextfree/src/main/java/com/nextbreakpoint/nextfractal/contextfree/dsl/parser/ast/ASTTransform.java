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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ASTTransform extends ASTReplacement {
	private final ASTRepContainer body;
	@Setter
    private ASTExpression expHolder;
	@Setter
    private boolean clone;
	
	public ASTTransform(Token token, CFDGDriver driver, ASTExpression exp) {
		super(token, driver, null, RepElemType.empty);
		body = new ASTRepContainer(token, driver);
		this.expHolder = exp;
		this.clone = false;
	}

    @Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		ASTExpression ret = null;
		if (expHolder != null) {
			ret = expHolder.compile(ph);
		}
		if (ret != null) {
			driver.error("Error analyzing transform list", getToken());
		}
		body.compile(ph, null, null);

        switch (ph) {
            case TypeCheck -> {
                if (clone && !ASTParameter.Impure) {
                    driver.error("Shape cloning only permitted in impure mode", getToken());
                }
            }
            case Simplify -> {
                if (expHolder != null) {
                    expHolder = expHolder.simplify();
                }
            }
            default -> {
            }
        }
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		AffineTransform dummy = new AffineTransform();
		List<AffineTransform> transforms = new ArrayList<>();
		List<ASTModification> mods = ASTUtils.getTransforms(driver, expHolder, transforms, renderer, false, dummy);
		Rand64 cloneSeed = renderer.getCurrentSeed();
		Shape transChild = (Shape)parent.clone();
		boolean opsOnly = body.getRepType() == RepElemType.op.getType();
		if (opsOnly && !tr) {
			transChild.getWorldState().getTransform().setToIdentity();
		}
		int modsLength = mods.size();
		int totalLength = modsLength + transforms.size();
		for (int i = 0; i < totalLength; i++) {
			//TODO revedere
            if (i < modsLength) {
				mods.get(i).evaluate(transChild.getWorldState(), true, renderer);
			} else {
				transChild.getWorldState().getTransform().concatenate(transforms.get(i - modsLength));
			}
			int size = renderer.getStackSize();
			for (ASTReplacement rep : body.getBody()) {
				if (clone) {
					renderer.setCurrentSeed(cloneSeed);
				}
				rep.traverse(transChild, tr || opsOnly, renderer);
			}
			renderer.unwindStack(size, body.getParameters());
		}
	}
}
