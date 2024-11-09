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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.AST;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTArray;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTCons;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTDefine;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTExpression;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTFunction;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTLet;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModTerm;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTModification;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTParameter;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTPathCommand;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReal;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRepContainer;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTReplacement;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTRuleSpecifier;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTSelect;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTStartSpecifier;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTSwitch;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTUserFunction;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTVariable;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.ASTWhere;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CFG;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.DefineType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FlagType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.FuncType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ModType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Param;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.PathOp;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.RepElemType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ShapeType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.logging.Level;

// builder.h
// this file is part of Context Free
// ---------------------
// Copyright (C) 2005-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2005-2012 John Horigan - john@glyphic.com
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
//
// Mark Lentczner can be contacted at markl@glyphic.com or at
// Mark Lentczner, 1209 Villa St., Mountain View, CA 94041-1123, USA

@Log
public class CFDGBuilder {
	@Getter
	private final ASTRepContainer paramDecls;
	@Getter
    private final Stack<ASTRepContainer> containerStack = new Stack<>();
	@Getter
	private final Stack<ASTSwitch> switchStack = new Stack<>();

	private final Stack<Boolean> includeNamespace = new Stack<>();
	private final Stack<Integer> stackStack = new Stack<>();
	private final Stack<String> filesToLoad = new Stack<>();
	private final Stack<String> fileNames = new Stack<>();

	@Getter
	private double maxNatural;
	@Getter
    private final Rand64 seed;
	@Getter
	private boolean errorOccurred;

	@Setter
    @Getter
    private String maybeVersion;
	@Setter
    @Getter
    private boolean inPathContainer;
	@Setter
	@Getter
	private int localStackDepth;
	@Setter
	@Getter
	private String currentPath;
	@Setter
	@Getter
	private String basePath;
	@Getter
	@Setter
	private boolean impure;

	private int maxNaturalDepth;
	private int paramSize;
	private int currentShape;
	private String currentNameSpace;
	private boolean allowOverlap;
	private int includeDepth;

	@Getter
	private final CFDG cfdg;

	@Getter
	private final CFDGSystem system;

	public CFDGBuilder(CFDG cfdg, int variation) {
		currentNameSpace = "";
		currentShape = -1;
		currentPath = null;
		includeDepth = 0;
		localStackDepth = 0;
		allowOverlap = false;
		errorOccurred = false;
		inPathContainer = false;
		maxNatural = 1000.0;
		maxNaturalDepth = Integer.MAX_VALUE;

		this.cfdg = cfdg;
		this.cfdg.setImpure(false);
		this.cfdg.setBuilder(this);

		system = cfdg.getSystem();

		seed = new Rand64(variation);

		paramDecls = new ASTRepContainer(system, (ASTWhere) null);
		pushRepContainer(this.cfdg.getContents());
	}

	public void info(String message, ASTWhere where) {
		system.info(message, where);
	}

    public void warning(String message, ASTWhere where) {
		system.warning(message, where);
	}

	public void error(String message, ASTWhere where) {
		errorOccurred = true;
		system.error(message, where);
	}

	public int stringToShape(String name, boolean colonsAllowed, ASTWhere where) {
		checkName(name, colonsAllowed, where);
		if (currentNameSpace.isEmpty()) {
			return cfdg.encodeShapeName(name, where);
		}
		final int maybePrimitive = Collections.binarySearch(PrimShape.getShapeNames(), name);
		final String n = currentNameSpace + name;
		if (maybePrimitive >= 0 && cfdg.tryEncodeShapeName(n) == -1) {
			return cfdg.encodeShapeName(name, where);
		} else {
			return cfdg.encodeShapeName(n, where);
		}
	}

	public String shapeToString(int shape) {
		return cfdg.decodeShapeName(shape);
	}

	public String flagToString(int flag) {
		return FlagType.flagToString(flag);
	}

