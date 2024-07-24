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
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ClassFactory;
import com.nextbreakpoint.nextfractal.mandelbrot.core.ParserException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.CompilerVariable;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.ExpressionContext;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.support.CompiledOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledStatement;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.common.CompiledTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Number;
import com.nextbreakpoint.nextfractal.mandelbrot.core.Orbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTException;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTFractal;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbit;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTOrbitTrap;
import com.nextbreakpoint.nextfractal.mandelbrot.dsl.grammar.ASTStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InterpreterOrbitFactory implements ClassFactory<Orbit> {
	private ASTFractal astFractal;
	private String source;
	private List<ParserError> errors;
	
	public InterpreterOrbitFactory(ASTFractal astFractal, String source, List<ParserError> errors) {
		this.astFractal = astFractal;
		this.source = source;
		this.errors = errors;
	}
	
	public Orbit create() throws ParserException {
		try {
			ExpressionContext context = new ExpressionContext();
			ASTOrbit astOrbit = astFractal.getOrbit();
			double ar = astOrbit.getRegion().getA().r();
			double ai = astOrbit.getRegion().getA().i();
			double br = astOrbit.getRegion().getB().r();
			double bi = astOrbit.getRegion().getB().i();
			List<CompilerVariable> orbitVars = new ArrayList<>();
			for (CompilerVariable var : astFractal.getOrbitVariables()) {
				orbitVars.add(var.copy());
			}
			List<CompilerVariable> stateVars = new ArrayList<>();
			for (CompilerVariable var : astFractal.getStateVariables()) {
				stateVars.add(var.copy());
			}
			Map<String, CompilerVariable> vars = new HashMap<>();
			for (Iterator<CompilerVariable> s = astFractal.getStateVariables().iterator(); s.hasNext();) {
				CompilerVariable var = s.next();
				vars.put(var.getName(), var);
			}
			for (Iterator<CompilerVariable> s = astFractal.getOrbitVariables().iterator(); s.hasNext();) {
				CompilerVariable var = s.next();
				vars.put(var.getName(), var);
			}
			Map<String, CompilerVariable> newScope = new HashMap<>(vars);
			ExpressionCompiler compiler = new ExpressionCompiler(context, newScope);
			CompiledOrbit orbit = new CompiledOrbit(orbitVars, stateVars, astOrbit.getLocation());
			orbit.setRegion(new Number[] { new Number(ar, ai), new Number(br, bi) });
			List<CompiledStatement> beginStatements = new ArrayList<>();
			List<CompiledStatement> loopStatements = new ArrayList<>();
			List<CompiledStatement> endStatements = new ArrayList<>();
			List<CompiledTrap> traps = new ArrayList<>();
			if (astOrbit.getBegin() != null) {
				for (ASTStatement astStatement : astOrbit.getBegin().getStatements()) {
					beginStatements.add(astStatement.compile(compiler));
				}
			}
			if (astOrbit.getLoop() != null) {
				for (ASTStatement astStatement : astOrbit.getLoop().getStatements()) {
					loopStatements.add(astStatement.compile(compiler));
				}
				orbit.setLoopCondition(astOrbit.getLoop().getExpression().compile(compiler));
				orbit.setLoopBegin(astOrbit.getLoop().getBegin());
				orbit.setLoopEnd(astOrbit.getLoop().getEnd());
			}
			if (astOrbit.getEnd() != null) {
				for (ASTStatement astStatement : astOrbit.getEnd().getStatements()) {
					endStatements.add(astStatement.compile(compiler));
				}
			}
			if (astOrbit.getTraps() != null) {
				for (ASTOrbitTrap astTrap : astOrbit.getTraps()) {
					traps.add(astTrap.compile(compiler));
				}
			}
			orbit.setBeginStatements(beginStatements);
			orbit.setLoopStatements(loopStatements);
			orbit.setEndStatements(endStatements);
			orbit.setTraps(traps);
			return new InterpreterOrbit(orbit, context);
		} catch (ASTException e) {
			ParserError.ErrorType type = ParserError.ErrorType.SCRIPT_COMPILER;
			long line = e.getLocation().getLine();
			long charPositionInLine = e.getLocation().getCharPositionInLine();
			long index = e.getLocation().getStartIndex();
			long length = e.getLocation().getStopIndex() - e.getLocation().getStartIndex();
			String message = e.getMessage();
			errors.add(new ParserError(type, line, charPositionInLine, index, length, message));
			throw new ParserException("Can't build orbit", errors);
		} catch (Exception e) {
			ParserError.ErrorType type = ParserError.ErrorType.SCRIPT_COMPILER;
			String message = e.getMessage();
			errors.add(new ParserError(type, 0, 0, 0, 0, message));
			throw new ParserException("Can't build orbit", errors);
		}
	}

	public List<ParserError> getErrors() {
		return errors;
	}
}
