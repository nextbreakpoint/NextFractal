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

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGBuilder;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGDeferUntilRuntimeException;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFDGSystem;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Modification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModClass;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// astexpression.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2011-2014 John Horigan - john@glyphic.com
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

@Getter
public class ASTModification extends ASTExpression {
	private Modification modData = new Modification();
	private ModClass modClass;
	@Setter
	private List<ASTModTerm> modExp = new ArrayList<>();
	@Setter
    private int entropyIndex;
	@Setter
    private boolean canonical;

	public ASTModification(CFDGSystem system, ASTWhere where) {
		super(system, where, true, false, ExpType.Mod);
		this.modClass = ModClass.NotAClass;
		this.entropyIndex = 0;
		this.canonical = true;
	}
	
	public ASTModification(CFDGSystem system, ASTWhere where, ASTModification mod) {
		super(system, where, true, false, ExpType.Mod);
		if (mod != null) {
			modData.getRand64Seed().setSeed(0);
			grab(mod);
		} else {
			this.modClass = ModClass.NotAClass;
		}
	}

	public ASTModification(CFDGSystem system, ASTModification mod) {
		super(system, mod.getWhere(), true, false, ExpType.Mod);
		this.modData = mod.modData;
		this.modClass = mod.modClass;
		this.entropyIndex = mod.entropyIndex;
		this.canonical = mod.canonical;
	}

    public void grab(ASTModification mod) {
		final Rand64 oldEntropy = modData.getRand64Seed();
		modData = mod.getModData();
		modData.getRand64Seed().add(oldEntropy);
		final List<ASTModTerm> tempTerms = modExp;
		modExp = mod.getModExp();
		mod.setModExp(tempTerms);
		modClass = mod.getModClass();
		entropyIndex = (entropyIndex + mod.getEntropyIndex()) & 7;
		constant = modExp.isEmpty();
		canonical = mod.isCanonical();
	}

	public void makeCanonical() {
	    // Receive a vector of modification terms and return an ASTexpression with
	    // those terms rearranged into TRSSF canonical order. Duplicate terms are
	    // deleted with a warning.
		final List<ASTModTerm> tempTerms = new ArrayList<>(modExp);
		modExp.clear();

		// no need for try/catch block to clean up temp array
		ASTModTerm x = null;
		ASTModTerm y = null;
		ASTModTerm z = null;
		ASTModTerm rotate = null;
		ASTModTerm skew = null;
		ASTModTerm size = null;
		ASTModTerm zsize = null;
		ASTModTerm flip = null;
		ASTModTerm xform = null;
		
		for (ASTModTerm term : tempTerms) {
            switch (term.getModType()) {
                case x -> x = term;
                case y -> y = term;
                case z -> z = term;
                case modification, transform -> xform = term;
                case rotate -> rotate = term;
                case size -> size = term;
                case zsize -> zsize = term;
                case skew -> skew = term;
                case flip -> flip = term;
                default -> modExp.add(term);
            }
		}

		tempTerms.clear();
		
		if (x != null) modExp.add(x); 
		if (y != null) modExp.add(y); 
		if (z != null) modExp.add(z); 
		if (rotate != null) modExp.add(rotate);
		if (size != null) modExp.add(size); 
		if (zsize != null) modExp.add(zsize); 
		if (skew != null) modExp.add(skew); 
		if (flip != null) modExp.add(flip); 
		if (xform != null) modExp.add(xform); 
	}