	public void includeFile(String fileName, ASTWhere where) {
		try {
			final File path = new File(relativeFilePath(currentPath, fileName));
			info("Loading rules from file " + path.getAbsolutePath(), where);
			fileNames.push(path.getAbsolutePath());
			currentPath = fileNames.getLast();
			filesToLoad.push(currentPath);
			includeNamespace.push(Boolean.FALSE);
			includeDepth++;
			currentShape = -1;
			setShape(null, where);
		} catch (Exception e) {
			log.log(Level.WARNING, "Couldn't open rules file " + fileName, e);
			error("Couldn't open rules file " + fileName, where);
		}
	}
	
	public boolean endInclude(ASTWhere where) {
		info("Rules loaded from file " + fileNames.peek(), where);
		final boolean endOfInput = includeDepth == 0;
		setShape(null, where);
		includeDepth--;
		if (filesToLoad.isEmpty()) {
			return endOfInput;
		}
		if (includeNamespace.peek()) {
			popNameSpace();
		}
		filesToLoad.pop();
		includeNamespace.pop();
		currentPath = filesToLoad.isEmpty() ? null : filesToLoad.peek();
		return endOfInput;
	}
	
	public void setShape(String name, ASTWhere where) {
		setShape(name, false, where);
	}
	
	public void setShape(String name, boolean isPath, ASTWhere where) {
		if (name == null) {
			currentShape = -1;
			return;
		}
		currentShape = stringToShape(name, false, where);
		final ASTDefine def = cfdg.findFunction(currentShape);
		if (def != null) {
			error("There is a function with the same name as this shape: " + def.getName(), where);
			error("The function " + def.getName() + " already exits", def.getWhere());
			return;
		}
		final String err = cfdg.setShapeParams(currentShape, paramDecls, paramSize, isPath);
		if (err != null) {
			error(err, where);
		}
		localStackDepth -= paramSize;
		paramDecls.getParameters().clear();
		paramSize = 0;
	}
	
	public void addRule(ASTRule rule) {
		final boolean isShapeItem = rule.getNameIndex() == -1;
		if (isShapeItem) {
			rule.setNameIndex(currentShape);
		} else {
			currentShape = -1;
		}
		if (rule.getNameIndex() == -1) {
			error("Shape rules/paths must follow a shape declaration", rule.getWhere());
			return;
		}
		final ShapeType type = cfdg.getShapeType(rule.getNameIndex());
		if ((rule.isPath() && type == ShapeType.RuleType) || (!rule.isPath() && type == ShapeType.PathType)) {
			error("Can't mix rules and shapes with the same name", rule.getWhere());
		}
		final boolean matchesShape = cfdg.addRule(rule);
		if (!isShapeItem && matchesShape) {
			error("Rule/path name matches existing shape name", rule.getWhere());
		}
	}

	public void nextParameterDecl(String type, String name, ASTWhere where) {
		final int nameIndex = stringToShape(name, false, where);
		checkVariableName(nameIndex, true, where);
		paramDecls.addParameter(type, nameIndex, where);
		ASTParameter param = paramDecls.getParameters().getLast();
		param.setStackIndex(localStackDepth);
		paramSize += param.getTupleSize();
		localStackDepth += param.getTupleSize();
	}

	public ASTDefine makeDefinition(CFG cfg, ASTExpression exp, ASTWhere where) {
		if (!containerStack.getLast().isGlobal()) {
			error("Configuration parameters must be at global scope", where);
			return null;
		}
		final ASTDefine def = new ASTDefine(system, where, cfg.getName());
		def.setConfigDepth(includeDepth);
		def.setDefineType(DefineType.Config);
		def.setExp(exp);
		return def;
	}

