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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ASTCons extends ASTExpression {
	private final List<ASTExpression> children = new ArrayList<>();

	public ASTCons(Token token, CFDGDriver driver, List<ASTExpression> kids) {
		super(token, driver, true, true, ExpType.None);
		locality = Locality.PureLocal;
		for (ASTExpression kid : kids) {
			append(kid);
		}
	}

	public ASTCons(Token token, CFDGDriver driver, ASTExpression... args) {
		super(token, driver, true, true, ExpType.None);
		//TODO controllare
		locality = Locality.PureLocal;
		for (ASTExpression arg : args) {
			append(arg);
		}
	}

	@Override
	public ASTExpression simplify() {
		if (children.size() == 1) {
			//TODO controllare
			return simplify(children.getFirst());
		}
        children.replaceAll(this::simplify);
        return this;
	}

	@Override
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		if ((type.getType() & (ExpType.Numeric.getType() | ExpType.Flag.getType())) == 0 || (type.getType() & (ExpType.Mod.getType() | ExpType.Rule.getType())) != 0) {
			driver.error("Non-numeric expression in a numeric context", null);
			return -1;
		}
		int count = 0;
		for (ASTExpression child : children) {
			double[] value = null;
			if (result != null) {
				value = new double[] { 0 };
			}
			int num = child.evaluate(value, length, renderer);
			if (num < 0) {
				return -1;
			}
			if (result != null) {
				result[count] = value[0];
			}
			count += num;
			if (result != null) {
				length -= num;
			}
		}
		return count;
	}		

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		for (ASTExpression child : children) {
			child.evaluate(result, shapeDest, renderer);
		}
	}

	@Override
	public void entropy(StringBuilder e) {
		for (ASTExpression child : children) {
			child.entropy(e);
		}
		e.append("\u00C5\u0060\u00A5\u00C5\u00C8\u0074");
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
        switch (ph) {
            case TypeCheck -> {
                constant = natural = false;
                locality = Locality.PureLocal;
                type = ExpType.None;
                for (int i = 0; i < children.size(); i++) {
                    children.set(i, compile(children.get(i), ph));
                    ASTExpression child = children.get(i);
                    constant = constant && child.isConstant();
                    natural = natural && child.isNatural();
                    locality = ASTUtils.combineLocality(locality, child.getLocality());
                    type = ExpType.fromType(type.getType() | child.getType().getType());
                }
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
	public int size() {
		return children.size();
	}

	@Override
	public ASTExpression getChild(int i) {
		if (i >= children.size()) {
			driver.error("Expression list bounds exceeded", getToken());
		}
		return children.get(i);
	}

	public void setChild(int i, ASTExpression exp) {
		if (i >= children.size()) {
			driver.error("Expression list bounds exceeded", getToken());
		}
		children.set(i, exp);
	}

	@Override
	public ASTExpression append(ASTExpression e) {
        if (e == null) return this;
        constant = constant && e.isConstant();
        natural = natural && e.isNatural();
		locality = ASTUtils.combineLocality(locality, e.getLocality());
        type = ExpType.fromType(type.getType() | e.getType().getType());
        
        // can't insert an ASTcons into children, it will be flattened away.
        // You must wrap the ASTcons in an ASTparen in order to insert it whole.
        if (e instanceof ASTCons c) {
            children.addAll(c.getChildren());
        } else {
        	children.add(e);
        }

        return this;
	}
}
