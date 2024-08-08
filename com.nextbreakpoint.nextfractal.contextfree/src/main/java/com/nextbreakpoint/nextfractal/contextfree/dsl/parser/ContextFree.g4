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
grammar ContextFree;

// cfdg.y
// this file is part of Context Free
// ---------------------
// Copyright (C) 2005-2008 Mark Lentczner - markl@glyphic.com
// Copyright (C) 2005-2013 John Horigan - john@glyphic.com
// Copyright (C) 2005 Chris Coyne - ccoyne77@gmail.com
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

options
{
} 

@header {
package com.nextbreakpoint.nextfractal.contextfree.dsl.parser;

import java.util.Map;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.*;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.ast.*;
}

@members {

private CFDGBuilder builder = null;

public void setBuilder(CFDGBuilder builder) {
    this.builder = builder;
}

public CFDGBuilder getBuilder() {
    return builder;
}

public CFDGSystem getSystem() {
    return builder.getSystem();
}

protected ASTWhere makeWhere(Token token) {
    return new ASTWhere(token.getLine(), token.getCharPositionInLine(), token.getStartIndex(), token.getStopIndex(), token.getText());
}

}

cfdg2
        :
        CFDG2? cfdg2_statements
        ;

cfdg3
        :
        CFDG3? cfdg3_statements
        ;

cfdg2_statements
        :
        cfdg2_statements r=statement_v2 {
	        if ($r.result != null) {
	          	builder.pushRep($r.result, true);
	        }
        }
        |
        ;

cfdg3_statements
        :
        cfdg3_statements r=statement_v3 {
            if ($r.result != null) {
                builder.pushRep($r.result, true);
            }
        }
        |
        ;

statement_v3 returns [ASTReplacement result]
        :
        initialization_v3 {
            $result = $initialization_v3.result;
        }
        | import_v3 {
        	$result = null;
        }
        | rule_v3 {
            $result = $rule_v3.result;
        }
        | path_v3 {
            $result = $path_v3.result;
        }
        | shape {
        	$result = null;
        }
        | shape_singleton {
            $result = $shape_singleton.result;
        }
        | global_definition {
        	$result = $global_definition.result;
        }
        | v2stuff .*? {
        	builder.error("Illegal mixture of old and new elements", makeWhere($v2stuff.start));
        	$result = null;
        }
        ;

statement_v2 returns [ASTReplacement result]
        : 
        initialization_v2 {
            $result = $initialization_v2.result;
        }
        | directive_v2 {
            $result = $directive_v2.result;
        }
        | inclusion_v2 { 
        	$result = null;
        }
        | rule_v2 {
            $result = $rule_v2.result;
        }
        | path_v2 {
            $result = $path_v2.result;
        }
        | v3clues .*? {
        	if ("CFDG2".equals(builder.getMaybeVersion())) {
        		builder.error("Illegal mixture of old and new elements", makeWhere($v3clues.start));
        	} else {
        		builder.setMaybeVersion("CFDG3");
        	}
        	$result = null;
        }
        ;

v3clues
		:
        USER_STRING BECOMES
        | modtype_v3 BECOMES
        | PARAM BECOMES
        | USER_STRING '('
        | USER_STRING USER_STRING '('
        | IMPORT
        | SHAPE
        | PATH USER_STRING '('
        | STARTSHAPE USER_STRING '('
        | STARTSHAPE USER_STRING '['
        | STARTSHAPE USER_ARRAYNAME '['
        ;
        
v2stuff
		:
        BACKGROUND modification_v2
        | TILE modification_v2
        | modtype_v2 modification_v2
        | INCLUDE fileName
        | rule_header_v2
        ;

inclusion_v2 
        : 
        INCLUDE f=fileName {
            builder.setMaybeVersion("CFDG2");
        	builder.setShape(null, makeWhere($INCLUDE));
        	builder.includeFile($f.result, makeWhere($INCLUDE));
        	builder.parseStream(makeWhere($INCLUDE));
        	builder.endInclude(makeWhere($INCLUDE));
        }
        ;

import_v3
        : 
        IMPORT n=fileNameSpace f=fileName {
            builder.setShape(null, makeWhere($IMPORT));
            builder.includeFile($f.result, makeWhere($IMPORT));
            if ($n.result != null) {
                builder.pushNameSpace($n.result, makeWhere($IMPORT));
            }
        	builder.parseStream(makeWhere($IMPORT));
        	builder.endInclude(makeWhere($IMPORT));
        }
        ;

fileName returns [String result]
		:
        f=USER_FILENAME {
        	$result = $f.getText();
        } 
        | 
        f=USER_QFILENAME {
        	$result = $f.getText().substring(1, $f.getText().length() - 1);
        } 
       	;
		
fileNameSpace returns [String result]
		:
        '@' r=USER_STRING { 
        	$result = $r.getText();
        }
        | { 
        	$result = null;
        }
        ;

initialization_v3 returns [ASTDefine result]
        : 
        STARTSHAPE s=USER_STRING p=parameter_spec m=modification {
        	String name = $s.getText();
        	ASTExpression p = $p.result;
        	ASTModification mod = $m.result;
        	builder.setShape(null, makeWhere($STARTSHAPE));
        	ASTRuleSpecifier ruleSpec = builder.makeRuleSpec(name, p, mod, true, makeWhere($s));
        	ASTDefine define = builder.makeDefinition(CFG.StartShape, ruleSpec, makeWhere($STARTSHAPE));
        	$result = define;
        }
        |
        STARTSHAPE s=USER_ARRAYNAME m=modification {
        	String name = $s.getText();
        	ASTModification mod = $m.result;
        	builder.setShape(null, makeWhere($STARTSHAPE));
        	ASTRuleSpecifier ruleSpec = builder.makeRuleSpec(name, null, mod, true, makeWhere($s));
        	ASTDefine define = builder.makeDefinition(CFG.StartShape, ruleSpec, makeWhere($STARTSHAPE));
        	$result = define;
        }
        |
        STARTSHAPE s=USER_STRING p=parameter_spec {
        	String name = $s.getText();
        	ASTExpression p = $p.result;
        	builder.setShape(null, makeWhere($STARTSHAPE));
        	ASTRuleSpecifier ruleSpec = builder.makeRuleSpec(name, p, null, true, makeWhere($s));
        	ASTDefine define = builder.makeDefinition(CFG.StartShape, ruleSpec, makeWhere($STARTSHAPE));
        	$result = define;
        }
        ;