	public ASTDefine makeDefinition(String name, boolean isFunction, ASTWhere where) {
		if (name.startsWith("CF::")) {
			if (isFunction) {
				error("Configuration parameters cannot be functions", where);
				return null;
			}
			if (!containerStack.lastElement().isGlobal()) {
				error("Configuration parameters must be at global scope", where);
				return null;
			}
			final ASTDefine def = new ASTDefine(system, where, name);
			def.setConfigDepth(includeDepth);
			def.setDefineType(DefineType.Config);
			return def;
		}
		if (FuncType.byName(name) != FuncType.NotAFunction) {
			error("Internal function names are reserved", where);
			return null;
		}
		// Check if a global definition shadows a pre-definition. If it does then
		// drop the global definition.
		if (containerStack.getLast().isGlobal() && includeDepth >= 0) {
			for (ASTReplacement rep : containerStack.getLast().getBody()) {
				if (rep instanceof ASTDefine def) {
					if (def.getName().equals(name) && def.getConfigDepth() == -1) {
						return null;
					}
				}
			}
		}
		final int nameIndex = stringToShape(name, false, where);
		ASTDefine def = cfdg.findFunction(nameIndex);
		if (def != null) {
			error("Definition with same name as user function: " + def.getWhere(), where);
			error("The function " + def.getName() + " already exists", where);
			return null;
		}
		checkVariableName(nameIndex, false, where);
		def = new ASTDefine(system, where, name);
		def.getShapeSpecifier().setShapeType(nameIndex);
		if (isFunction) {
			for (ASTParameter param : paramDecls.getParameters()) {
				param.setLocality(Locality.PureNonLocal);
			}
			def.getParameters().clear();
			def.getParameters().addAll(paramDecls.getParameters());
			def.setParamSize(paramSize);
			def.setDefineType(DefineType.Function);
			localStackDepth -= paramSize;
			paramSize = 0;
			cfdg.declareFunction(nameIndex, def);
		} else {
			// Add parameters during parse even though the type info is unknown. At least
			// we know the name of parameters, and we can use this info to help distinguish
			// function applications from variables followed by an expression
			containerStack.lastElement().addDefParameter(nameIndex, def, where);
		}
		return def;
	}

	public void checkConfig(ASTDefine define) {
		final CFG cfg = CFG.byName(define.getName());
		if (cfg == CFG.AllowOverlap) {
			final double[] v = new double[] { 0.0 };
			if (define.getExp() == null || !define.getExp().isConstant() || define.getExp().getType() != ExpType.Numeric || define.getExp().evaluate(this, v, 1) != 1) {
				final ASTWhere expWhere = define.getExp() != null ? define.getExp().getWhere() : define.getWhere();
				error("CF::AllowOverlap requires a constant numeric expression", expWhere);
			} else {
				allowOverlap = v[0] != 0.0;
			}
		}
	}

	public void makeConfig(ASTDefine define) {
		final CFG cfg = CFG.byName(define.getName());
		if (cfg == CFG.Unknown) {
			warning("Unknown configuration parameter", define.getWhere());
			return;
		}
		final ASTWhere expWhere = define.getExp() != null ? define.getExp().getWhere() : define.getWhere();
		if (cfg == CFG.StartShape && define.getExp() != null && !(define.getExp() instanceof ASTStartSpecifier)) {
			// This code supports setting the startshape with a config statement:
			//    CF::StartShape = foo(foo params), [mods]
			// It converts the ASTruleSpec and optional ASTmod to a single ASTstartSpec
			ASTRuleSpecifier rule = null;
			ASTModification mod = null;
			final List<ASTExpression> specAndMod = extract(define.getExp());
			switch (specAndMod.size()) {
				case 2:
					if (!(specAndMod.get(1) instanceof ASTModification)) {
						error("CF::StartShape second term must be a modification", specAndMod.get(1).getWhere());
						return;
					} else {
						mod = (ASTModification) specAndMod.get(1);
					}
					// fall through
				case 1:
					if (!(specAndMod.getFirst() instanceof ASTRuleSpecifier)) {
						error("CF::StartShape must start with a shape specification", specAndMod.getFirst().getWhere());
						return;
					} else {
						rule = (ASTRuleSpecifier) specAndMod.getFirst();
					}
					break;
				default:
					error("CF::StartShape expression must have the form shape_spec or shape_spec, modification", expWhere);
					break;
			}
			if (mod == null) {
				mod = new ASTModification(system, expWhere);
			}
			define.setExp(new ASTStartSpecifier(system, expWhere, rule, mod));
		}
		cfdg.addParameter(cfg, define.getExp(), define.getConfigDepth());
	}

