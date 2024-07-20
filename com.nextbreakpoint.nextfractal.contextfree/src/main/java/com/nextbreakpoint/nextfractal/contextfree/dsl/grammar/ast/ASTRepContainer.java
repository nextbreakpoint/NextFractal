/*
 * NextFractal 2.3.1
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

import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

public class ASTRepContainer {
	private PathOp pathOp;
	private int repType;
	private List<ASTReplacement> body = new ArrayList<>();
	private List<ASTParameter> parameters = new ArrayList<>();
	private boolean isGlobal;
	private int stackCount;
	private CFDGDriver driver;

	public ASTRepContainer(CFDGDriver driver) {
		this.driver = driver;
		pathOp = PathOp.UNKNOWN;
		repType = RepElemType.empty.getType();
		isGlobal = false;
		stackCount = 0;
	}
	
	public ASTRepContainer(ASTRepContainer repCont) {
		driver = repCont.driver;
		pathOp = repCont.pathOp;
		repType = repCont.repType;
		isGlobal = repCont.isGlobal;
		stackCount = repCont.stackCount;
	}
	
	public void setStackCount(int stackCount) {
		this.stackCount = stackCount;
	}

	public PathOp getPathOp() {
		return pathOp;
	}

	public void setPathOp(PathOp pathOp) {
		this.pathOp = pathOp;
	}
	
	public int getRepType() {
		return repType;
	}

	public void setRepType(int repType) {
		this.repType = repType;
	}
	
	public List<ASTReplacement> getBody() {
		return body;
	}

	public List<ASTParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<ASTParameter> parameters) {
		this.parameters = parameters;
	}	

	public boolean isGlobal() {
		return isGlobal;
	}

	public int getStackCount() {
		return stackCount;
	}

	public void addParameter(String type, int nameIndex, Token nameLocation) {
		parameters.add(new ASTParameter(driver, type, nameIndex, nameLocation));
		ASTParameter param = parameters.get(parameters.size() - 1);
		param.setIsParameter(true);
		param.checkParam();
	}

	public ASTParameter addDefParameter(int nameIndex, ASTDefine def, Token nameLocation) {
		parameters.add(new ASTParameter(driver, nameIndex, def, nameLocation));
		ASTParameter param = parameters.get(parameters.size() - 1);
		param.checkParam();
		return param;
	}

	public void addLoopParameter(int nameIndex, boolean natural, boolean local, Token nameLocation) {
		parameters.add(new ASTParameter(driver, nameIndex, natural, local, nameLocation));
		ASTParameter param = parameters.get(parameters.size() - 1);
		param.checkParam();
		stackCount += param.getTupleSize();
	}

	public void compile(CompilePhase ph, ASTLoop loop, ASTDefine def) {
		if (ph == CompilePhase.TypeCheck) {
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