initialization_v2 returns [ASTDefine result]
        : 
        STARTSHAPE s=USER_STRING {
        	String name = $s.getText();
        	builder.setShape(null, makeWhere($STARTSHAPE));
        	ASTRuleSpecifier ruleSpec = builder.makeRuleSpec(name, null, null, true, makeWhere($STARTSHAPE));
        	ASTDefine define = builder.makeDefinition(CFG.StartShape, ruleSpec, makeWhere($STARTSHAPE));
        	$result = define;
        }
        ;

directive_v2 returns [ASTDefine result]
		:
        s=directive_num m=modification_v2 {
            builder.setMaybeVersion("CFDG2");
        	ASTModification mod = $m.result;
            ASTDefine define = builder.makeDefinition($s.result, mod, makeWhere($s.start));
        	$result = define;
        }
        ;

directive_num returns [CFG result]
		:
        BACKGROUND { 
        	$result = CFG.Background;
        }
        |
        TILE { 
        	$result = CFG.Tile;
        }
        |
        t=modtype_v2 {
        	if (ModType.size.name().equals($t.result)) {
                $result = CFG.Size;
        	} else if (ModType.time.name().equals($t.result)) {
                $result = CFG.Time;
        	} else {
                $result = CFG.Size;
                builder.error("Syntax error", makeWhere($t.start));
        	} 
        }
        ;

global_definition returns [ASTDefine result]
		:
        r=global_definition_header e=exp2 {
            ASTDefine var = $r.result;
            ASTExpression exp = $e.result;
            if (var != null) {
                switch (var.getDefineType()) {
                    case Config:
                        var.setExp(exp);
                        builder.checkConfig(var);
                        break;
                    case Stack:
                        if (exp instanceof ASTModification) {
                        	ASTModification mod = (ASTModification)exp;
                            var.getChildChange().grab(mod); // emptied ASTmod gets deleted
                        } else {
                            var.setExp(exp);
                        }
                        break;
                    case Let:
                        assert(false);
                        break;
                    case Function:
                        builder.popRepContainer(null);
                        builder.getParamDecls().getParameters().clear();
                        builder.setParamSize(0);
                        // fall through
                    default:
                        var.setExp(exp);
                        break;
                }
                $result = var;
            } else {
                $result = null;
            }
        }
        ;

function_definition_header returns [ASTDefine result]
		:
        SHAPE f=USER_STRING p=function_parameter_list BECOMES {
        	String name = $f.getText();
            $result = builder.makeDefinition(name, true, makeWhere($SHAPE));
            if ($result != null) {
                $result.setExpType(ExpType.Rule);
                $result.setTupleSize(1);
            }
        }
        |
        f=USER_STRING p=function_parameter_list BECOMES {
        	String name = $f.getText();
            $result = builder.makeDefinition(name, true, makeWhere($f));
            if ($result != null) {
                $result.setExpType(ExpType.Numeric);
                $result.setTupleSize(1);
            }
        }
        |
        t=USER_STRING f=USER_STRING p=function_parameter_list BECOMES {
        	String name = $t.getText();
        	String type = $f.getText();
            $result = builder.makeDefinition(name, true, makeWhere($t));
            if ($result != null) {
            	int[] tupleSize = new int[1];
            	boolean[] natural = new boolean[1];
                $result.setExpType(AST.decodeType(getSystem(), type, tupleSize, natural, makeWhere($t)));
                $result.setTupleSize(tupleSize[0]);
                $result.setNatural(natural[0]);
            }
        }
        |
        SHAPE modtype_v3 p=function_parameter_list BECOMES {
            builder.error("Reserved keyword: adjustment", makeWhere($SHAPE));
            $result = null;
        }
        |
        modtype_v3 p=function_parameter_list BECOMES {
            builder.error("Reserved keyword: adjustment", makeWhere($modtype_v3.start));
            $result = null;
        }
        |
        t=USER_STRING modtype_v3 p=function_parameter_list BECOMES {
            builder.error("Reserved keyword: adjustment", makeWhere($t));
            $result = null;
        }
        ;

global_definition_header returns [ASTDefine result]
		:
        fd=function_definition_header {
            if ($fd.result != null) {
                assert($fd.result.getDefineType() == DefineType.Function);
                builder.pushRepContainer(builder.getParamDecls());
            } else {
                // An error occurred
                builder.getParamDecls().getParameters().clear();
                builder.setParamSize(0);
            }
            $result = $fd.result;
        }
        |
       	d=definition_header {
            $result = $d.result;
        }
        ;

definition_header returns [ASTDefine result]
		:
        n=USER_STRING BECOMES {
        	String name = $n.getText();
            $result = builder.makeDefinition(name, false, makeWhere($n));
        }
        | modtype_v3 BECOMES {
            builder.error("Reserved keyword: adjustment", makeWhere($modtype_v3.start));
            $result = null;
        }
        ;

