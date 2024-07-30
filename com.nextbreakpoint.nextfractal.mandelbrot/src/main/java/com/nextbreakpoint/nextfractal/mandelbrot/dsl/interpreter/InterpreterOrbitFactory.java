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
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Variable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.DSLCompilerException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.ExpressionCompilerContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitBegin;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitEnd;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.CompiledOrbitLoop;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.compiled.ExpressionCompiler;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			ExpressionCompilerContext context = new ExpressionCompilerContext();
			ASTOrbit astOrbit = fractal.getOrbit();
			double ar = astOrbit.getRegion().getA().r();
			double ai = astOrbit.getRegion().getA().i();
			double br = astOrbit.getRegion().getB().r();
			double bi = astOrbit.getRegion().getB().i();
			List<Variable> orbitVars = new ArrayList<>();
			for (Variable var : fractal.getOrbitVariables()) {
				orbitVars.add(var.copy());
			}
			List<Variable> stateVars = new ArrayList<>();
			for (Variable var : fractal.getStateVariables()) {
				stateVars.add(var.copy());
			}
			Map<String, Variable> vars = new HashMap<>();
            for (Variable var : fractal.getStateVariables()) {
                vars.put(var.getName(), var);
            }
            for (Variable var : fractal.getOrbitVariables()) {
                vars.put(var.getName(), var);
            }
			Map<String, Variable> newScope = new HashMap<>(vars);
			ExpressionCompiler compiler = new ExpressionCompiler(context, newScope);
			CompiledOrbit orbit = new CompiledOrbit(orbitVars, stateVars, astOrbit.getLocation());
			orbit.setRegion(new Number[] { new Number(ar, ai), new Number(br, bi) });
			List<CompiledStatement> beginStatements = new ArrayList<>();
			if (astOrbit.getBegin() != null) {
				for (ASTStatement astStatement : astOrbit.getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			orbit.setBegin(new CompiledOrbitBegin(beginStatements));
			List<CompiledStatement> loopStatements = new ArrayList<>();
			if (astOrbit.getLoop() != null) {
				for (ASTStatement astStatement : astOrbit.getLoop().getStatements()) {
					loopStatements.add(astStatement.compile(compiler));
				}
			}
			final CompiledOrbitLoop orbitLoop = new CompiledOrbitLoop(loopStatements);
			orbit.setLoop(orbitLoop);
			if (astOrbit.getLoop() != null) {
				orbitLoop.setCondition(astOrbit.getLoop().getExpression().compile(compiler));
				orbitLoop.setBegin(astOrbit.getLoop().getBegin());
				orbitLoop.setEnd(astOrbit.getLoop().getEnd());
			}
			List<CompiledStatement> endStatements = new ArrayList<>();
			if (astOrbit.getEnd() != null) {
				for (ASTStatement astStatement : astOrbit.getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			orbit.setEnd(new CompiledOrbitEnd(endStatements));
			List<CompiledTrap> traps = new ArrayList<>();
			if (astOrbit.getTraps() != null) {
				for (ASTOrbitTrap astTrap : astOrbit.getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			orbit.setTraps(traps);
			return new InterpretedOrbit(orbit, context);
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
}