	@Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		system.error("Improper evaluation of an adjustment expression", getWhere());
		return -1;
	}

	@Override
	public void evaluate(CFDGBuilder builder, CFDGRenderer renderer, Modification mod, boolean shapeDest) {
		if (shapeDest) {
			mod.concat(modData);
		} else {
			if (mod.merge(modData)) {
				if (renderer != null) renderer.colorConflict(getWhere());
			}
		}
		for (ASTModTerm term : modExp) {
			term.evaluate(builder, renderer, mod, shapeDest);
		}
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		int nonConstant = 0;

		final List<ASTModTerm> tempTerms = new ArrayList<>(modExp);
		modExp.clear();

		for (ASTModTerm term : tempTerms) {
			if (term == null) {
				system.error("Unknown term in shape adjustment", getWhere());
				continue;
			}

			term.setArguments(ASTExpression.simplify(builder, term.getArguments()));
			// Put in code for separating color changes and target color changes

			// Drop identity transforms here, not in type-check
			final double[] value = new double[2];
			if (term.isConstant() && term.getModType() == ModType.size &&
					term.getArguments().evaluate(builder, value, 2) == 2 && value[0] == 1.0 && value[1] == 1.0)
				continue;

			ModClass mc = ModClass.byModType(term.getModType());

			final ASTModification modmod = term.getArguments() instanceof ASTModification mod ? mod : null;
			if (term.getModType() == ModType.modification && modmod != null) {
				mc = modmod.modClass;
			}

			modClass = ModClass.fromType(modClass.getType() | mc.getType());

			if (!term.isConstant()) {
				nonConstant |= mc.getType();
			}

			boolean keepThisOne = (mc.getType() & nonConstant) != 0;

			if (builder.isInPathContainer() && (mc.getType() & ModClass.ZClass.getType()) != 0) {
				system.error("Z changes are not supported within paths", term.getWhere());
			}
			if (builder.isInPathContainer() && (mc.getType() & ModClass.TimeClass.getType()) != 0) {
				system.error("Time changes are not supported within paths", term.getWhere());
			}

			try {
				if (!keepThisOne) {
					term.evaluate(builder, null, modData, false);
				}
			} catch (CFDGDeferUntilRuntimeException e) {
				keepThisOne = true;
			}

			try {
				if (!keepThisOne) {
					if (modmod != null) { // merge in mod data
						if (modData.merge(modmod.getModData())) {
							keepThisOne = true;  // unless color conflict
						} else {
							term.evaluate(builder, null, modData, false);
						}
					}
				}
			} catch (CFDGDeferUntilRuntimeException e) {
				keepThisOne = true;
			}

			if (keepThisOne) {
				term.setArguments(ASTExpression.simplify(builder, term.getArguments()));
				modExp.add(term);
			}
		}
		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		for (ASTModTerm term : modExp) {
			term.compile(builder, phase);
		}

        switch (phase) {
            case TypeCheck -> {
                final List<ASTModTerm> tempTerms = new ArrayList<>(modExp);
                modExp.clear();

                for (int i = 0; i < tempTerms.size(); i++) {
					final ASTModTerm term = tempTerms.get(i);
					if (term == null) {
						continue;  // skip deleted terms
					}
                    if (term.getArguments() == null || term.getArguments().getType() != ExpType.Numeric) {
                        modExp.add(term);
						continue;
                    }
					final int argCount = term.getArguments().evaluate(builder, null, 0);
					switch (term.getModType()) {
						// Try to merge consecutive x and y adjustments
                        case x:
                        case y: {
                            if (i >= tempTerms.size() - 1) {
                                break;
                            }
							final ASTModTerm next = tempTerms.get(i + 1);
                            if (term.getModType() == ModType.x && next.getModType() == ModType.y && argCount == 1) {
                                term.getArguments().append(next.getArguments());
								term.setConstant(term.getArguments().isConstant());
								term.setNatural(term.getArguments().isNatural());
								term.setLocality(term.getArguments().getLocality());
								term.setWhere(term.getArguments().getWhere());
								tempTerms.set(i, null);
                                break;
                            }
                            break;
                        }
                        // Try to split the XYZ term into an XY term and a Z term. Drop the XY term
                        // if it is the identity. First try an all-constant route, then try to tease
                        // apart the arguments.
                        case xyz:
                        case sizexyz: {
							final List<ASTExpression> xyzArgs = AST.extract(term.getArguments());
							ASTExpression xyArgs = null;
							ASTExpression zArgs = null;
							for (ASTExpression arg : xyzArgs) {
								if (xyArgs == null || xyArgs.evaluate(builder, null, 0) < 2) {
									xyArgs = append(xyArgs, arg);
								} else {
									zArgs = append(zArgs, arg);
								}
							}
							if (xyArgs != null && zArgs != null && xyArgs.evaluate(builder, null, 0) == 2) {
								// We have successfully split the 3-tuple into a 2-tuple and a scalar
								term.setArguments(xyArgs);
								term.setModType(term.getModType() == ModType.xyz ? ModType.x : ModType.size);
								term.setConstant(term.getArguments().isConstant());
								term.setNatural(term.getArguments().isNatural());
								term.setLocality(term.getArguments().getLocality());
								term.setArgCountOrFlags(2);

								final ModType zType = term.getModType() == ModType.size ? ModType.zsize : ModType.z;
								final ASTModTerm zMod = new ASTModTerm(system, term.getWhere(), zType, zArgs);
								zMod.setNatural(zArgs.isNatural());
								zMod.setLocality(zArgs.getLocality());
								zMod.setArgCountOrFlags(1);

								modExp.add(zMod);
							} else {
								// No dice, put it all back
								xyArgs = append(xyArgs, zArgs);
								term.setArguments(xyArgs);
							}
                        }
                        default:
                            break;
                    }
                    modExp.add(term);
                }

                constant = true;
                locality = Locality.PureLocal;
                for (ASTModTerm term : modExp) {
                    constant = constant && term.isConstant();
                    locality = AST.combineLocality(locality, term.getLocality());
                    StringBuilder entropy = new StringBuilder();
                    term.entropy(entropy);
                    addEntropy(entropy.toString());
                }

                if (canonical) {
                    makeCanonical();
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

	public void setVal(CFDGBuilder builder, CFDGRenderer renderer, Modification[] mod) {
		mod[0] = (Modification) modData.clone();
		for (ASTModTerm term : modExp) {
			term.evaluate(builder, renderer, mod[0], false);
		}
	}

	public void addEntropy(String name) {
		final int[] index = new int[1];
		modData.getRand64Seed().xorString(name, index);
		entropyIndex = index[0];
	}
}