definition returns [ASTDefine result]
        :
        d=definition_header e=exp2 {
        	ASTDefine var = $d.result;
        	ASTExpression exp = $e.result;
        	if (var != null) {
        		if (exp instanceof ASTModification) {
        			ASTModification mod = (ASTModification)exp;
        			mod.getModData().getRand64Seed().getSeed();
        			var.getChildChange().grab(mod);
        		} else {
        			var.setExp(exp);
        		}
        		$result = var;
        	} else {
        		$result = null;
        	}
        }
        ;

shape
        : 
        SHAPE s=USER_STRING parameter_list {
        	String name = $s.getText(); 
			builder.setShape(name, makeWhere($SHAPE));
        }
        ;

shape_singleton_header returns [ASTRule result]
        : 
        s=shape t='{' {
        	builder.setInPathContainer(false);
        	$result = new ASTRule(getSystem(), makeWhere($s.start), -1);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        ; 

shape_singleton returns [ASTRule result]
        :
        s=shape_singleton_header buncha_elements '}' {
        	$result = $s.result;
        	builder.popRepContainer($result);
        	builder.setInPathContainer(false);
        }
        ; 

rule_header_v2 returns [ASTRule result]
        : 
        RULE s=USER_STRING {
        	String name = $s.getText();
        	builder.setShape(null, makeWhere($RULE));
        	$result = new ASTRule(getSystem(), makeWhere($RULE), builder.stringToShape(name, false, makeWhere($RULE)));
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        |
        RULE s=USER_STRING w=user_rational {
        	String name = $s.getText();
        	Float weight = $w.result.getValue();
        	Boolean percentage = $w.result.isPercentage();
        	builder.setShape(null, makeWhere($RULE));
        	$result = new ASTRule(getSystem(), makeWhere($RULE), builder.stringToShape(name, false, makeWhere($RULE)), weight, percentage);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        ;

rule_v2 returns [ASTRule result]
        : 
        h=rule_header_v2 '{' buncha_replacements_v2 '}' {
            builder.setMaybeVersion("CFDG2");
        	$result = $h.result;
        	builder.popRepContainer($h.result);
        }
        ;

rule_header returns [ASTRule result]
        : 
        RULE {
        	builder.setInPathContainer(false);
        	$result = new ASTRule(getSystem(), makeWhere($RULE), -1);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        |
        RULE w=user_rational {
        	builder.setInPathContainer(false);
        	Float weight = $w.result.getValue();
        	Boolean percentage = $w.result.isPercentage();
        	$result = new ASTRule(getSystem(), makeWhere($RULE), -1, weight, percentage);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        ;

path_header returns [ASTRule result]
        : 
        PATH s=USER_STRING parameter_list {
        	String name = $s.getText();
        	builder.setShape(name, true, makeWhere($PATH));
        	builder.setInPathContainer(true);
        	$result = new ASTRule(getSystem(), makeWhere($PATH), -1);
        	$result.setPath(true);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        }
        ;

rule_v3 returns [ASTRule result]
        : 
        h=rule_header '{' buncha_elements '}' {
        	$result = $h.result;
        	builder.popRepContainer($result);
        	builder.setInPathContainer(false);
        }
        ;

path_v3 returns [ASTRule result]
        : 
        h=path_header '{' buncha_elements '}' {
        	$result = $h.result;
        	builder.popRepContainer($result);
        	builder.setInPathContainer(false);
        	builder.setShape(null, makeWhere($h.start));
        }
        ;
        
path_header_v2 returns [ASTRule result]
        : 
        PATH s=USER_STRING {
        	String name = $s.getText();
            builder.setMaybeVersion("CFDG2");
        	builder.setShape(null, makeWhere($PATH));
        	$result = new ASTRule(getSystem(), makeWhere($PATH), builder.stringToShape(name, false, makeWhere($PATH)));
        	$result.setPath(true);
        	builder.addRule($result);
        	builder.pushRepContainer($result.getRuleBody());
        	builder.setInPathContainer(true);
        }
        ;

path_v2 returns [ASTRule result]
		:
        r=path_header_v2 '{' buncha_pathOps_v2 '}' {
            $result = $r.result;
            builder.popRepContainer($result);
        }
        ;

parameter
       : 
       t=USER_STRING v=USER_STRING {
			String type = $t.getText();
			String var = $v.getText();
			builder.nextParameterDecl(type, var, makeWhere($t));
        }
        |
        SHAPE v=USER_STRING {
			String var = $v.getText();
			builder.nextParameterDecl("shape", var, makeWhere($SHAPE));
        }
        |
        v=USER_STRING modtype_v3 {
        	builder.error("Reserved keyword: adjustment", makeWhere($v));
        }
        |
        SHAPE modtype_v3 {
        	builder.error("Reserved keyword: adjustment", makeWhere($SHAPE));
        }
        |
        v=USER_STRING {
			String var = $v.getText();
			builder.nextParameterDecl("number", var, makeWhere($v));
        }
        |
        modtype_v3 {
        	builder.error("Reserved keyword: adjustment", makeWhere($modtype_v3.start));
        }
        ;

buncha_parameters 
        : 
        buncha_parameters ',' parameter 
        | 
        parameter
        ;

parameter_list
        : 
        '(' buncha_parameters ')' {
        }
        |
        ;

function_parameter_list
		:
        '(' buncha_parameters ')'
        | 
        '(' ')'
        ;

parameter_spec returns [ASTExpression result]
        : 
        '(' a=arglist ')' { 
        	$result = $a.result;
        }
        |
        t='(' BECOMES ')' { 
        	$result = new ASTExpression(getSystem(), makeWhere($t), false, false, ExpType.Reuse);
        }
        | '(' ')' { 
        	$result = null; 
        }
        | {
        	$result = null;
        }
        ;

buncha_elements 
        : 
        buncha_elements r=element {
        	builder.pushRep($r.result, false);
        }
        |
        ;

buncha_pathOps_v2 
        : 
        buncha_pathOps_v2 r=pathOp_v2 {
        	builder.pushRep($r.result, false);
        }
        |
        ;

pathOp_simple_v2 returns [ASTReplacement result]
        : 
        o=USER_PATHOP '{' a=buncha_adjustments '}' {
        	String pop = $o.getText();
        	ASTModification mod = $a.result;
            builder.setMaybeVersion("CFDG2");
        	$result = new ASTPathOp(getSystem(), makeWhere($o), pop, mod);
        }
        |
        s=shapeName m=modification_v2 {
        	String cmd = $s.result;
        	ASTModification mod = $m.result;
            builder.setMaybeVersion("CFDG2");
        	$result = new ASTPathCommand(getSystem(), makeWhere($s.start), cmd, mod, null);
        }
        ;

element_simple returns [ASTReplacement result]
        : 
        o=USER_PATHOP '(' e=exp2 ')' {
        	String pop = $o.getText();
        	ASTExpression exp = $e.result;
        	$result = new ASTPathOp(getSystem(), makeWhere($o), pop, exp);
        }
        |
        o=USER_PATHOP '(' ')' {
        	String operator = $o.getText();
        	$result = new ASTPathOp(getSystem(), makeWhere($o), operator, null);
        }
        |
        s=shapeName p=parameter_spec m=modification {
        	String cmd = $s.result;
        	ASTExpression p = $p.result;
        	ASTModification mod = $m.result;
        	$result = builder.makeElement(cmd, mod, p, false, makeWhere($s.start));
        }
        |
        IF '(' e=exp2 ')' m=modification {
        	ASTExpression args = $e.result;
        	ASTModification mod = $m.result;
        	args = builder.makeFunction("if", args, false, makeWhere($IF));
        	$result = builder.makeElement("if", mod, args, false, makeWhere($IF));
        }
        |
        h=letHeader b=letBody m=modification {
        	ASTRepContainer vars = $h.result;
        	ASTExpression exp = $b.result;
        	ASTModification mod = $m.result;
        	exp = builder.makeLet(vars, exp, makeWhere($h.start));
        	$result = builder.makeElement("let", mod, exp, false, makeWhere($m.start));
        }
        |
        PATH n=USER_STRING p=parameter_spec m=modification {
        	String cmd = $n.getText();
        	ASTExpression p = $p.result;
        	ASTModification mod = $m.result;
        	$result = builder.makeElement(cmd, mod, p, true, makeWhere($PATH));
        }
        ;

one_or_more_elements
        : 
        '{' buncha_elements '}' { }
        |
        r=element_simple {
        	builder.pushRep($r.result, false);
        }
        ;

one_or_more_pathOp_v2
        : 
        '{' buncha_pathOps_v2 '}' { }
        |
        r=pathOp_simple_v2 {
        	builder.pushRep($r.result, false);
        }
        ;

caseBody
        : 
        caseBody_element caseBody 
        |
        ;

caseBody_element
        : 
        h=caseHeader one_or_more_elements {
        	builder.popRepContainer(builder.getSwitchStack().lastElement());
        }
        ;

element returns [ASTReplacement result]
        : 
        r=element_simple { 
        	$result = $r.result; 
        }
        |
        definition { 
        	$result = null;
        }
        |
        rl=element_loop { 
        	$result = $rl.result; 
        	builder.popRepContainer($result);
        	if ($result.getRepType().getType() == 0) {
	        	$result = null; 
        	}
        }
        |
        rl=element_loop FINALLY {
        	builder.popRepContainer($rl.result);
        	builder.pushRepContainer(((ASTLoop) $rl.result).getFinallyBody());
        } one_or_more_elements {
        	builder.popRepContainer($result);
        	$result = $rl.result; 
        	if ($result.getRepType().getType() == 0) {
	        	$result = null; 
        	}
        }
        |
        ri=ifHeader one_or_more_elements {
        	$result = $ri.result; 
        	builder.popRepContainer($result);
        	if ($result.getRepType().getType() == 0) {
	        	$result = null; 
        	}
        }
        |
        rei=ifElseHeader one_or_more_elements {
        	$result = $rei.result; 
        	builder.popRepContainer($result);
        	if ($result.getRepType().getType() == 0) {
	        	$result = null; 
        	}
        }
        |
        rt=transHeader one_or_more_elements {
        	$result = $rt.result; 
        	builder.popRepContainer($result);
        	if ($result.getRepType().getType() == 0) {
	        	$result = null; 
        	}
        }
        |
        rs=switchHeader '{' caseBody '}' {
        	$result = $rs.result; 
			$rs.result.unify();
        	builder.getSwitchStack().pop();
        }
        |
	    element_v2clue .*? {
            builder.error("Illegal mixture of old and new elements", makeWhere($element_v2clue.start));
            $result = null;
        }
        ;

element_v2clue
		:
        user_rational '*'
        | USER_STRING '{'
        | USER_PATHOP '{'
        ;
        
pathOp_v2 returns [ASTReplacement result]
        : 
        rp=pathOp_simple_v2 { 
        	$result = $rp.result;
        }
        |
        rl=loopHeader_v2 one_or_more_pathOp_v2 { 
        	$result = $rl.result;
			builder.popRepContainer($result);
			if ($result.getRepType().getType() == 0) {
				$result = null;			
			}
        }
        | pathOp_v3clues .*? {
            if ("CFDG2".equals(builder.getMaybeVersion())) {
                builder.error("Illegal mixture of old and new elements", makeWhere($pathOp_v3clues.start));
            } else {
                builder.setMaybeVersion("CFDG3");
            }
            $result = null;
        }
        ;

pathOp_v3clues
		:
        USER_PATHOP '('
        | USER_STRING '('
        | PATH
        | LOOP
        | USER_STRING BECOMES
        | modtype_v3 BECOMES
        | IF
        | modtype_v3
        | SWITCH
        ;

element_loop returns [ASTLoop result]
        : 
        h=loopHeader m=modification one_or_more_elements {
        	// parse loop mod and loop body with loop index in scope
        	$h.result.setLoopModHolder($m.result);
        	$result = $h.result;
        }
        ;

buncha_replacements_v2 
        : 
        r=replacement_v2 buncha_replacements_v2 {
        	builder.pushRep($r.result, false);
        }
        |
        ;

one_or_more_replacements_v2
        : 
        '{' buncha_replacements_v2 '}' { 
        }
        |
        r=replacement_simple_v2 {
        	builder.pushRep($r.result, false);
        }
        ;

replacement_simple_v2 returns [ASTReplacement result]
        : 
        s=shapeName m=modification_v2 {
        	String name = $s.result;
        	ASTModification mod = $m.result;
        	ASTRuleSpecifier r = builder.makeRuleSpec(name, null, null, false, makeWhere($s.start));
        	$result = new ASTReplacement(getSystem(), makeWhere($s.start), r, mod, RepElemType.replacement);
        }
        ;

replacement_v2 returns [ASTReplacement result]
        : 
        r=replacement_simple_v2 { 
        	$result = $r.result;
        }
        |
        rl=loopHeader_v2 one_or_more_replacements_v2 {
        	$result = $rl.result;
			builder.popRepContainer($result);
			if ($result.getRepType().getType() == 0) {
	        	$result = null;			
			}
        }
        ;

loopHeader_v2 returns [ASTLoop result]
        : 
        r=user_rational '*' {
        	builder.incLocalStackDepth();
        } m=modification_v2 {
        	ASTExpression count = new ASTReal(getSystem(), makeWhere($r.start), $r.result.getValue());
        	ASTModification mod = $m.result;
        	builder.decLocalStackDepth();
            builder.setMaybeVersion("CFDG2");
            String dummyvar = "~~inaccessiblevar~~";
        	$result = new ASTLoop(getSystem(), makeWhere($m.start), builder.stringToShape(dummyvar, false, makeWhere($m.start)), dummyvar, count, mod);
        	builder.pushRepContainer($result.getLoopBody());
        }
        ;

loopHeader returns [ASTLoop result]
        : 
        LOOP v=USER_STRING BECOMES i=exp2 {
        	String var = $v.getText();
        	ASTExpression index = $i.result;
        	$result = new ASTLoop(getSystem(), makeWhere($LOOP), builder.stringToShape(var, false, makeWhere($LOOP)), var, index, null);
        	builder.pushRepContainer($result.getLoopBody());
        }
        |
        LOOP modtype_v3 BECOMES c=exp2 {
        	ASTExpression index = $c.result;
            String dummyvar = "~~inaccessiblevar~~";
        	$result = new ASTLoop(getSystem(), makeWhere($LOOP), builder.stringToShape(dummyvar, false, makeWhere($LOOP)), dummyvar, index, null);
        	builder.pushRepContainer($result.getLoopBody());
        }
        |
        LOOP c=exp2 {
        	ASTExpression count = $c.result;
            String dummyvar = "~~inaccessiblevar~~";
        	$result = new ASTLoop(getSystem(), makeWhere($LOOP), builder.stringToShape(dummyvar, false, makeWhere($LOOP)), dummyvar, count, null);
        	builder.pushRepContainer($result.getLoopBody());
        }
        ;

ifHeader returns [ASTIf result]
        : 
        c=IF '(' e=exp2 ')' {
        	ASTExpression cond = $e.result;
        	$result = new ASTIf(getSystem(), makeWhere($c), cond);
        	builder.pushRepContainer($result.getThenBody());
        }
        ;

ifElseHeader returns [ASTIf result]
        : 
        h=ifHeader one_or_more_elements ELSE {
        	$result = $h.result;
        	builder.popRepContainer($result);
        	builder.pushRepContainer($result.getElseBody());
        }
        ;

transHeader returns [ASTTransform result]
        : 
        t=modtype_v3 e=exp2 {
        	ASTExpression exp = $e.result;
        	if (!$t.result.equals(ModType.transform.name())) {
        		builder.error("Syntax error", makeWhere($t.start));
        	} 
        	$result = new ASTTransform(getSystem(), makeWhere($t.start), exp);
        	builder.pushRepContainer($result.getBody());
        }
        |
        CLONE e=exp2 {
        	ASTExpression exp = $e.result;
        	$result = new ASTTransform(getSystem(), makeWhere($CLONE), exp);
        	builder.pushRepContainer($result.getBody());
        	$result.setClone(true);
        }
        ;

switchHeader returns [ASTSwitch result]
        : 
        SWITCH '(' e=exp2 ')' {
        	ASTExpression caseVal = $e.result;
            $result = new ASTSwitch(getSystem(), makeWhere($SWITCH), caseVal);
            builder.getSwitchStack().push($result);
        }
        ;

caseHeader returns [Integer result]
        :  
        CASE e=exp2 ':' {
        	ASTExpression valExp = $e.result;
        	ASTSwitch caseSwitch = builder.getSwitchStack().peek();
            ASTRepContainer caseBody = new ASTRepContainer(getSystem(), makeWhere($CASE));
            builder.pushRepContainer(caseBody);
            caseSwitch.appendCase(valExp, caseBody);
            $result = 0;
        }
        |
        ELSE ':' {
            if (!builder.getSwitchStack().peek().getElseBody().getBody().isEmpty()) {
                builder.error("There can only be one 'else:' clause", makeWhere($ELSE));
            } else {
                builder.pushRepContainer(builder.getSwitchStack().peek().getElseBody());
            }
            $result = 0;
        }
        ;

modification returns [ASTModification result]
        :
        t='[' m=buncha_adjustments ']' {
        	$result = builder.makeModification($m.result, true, makeWhere($t));
        }
        |
        t='[' '[' m=buncha_adjustments ']' ']' {
        	$result = builder.makeModification($m.result, false, makeWhere($t));
        }
        ;

modification_v2 returns [ASTModification result]
        : 
        t='{' m=buncha_adjustments_v2 '}' {
        	$result = builder.makeModification($m.result, true, makeWhere($t));
        }
        |
        t='[' m=buncha_adjustments_v2 ']' {
        	$result = builder.makeModification($m.result, false, makeWhere($t));
        }
        ;

buncha_adjustments returns [ASTModification result]
        : 
        a2=buncha_adjustments a1=adjustment {
        	builder.makeModTerm($a2.result, $a1.result, makeWhere($a1.start));
        	$result = $a2.result;
        }
        | {
			$result = new ASTModification(getSystem(), (ASTWhere)null);
        }
        ;

buncha_adjustments_v2 returns [ASTModification result]
        :
        a2=buncha_adjustments_v2 a1=adjustment_v2 {
        	builder.makeModTerm($a2.result, $a1.result, makeWhere($a1.start));
        	$result = $a2.result;
        }
        | {
			$result = new ASTModification(getSystem(), (ASTWhere)null);
        }
        ;

adjustment returns [ASTModTerm result]
        : 
        t=modtype_v3 el=explist {
        	$result = new ASTModTerm(getSystem(), makeWhere($t.start), ModType.byName($t.result), $el.result);
        }
        |
        t=modtype_v3 e=exp '|' {
        	ModType type = ModType.byName($t.result);
        	if (type.getType() < ModType.hue.getType() || type.getType() > ModType.alpha.getType()) {
        		builder.error("The target operator can only be applied to color adjustments", makeWhere($t.start));
        		$result = null;
        	} else {
	        	$result = new ASTModTerm(getSystem(), makeWhere($t.start), ModType.fromType(type.getType() + 4), $e.result);
        	}
        }
        |
        PARAM p=USER_STRING {
        	$result = new ASTModTerm(getSystem(), makeWhere($PARAM), ModType.param, $p.getText());
        }
        |
        PARAM p=USER_QSTRING {
        	$result = new ASTModTerm(getSystem(), makeWhere($PARAM), ModType.param, $p.getText().substring(1, $p.getText().length() - 1));
        }
        ;

adjustment_v2 returns [ASTModTerm result]
        :
        t=modtype_v2 el=explist {
        	$result = new ASTModTerm(getSystem(), makeWhere($t.start), ModType.byName($t.result), $el.result);
        }
        |
        t=modtype_v2 e=exp '|' {
        	ModType type = ModType.byName($t.result);
        	if (type.getType() < ModType.hue.getType() || type.getType() > ModType.alpha.getType()) {
        		builder.error("The target operator can only be applied to color adjustments", makeWhere($t.start));
        		$result = null;
        	} else {
	        	$result = new ASTModTerm(getSystem(), makeWhere($t.start), ModType.fromType(type.getType() + 4), $e.result);
        	}
        }
        |
        PARAM p=USER_STRING {
        	$result = new ASTModTerm(getSystem(), makeWhere($PARAM), ModType.param, $p.getText());
        }
        |
        PARAM p=USER_QSTRING {
        	$result = new ASTModTerm(getSystem(), makeWhere($PARAM), ModType.param, $p.getText().substring(1, $p.getText().length() - 1));
        }
        ;

letHeader returns [ASTRepContainer result]
		:
        LET {
            $result = new ASTRepContainer(getSystem(), makeWhere($LET));
            builder.pushRepContainer($result);
        }
        ;
        
letBody returns [ASTExpression result]
		:
        '(' letVariables ';' e=exp2 ')' {
            $result = $e.result;
        }
        ;
        
letVariables
		:
        letVariables ';' letVariable
        |
        letVariable
        ;
        
letVariable returns [ASTDefine result]
		:
        r=definition {
            builder.pushRep($r.result, false);
        }
        ;
        
explist returns [ASTExpression result]
        :
        el=explist e=exp {
            $result = $el.result.append($e.result);
        }
        |
        e=exp {
        	$result = $e.result;
        }
        ;

arglist returns [ASTExpression result]
        :
        el=arglist ',' e=exp3 {
            $result = $el.result.append($e.result);
        }
        |
        e=exp3 {
        	$result = new ASTCons(getSystem(), makeWhere($e.start), new ASTParen(getSystem(), makeWhere($e.start), $e.result));
        }
        ;

exp returns [ASTExpression result]
        :
        (
        n=user_rational {
			$result = new ASTReal(getSystem(), makeWhere($n.start), $n.result.getValue());
        }
        |
        CF_INFINITY { 
			$result = new ASTReal(getSystem(), makeWhere($CF_INFINITY), Float.MAX_VALUE);
        }
        |
        s=USER_STRING '(' a=arglist ')' {
        	String func = $s.getText();
        	ASTExpression args = $a.result;
        	$result = builder.makeFunction(func, args, false, makeWhere($s));
        }
        |
        f=expfunc {
			$result = $f.result; 
        }
        |
        t='(' x=exp2 ')' {
			$result = new ASTParen(getSystem(), makeWhere($t), $x.result);
        }
        |
        t='-' e=exp {
			$result = new ASTOperator(getSystem(), makeWhere($t), 'N', $e.result);
        }
        |
        t='+' e=exp { 
			$result = new ASTOperator(getSystem(), makeWhere($t), 'P', $e.result);
        }
        )
        (
        RANGE r=exp {
        	ASTExpression pair = $result.append($r.result);
        	$result = new ASTFunction(getSystem(), $result.getWhere(), "rand", pair, builder.getSeed());
        }
        |
        PLUSMINUS r=exp {
        	ASTExpression pair = $result.append($r.result);
        	$result = new ASTFunction(getSystem(), $result.getWhere(), "rand+/-", pair, builder.getSeed());
        }
        )?
        ;

exp2 returns [ASTExpression result]	
        :
        l=exp2 ',' r=exp3 {
            $result = $l.result.append($r.result);
        }
        |
        e=exp3 {
        	$result = $e.result;
        }
        ;

exp3 returns [ASTExpression result]	
        :
        (
        n=user_rational {
			$result = new ASTReal(getSystem(), makeWhere($n.start), $n.result.getValue());
        }
        |
        CF_INFINITY { 
			$result = new ASTReal(getSystem(), makeWhere($CF_INFINITY), Float.MAX_VALUE);
        }
        |
        s=USER_STRING '(' a=arglist ')' {
        	String func = $s.getText();
        	ASTExpression args = $a.result;
        	$result = builder.makeFunction(func, args, false, makeWhere($s));
        }
        |
        f=expfunc {
        	$result = $f.result;
        }
        |
        t='(' x=exp2 ')' {
			$result = new ASTParen(getSystem(), makeWhere($t), $x.result);
        }
        |
        t='-' e=exp3 {
			$result = new ASTOperator(getSystem(), makeWhere($t), 'N', $e.result);
        }
        |
        t='+' e=exp3 { 
			$result = new ASTOperator(getSystem(), makeWhere($t), 'P', $e.result);
        }
        |
        t=NOT e=exp3 { 
			$result = new ASTOperator(getSystem(), makeWhere($t), '!', $e.result);
        }
        |
        m=modification {
        	$result = $m.result;
        }
        )
        (
        '+' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '+', $result, $r.result);
        }
        |
        '-' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '-', $result, $r.result);
        }
        |
        '_' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '_', $result, $r.result);
        }
        |
        '*' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '*', $result, $r.result);
        }
        |
        '/' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '/', $result, $r.result);
        }
        |
        '^' r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '^', $result, $r.result);
        }
        |
        LT r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '<', $result, $r.result);
        }
        |
        GT r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '>', $result, $r.result);
        }
        |
        LE r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), 'L', $result, $r.result);
        }
        |
        GE r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), 'G', $result, $r.result);
        }
        |
        EQ r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '=', $result, $r.result);
        }
        |
        NEQ r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), 'n', $result, $r.result);
        }
        |
        AND r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '&', $result, $r.result);
        }
        |
        OR r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), '|', $result, $r.result);
        }
        |
        XOR r=exp3 {
        	$result = new ASTOperator(getSystem(), $result.getWhere(), 'X', $result, $r.result);
        }
        |
        RANGE r=exp3 {
        	ASTExpression pair = $result.append($r.result);
        	$result = new ASTFunction(getSystem(), $result.getWhere(), "rand", pair, builder.getSeed());
        }
        |
        PLUSMINUS r=exp3 {
        	ASTExpression pair = $result.append($r.result);
        	$result = new ASTFunction(getSystem(), $result.getWhere(), "rand+/-", pair, builder.getSeed());
        }
        )?
        ;

