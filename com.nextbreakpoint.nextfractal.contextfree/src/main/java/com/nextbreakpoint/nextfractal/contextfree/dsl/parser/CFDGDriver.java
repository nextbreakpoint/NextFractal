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
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

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
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

public class CFDGDriver {
	private final CFDG cfdg = new CFDG(this);
	@Getter
    private final Stack<ASTRepContainer> containerStack = new Stack<>();
	@Getter
    private final ASTRepContainer paramDecls = new ASTRepContainer(this);
	private final Map<String, Long> flagNames = new HashMap<>();
	private final List<CFStackRule> longLivedParams = new ArrayList<>();
	private final Stack<String> fileNames = new Stack<>();
	private final Stack<String> filesToLoad = new Stack<>();
	private final Stack<CharStream> streamsToLoad = new Stack<>();
	private final Stack<Boolean> includeNamespace = new Stack<>();
	@Getter
    private final Stack<ASTSwitch> switchStack = new Stack<>();
	@Getter
    private final Rand64 seed = new Rand64();
	@Setter
    @Getter
    private Logger logger = new Logger();
	private String currentNameSpace = "";
	@Setter
    @Getter
    private String currentPath;
	private String filePath;
	@Setter
    @Getter
    private String maybeVersion;
	private int currentShape;
	private int includeDepth;
	@Setter
    @Getter
    private int localStackDepth;
	private boolean allowOverlap;
	@Setter
    @Getter
    private boolean inPathContainer;
	@Getter
    private boolean errorOccurred;
	private double maxNatual = 1000.0;
	private int pathCount = -1;

	public CFDGDriver() {
		containerStack.add(new ASTRepContainer(this));
		currentShape = -1;
		pathCount = 1;
		flagNames.put("CF::None",        FlagType.CF_NONE.getMask());
		flagNames.put("CF::MiterJoin",   FlagType.CF_MITER_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::RoundJoin",   FlagType.CF_ROUND_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::BevelJoin",   FlagType.CF_BEVEL_JOIN.getMask() | FlagType.CF_JOIN_PRESENT.getMask());
		flagNames.put("CF::ButtCap",     FlagType.CF_BUTT_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::RoundCap",    FlagType.CF_ROUND_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::SquareCap",   FlagType.CF_SQUARE_CAP.getMask() | FlagType.CF_CAP_PRESENT.getMask());
		flagNames.put("CF::ArcCW",       FlagType.CF_ARC_CW.getMask());
		flagNames.put("CF::ArcLarge",    FlagType.CF_ARC_LARGE.getMask());
		flagNames.put("CF::Continuous",  FlagType.CF_CONTINUOUS.getMask());
		flagNames.put("CF::Align",       FlagType.CF_ALIGN.getMask());
		flagNames.put("CF::EvenOdd",     FlagType.CF_EVEN_ODD.getMask());
		flagNames.put("CF::IsoWidth",    FlagType.CF_ISO_WIDTH.getMask());
		flagNames.put("~~CF_FILL~~",     FlagType.CF_FILL.getMask());
		flagNames.put("CF::Cyclic",      FlagType.CF_CYCLIC.getMask());
		flagNames.put("CF::Dihedral",    FlagType.CF_DIHEDRAL.getMask());
		flagNames.put("CF::p11g",        FlagType.CF_P11G.getMask());
		flagNames.put("CF::p11m",        FlagType.CF_P11M.getMask());
		flagNames.put("CF::p1m1",        FlagType.CF_P1M1.getMask());
		flagNames.put("CF::p2",          FlagType.CF_P2.getMask());
		flagNames.put("CF::p2mg",        FlagType.CF_P2MG.getMask());
		flagNames.put("CF::p2mm",        FlagType.CF_P2MM.getMask());
		flagNames.put("CF::pm",          FlagType.CF_PM.getMask());
		flagNames.put("CF::pg",          FlagType.CF_PG.getMask());
		flagNames.put("CF::cm",          FlagType.CF_CM.getMask());
		flagNames.put("CF::pmm",         FlagType.CF_PMM.getMask());
		flagNames.put("CF::pmg",         FlagType.CF_PMG.getMask());
		flagNames.put("CF::pgg",         FlagType.CF_PGG.getMask());
		flagNames.put("CF::cmm",         FlagType.CF_CMM.getMask());
		flagNames.put("CF::p4",          FlagType.CF_P4.getMask());
		flagNames.put("CF::p4m",         FlagType.CF_P4M.getMask());
		flagNames.put("CF::p4g",         FlagType.CF_P4G.getMask());
		flagNames.put("CF::p3",          FlagType.CF_P3.getMask());
		flagNames.put("CF::p3m1",        FlagType.CF_P3M1.getMask());
		flagNames.put("CF::p31m",        FlagType.CF_P31M.getMask());
		flagNames.put("CF::p6",          FlagType.CF_P6.getMask());
		flagNames.put("CF::p6m",         FlagType.CF_P6M.getMask());
	}

