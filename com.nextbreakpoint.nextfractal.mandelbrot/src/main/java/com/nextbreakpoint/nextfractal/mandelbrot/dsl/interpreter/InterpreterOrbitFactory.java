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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.model.DSLTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.SimpleASTCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.parser.ast.ASTStatement;
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
			DSLExpressionContext context = new DSLExpressionContext();
			ASTOrbit astOrbit = fractal.getOrbit();
            List<VariableDeclaration> orbitVars = new ArrayList<>(fractal.getOrbitVariables());
            List<VariableDeclaration> stateVars = new ArrayList<>(fractal.getStateVariables());
			Map<String, VariableDeclaration> vars = new HashMap<>();
            for (VariableDeclaration var : fractal.getStateVariables()) {
                vars.put(var.name(), var);
            }
            for (VariableDeclaration var : fractal.getOrbitVariables()) {
                vars.put(var.name(), var);
            }
            SimpleASTCompiler compiler = new SimpleASTCompiler(context, new HashMap<>(vars));
			final List<DSLStatement> beginStatements = new ArrayList<>();
			if (astOrbit.getBegin() != null) {
				for (ASTStatement astStatement : astOrbit.getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitBegin orbitBegin = new DSLOrbitBegin(astOrbit.getLocation(), beginStatements);
			final ASTOrbitLoop astOrbitLoop = astOrbit.getLoop();
			final List<DSLStatement> loopStatements = new ArrayList<>();
			for (ASTStatement astStatement1 : astOrbitLoop.getStatements()) {
				loopStatements.add(astStatement1.compile(compiler));
			}
			final DSLOrbitLoop orbitLoop = new DSLOrbitLoop(
					astOrbitLoop.getLocation(),
					astOrbitLoop.getExpression().compile(compiler),
					astOrbitLoop.getBegin(),
					astOrbitLoop.getEnd(),
					loopStatements,
					stateVars
			);
			final List<DSLStatement> endStatements = new ArrayList<>();
			if (astOrbit.getEnd() != null) {
				for (ASTStatement astStatement : astOrbit.getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			final DSLOrbitEnd orbitEnd = new DSLOrbitEnd(astOrbit.getLocation(), endStatements);
			final List<DSLTrap> traps = new ArrayList<>();
			if (astOrbit.getTraps() != null) {
				for (ASTOrbitTrap astTrap : astOrbit.getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			final double ar = astOrbit.getRegion().getA().r();
			final double ai = astOrbit.getRegion().getA().i();
			final double br = astOrbit.getRegion().getB().r();
			final double bi = astOrbit.getRegion().getB().i();
			final DSLOrbit orbit = DSLOrbit.builder()
					.withToken(astOrbit.getLocation())
					.withRegion(getRegion(ar, ai, br, bi))
					.withBegin(orbitBegin)
					.withLoop(orbitLoop)
					.withEnd(orbitEnd)
					.withTraps(traps)
					.withOrbitVariables(orbitVars)
					.withStateVariables(stateVars)
					.withExpressionContext(context)
					.build();
			return new InterpretedOrbit(orbit);
		} catch (ASTException e) {
            long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			errors.add(new ParserError(ParserErrorType.COMPILE, line, charPositionInLine, index, length, message));
			throw new DSLCompilerException("Can't build orbit", source, errors);
		} catch (Exception e) {
            String message = e.getMessage();
			errors.add(new ParserError(ParserErrorType.COMPILE, 0, 0, 0, 0, message));
			throw new DSLCompilerException("Can't build orbit", source, errors);
		}
	}

	private static ComplexNumber[] getRegion(double ar, double ai, double br, double bi) {
		return new ComplexNumber[]{new ComplexNumber(ar, ai), new ComplexNumber(br, bi)};
	}
}