expfunc returns [ASTExpression result]
        : 
        f=USER_STRING '(' ')' { 
        	String func = $f.getText();
        	$result = builder.makeFunction(func, null, false, makeWhere($f));
        }
        |
        f=USER_ARRAYNAME '(' e=exp2 ')' { 
        	String func = $f.getText();
        	ASTExpression args = $e.result;
        	$result = builder.makeArray(func, args, makeWhere($f));
        }
        |
        IF '(' e=exp2 ')' { 
        	ASTExpression args = $e.result;
        	$result = builder.makeFunction("if", args, false, makeWhere($IF));
        }
        |
        f=USER_STRING '(' BECOMES ')' { 
        	String func = $f.getText();
        	ASTExpression args = new ASTExpression(getSystem(), makeWhere($f), false, false, ExpType.Reuse);
        	$result = builder.makeArray(func, args, makeWhere($f));
        }
        |
        h=letHeader b=letBody {
        	builder.popRepContainer(null);
        	$result = builder.makeLet($h.result, $b.result, makeWhere($h.start));
        }
        |
        v=USER_STRING { 
        	String var = $v.getText();
        	$result = builder.makeVariable(var, makeWhere($v));
        }
        ;
        
shapeName returns [String result]
		:
        r=USER_STRING { 
        	$result = $r.getText();
        }
        |
        r=USER_ARRAYNAME { 
        	$result = $r.getText();
        }
        ;

