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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.SimpleASTCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ASTOrbit extends ASTObject {
	@Setter
    private List<ASTOrbitTrap> traps = new ArrayList<>();
	@Setter
    private ASTOrbitBegin begin;
	@Setter
    private ASTOrbitLoop loop;
	@Setter
    private ASTOrbitEnd end;
	private final ASTRegion region;

	public ASTOrbit(Token location, ASTRegion region) {
		super(location);
		this.region = region;
	}

    public void addTrap(ASTOrbitTrap trap) {
		if (traps == null) {
			traps = new ArrayList<>();
		}
		traps.add(trap);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String suffix = "";
		if (region != null) {
			builder.append("contentRegion = ");
			builder.append(region);
			suffix = ",";
		}
		if (begin != null) {
			if (!suffix.isEmpty()) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("begin = {");
			builder.append(begin);
			builder.append("}");
		}
		if (loop != null) {
			if (!suffix.isEmpty()) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("loop = {");
			builder.append(loop);
			builder.append("}");
		}
		if (end != null) {
			if (!suffix.isEmpty()) {
				builder.append(suffix);
			} else {
				suffix = ",";
			}
			builder.append("end = {");
			builder.append(end);
			builder.append("}");
		}
		if (!suffix.isEmpty()) {
			builder.append(suffix);
		} else {
			suffix = ",";
		}
		builder.append("traps = [");
		for (int i = 0; i < traps.size(); i++) {
			ASTOrbitTrap trap = traps.get(i);
			builder.append("{");
			builder.append(trap);
			builder.append("}");
			if (i < traps.size() - 1) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}

	public DSLOrbit compile(ASTVariables variables) {
		try {
			ExpressionContext context = new ExpressionContext();
			double ar = getRegion().getA().r();
			double ai = getRegion().getA().i();
			double br = getRegion().getB().r();
			double bi = getRegion().getB().i();
            List<VariableDeclaration> orbitVars = new ArrayList<>(variables.getOrbitVariables());
            List<VariableDeclaration> stateVars = new ArrayList<>(variables.getStateVariables());
			Map<String, VariableDeclaration> vars = new HashMap<>();
			for (VariableDeclaration var : variables.getStateVariables()) {
				vars.put(var.getName(), var);
			}
			for (VariableDeclaration var : variables.getOrbitVariables()) {
				vars.put(var.getName(), var);
			}
            SimpleASTCompiler compiler = new SimpleASTCompiler(context, new HashMap<>(vars));
			List<DSLStatement> beginStatements = new ArrayList<>();
			if (getBegin() != null) {
				for (ASTStatement astStatement : getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitBegin orbitBegin = new DSLOrbitBegin(location, beginStatements);
			final DSLOrbitLoop orbitLoop = getOrbitLoop(getLoop(), compiler, stateVars);
			List<DSLStatement> endStatements = new ArrayList<>();
			if (getEnd() != null) {
				for (ASTStatement astStatement : getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitEnd orbitEnd = new DSLOrbitEnd(location, endStatements);
			List<DSLTrap> traps = new ArrayList<>();
			if (getTraps() != null) {
				for (ASTOrbitTrap astTrap : getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			return DSLOrbit.builder()
					.widthLocation(getLocation())
					.widthRegion(getRegion(ar, ai, br, bi))
					.widthBegin(orbitBegin)
					.widthLoop(orbitLoop)
					.widthEnd(orbitEnd)
					.widthTraps(traps)
					.widthOrbitVariables(orbitVars)
					.widthStateVariables(stateVars)
					.widthExpressionContext(context)
					.build();
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(type, line, charPositionInLine, index, length, message));
//			throw new DSLParserException("Can't build orbit", errors);
		} catch (Exception e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			String message = e.getMessage();
			final List<ParserError> errors = new ArrayList<>();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
//			throw new DSLParserException("Can't build orbit", errors);
		}
		return null;
	}

	private static DSLOrbitLoop getOrbitLoop(ASTOrbitLoop astOrbitLoop, SimpleASTCompiler compiler, List<VariableDeclaration> stateVars) {
		List<DSLStatement> loopStatements = new ArrayList<>();
		for (ASTStatement astStatement : astOrbitLoop.getStatements()) {
			loopStatements.add(astStatement.compile(compiler));
		}
        return new DSLOrbitLoop(
				astOrbitLoop.getLocation(),
				astOrbitLoop.getExpression().compile(compiler),
				astOrbitLoop.getBegin(),
				astOrbitLoop.getEnd(),
                loopStatements,
                stateVars
        );
	}

	private static ComplexNumber[] getRegion(double ar, double ai, double br, double bi) {
		return new ComplexNumber[]{new ComplexNumber(ar, ai), new ComplexNumber(br, bi)};
	}
}
