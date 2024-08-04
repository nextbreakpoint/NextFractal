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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast;

import com.nextbreakpoint.nextfractal.core.common.ScriptError;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.SimpleASTCompiler;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nextbreakpoint.nextfractal.core.common.ErrorType.COMPILE;

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

	public DSLOrbit compile(ASTVariables variables) throws DSLParserException {
		try {
			final DSLExpressionContext context = new DSLExpressionContext();
			final List<VariableDeclaration> orbitVars = new ArrayList<>(variables.getOrbitVariables());
			final List<VariableDeclaration> stateVars = new ArrayList<>(variables.getStateVariables());
			final Map<String, VariableDeclaration> vars = new HashMap<>();
			for (VariableDeclaration var : variables.getStateVariables()) {
				vars.put(var.name(), var);
			}
			for (VariableDeclaration var : variables.getOrbitVariables()) {
				vars.put(var.name(), var);
			}
			final SimpleASTCompiler compiler = new SimpleASTCompiler(context, new HashMap<>(vars));
			final List<DSLStatement> beginStatements = new ArrayList<>();
			if (getBegin() != null) {
				for (ASTStatement astStatement : getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitBegin orbitBegin = new DSLOrbitBegin(getLocation(), beginStatements);
			final List<DSLStatement> loopStatements = new ArrayList<>();
			for (ASTStatement astStatement1 : getLoop().getStatements()) {
				loopStatements.add(astStatement1.compile(compiler));
			}
			final DSLOrbitLoop orbitLoop = new DSLOrbitLoop(
					getLoop().getLocation(),
					getLoop().getExpression().compile(compiler),
					getLoop().getBegin(),
					getLoop().getEnd(),
					loopStatements,
					stateVars
			);
			final List<DSLStatement> endStatements = new ArrayList<>();
			if (getEnd() != null) {
				for (ASTStatement astStatement : getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitEnd orbitEnd = new DSLOrbitEnd(getLocation(), endStatements);
			final List<DSLTrap> traps = new ArrayList<>();
			if (getTraps() != null) {
				for (ASTOrbitTrap astTrap : getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			final double ar = getRegion().getA().r();
			final double ai = getRegion().getA().i();
			final double br = getRegion().getB().r();
			final double bi = getRegion().getB().i();
			return DSLOrbit.builder()
					.withToken(getLocation())
					.withRegion(getRegion(ar, ai, br, bi))
					.withBegin(orbitBegin)
					.withLoop(orbitLoop)
					.withEnd(orbitEnd)
					.withTraps(traps)
					.withOrbitVariables(orbitVars)
					.withStateVariables(stateVars)
					.withExpressionContext(context)
					.build();
		} catch (ASTException e) {
            long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
            final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, line, charPositionInLine, index, length, e.getMessage()));
			throw new DSLParserException("Can't build orbit", errors);
		} catch (Exception e) {
            final List<ScriptError> errors = new ArrayList<>();
			errors.add(new ScriptError(COMPILE, 0, 0, 0, 0, e.getMessage()));
			throw new DSLParserException("Can't build orbit", errors);
		}
	}

	private static ComplexNumber[] getRegion(double ar, double ai, double br, double bi) {
		return new ComplexNumber[]{new ComplexNumber(ar, ai), new ComplexNumber(br, bi)};
	}
}