modtype_v2 returns [String result]
	:
	t=(TIME | TIMESCALE | ROTATE | FLIP | BLEND | HUE | SATURATION | BRIGHTNESS | ALPHA | TRANSFORM | X | Y | Z | SIZE | SKEW | TARGETHUE | TARGETSATURATION | TARGETBRIGHTNESS | TARGETALPHA | WIDTH | X1 | X2 | Y1 | Y2 | RX | RY) {
	    $result = $t.getText();
	}
	;

modtype_v3 returns [String result]
	:
	t=(TIME | TIMESCALE | ROTATE | FLIP | BLEND | HUE | SATURATION | BRIGHTNESS | ALPHA | TRANSFORM | X | Y | Z | SIZE | SKEW | TARGETHUE | TARGETSATURATION | TARGETBRIGHTNESS | TARGETALPHA) {
	    $result = $t.getText();
	}
	;

user_rational returns [ASTValue result]
    :
    t=(INTEGER | RATIONAL | FLOAT) {
        $result = new ASTValue(getSystem(), makeWhere($t), $t.getText());
    }
    ;

CFDG2
    :
    'CFDG2'
    ;

CFDG3
    :
    'CFDG3'
    ;

STARTSHAPE
	: 
	'startshape' 
	;

BACKGROUND
	: 
	'background' 
	;

