/*
 * NextFractal 2.4.0
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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CommandInfo;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Shape;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_CAP_MASK;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_CAP_PRESENT;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_FILL;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_JOIN_MASK;
import static com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType.CF_JOIN_PRESENT;

// astreplacement.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2013 John Horigan - john@glyphic.com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
// John Horigan can be contacted at john@glyphic.com or at
// John Horigan, 1209 Villa St., Mountain View, CA 94041-1123, USA

public class ASTPathCommand extends ASTReplacement {
    public static final double DEFAULT_MITER_LIMIT = 1.0;
    public static final double DEFAULT_STROKE_WIDTH = 0.1;

    private final CommandInfo commandInfo;
	@Getter
    private final double miterLimit;
	@Getter
    private ASTExpression parameters;
	@Getter
    private double strokeWidth;
	@Getter
    private long flags;

	public ASTPathCommand(CFDGSystem system, ASTWhere where) {
		super(system, where, null, RepElemType.empty);
		this.miterLimit = DEFAULT_MITER_LIMIT;
		this.strokeWidth = DEFAULT_STROKE_WIDTH;
		this.parameters = null;
		this.commandInfo = new CommandInfo();
		this.flags = FlagType.CF_ROUND_JOIN.getMask() + FlagType.CF_BUTT_CAP.getMask() + CF_FILL.getMask();
	}

	public ASTPathCommand(CFDGSystem system, ASTWhere where, String operation, ASTModification mods, ASTExpression params) {
		super(system, where, mods, RepElemType.command);
		this.miterLimit = DEFAULT_MITER_LIMIT;
		this.strokeWidth = DEFAULT_STROKE_WIDTH;
		this.parameters = params;
		this.commandInfo = new CommandInfo();
		this.flags = FlagType.CF_ROUND_JOIN.getMask() + FlagType.CF_BUTT_CAP.getMask();
		if (operation.equals("FILL")) {
			this.flags |= CF_FILL.getMask();
		} else if (!operation.equals("STROKE")) {
            system.error("Unknown path command/operation", where);
		}
	}

    @Override
	public void traverse(CFDGBuilder builder, CFDGRenderer renderer, Shape parent, boolean tr) {
		if (renderer.isOpsOnly()) {
			system.error("Path commands not allowed at this point", getWhere());
		}

		final Shape child = new Shape(parent);
		double width = strokeWidth;

		replace(builder, renderer, child);

        final double[] value = new double[] { width };
		if (parameters != null && parameters.evaluate(builder, renderer, value, 1) != 1) {
			system.error("Error computing stroke width", getWhere());
		}
		width = value[0];

		CommandInfo info;

		if (renderer.getCurrentPath().isCached()) {
			if (!renderer.getCurrentCommand().hasNext()) {
				system.error("Not enough path commands in cache", getWhere());
			}
			info = renderer.getCurrentCommand().current();
			renderer.getCurrentCommand().next();
		} else {
			if (renderer.getCurrentPath().getPath().getTotalVertices() == 0) {
				system.error("Path commands must be preceded by at least one path operation", getWhere());
			}

			renderer.setWantCommand(false);
			renderer.getCurrentPath().finish(false, renderer);

            // Auto-align the previous set of paths ops unless the previous path
            // command already auto-aligned them
            if (renderer.getCurrentPath().getCommandInfo().isEmpty() || renderer.getCurrentPath().getCommandInfo().back().getIndex() != renderer.getIndex()) {
                for (int i = renderer.getIndex(); i < renderer.getCurrentPath().getPath().getTotalVertices(); i = renderer.getCurrentPath().getPath().alignPath(i)) {}
            }

			commandInfo.tryInit(renderer.getIndex(), renderer.getCurrentPath(), width, this);

			if (Objects.equals(commandInfo.getPathUID().get(), renderer.getCurrentPath().getPathUID()) && commandInfo.getIndex() == renderer.getIndex()) {
				renderer.getCurrentPath().getCommandInfo().pushBack(commandInfo);
			} else {
                final CommandInfo newInfo = new CommandInfo(renderer.getIndex(), renderer.getCurrentPath(), width, this);
                renderer.getCurrentPath().getCommandInfo().pushBack(newInfo);
			}
			info = renderer.getCurrentPath().getCommandInfo().back();
            info.setFlags(info.getFlags() | child.getWorldState().getBlendMode());
        }

		renderer.processPathCommand(child, info);
	}

	@Override
	public void compile(CFDGBuilder builder, CompilePhase phase) {
		super.compile(builder, phase);
		parameters = ASTExpression.compile(builder, phase, parameters);

        switch (phase) {
            case TypeCheck -> {
                getChildChange().addEntropy((flags & CF_FILL.getMask()) != 0 ? "FILL" : "STROKE");
                final long[] flagValue = new long[]{flags};
                final ASTExpression w = AST.getFlagsAndStroke(system, getChildChange().getModExp(), flagValue);
                flags = flagValue[0];
                if (w != null) {
                    if (parameters != null) {
                        system.error("Can't have a stroke adjustment in a v3 path command", w.getWhere());
                    } else if (w.size() != 1 || w.getType() != ExpType.Numeric || w.evaluate(builder, null, 0) != 1) {
                        system.error("Stroke adjustment is ill-formed", w.getWhere());
                    }
                    parameters = w;
                }

                if (parameters == null) {
                    return;
                }

                ASTExpression stroke = null;
                ASTExpression flags = null;
                final List<ASTExpression> cmdParams = AST.extract(parameters);
                switch (cmdParams.size()) {
                    case 2: {
                        stroke = cmdParams.get(0);
                        flags = cmdParams.get(1);
                        break;
                    }
                    case 1: {
                        switch (cmdParams.getFirst().getType()) {
                            case Numeric:
                                stroke = cmdParams.getFirst();
                                break;
                            case Flag:
                                flags = cmdParams.getFirst();
                                break;
                            default:
                                system.error("Bad expression type in path command parameters", getWhere());
                        }
                        break;
                    }
                    case 0: {
                        return;
                    }
                    default: {
                        system.error("Path commands can have zero, one, or two parameters", getWhere());
                    }
                }

                if (stroke != null) {
                    if ((this.flags & CF_FILL.getMask()) != 0) {
                        builder.warning("Stroke width only useful for STROKE commands", stroke.getWhere());
                    }
                    if (stroke.getType() != ExpType.Numeric || stroke.evaluate(builder, null, 0) != 1) {
                        system.error("Stroke width expression must be numeric scalar", stroke.getWhere());
                    }
                    double[] value = new double[] { strokeWidth };
                    if (!stroke.isConstant() || stroke.evaluate(builder, value, 1) != 1) {
                        parameters = stroke;
                    }
                    strokeWidth = value[0];
                }

                if (flags != null) {
                    if (flags.getType() != ExpType.Flag) {
                        system.error("Unexpected argument in path command", flags.getWhere());
                        return;
                    }
                    flags = ASTExpression.simplify(builder, flags);
                    if (flags instanceof ASTReal) {
                        int f = (int) ((ASTReal) flags).getValue();
                        if ((f & CF_JOIN_PRESENT.getMask()) != 0) {
                            this.flags &= ~CF_JOIN_MASK.getMask();
                        }
                        if ((f & CF_CAP_PRESENT.getMask()) != 0) {
                            this.flags &= ~CF_CAP_MASK.getMask();
                        }
                        this.flags |= f;
                        if ((this.flags & CF_FILL.getMask()) != 0 && (this.flags & (CF_JOIN_PRESENT.getMask() | CF_CAP_PRESENT.getMask())) != 0) {
                            builder.warning("Stroke flags only useful for STROKE commands", flags.getWhere());
                        }
                    } else {
                        system.error("Flag expressions must be constant", flags.getWhere());
                    }
                }

            }
            case Simplify -> parameters = ASTExpression.simplify(builder, parameters);
            default -> {
            }
        }
	}
}