	public void typeCheckConfig(ASTDefine define) {
		final CFG cfg = CFG.byName(define.getName());
		final ASTWhere expWhere = define.getExp() != null ? define.getExp().getWhere() : define.getWhere();
		if (cfg == CFG.Impure) {
			final double[] v = new double[] { 0.0 };
			if (define.getExp() == null || !define.getExp().isConstant() || define.getExp().evaluate(this, null, v, 1) != 1) {
				error("CF::Impure requires a constant numeric expression", expWhere);
			} else {
				ASTParameter.Impure = v[0] != 0.0;
			}
		}
		if (cfg == CFG.MaxNatural && maxNaturalDepth > define.getConfigDepth()) {
			final ASTExpression max = define.getExp();
			final double[] v = new double[] { -1.0 };
			if (max == null || !max.isConstant() || max.getType() != ExpType.Numeric || max.evaluate(this, v, 1) != 1) {
				error("CF::MaxNatural requires a constant numeric expression", expWhere);
			} else if (v[0] < 1.0 || v[0] > AST.MAX_NATURAL) {
				error(v[0] < 1.0 ? "CF::MaxNatural must be >= 1" : "CF::MaxNatural must be < " + AST.MAX_NATURAL, max.getWhere());
			} else {
				maxNatural = v[0];
				maxNaturalDepth = define.getConfigDepth();
			}
		}
	}

	public ASTExpression makeVariable(String name, ASTWhere where) {
		final Long flagItem = FlagType.findFlag(name);
		if (flagItem != null) {
			final ASTReal flag = new ASTReal(system, where, flagItem);
			flag.setType(ExpType.Flag);
			return flag;
		}
		if (name.startsWith("CF::")) {
			error("Configuration parameter names are reserved", where);
			return new ASTExpression(system, where);
		}
		if (FuncType.byName(name) != FuncType.NotAFunction) {
			error("Internal function names are reserved", where);
			return new ASTExpression(system, where);
		}
		final int varNum = stringToShape(name, true, where);
		final boolean[] isGlobal = new boolean[] { false };
		final ASTParameter bound = findExpression(varNum, isGlobal);
		if (bound == null) {
			return new ASTRuleSpecifier(system, where, varNum, name, null, cfdg.getShapeParams(currentShape));
		}
		return new ASTVariable(system, where, varNum, name);
	}

	public ASTExpression makeArray(String name, ASTExpression args, ASTWhere where) {
		if (name.startsWith("CF::")) {
			error("Configuration parameter names are reserved", where);
			return args;
		}
		int varNum = stringToShape(name, true, where);
		final boolean[] isGlobal = new boolean[] { false };
		ASTParameter bound = findExpression(varNum, isGlobal);
		if (bound == null) {
			error("Cannot find variable or parameter with name " + name, where);
			return args;
		}
		return new ASTArray(system, where, varNum, args, "");
	}

	public ASTExpression makeLet(ASTRepContainer vars, ASTExpression exp, ASTWhere where) {
		final int nameIndex = stringToShape("let", false, where);
		final ASTDefine def = new ASTDefine(system, where, "let");
		def.getShapeSpecifier().setShapeType(nameIndex);
		def.setExp(exp);
		def.setDefineType(DefineType.Let);
		return new ASTLet(system, where, vars, def);
	}