INCLUDE
	: 
	'include' 
	;

IMPORT
	: 
	'import' 
	;

TILE
	: 
	'tile' 
	;

RULE
	: 
	'rule' 
	;

PATH
	: 
	'path' 
	;

SHAPE
	: 
	'shape' 
	;

LOOP
	: 
	'loop' 
	;

FINALLY
	: 
	'finally' 
	;

IF
	: 
	'if' 
	;

ELSE
	: 
	'else' 
	;

SWITCH
	: 
	'switch' 
	;

CASE
	: 
	'case' 
	;

RANGE
	:
	'..' | '\u2026'
	;

PLUSMINUS
	: 
	'+/-' | '\u00b1'
	;

TIME
	: 
	'time' 
	;

TIMESCALE
	: 
	'timescale' 
	;

X
	: 
	'x' 
	;

Y
	: 
	'y' 
	;

Z
	: 
	'z' 
	;
	
ROTATE
	: 
	'rotate' | 'r' 
	;

SIZE
	: 
	'size' | 's' 
	;
	
SKEW
	: 
	'skew' 
	;

FLIP
	: 
	'flip' | 'f' 
	;

BLEND
	:
	'blend'
	;

HUE
	: 
	'hue' | 'h' 
	;

SATURATION
	: 
	'saturation' | 'sat'
	;

