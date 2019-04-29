/*
 * NextFractal 2.1.1
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2019 Andrea Medeghini
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
package com.nextbreakpoint.nextfractal.contextfree.grammar.ast;

import com.nextbreakpoint.nextfractal.contextfree.core.Rand64;
import com.nextbreakpoint.nextfractal.contextfree.grammar.CFDGDriver;
import com.nextbreakpoint.nextfractal.contextfree.grammar.CFDGRenderer;
import com.nextbreakpoint.nextfractal.contextfree.grammar.Modification;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.ModClass;
import com.nextbreakpoint.nextfractal.contextfree.grammar.enums.ModType;
import com.nextbreakpoint.nextfractal.contextfree.grammar.exceptions.DeferUntilRuntimeException;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ASTModification extends ASTExpression {
	private ModClass modClass;
	private Modification modData = new Modification();
	private List<ASTModTerm> modExp = new ArrayList<ASTModTerm>();
	private int entropyIndex;
	private boolean canonical;
	private CFDGDriver driver;

	private static HashMap<ModType, ModClass> evalMap = new HashMap<>();
	static {
		evalMap.put(ModType.unknown, ModClass.fromType(ModClass.NotAClass.getType()));
		evalMap.put(ModType.x, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
		evalMap.put(ModType.y, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
		evalMap.put(ModType.z, ModClass.ZClass);
		evalMap.put(ModType.xyz, ModClass.fromType(ModClass.NotAClass.getType()));
		evalMap.put(ModType.transform, ModClass.GeomClass);
		evalMap.put(ModType.size, ModClass.GeomClass);
		evalMap.put(ModType.sizexyz, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.ZClass.getType()));
		evalMap.put(ModType.rotate, ModClass.fromType(ModClass.GeomClass.getType() | ModClass.PathOpClass.getType()));
		evalMap.put(ModType.skew, ModClass.GeomClass);
		evalMap.put(ModType.flip, ModClass.GeomClass);
		evalMap.put(ModType.zsize, ModClass.ZClass);
		evalMap.put(ModType.hue, ModClass.HueClass);
		evalMap.put(ModType.sat, ModClass.SatClass);
		evalMap.put(ModType.bright, ModClass.BrightClass);
		evalMap.put(ModType.alpha, ModClass.AlphaClass);
		evalMap.put(ModType.hueTarg, ModClass.HueClass);
		evalMap.put(ModType.satTarg, ModClass.SatClass);
		evalMap.put(ModType.brightTarg, ModClass.BrightClass);
		evalMap.put(ModType.alphaTarg, ModClass.AlphaClass);
		evalMap.put(ModType.targHue, ModClass.HueTargetClass);
		evalMap.put(ModType.targSat, ModClass.SatTargetClass);
		evalMap.put(ModType.targBright, ModClass.BrightTargetClass);
		evalMap.put(ModType.targAlpha, ModClass.AlphaTargetClass);
		evalMap.put(ModType.time, ModClass.TimeClass);
		evalMap.put(ModType.timescale, ModClass.TimeClass);
		evalMap.put(ModType.param, ModClass.ParamClass);
		evalMap.put(ModType.x1, ModClass.PathOpClass);
		evalMap.put(ModType.y1, ModClass.PathOpClass);
		evalMap.put(ModType.x2, ModClass.PathOpClass);
		evalMap.put(ModType.y2, ModClass.PathOpClass);
		evalMap.put(ModType.xrad, ModClass.PathOpClass);
		evalMap.put(ModType.yrad, ModClass.PathOpClass);
		evalMap.put(ModType.modification, ModClass.InvalidClass);
	}

	public ASTModification(CFDGDriver driver, Token location) {
		super(driver, true, false, ExpType.ModType, location);
		this.driver = driver;
		this.modClass = ModClass.NotAClass;
		this.entropyIndex = 0;
		this.canonical = true;
	}
	
	public ASTModification(CFDGDriver driver, ASTModification mod, Token location) {
		super(driver, true, false, ExpType.ModType, location);
		this.driver = driver;
		if (mod != null) {
			modData.setRand64Seed(new Rand64());
			grab(mod);
		} else {
			this.modClass = ModClass.NotAClass;
		}
	}

	public ASTModification(ASTModification mod) {
		super(mod.driver, true, false, ExpType.ModType, mod.getLocation());
		this.driver = mod.driver;
		this.modClass = mod.modClass;
		this.entropyIndex = mod.entropyIndex;
		this.canonical = mod.canonical;
	}

	public ModClass getModClass() {
		return modClass;
	}

	public Modification getModData() {
		return modData;
	}

	public List<ASTModTerm> getModExp() {
		return modExp;
	}

	public boolean isCanonial() {
		return canonical;
	}

	public void setCanonical(boolean canonical) {
		this.canonical = canonical;
	}

	public int getEntropyIndex() {
		return entropyIndex;
	}
	
	public void setEntropyIndex(int entropyIndex) {
		this.entropyIndex = entropyIndex;
	}

	public void grab(ASTModification mod) {
		Rand64 oldEntropy = modData.getRand64Seed();
		modData = mod.getModData();
		modData.getRand64Seed().add(oldEntropy);
		modExp.clear();
		modExp.addAll(mod.getModExp());
		modClass = mod.getModClass();
		entropyIndex = (entropyIndex + mod.getEntropyIndex()) & 7;
		isConstant = modExp.isEmpty();
		canonical = mod.isCanonial();
	}

	public void makeCanonical() {
	    // Receive a vector of modification terms and return an ASTexpression with
	    // those terms rearranged into TRSSF canonical order. Duplicate terms are
	    // deleted with a warning.
		List<ASTModTerm> temp = new ArrayList<>(modExp);
		modExp.clear();
		
		ASTModTerm x = null;
		ASTModTerm y = null;
		ASTModTerm z = null;
		ASTModTerm rotate = null;
		ASTModTerm skew = null;
		ASTModTerm size = null;
		ASTModTerm zsize = null;
		ASTModTerm flip = null;
		ASTModTerm xform = null;
		
		for (ASTModTerm term : temp) {
			switch (term.getModType()) {
				case x:
					x = term;
					break;
	
				case y:
					y = term;
					break;
	
				case z:
					z = term;
					break;
	
				case modification:
				case transform:
					xform = term;
					break;
	
				case rotate:
					rotate = term;
					break;
	
				case size:
					size = term;
					break;
	
				case zsize:
					zsize = term;
					break;
	
				case skew:
					skew = term;
					break;
	
				case flip:
					flip = term;
					break;
	
				default:
					modExp.add(term);
					break;
			}
		}
		
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
	public int evaluate(double[] result, int length, CFDGRenderer renderer) {
		driver.error("Improper evaluation of an adjustment expression", location);
		return -1;
	}

	@Override
	public void evaluate(Modification result, boolean shapeDest, CFDGRenderer renderer) {
		if (shapeDest) {
			result.concat(modData);
		} else {
			if (result.merge(modData)) {
				if (renderer != null) renderer.colorConflict(getLocation());
			}
		}
		for (ASTModTerm term : modExp) {
			term.evaluate(result, shapeDest, renderer);
		}
	}

	public void setVal(Modification[] mod, CFDGRenderer renderer) {
		mod[0] = modData;
		for (ASTModTerm term : modExp) {
			term.evaluate(modData, false, renderer);
		}
	}

	@Override
	public ASTExpression simplify() {
		evalConst();
		return this;
	}

	@Override
	public ASTExpression compile(CompilePhase ph) {
		for (ASTModTerm term : modExp) {
			term.compile(ph);
		}
		
		switch (ph) {
			case TypeCheck: {
				List<ASTModTerm> temp = new ArrayList<>(modExp);
				modExp.clear();

				for (int i = 0; i < temp.size(); i++) {
					ASTModTerm term = temp.get(i);
					if (term.getArguments() == null || term.getArguments().getType() != ExpType.NumericType) {
						modExp.add(term);
					}
					switch (term.getModType()) {
						case x:
						case y: {
							if (i >= temp.size() - 1) {
								break;
							}
							ASTModTerm next = temp.get(i + 1);
							int argcount = term.getArguments().evaluate(null, 0);
							if (term.getModType() == ModType.x && next.getModType() == ModType.y && argcount == 1) {
								term.setArguments(term.getArguments().append(next.getArguments()));
								term.setArgumentsCount(2);
								modExp.add(term);
								i += 1;
								continue;
							}
							break;
						}

						// Try to split the XYZ term into an XY term and a Z term. Drop the XY term
						// if it is the identity. First try an all-constant route, then try to tease
						// apart the arguments.
						case xyz:
						case sizexyz: {
							double[] d = new double[3];
							if (term.getArguments().isConstant() && term.getArguments().evaluate(d, 3) == 3) {
								term.setArguments(new ASTCons(driver, location, new ASTReal(driver, d[0], location), new ASTReal(driver, d[1], location)));
								term.setModType(term.getModType() == ModType.xyz ? ModType.x : ModType.size);
								term.setArgumentsCount(2);

								ModType ztype = term.getModType() == ModType.size ? ModType.zsize : ModType.z;
								ASTModTerm zmod = new ASTModTerm(driver, ztype, new ASTReal(driver, d[2], location), location);
								zmod.setArgumentsCount(1);

								// Check if xy part is the identity transform and only save it if it is not
								if (d[0] == 1.0 && d[1] == 1.0 && term.getModType() == ModType.size) {
									// Drop xy term and just save z term if xy term
									// is the identity transform
									term.setArguments(zmod);
								} else {
									modExp.add(zmod);
								}
								modExp.add(term);
								continue;
							}

							List<ASTExpression> xyzargs = AST.extract(term.getArguments());
							ASTExpression xyargs = null;
							ASTExpression zargs = null;

							for (ASTExpression arg : xyzargs) {
								if (xyargs == null || xyargs.evaluate(null, 0) < 2) {
									xyargs = append(xyargs, arg);
								} else {
									zargs = append(zargs, arg);
								}
							}

							if (xyargs != null && zargs != null && xyargs.evaluate(null, 0) == 2) {
								// We have successfully split the 3-tuple into a 2-tuple and a scalar
								term.setArguments(xyargs);
								term.setModType(term.getModType() == ModType.xyz ? ModType.x : ModType.size);
								term.setArgumentsCount(2);

								ModType ztype = term.getModType() == ModType.size ? ModType.zsize : ModType.z;
								ASTModTerm zmod = new ASTModTerm(driver, ztype, new ASTReal(driver, d[2], location), location);
								zmod.setArgumentsCount(1);

								if (term.getModType() == ModType.size && xyargs.isConstant() && xyargs.evaluate(d, 2) == 2 && d[0] == 1.0 && d[1] == 1.0) {
									// Drop xy term and just save z term if xy term
									// is the identity transform
									term.setArguments(zmod);
								} else {
									modExp.add(zmod);
								}
							} else {
								// No dice, put it all back
								xyargs = append(xyargs, zargs);
								term.setArguments(xyargs);
							}
							modExp.add(term);
							continue;
						}

						default:
							break;
					}
					modExp.add(term);
				}

				isConstant = true;
				locality = Locality.PureLocal;
				for (ASTModTerm term : modExp) {
					isConstant = isConstant && term.isConstant();
					locality = AST.combineLocality(locality, term.getLocality());
					StringBuilder ent = new StringBuilder();
					term.entropy(ent);
					addEntropy(ent.toString());
				}

				if (canonical) {
					makeCanonical();
				}
				break;
			}

			case Simplify: 
				break;

			default:
				break;
		}
		return null;
	}

	protected void evalConst() {
		int nonConstant = 0;

		List<ASTModTerm> temp = new ArrayList<ASTModTerm>(modExp);
		modExp.clear();

		for (ASTModTerm term : temp) {
			ModClass mc = evalMap.get(term.getModType());
			modClass = ModClass.fromType(modClass.getType() | mc.getType());
			if (!term.isConstant()) {
				nonConstant |= mc.getType();
			}
			boolean keepThisOne = (mc.getType() & nonConstant) != 0;
			if (driver.isInPathContainer() && (mc.getType() & ModClass.ZClass.getType()) != 0) {
				driver.error("Z changes are not supported within paths", term.getLocation());
			}
			if (driver.isInPathContainer() && (mc.getType() & ModClass.TimeClass.getType()) != 0) {
				driver.error("Time changes are not supported within paths", term.getLocation());
			}
			try {
				if (!keepThisOne) {
					term.evaluate(modData, false, null);
				}
			} catch (DeferUntilRuntimeException e) {
				keepThisOne = true;
			}
			if (keepThisOne) {
				if (term.getArguments() != null) {
					term.setArguments(term.getArguments().simplify());
				}
				modExp.add(term);
			}
		}
	}

	public void addEntropy(String name) {
		int[] index = new int[1];
		modData.getRand64Seed().xorString(name, index);
		entropyIndex = index[0];
	}
}
