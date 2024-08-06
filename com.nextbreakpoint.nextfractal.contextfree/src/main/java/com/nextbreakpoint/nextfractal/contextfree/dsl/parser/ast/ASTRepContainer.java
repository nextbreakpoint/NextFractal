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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

//TODO ASTObject?
public class ASTRepContainer {
	private final CFDGDriver driver;
	@Getter
    private final List<ASTReplacement> body = new ArrayList<>();
	@Getter
    private final boolean global;
	@Setter
    @Getter
    private List<ASTParameter> parameters = new ArrayList<>();
	@Setter
    @Getter
    private PathOp pathOp;
	@Setter
    @Getter
    private int repType;
	@Getter
    @Setter
    private int stackCount;

	public ASTRepContainer(CFDGDriver driver) {
		this.driver = driver;
		pathOp = PathOp.UNKNOWN;
		repType = RepElemType.empty.getType();
		global = false;
		stackCount = 0;
	}
	
	public ASTRepContainer(ASTRepContainer repCont) {
		driver = repCont.driver;
		pathOp = repCont.pathOp;
		repType = repCont.repType;
		global = repCont.global;
		stackCount = repCont.stackCount;
	}

    public void addParameter(String type, int nameIndex, Token nameLocation) {
		parameters.add(new ASTParameter(driver, type, nameIndex, nameLocation));
		ASTParameter param = parameters.getLast();
		param.setParameter(true);
		param.checkParam();
	}

	public ASTParameter addDefParameter(int nameIndex, ASTDefine def, Token nameLocation) {
		parameters.add(new ASTParameter(driver, nameIndex, def, nameLocation));
		ASTParameter param = parameters.getLast();
		param.checkParam();
		return param;
	}

	public void addLoopParameter(int nameIndex, boolean natural, boolean local, Token nameLocation) {
		parameters.add(new ASTParameter(driver, nameIndex, natural, local, nameLocation));
		ASTParameter param = parameters.getLast();
		param.checkParam();
		stackCount += param.getTupleSize();
	}

	public void compile(CompilePhase ph, ASTLoop loop, ASTDefine def) {
        switch (ph) {
            case TypeCheck -> {
                stackCount = 0;
                for (int i = 0; i < parameters.size(); i++) {
                    if (parameters.get(i).isParameter() || parameters.get(i).isLoopIndex()) {
                        stackCount += parameters.get(i).getTupleSize();
                    } else {
                        parameters = parameters.subList(0, i);
                        break;
                    }
                }

                driver.pushRepContainer(this);
                if (loop != null) {
                    loop.compileLoopMod();
                }
                for (ASTReplacement rep : body) {
                    rep.compile(ph);
                }
                if (def != null) {
                    def.compile(ph);
                }
                driver.popRepContainer(null);
            }
			case Simplify -> {
				// do nothing
			}
			default -> {
			}
        }
	}
	
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer, boolean getParams) {
		int size = renderer.getStackSize();
		if (getParams && parent.getParameters() != null) {
			renderer.initStack(parent.getParameters());
		}
		for (ASTReplacement rep : body) {
			rep.traverse(parent, tr, renderer);
		}
		renderer.unwindStack(size, getParameters());
	}
}