    public void warning(String message, Token location) {
		logger.warning(message, location);
	}

	public void error(String message, Token location) {
		logger.error(message, location);
	}

	public void fail(String message, Token location) {
		logger.fail(message, location);
	}

	public void info(String message, Token location) {
		logger.info(message, location);
	}

	protected boolean isPrimeShape(int nameIndex) {
		return nameIndex < 4;
	}

	public int stringToShape(String name, boolean colonsAllowed, Token location) {
		checkName(name, colonsAllowed, location);
		if (currentNameSpace.isEmpty()) {
			return cfdg.encodeShapeName(name);
		}
		int index = Collections.binarySearch(PrimShape.getShapeNames(), name);
		String n = currentNameSpace + name;
		if (index >= 0 && cfdg.tryEncodeShapeName(n) == -1) {
			return cfdg.encodeShapeName(name);
		} else {
			return cfdg.encodeShapeName(n);
		}
	}

	public String shapeToString(int shape) {
		return cfdg.decodeShapeName(shape);
	}

	public void includeFile(String fileName, Token location) {
		try {
			String path = relativeFilePath(currentPath, fileName);
			info("Include file " + path, location);
//			if (path.endsWith(".nf.zip")) {
//				throw new UnsupportedOperationException();
//			}
			CharStream is = CharStreams.fromFileName(path);
			fileNames.push(path);
			filePath = path;
			filesToLoad.push(filePath);
			streamsToLoad.push(is);
			includeNamespace.push(Boolean.FALSE);
			pathCount++;
			includeDepth++;
			currentShape = -1;
			setShape(null, false, location);
			info("Reading rules file " + path, location);
		} catch (Exception e) {
			fail(e.getMessage(), location);
		}
	}
	
	public boolean endInclude(Token location) {
		boolean endOfInput = includeDepth == 0;
		try {
			setShape(null, false, location);
			includeDepth--;
			if (filesToLoad.isEmpty()) {
				return endOfInput;
			}
			if (includeNamespace.peek()) {
				popNameSpace();
			}
			streamsToLoad.pop();
			filesToLoad.pop();
			includeNamespace.pop();
			filePath = filesToLoad.isEmpty() ? null : filesToLoad.peek();
		} catch (Exception e) {
			error(e.getMessage(), location);
		}
		return endOfInput;
	}
	
	public void setShape(String name, Token location) {
		setShape(name, false, location);
	}
	
	public void setShape(String name, boolean isPath, Token location) {
		if (name == null) {
			currentShape = -1;
			return;
		}
		currentShape = stringToShape(name, false, location);
		ASTDefine def = cfdg.findFunction(currentShape);
		if (def != null) {
			error("There is a function with the same name as this shape: " + def.getName(), location);
			return;
		}
		String err = cfdg.setShapeParams(currentShape, paramDecls, paramDecls.getStackCount(), isPath);
		if (err != null) {
			errorOccurred = true;
			error("Can't set shape params: " + err, location);
		}
		localStackDepth -= paramDecls.getStackCount();
		paramDecls.getParameters().clear();
		paramDecls.setStackCount(0);
	}
	
	public void addRule(ASTRule rule) {
		boolean isShapeItem = rule.getNameIndex() == -1;
		if (isShapeItem) {
			rule.setNameIndex(currentShape);
		} else {
			currentShape = -1;
		}
		if (rule.getNameIndex() == -1) {
			error("Shape rules/paths must follow a shape declaration", rule.getLocation());
		}
		ShapeType type = cfdg.getShapeType(rule.getNameIndex());
		if ((rule.isPath() && type == ShapeType.RuleType) || (!rule.isPath() && type == ShapeType.PathType)) {
			error("Can't mix rules and shapes with the same name", rule.getLocation());
		}
		boolean matchesShape = cfdg.addRule(rule);
		if (!isShapeItem && matchesShape) {
			error("Rule/path name matches existing shape name", rule.getLocation());
		}
	}