BRIGHTNESS
	: 
	'brightness' | 'b' 
	;

ALPHA
	: 
	'alpha' | 'a' 
	;

TARGETHUE
	: 
	'|hue' | '|h' 
	;

TARGETSATURATION
	: 
	'|saturation' | '|sat' 
	;

TARGETBRIGHTNESS
	: 
	'|brightness' | '|b' 
	;

TARGETALPHA
	: 
	'|alpha' | '|a' 
	;

X1
	: 
	'x1' 
	;

X2
	: 
	'x2' 
	;

Y1
	: 
	'y1' 
	;

Y2
	: 
	'y2' 
	;

RX
	: 
	'rx' 
	;

RY
	: 
	'ry' 
	;

WIDTH
	: 
	'width' 
	;

TRANSFORM
	: 
	'transform' | 'trans' 
	;

PARAM
	: 
	'param' | 'p' 
	;
	
BECOMES
	: 
	'=' 
	;

LT
	: 
	'<' 
	;

GT
	: 
	'>' 
	;

LE
	: 
	'<=' | '\u2264'
	;

GE
	: 
	'>=' | '\u2265' 
	;

EQ
	: 
	'==' 
	;

NEQ
	: 
	'<>' | '\u2276'
	;

NOT
	: 
	'!' 
	;

