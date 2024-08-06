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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackNumber;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.List;

public class ASTUserFunction extends ASTExpression {
	@Setter
    @Getter
    private ASTExpression arguments;
	@Getter
    private ASTDefine definition;
	@Getter
    private final int nameIndex;
	@Getter
    protected boolean let;
	private int oldTop;
	private int oldSize;

	public ASTUserFunction(CFDGDriver driver, int nameIndex, ASTExpression arguments, ASTDefine definition, Token location) {
		super(driver, false, false, ExpType.None, location);
		this.nameIndex = nameIndex;
		this.definition = definition;
		this.arguments = arguments;
		this.let = false;
	}

    @Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if (type != ExpType.Numeric) {
			driver.error("Function does not evaluate to a number", getToken());
			return -1;
		}
		if (result != null && length < definition.getTupleSize()) {
			return -1;
		}
		if (result == null) {
			return definition.getTupleSize();
		}
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getToken());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGException("Stopping", getToken());
		}
		setupStack(renderer);
		if (definition.getExp().evaluate(result, length, renderer) != definition.getTupleSize()) {
			driver.error("Error evaluating function", getToken());
		};
		cleanupStack(renderer);
		return definition.getTupleSize();
	}

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		if (type != ExpType.Mod) {
			driver.error("Function does not evaluate to an adjustment", getToken());
			return;
		}
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getToken());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGException("Stopping", getToken());
		}
		setupStack(renderer);
		definition.getExp().evaluate(result, shapeDest, renderer);
		cleanupStack(renderer);
	}

	@Override
	public void entropy(StringBuilder e) {
		if (arguments != null) {
			arguments.entropy(e);
		}
		e.append(definition.getName());
	}

	@Override
	public ASTExpression simplify() {
		if (arguments != null) {
			if (arguments instanceof ASTCons carg) {
                for (int i = 0; i < carg.getChildren().size(); i++) {
					carg.setChild(i, simplify(carg.getChild(i)));
				}
			} else {
				arguments = simplify(arguments);
			}
		}
		return this;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
        switch (ph) {
            case TypeCheck -> {
                // Function calls and shape specifications are ambiguous at parse
                // time so the parser always chooses a function call. During
                // type checkParam we may need to convert to a shape spec.
                ASTDefine[] def = new ASTDefine[1];
                @SuppressWarnings("unchecked")
                List<ASTParameter>[] p = new List[1];
                String name = driver.getTypeInfo(nameIndex, def, p);
                if (def[0] != null && p[0] != null) {
                    driver.error("Name matches both a function and a shape", getToken());
                    return null;
                }
                if (def[0] == null && p[0] == null) {
                    driver.error("Name does not match shape name or function name", getToken());
                    return null;
                }
                if (def[0] != null) {
                    arguments = compile(arguments, ph);
                    definition = def[0];
                    ASTParameter.checkType(driver, def[0].getParameters(), arguments, false);
                    constant = false;
                    natural = definition.isNatural();
                    type = definition.getExpType();
                    locality = arguments != null ? arguments.getLocality() : Locality.PureLocal;
                    if (definition.getExp() != null && definition.getExp().getLocality() == Locality.ImpureNonLocal && locality == Locality.PureNonLocal) {
                        locality = Locality.ImpureNonLocal;
                    }
                    return null;
                }
                ASTRuleSpecifier r = new ASTRuleSpecifier(driver, nameIndex, name, arguments, null, getToken());
                r.compile(ph);
                return r;
            }
			case Simplify -> {
				// do nothing
			}
			default -> {
            }
        }
		return null;
	}

	@Override
	public CFStackRule evalArgs(CFDGRenderer renderer, CFStackRule parent) {
		if (type != ExpType.Rule) {
			driver.error("Function does not evaluate to a shape", getToken());
			return null;
		}
		if (renderer == null) throw new CFDGDeferUntilRuntimeException(getToken());
		if (renderer.isRequestStop() || CFDGRenderer.abortEverything()) {
			throw new CFDGException("Stopping", getToken());
		}
		//TODO controllare
		setupStack(renderer);
		CFStackRule ret = definition.getExp().evalArgs(renderer, parent);
		cleanupStack(renderer);
		return ret;
	}
	
	private void setupStack(CFDGRenderer renderer) {
		oldTop = renderer.getLogicalStackTop();
		oldSize = renderer.getStackSize();
		if (definition.getStackCount() > 0) {
			if (oldSize + definition.getStackCount() > renderer.getMaxStackSize()) {
				driver.error("Maximum stack getMaxStackSize exceeded", getToken());
			}
			renderer.setStackSize(oldSize + definition.getStackCount());
			renderer.setStackItem(oldSize, new CFStackNumber(renderer.getStack(), 0));
			renderer.getStackItem(oldSize).evalArgs(renderer, arguments, definition.getParameters(), let);
			renderer.setLogicalStackTop(renderer.getStackSize());
		}
	}

	private void cleanupStack(CFDGRenderer renderer) {
		if (definition.getStackCount() > 0) {
			//TODO competare cleanupStack
			renderer.setStackItem(oldSize, null);
			renderer.setLogicalStackTop(oldTop);
			renderer.setStackSize(oldSize);
		}
	}
}