	public void nextParameterDecl(String type, String name, Token location) {
		int nameIndex = stringToShape(name, false, location);
		checkVariableName(nameIndex, true, location);
		paramDecls.addParameter(type, nameIndex, location);
		ASTParameter param = paramDecls.getParameters().getLast();
		param.setStackIndex(localStackDepth);
		paramDecls.setStackCount(paramDecls.getStackCount() + param.getTupleSize());
		localStackDepth += param.getTupleSize();
	}

	public ASTDefine makeDefinition(String name, boolean isFunction, Token location) {
		if (name.startsWith("CF::")) {
			if (isFunction) {
				error("Configuration parameters can't  be functions", location);
				return null;
			}
			if (containerStack.lastElement().isGlobal()) {
				error("Configuration parameters must be at global scope", location);
				return null;
			}
			ASTDefine def = new ASTDefine(this, name, location);
			def.setConfigDepth(includeDepth);
			def.setDefineType(DefineType.Config);
			return def;
		}
		if (FuncType.byName(name) != FuncType.NotAFunction) {
			error("Internal function names are reserved", location);
			return null;
		}
		int nameIndex = stringToShape(name, false, location);
		ASTDefine def = cfdg.findFunction(nameIndex);
		if (def != null) {
			error("Definition with same name as user function: " + def.getLocation(), location);
			return null;
		}
		checkVariableName(nameIndex, false, location);
		def = new ASTDefine(this, name, location);
		def.getShapeSpecifier().setShapeType(nameIndex);
		if (isFunction) {
			for (ASTParameter param : paramDecls.getParameters()) {
				param.setLocality(Locality.PureNonLocal);
			}
			def.getParameters().clear();
			def.getParameters().addAll(paramDecls.getParameters());
			def.setStackCount(paramDecls.getStackCount());
			def.setDefineType(DefineType.Function);
			localStackDepth -= paramDecls.getStackCount();
			paramDecls.setStackCount(0);
			cfdg.declareFunction(nameIndex, def);
		} else {
			containerStack.lastElement().addDefParameter(nameIndex, def, location);
		}
		return def;
	}
	
	public void makeConfig(ASTDefine cfg) {
		if (cfg.getName().equals(CFG.Impure.getName())) {
			double[] v = new double[] { 0.0 };
			if (cfg.getExp() == null || !cfg.getExp().isConstant() || cfg.getExp().evaluate(v, 1, null) != 1) {
				error("CF::Impure requires a constant numeric expression", cfg.getLocation());
			} else {
				ASTParameter.Impure = v[0] != 0.0;
			}
		}
		if (cfg.getName().equals(CFG.AllowOverlap.getName())) {
			double[] v = new double[] { 0.0 };
			if (cfg.getExp() != null || cfg.getExp().isConstant() || cfg.getExp().evaluate(v, 1, null) != 1) {
				error("CF::AllowOverlap requires a constant numeric expression", cfg.getLocation());
			} else {
				allowOverlap = v[0] != 0.0;
			}
		}
		if (cfg.getName().equals(CFG.StartShape.getName()) && cfg.getExp() != null && !(cfg.getExp() instanceof ASTStartSpecifier)) {
			ASTRuleSpecifier rule = null;
			ASTModification mod = null;
			List<ASTExpression> specAndMod = extract(cfg.getExp());
			switch (specAndMod.size()) {
				case 2:
					if (!(specAndMod.get(1) instanceof ASTModification)) {
						error("CF::StartShape second term must be a modification", cfg.getLocation());
					} else {
						mod = (ASTModification) specAndMod.get(1);
					}
					break;
	
				case 1:
					if (!(specAndMod.getFirst() instanceof ASTRuleSpecifier)) {
						error("CF::StartShape must start with a shape specification", cfg.getLocation());
					} else {
						rule = (ASTRuleSpecifier) specAndMod.getFirst();
					}
					break;
					
				default:
					error("CF::StartShape expression must have the form shape_spec or shape_spec, modification", cfg.getLocation());
					break;
			}
			if (mod == null) {
				mod = new ASTModification(this, cfg.getLocation());
			}
			cfg.setExp(new ASTStartSpecifier(this, rule, mod, cfg.getLocation()));
		}
		ASTExpression current = cfg.getExp();
		if (!cfdg.addParameter(cfg.getName(), cfg.getExp(), cfg.getConfigDepth())) {
			error("Unknown configuration parameter", cfg.getLocation());
		}
		if (cfg.getName().equals(CFG.MaxNatural.getName())) {
			ASTExpression max = cfdg.hasParameter(CFG.MaxNatural);
			if (max != current) {
				return;
			}
			double[] v = new double[] { -1.0 };
			if (max == null || !max.isConstant() || max.getType() != ExpType.Numeric || max.evaluate(v, 1, null) != 1) {
				error("CF::MaxNatural requires a constant numeric expression", cfg.getLocation());
			} else if (v[0] < 1.0 || v[0] > 9007199254740992.0) {
				error(v[0] < 1.0 ? "CF::MaxNatural must be >= 1" : "CF::MaxNatural must be < 9007199254740992", cfg.getLocation());
			} else {
				maxNatual = v[0];
			}
		}
	}
	