	public ASTRuleSpecifier makeRuleSpec(String name, ASTExpression args, ASTModification mod, boolean makeStart, ASTWhere where) {
		if (name.equals("if") || name.equals("let") || name.equals("select")) {
			if (args == null) {
				error("Arguments required here", where);
				return new ASTRuleSpecifier(system, where, 0, name, args, null);
			}
			// if and let are handled by the parser, select is handled here
			if (name.equals("select")) {
				args = new ASTSelect(system, where, args, false);
			}
			if (makeStart) {
				return new ASTStartSpecifier(system, where, args, mod);
			} else {
				return new ASTRuleSpecifier(system, where, args);
			}
		}
		int nameIndex = stringToShape(name, true, where);
		boolean[] isGlobal = new boolean[] { false };
		ASTParameter bound = findExpression(nameIndex, isGlobal);
		if (bound != null && args != null && args.getType() == ExpType.Reuse && !makeStart && isGlobal[0] && nameIndex == currentShape) {
			warning("Shape name binds to global variable and current shape, using current shape", where);
		}
		if (bound != null && bound.isParameter() && bound.getType() == ExpType.Rule) {
			return new ASTRuleSpecifier(system, where, nameIndex, name);
		}
		ASTRuleSpecifier ret;
		cfdg.setShapeHasNoParam(nameIndex, args);
		if (makeStart) {
			ret = new ASTStartSpecifier(system, where, nameIndex, name, args, mod);
		} else {
			ret = new ASTRuleSpecifier(system, where, nameIndex, name, args, cfdg.getShapeParams(currentShape));
		}
		if (ret.getArguments() != null && ret.getArguments().getType() == ExpType.Reuse) {
			if (makeStart) {
				error("Startshape cannot reuse parameters", where);
			} else if (nameIndex == currentShape)  {
				ret.setArgSource(ArgSource.SimpleParentArgs);
				ret.setTypeSignature(ret.getTypeSignature());
			}
		}
		return ret;
	}

	public void makeModTerm(ASTModification dest, ASTModTerm t, ASTWhere where) {
		if (t == null) {
			return;
		}
		if (t.getModType() == ModType.time) {
			timeWise();
		}
		if (t.getModType() == ModType.sat || t.getModType() == ModType.satTarg) {
			inColor();
		}
		if ("CFDG3".equals(getMaybeVersion()) && t.getModType().getType() >= ModType.hueTarg.getType() && t.getModType().getType() <= ModType.alphaTarg.getType()) {
			error("Color target feature unavailable in v3 syntax", where);
		}
		dest.getModExp().add(t);
	}
	
	public ASTReplacement makeElement(String name, ASTModification mods, ASTExpression params, boolean subPath, ASTWhere where) {
		if (inPathContainer && !subPath && (name.equals("FILL") || name.equals("STROKE"))) {
			return new ASTPathCommand(system, where, name, mods, params);
		}
		final ASTRuleSpecifier r = makeRuleSpec(name, params, null, false, where);
		RepElemType t = RepElemType.replacement;
		if (r.getArgSource() == ArgSource.ParentArgs) {
			r.setArgSource(ArgSource.SimpleParentArgs);
		}
		if (inPathContainer) {
			final boolean[] isGlobal = new boolean[] { false };
			ASTParameter bound = findExpression(r.getShapeType(), isGlobal);
			if (!subPath) {
				error("Replacements are not allowed in paths", where);
			} else if (r.getArgSource() == ArgSource.StackArgs || r.getArgSource() == ArgSource.ShapeArgs) {
	            // Parameter subpaths must be all ops, but we must checkParam at runtime
				t = RepElemType.op;
			} else if (cfdg.getShapeType(r.getShapeType()) == ShapeType.PathType) {
				ASTRule rule = cfdg.findRule(r.getShapeType());
				if (rule != null && rule.getRuleBody().getRepType() > 0) {
					t = RepElemType.fromType(rule.getRuleBody().getRepType());
				} else {
					// Recursive calls must be all ops, checkParam at runtime
					t = RepElemType.op;
				}
			} else if (bound != null) {
	            // Variable subpaths must be all ops, but we must checkParam at runtime
				t = RepElemType.op;
			} else if (isPrimeShape(r.getShapeType())) {
				t = RepElemType.op;
			} else {
				// Forward calls must be all ops, checkParam at runtime
				t = RepElemType.op;
			}
		}
		return new ASTReplacement(system, where, r, mods, t);
	}