AND
	: 
	'&&' 
	;

OR
	: 
	'||' 
	;

XOR
	: 
	'^^' 
	;

CF_INFINITY
	: 
	'CF_INFINITY' | '\u221E'
	;
	
USER_PATHOP
	: 
	'MOVETO'
	| 
	'LINETO'
	| 
	'ARCTO'
	| 
	'CURVETO'
	| 
	'MOVEREL'
	| 
	'LINEREL'
	| 
	'ARCREL'
	| 
	'CURVEREL'
	| 
	'CLOSEPOLY' 
	;

CLONE 
	:
	'clone'
	;

LET 
	:
	'LET'
	;
	
INTEGER
	:
	('0'..'9')+ '%'?
	;

RATIONAL
	:
	('0'..'9')+ '.' ('0'..'9')+ '%'? | '.' ('0'..'9')+ '%'?
	;

FLOAT
	:
	('0'..'9')+ '.' ('0'..'9')+ ('e'|'E') ('+|-')? ('0'..'9')+ '%'? | '.' ('0'..'9')+ ('e'|'E') ('+|-')? ('0'..'9')+ '%'? | ('0'..'9')+ ('e'|'E') ('+|-')? ('0'..'9')+ '%'?
	;

USER_STRING
	: 
	('a'..'z'|'A'..'Z'|'_'|'\u0200'..'\u0301'|'\u0303'..'\u0377') (('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'::'|'\u0200'..'\u0301'|'\u0303'..'\u0377') | ('\u0302'('\u0200'..'\u0260'|'\u0262'..'\u0377')))*
	;

USER_QSTRING
	:
	'"' USER_STRING '"'
	;

USER_ARRAYNAME
	:
	('a'..'z'|'A'..'Z'|'_'|'\u0200'..'\u0301'|'\u0303'..'\u0377') (('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'\u0200'..'\u0301'|'\u0303'..'\u0377') | ('\u0302'('\u0200'..'\u0260'|'\u0262'..'\u0377')))*
	;

USER_FILENAME
	:
	('a'..'z'|'A'..'Z'|'\u0200'..'\u0377') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'-'|'\u0200'..'\u0377'|'.')* ('.cfdg')
	;

USER_QFILENAME
	:
	'"' USER_FILENAME '"'
	;

COMMENT
	: 
	('//' ~('\n'|'\r')* '\r'? '\n' {} | '/*' (.)*? '*/' {}) -> skip 
	;

WHITESPACE  
	: 
	( ' ' | '\t' | '\r' | '\n' ) -> skip
	;