	private List<ASTExpression> extract(ASTExpression exp) {
		if (exp instanceof ASTCons) {
			return ((ASTCons)exp).getChildren();
		} else {
			List<ASTExpression> ret = new ArrayList<ASTExpression>();
			ret.add(exp);
			return ret;
		}
	}

	public ASTExpression makeVariable(String name, Token location) {
		Long flagItem = flagNames.get(name);
		if (flagItem != null) {
			ASTReal flag = new ASTReal(this, flagItem, location);
			flag.setType(ExpType.Flag);
			return flag;
		}
		if (name.startsWith("CF::")) {
			error("Configuration parameter names are reserved", location);
			return new ASTExpression(this, location);
		}
		if (FuncType.byName(name) != FuncType.NotAFunction) {
			error("Internal function names are reserved", location);
			return new ASTExpression(this, location);
		}
		int varNum = stringToShape(name, true, location);
		boolean isGlobal = false;
		ASTParameter bound = findExpression(varNum, isGlobal);
		if (bound == null) {
			return new ASTRuleSpecifier(this, varNum, name, null, cfdg.getShapeParams(currentShape), location);
		}
		return new ASTVariable(this, varNum, name, location);
	}

	public ASTExpression makeArray(String name, ASTExpression args, Token location) {
		if (name.startsWith("CF::")) {
			error("Configuration parameter names are reserved", location);
			return args;
		}
		int varNum = stringToShape(name, true, location);
		boolean isGlobal = false;
		ASTParameter bound = findExpression(varNum, isGlobal);
		if (bound == null) {
			return args;
		}
		return new ASTArray(this, varNum, args, "", location);
	}

	public ASTExpression makeLet(ASTRepContainer vars, ASTExpression exp, Token location) {
		int nameIndex = stringToShape("let", false, location);
		ASTDefine def = new ASTDefine(this, "let", location);
		def.getShapeSpecifier().setShapeType(nameIndex);
		def.setExp(exp);
		def.setDefineType(DefineType.Let);
		return new ASTLet(this, vars, def, location);
	}

	public ASTRuleSpecifier makeRuleSpec(String name, ASTExpression args, Token location) {
			return makeRuleSpec(name, args, null, false, location);
	}
	
	public ASTRuleSpecifier makeRuleSpec(String name, ASTExpression args, ASTModification mod, boolean makeStart, Token location) {
		if (name.equals("if") || name.equals("let") || name.equals("select")) {
			if (name.equals("select")) {
				args = new ASTSelect(this, args, false, location);
			}
			if (makeStart) {
				return new ASTStartSpecifier(this, args, mod, location);
			} else {
				return new ASTRuleSpecifier(this, args, location);
			}
		}
		int nameIndex = stringToShape(name, true, location);
		boolean isGlobal = false;
		//TODO fix isGlobal
		ASTParameter bound = findExpression(nameIndex, isGlobal);
		if (bound != null && args != null && args.getType() == ExpType.Reuse && !makeStart && isGlobal && nameIndex == currentShape) {
			error("Shape name binds to global variable and current shape, using current shape", location);
		}
		if (bound != null && bound.isParameter() && bound.getType() == ExpType.Rule) {
			return new ASTRuleSpecifier(this, nameIndex, name, location);
		}
		ASTRuleSpecifier ret = null;
		cfdg.setShapeHasNoParam(nameIndex, args);
		if (makeStart) {
			ret = new ASTStartSpecifier(this, nameIndex, name, args, mod, location);
		} else {
			ret = new ASTRuleSpecifier(this, nameIndex, name, args, cfdg.getShapeParams(currentShape), location);
		}
		if (ret.getArguments() != null && ret.getArguments().getType() == ExpType.Reuse) {
			if (makeStart) {
				error("Startshape can't  reuse parameters", location);
			} else if (nameIndex == currentShape)  {
				ret.setArgSource(ArgSource.SimpleParentArgs);
				ret.setTypeSignature(ret.getTypeSignature());
			}
		}
		return ret;
	}