	public ASTExpression makeFunction(String name, ASTExpression args, boolean consAllowed, ASTWhere where) {
		final int nameIndex = stringToShape(name, true, where);
		final boolean[] isGlobal = new boolean[] { false };
		final ASTParameter bound = findExpression(nameIndex, isGlobal);
		if (bound != null) {
			if (!consAllowed) {
				error("Can't bind expression to variable/parameter", where);
			}
			return makeVariable(name, where).append(args); 
		}
		if (name.equals("select") || name.equals("if")) {
			return new ASTSelect(system, where, args, name.equals("if"));
		}
		final FuncType t = FuncType.byName(name);
		if (t == FuncType.Ftime || t == FuncType.Frame) {
			cfdg.addParameter(Param.FrameTime);
		}
		if (t != FuncType.NotAFunction) {
			return new ASTFunction(system, where, name, args, seed);
		}
		// If args are parameter reuse args then it must be a rule spec
		if (args != null && args.getType() == ExpType.Reuse) {
			return makeRuleSpec(name, args, null, false, where);
		}
		// At this point we don't know if this is a typo or a to-be-defined shape or
		// user function. Return an ASTuserFunction and fix it up during type check.
		return new ASTUserFunction(system, where, nameIndex, args, null);
	}
	
	public ASTModification makeModification(ASTModification mod, boolean canonical, ASTWhere where) {
		if (mod != null) {
			mod.setCanonical(mod.getModExp().isEmpty());
			mod.setCanonical(canonical);
			mod.setWhere(where);
		}
		return mod;
	}
	
	public String getTypeInfo(int nameIndex, ASTDefine[] func, List<ASTParameter>[] p) {
		func[0] = cfdg.findFunction(nameIndex);
		p[0] = cfdg.getShapeParams(nameIndex);
		return cfdg.decodeShapeName(nameIndex);
	}

	public ASTRule getRule(int nameIndex) {
		return cfdg.findRule(nameIndex);
	}

	public void pushRepContainer(ASTRepContainer c) {
		containerStack.push(c);
		stackStack.push(localStackDepth);
		processRepContainer(c);
	}

	private void processRepContainer(ASTRepContainer c) {
		for (ASTParameter param : c.getParameters()) {
			if (param.isParameter() || param.isLoopIndex()) {
				param.setStackIndex(localStackDepth);
				localStackDepth += param.getTupleSize();
			} else {
				break;  // the parameters are all in front
			}
		}
	}

	public void popRepContainer(ASTReplacement r) {
		if (cfdg != null) {
			cfdg.reportStackDepth(localStackDepth);
		}
		assert(!containerStack.empty());
		final ASTRepContainer lastContainer = containerStack.lastElement();
		localStackDepth = stackStack.getLast();
		if (r != null) {
			//TODO verify RepType
			r.setRepType(RepElemType.fromType(r.getRepType().getType() | lastContainer.getRepType()));
			if (r.getPathOp() == PathOp.UNKNOWN) {
				r.setPathOp(lastContainer.getPathOp());
			}
		}
		containerStack.pop();
		stackStack.pop();
	}

	private boolean badContainer(int containerType) {
		return (containerType & (RepElemType.op.getType() | RepElemType.replacement.getType())) == (RepElemType.op.getType() | RepElemType.replacement.getType());
	}
	
	public void pushRep(ASTReplacement r, boolean global) {
		if (r == null) {
			return;
		}
		final ASTRepContainer container = containerStack.lastElement();
		container.getBody().add(r);
		if (container.getPathOp() == PathOp.UNKNOWN) {
			container.setPathOp(r.getPathOp());
		}
		final int oldType = container.getRepType();
		container.setRepType(oldType | r.getRepType().getType());
		if (badContainer(container.getRepType()) && !badContainer(oldType) && !global) {
			error("Can't mix path elements and replacements in the same container", r.getWhere());
		}
	}
	
	public ASTParameter findExpression(int nameIndex, boolean[] isGlobal) {
		if (!containerStack.isEmpty()) {
			for (ListIterator<ASTRepContainer> i = containerStack.listIterator(containerStack.size()); i.hasPrevious();) {
				final ASTRepContainer repCont = i.previous();
				if (!repCont.getParameters().isEmpty()) {
					for (ListIterator<ASTParameter> p = repCont.getParameters().listIterator(repCont.getParameters().size()); p.hasPrevious();) {
						ASTParameter param = p.previous();
						if (param.getNameIndex() == nameIndex) {
							isGlobal[0] = repCont.isGlobal();
							return param;
						}
					}
				}
			}
		}
		return null;
	}

