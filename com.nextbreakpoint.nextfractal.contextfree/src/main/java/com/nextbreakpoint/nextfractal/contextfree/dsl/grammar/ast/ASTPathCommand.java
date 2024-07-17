/*
 * NextFractal 2.3.0
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
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.CommandInfo;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.RepElemType;
import org.antlr.v4.runtime.Token;

import java.util.List;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_CAP_MASK;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_CAP_PRESENT;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_FILL;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_JOIN_MASK;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.grammar.enums.FlagType.CF_JOIN_PRESENT;

public class ASTPathCommand extends ASTReplacement {
	private double miterLimit;
	private double strokeWidth;
	private ASTExpression parameters;
	private CommandInfo commandInfo;
	private long flags;

	public ASTPathCommand(CFDGDriver driver, Token location) {
		super(driver, null, RepElemType.command, location);
		this.miterLimit = 1.0;
		this.strokeWidth = 0.1;
		this.parameters = null;
		this.commandInfo = new CommandInfo();
		this.flags = FlagType.CF_ROUND_JOIN.getMask() + FlagType.CF_BUTT_CAP.getMask() + CF_FILL.getMask();
	}

	public ASTPathCommand(CFDGDriver driver, String s, ASTModification mods, ASTExpression params, Token location) {
		super(driver, mods, RepElemType.command, location);
		this.miterLimit = 1.0;
		this.strokeWidth = 0.1;
		this.parameters = params;
		this.commandInfo = new CommandInfo();
		this.flags = FlagType.CF_ROUND_JOIN.getMask() + FlagType.CF_BUTT_CAP.getMask();
		if (s.equals("FILL")) {
			this.flags |= CF_FILL.getMask();
		} else if (!s.equals("STROKE")) {
			driver.error("Unknown path command/operation", location);
		}
	}

	public double getMiterLimit() {
		return miterLimit;
	}

	public double getStrokeWidth() {
		return strokeWidth;
	}

	public ASTExpression getParameters() {
		return parameters;
	}

	public long getFlags() {
		return flags;
	}

	@Override
	public void traverse(Shape parent, boolean tr, CFDGRenderer renderer) {
		if (renderer.isOpsOnly()) {
			driver.error("Path commands not allowed at this point", location);
		}

		Shape child = new Shape(parent);
		double width = strokeWidth;
		replace(child, renderer);

		double[] value = new double[] { width };
		if (parameters != null && parameters.evaluate(value, 1, renderer) != 1) {
			driver.error("Error computing stroke width", location);
		}
		width = value[0];

		CommandInfo info = null;

		if (renderer.getCurrentPath().isCached()) {
			if (renderer.getCurrentCommand().current() == null) {
				driver.error("Not enough path commands in cache", location);
			}
			info = renderer.getCurrentCommand().current();
			renderer.getCurrentCommand().next();
		} else {
			if (renderer.getCurrentPath().getPathStorage().getTotalVertices() == 0) {
				driver.error("Path commands must be preceeded by at least one path operation", location);
			}

			renderer.setWantCommand(false);
			renderer.getCurrentPath().finish(false, renderer);

			//TODO controllare

			commandInfo.tryInit(renderer.getIndex(), renderer.getCurrentPath(), width, this);

			if (commandInfo.getPathUID() == renderer.getCurrentPath().getPathUID() && commandInfo.getIndex() == renderer.getIndex()) {
				renderer.getCurrentPath().getCommandInfo().pushBack(commandInfo);
			} else {
				renderer.getCurrentPath().getCommandInfo().pushBack(new CommandInfo(renderer.getIndex(), renderer.getCurrentPath(), width, this));
			}

			info = renderer.getCurrentPath().getCommandInfo().back();
		}

		renderer.processPathCommand(child, info);


	}

	@Override
	public void compile(CompilePhase ph) {
		super.compile(ph);
		parameters = compile(parameters, ph);

		switch (ph) {
			case TypeCheck: {
				getChildChange().addEntropy((flags & CF_FILL.getMask()) != 0 ? "FILL" : "STROKE");
				long[] flagValue = new long[] { flags };
				ASTExpression w = AST.getFlagsAndStroke(driver, getChildChange().getModExp(), flagValue);
				flags = flagValue[0];
				if (w != null) {
					if (parameters != null) {
						driver.error("Can't have a stroke adjustment in a v3 path command", w.getLocation());
					} else if (w.size() != 1 || w.getType() != ExpType.NumericType || w.evaluate(null, 0) != 1) {
						driver.error("Stroke adjustment is ill-formed", w.getLocation());
					}
					parameters = w;
					w = null;
				}

				if (parameters == null) {
					return;
				}

				ASTExpression stroke = null;
				ASTExpression flags = null;
				List<ASTExpression> pcmdParams = AST.extract(parameters);
				parameters = null;
				switch (pcmdParams.size()) {
					case 2: {
						stroke = pcmdParams.get(0);
						flags = pcmdParams.get(1);
						break;
					}
					case 1: {
						switch (pcmdParams.get(0).getType()) {
							case NumericType:
								stroke = pcmdParams.get(0);
								break;
							case FlagType:
								flags = pcmdParams.get(0);
								break;
							default:
								driver.error("Bad expression type in path command parameters", location);
								break;
						}
						break;
					}
					case 0: {
						return;
					}
					default: {
						driver.error("Path commands can have zero, one, or two parameters", location);
						return;
					}
				}

				if (stroke != null) {
					if ((this.flags & CF_FILL.getMask()) != 0) {
						driver.warning("Stroke width only useful for STROKE commands", stroke.getLocation());
					}
					double[] value = new double[1];
					value[0] = strokeWidth;
					if (stroke.getType() != ExpType.NumericType || stroke.evaluate(null, 0) != 1) {
						driver.error("Stroke width expression must be numeric scalar", stroke.getLocation());
					} else if (!stroke.isConstant() || stroke.evaluate(value, 1) != 1) {
						parameters = stroke;
					}
					strokeWidth = value[0];
				}

				if (flags != null) {
					if (flags.getType() != ExpType.FlagType) {
						driver.error("Unexpected argument in path command", flags.getLocation());
						return;
					}
					flags = simplify(flags);

					if (flags instanceof ASTReal) {
						int f = (int)((ASTReal)flags).getValue();
						if ((f & CF_JOIN_PRESENT.getMask()) != 0) {
							this.flags &= ~CF_JOIN_MASK.getMask();
						}
						if ((f & CF_CAP_PRESENT.getMask()) != 0) {
							this.flags &= ~CF_CAP_MASK.getMask();
						}
						this.flags |= f;
						if ((this.flags & CF_FILL.getMask()) != 0 && (this.flags & (CF_JOIN_PRESENT.getMask() | CF_CAP_PRESENT.getMask())) != 0) {
							driver.warning("Stroke flags only useful for STROKE commands", flags.getLocation());
						}
					} else {
						driver.error("Flag expressions must be constant", flags.getLocation());
					}
				}

				break;
			}

			case Simplify: {
				parameters = simplify(parameters);
				break;
			}

			default:
				break;
		}
	}
}