	public void makeModTerm(ASTModification dest, ASTModTerm t, Token location) {
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
			error("Color target feature unavailable in v3 syntax", dest.getLocation());
		}
		dest.getModExp().add(t);
	}
	
	public ASTReplacement makeElement(String name, ASTModification mods, ASTExpression params, boolean subPath, Token location) {
		if (inPathContainer && !subPath && (name.equals("FILL") || name.equals("STROKE"))) {
			return new ASTPathCommand(this, name, mods, params, location);
		}
		ASTRuleSpecifier ruleSpecifier = makeRuleSpec(name, params, null, false, location);
		RepElemType t = RepElemType.replacement;
		if (inPathContainer) {
			boolean isGlobal = false;
			ASTParameter bound = findExpression(ruleSpecifier.getShapeType(), isGlobal);
			if (!subPath) {
				error("Replacements are not allowed in paths", location);
			} else if (ruleSpecifier.getArgSource() == ArgSource.StackArgs || ruleSpecifier.getArgSource() == ArgSource.ShapeArgs) {
	            // Parameter subpaths must be all ops, but we must checkParam at runtime
				t = RepElemType.op;
			} else if (cfdg.getShapeType(ruleSpecifier.getShapeType()) == ShapeType.PathType) {
				ASTRule rule = cfdg.findRule(ruleSpecifier.getShapeType());
				if (rule != null) {
					t = RepElemType.fromType(rule.getRuleBody().getRepType());
				} else {
					// Recursive calls must be all ops, checkParam at runtime
					t = RepElemType.op;
				}
			} else if (bound != null) {
	            // Variable subpaths must be all ops, but we must checkParam at runtime
				t = RepElemType.op;
			} else if (isPrimeShape(ruleSpecifier.getShapeType())) {
				t = RepElemType.op;
			} else {
				// Forward calls must be all ops, checkParam at runtime
				t = RepElemType.op;
			}
		}
		return new ASTReplacement(this, ruleSpecifier, mods, t, location);
	}

	public ASTExpression makeFunction(String name, ASTExpression args, Token location) {
		return makeFunction(name, args, false, location);
	}
	
	public ASTExpression makeFunction(String name, ASTExpression args, boolean consAllowed, Token location) {
		int nameIndex = stringToShape(name, true, location);
		boolean isGlobal = false;
		ASTParameter bound = findExpression(nameIndex, isGlobal);
		if (bound != null) {
			if (!consAllowed) {
				error("Can't bind expression to variable/parameter", location);
			}
			return makeVariable(name, location).append(args); 
		}
		if (name.equals("select") || name.equals("if")) {
			return new ASTSelect(this, args, name.equals("if"), location);
		}
		FuncType t = FuncType.byName(name);
		if (t == FuncType.Ftime || t == FuncType.Frame) {
			cfdg.addParameter(Param.FrameTime);
		}
		if (t != FuncType.NotAFunction) {
			return new ASTFunction(this, name, args, seed, location);
		}
		if (args != null && args.getType() == ExpType.Reuse) {
			return makeRuleSpec(name, args, null, false, location);
		}
		return new ASTUserFunction(this, nameIndex, args, null, location);
	}
	
	public ASTModification makeModification(ASTModification mod, boolean canonial, Token location) {
		mod.setCanonical(mod.getModExp().isEmpty());
		mod.setCanonical(canonial);
		mod.setLocation(location);
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
		processRepContainer(c);
	}
	
	private void processRepContainer(ASTRepContainer c) {
		c.setStackCount(0);
		for (ASTParameter param : c.getParameters()) {
			if (param.isParameter() || param.isLoopIndex()) {
				param.setStackIndex(localStackDepth);
				c.setStackCount(c.getStackCount() + param.getTupleSize());
				localStackDepth += param.getTupleSize();
			} else {
				break;  // the parameters are all in front
			}
		}
	}
	
	public void popRepContainer(ASTReplacement r) {
		ASTRepContainer lastContainer = containerStack.lastElement();
		localStackDepth -= lastContainer.getStackCount();
		if (r != null) {
			r.setRepType(RepElemType.fromType(r.getRepType().getType() | lastContainer.getRepType()));
			if (r.getPathOp() == PathOp.UNKNOWN) {
				r.setPathOp(lastContainer.getPathOp());
			}
		}
		containerStack.pop();
	}

	private boolean badContainer(int containerType) {
		return (containerType & (RepElemType.op.getType() | RepElemType.replacement.getType())) == (RepElemType.op.getType() | RepElemType.replacement.getType());
	}
	
	public void pushRep(ASTReplacement r, boolean global) {
		if (r == null) {
			return;
		}
		ASTRepContainer container = containerStack.lastElement();
		container.getBody().add(r);
		if (container.getPathOp() == PathOp.UNKNOWN) {
			container.setPathOp(r.getPathOp());
		}
		int oldType = container.getRepType();
		container.setRepType(oldType | r.getRepType().getType());
		if (badContainer(container.getRepType()) && !badContainer(oldType) && !global) {
			error("Can't mix path elements and replacements in the same container", r.getLocation());
		}
	}
	
	public ASTParameter findExpression(int nameIndex, boolean isGlobal) {
		if (!containerStack.isEmpty()) {
			for (ListIterator<ASTRepContainer> i = containerStack.listIterator(containerStack.size()); i.hasPrevious();) {
				ASTRepContainer repCont = i.previous();
				if (!repCont.getParameters().isEmpty()) {
					for (ListIterator<ASTParameter> p = repCont.getParameters().listIterator(repCont.getParameters().size()); p.hasPrevious();) {
						ASTParameter param = p.previous();
						if (param.getNameIndex() == nameIndex) {
							isGlobal = repCont.isGlobal();
							return param;
						}
					}
				}
			}
		}
		return null;
	}
	
	protected void checkVariableName(int nameIndex, boolean param, Token location) {
		if (allowOverlap && !param) {
			return;
		}
		if (!containerStack.isEmpty()) {
			ASTRepContainer repCont = param ? paramDecls : containerStack.lastElement();
			if (!repCont.getParameters().isEmpty()) {
				for (ListIterator<ASTParameter> i = repCont.getParameters().listIterator(repCont.getParameters().size()); i.hasPrevious(); ) {
					ASTParameter p = i.previous();
					if (p.getNameIndex() == nameIndex) {
						warning("Scope of name overlaps variable/parameter with same name", location);
						warning("Previous variable/parameter declared here", p.getLocation());
					}
				}
			}
		}
	}

	public void checkName(String name, boolean colonsAllowed, Token location) {
		int pos = name.indexOf(":");
		if (pos == -1) {
			return;
		}
		if (!colonsAllowed) {
			error("namespace specification not allowed in this context", location);
			return;
		}
		if (pos == 0) {
			error("improper namespace specification", location);
			return;
		}
		for (;;) {
			if (pos == name.length() - 1 || name.charAt(pos + 1) != ':') break;
			int next = name.indexOf(":", pos + 2);
			if (next == -1) return;
			if (next == pos + 2) break;
			pos = next;
		}
		error("improper namespace specification", location);
	}

	protected String relativeFilePath(String base, String rel) {
		return base + "/" + rel;
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

	protected void pushNameSpace(String n, Token location) {
		if (n.equals("CF")) {
			error("CF namespace is reserved", location);
			return;
		}
		if (n.isEmpty()) {
			error("zero-length namespace", location);
			return;
		}
		checkName(n, false, location);
		includeNamespace.pop();
		includeNamespace.push(Boolean.TRUE);
		currentNameSpace = currentNameSpace + n + "::";
	}

	protected void parseStream() {
		ContextFreeParser parser = new ContextFreeParser(new CommonTokenStream(new ContextFreeLexer(streamsToLoad.peek())));
		parser.setDriver(this);
		parser.choose();
	}

	public void inColor() {
		cfdg.addParameter(Param.Color);
	}

	public void timeWise() {
		cfdg.addParameter(Param.Time);
	}

	public void storeParams(CFStackRule p) {
		longLivedParams.add(p);
	}

    public CFDG getCFDG() {
		return cfdg;
	}

    public void incSwitchStack() {
		localStackDepth--;
	}

	public void decSwitchStack() {
		localStackDepth++;
	}

    public ASTRepContainer getCFDGContent() {
		return cfdg.getContents();
	}
}