	protected void pushNameSpace(String n, ASTWhere where) {
		if (n.equals("CF")) {
			error("CF namespace is reserved", where);
			return;
		}
		if (n.isEmpty()) {
			error("zero-length namespace", where);
			return;
		}
		checkName(n, false, where);
		includeNamespace.pop();
		includeNamespace.push(Boolean.TRUE);
		currentNameSpace = currentNameSpace + n + "::";
	}

	protected void popNameSpace() {
		currentNameSpace = currentNameSpace.substring(0, currentNameSpace.length() - 2);
		int end = currentNameSpace.lastIndexOf(":");
		if (end == -1) {
			currentNameSpace = "";
		} else {
			currentNameSpace = currentNameSpace.substring(0, end + 1);
		}
	}

	protected void checkVariableName(int nameIndex, boolean param, ASTWhere where) {
		if (allowOverlap && !param) {
			return;
		}
		if (!containerStack.isEmpty()) {
			ASTRepContainer repCont = param ? paramDecls : containerStack.lastElement();
			if (!repCont.getParameters().isEmpty()) {
				for (ListIterator<ASTParameter> i = repCont.getParameters().listIterator(repCont.getParameters().size()); i.hasPrevious(); ) {
					ASTParameter p = i.previous();
					if (p.getNameIndex() == nameIndex) {
						warning("Scope of name overlaps variable/parameter with same name", where);
						warning("Previous variable/parameter declared here", p.getWhere());
					}
				}
			}
		}
	}

	public void checkName(String name, boolean colonsAllowed, ASTWhere where) {
		int pos = name.indexOf(":");
		if (pos == -1) {
			return;
		}
		if (!colonsAllowed) {
			error("namespace specification not allowed in this context", where);
			return;
		}
		if (pos == 0) {
			error("improper namespace specification", where);
			return;
		}
		for (;;) {
			if (pos == name.length() - 1 || name.charAt(pos + 1) != ':') break;
			int next = name.indexOf(":", pos + 2);
			if (next == -1) return;
			if (next == pos + 2) break;
			pos = next;
		}
		error("improper namespace specification", where);
	}

	public void inColor() {
		cfdg.addParameter(Param.Color);
	}

	public void timeWise() {
		cfdg.addParameter(Param.Time);
	}

	public void blended() {
		cfdg.addParameter(Param.Blend);
	}

	public boolean impure() {
		return cfdg.isImpure();
	}

	protected String relativeFilePath(String base, String rel) {
		return base + "/" + rel;
	}

	protected void parseStream(ASTWhere where) {
		try {
			final CharStream cs = CharStreams.fromFileName(filesToLoad.peek());
			final ContextFreeLexer lexer = new ContextFreeLexer(cs);
			final CommonTokenStream tokens = new CommonTokenStream(lexer);
			final ContextFreeParser parser = new ContextFreeParser(tokens);
			parser.setBuilder(this);
			if (cfdg.getVersion().equals("CFDG3")) {
				parser.cfdg3();
			} else {
				parser.cfdg2();
			}
		} catch (IOException e) {
			throw new CFDGException("Can't import file " + fileNames.peek(), where);
		}
	}

	protected void incLocalStackDepth() {
		localStackDepth++;
	}

	protected void decLocalStackDepth() {
		localStackDepth--;
	}

	protected void setParamSize(int paramSize) {
		this.paramSize = paramSize;
	}

	private boolean isPrimeShape(int nameIndex) {
		return nameIndex < 4;
	}

	private List<ASTExpression> extract(ASTExpression exp) {
		if (exp instanceof ASTCons) {
			return ((ASTCons)exp).getChildren();
		} else {
			List<ASTExpression> ret = new ArrayList<>();
			ret.add(exp);
			return ret;
		}
	}
}
