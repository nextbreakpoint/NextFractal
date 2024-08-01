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
package com.nextbreakpoint.nextfractal.mandelbrot.dsl.interpreter;

import com.nextbreakpoint.nextfractal.core.common.ParserError;
import com.nextbreakpoint.nextfractal.core.common.ParserErrorType;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ComplexNumber;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.VariableDeclaration;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.SimpleASTCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class InterpreterOrbitFactory implements ClassFactory<Orbit> {
	private final ASTFractal fractal;
	private final String source;
	@Getter
    private final List<ParserError> errors;
	
	public InterpreterOrbitFactory(ASTFractal fractal, String source, List<ParserError> errors) {
		this.fractal = fractal;
		this.source = source;
		this.errors = errors;
	}
	
	public Orbit create() throws DSLCompilerException {
		try {
			ExpressionContext context = new ExpressionContext();
			ASTOrbit astOrbit = fractal.getOrbit();
			double ar = astOrbit.getRegion().getA().r();
			double ai = astOrbit.getRegion().getA().i();
			double br = astOrbit.getRegion().getB().r();
			double bi = astOrbit.getRegion().getB().i();
            List<VariableDeclaration> orbitVars = new ArrayList<>(fractal.getOrbitVariables());
            List<VariableDeclaration> stateVars = new ArrayList<>(fractal.getStateVariables());
			Map<String, VariableDeclaration> vars = new HashMap<>();
            for (VariableDeclaration var : fractal.getStateVariables()) {
                vars.put(var.getName(), var);
            }
            for (VariableDeclaration var : fractal.getOrbitVariables()) {
                vars.put(var.getName(), var);
            }
            SimpleASTCompiler compiler = new SimpleASTCompiler(context, new HashMap<>(vars));
			List<DSLStatement> beginStatements = new ArrayList<>();
			if (astOrbit.getBegin() != null) {
				for (ASTStatement astStatement : astOrbit.getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitBegin orbitBegin = new DSLOrbitBegin(astOrbit.getLocation(), beginStatements);
			final DSLOrbitLoop orbitLoop = getOrbitLoop(astOrbit.getLoop(), compiler, stateVars);
			List<DSLStatement> endStatements = new ArrayList<>();
			if (astOrbit.getEnd() != null) {
				for (ASTStatement astStatement : astOrbit.getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitEnd orbitEnd = new DSLOrbitEnd(astOrbit.getLocation(), endStatements);
			List<DSLTrap> traps = new ArrayList<>();
			if (astOrbit.getTraps() != null) {
				for (ASTOrbitTrap astTrap : astOrbit.getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			final DSLOrbit orbit = DSLOrbit.builder()
					.widthLocation(astOrbit.getLocation())
					.widthRegion(getRegion(ar, ai, br, bi))
					.widthBegin(orbitBegin)
					.widthLoop(orbitLoop)
					.widthEnd(orbitEnd)
					.widthTraps(traps)
					.widthOrbitVariables(orbitVars)
					.widthStateVariables(stateVars)
					.widthExpressionContext(context)
					.build();
			return new InterpretedOrbit(orbit);
		} catch (ASTException e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			errors.add(new ParserError(type, line, charPositionInLine, index, length, message));
			throw new DSLCompilerException("Can't build orbit", source, errors);
		} catch (Exception e) {
			ParserErrorType type = ParserErrorType.COMPILE;
			String message = e.getMessage();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
			throw new DSLCompilerException("Can't build orbit", source, errors);
		}
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
