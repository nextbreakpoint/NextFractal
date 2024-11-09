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
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStack;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.CFStackRule;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ArgSource;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.CompilePhase;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.ExpType;
import com.nextbreakpoint.nextfractal.contextfree.dsl.parser.enums.Locality;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

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

public class ASTRuleSpecifier extends ASTExpression {
	@Getter
	@Setter
    private int shapeType;
	@Getter
	private int argSize;
    private final StringBuilder entropy;
	@Getter
	@Setter
    private ArgSource argSource;
	@Getter
	private ASTExpression arguments;
	@Getter
	@Setter
    private CFStackRule simpleRule;
	@Getter
	private int stackIndex;
	@Getter
	@Setter
    private List<ASTParameter> typeSignature;
	@Getter
	@Setter
    private List<ASTParameter> parentSignature;
	@Getter
	private ASTParameter bound;

	public ASTRuleSpecifier(CFDGSystem system, ASTWhere where) {
		super(system, where, false, false, ExpType.Rule);
		this.shapeType = -1;
		this.argSize = 0;
		this.entropy = new StringBuilder();
		this.argSource = ArgSource.NoArgs;
		this.arguments = null;
		this.simpleRule = null;
		this.stackIndex = 0;
		this.typeSignature = null;
		this.parentSignature = null;
		this.bound = null;
	}

	public ASTRuleSpecifier(CFDGSystem system, ASTWhere where, int nameIndex, String name, ASTExpression arguments, List<ASTParameter> parent) {
		super(system, where, arguments == null || arguments.isConstant(), false, ExpType.Rule);
		this.shapeType = nameIndex;
		this.entropy = new StringBuilder(name);
		this.argSource = ArgSource.DynamicArgs;
		this.arguments = arguments;
		this.simpleRule = null;
		this.stackIndex = 0;
		this.typeSignature = null;
		this.parentSignature = parent;
		this.bound = null;
		if (parentSignature != null && parentSignature.isEmpty()) {
			parentSignature = null;
		}
	}

	public ASTRuleSpecifier(CFDGSystem system, ASTWhere where, int nameIndex, String name) {
		super(system, where, false, false, ExpType.Rule);
		this.shapeType = nameIndex;
		this.argSize = 0;
		this.entropy = new StringBuilder(name);
		this.argSource = ArgSource.StackArgs;
		this.arguments = null;
		this.simpleRule = null;
		this.stackIndex = 0;
		this.typeSignature = null;
		this.parentSignature = null;
		this.bound = null;
	}

	public ASTRuleSpecifier(CFDGSystem system, ASTWhere where, ASTExpression args) {
		super(system, where, false, false, ExpType.Rule);
		this.shapeType = -1;
		this.argSize = 0;
		this.entropy = new StringBuilder();
		this.argSource = ArgSource.ShapeArgs;
		this.arguments = args;
		this.simpleRule = null;
		this.stackIndex = 0;
		this.typeSignature = null;
		this.parentSignature = null;
		this.bound = null;
	}

	public ASTRuleSpecifier(CFDGSystem system, ASTRuleSpecifier spec) {
		super(system, spec.getWhere(), spec.constant, false, spec.type);
		this.argSize = spec.argSize;
		this.entropy = spec.entropy;
		this.argSource = spec.argSource;
		this.arguments = spec.arguments;
		this.simpleRule = spec.simpleRule;
		this.stackIndex = spec.stackIndex;
		this.typeSignature = spec.typeSignature;
		this.parentSignature = spec.parentSignature;
		this.bound = spec.bound;
	}

    public void grab(ASTRuleSpecifier src) {
		constant = true;
		shapeType = src.getShapeType();
		argSize = 0;
		argSource = src.getArgSource();
		arguments = null;
		simpleRule = src.getSimpleRule();
		stackIndex = 0;
		typeSignature = src.getTypeSignature();
		parentSignature = src.getParentSignature();
	}

	@Override
	public CFStackRule evalArgs(CFDGBuilder builder, CFDGRenderer renderer, CFStackRule parent) {
		switch (argSource) {
			case NoArgs:
			case SimpleArgs: {
				return simpleRule;
			}
			case StackArgs: {
				return (CFStackRule) renderer.getStackItem(stackIndex);
			}
			case ParentArgs: {
				if (shapeType != parent.getRuleName()) {
					// Child shape is different fromType parent, even though parameters are reused,
					// and we can't finesse it in ASTreplacement::traverse(). Just
					// copy the parameters with the correct shape type.
                    return CFStack.createStackRule(parent, shapeType);
				}
			}
			case SimpleParentArgs: {
				return parent;
			}
			case DynamicArgs: {
				CFStackRule ret = CFStack.createStackRule(shapeType, argSize, typeSignature);
				ret.evalArgs(builder, renderer, arguments, typeSignature, false);
				return ret;
			}
			case ShapeArgs: {
				return arguments.evalArgs(builder, renderer, parent);
			}
			default: {
				return null;
			}
		}
	}

	@Override
	public int evaluate(CFDGBuilder builder, CFDGRenderer renderer, double[] result, int length) {
		system.error("Improper evaluation of a rule specifier", getWhere());
		return -1;
	}

	@Override
	public void entropy(StringBuilder entropy) {
		entropy.append(this.entropy);
	}

	@Override
	public ASTExpression simplify(CFDGBuilder builder) {
		if (arguments != null) {
			if (arguments instanceof ASTCons args) {
                for (int i = 0; i < args.getChildren().size(); i++) {
					args.setChild(i, ASTExpression.simplify(builder, args.getChild(i)));
				}
			} else {
				arguments = ASTExpression.simplify(builder, arguments);
			}
		}
		if (argSource == ArgSource.StackArgs) {
			if (bound.getType() != ExpType.Rule) {
				return null;
			}
			if (bound.getStackIndex() == -1) {
				if (bound.getDefinition() == null || bound.getDefinition().getExp() == null) {
					system.error("Error processing shape variable.", getWhere());
					return null;
				}
				if (bound.getDefinition().getExp() instanceof ASTRuleSpecifier r) {
					// The source ASTruleSpec must already be type-checked
					// because it is lexically earlier
					shapeType = r.getShapeType();
					argSize = r.getArgSize();
					argSource = r.getArgSource();
					arguments = null;
					simpleRule = r.getSimpleRule();
					typeSignature = r.getTypeSignature();
					parentSignature = r.getParentSignature();
					constant = true;
					locality = Locality.PureLocal;
				} else {
					system.error("Error processing shape variable.", getWhere());
				}
			}
		}
		if (argSource == ArgSource.DynamicArgs && constant) {
			simpleRule = evalArgs(builder, null, null);
			argSource = ArgSource.SimpleArgs;
		}
		return null;
	}

	@Override
	public ASTExpression compile(CFDGBuilder builder, CompilePhase phase) {
		arguments = ASTExpression.compile(builder, phase, arguments);

        switch (phase) {
            case TypeCheck -> {
                switch (argSource) {
                    case ShapeArgs -> {
						if (arguments == null) {
							system.error("Error in shape specification", getWhere());
						}
                        if (arguments.getType() == ExpType.Rule) {
                            system.error("Expression does not return a shape", arguments.getWhere());
                        }
                        constant = false;
                        locality = arguments.getLocality();
                        arguments.entropy(entropy);
                        return null;
                    }
                    case SimpleParentArgs -> {
						if (Objects.equals(typeSignature, parentSignature) || arguments == null || arguments.getType() != ExpType.Reuse) {
							system.error("Error reusing parent shape's parameters", getWhere());
							return null;
						}
                        constant = true;
                        locality = Locality.PureLocal;
                        return null;
                    }
                    case StackArgs -> {
						final boolean[] isGlobal = new boolean[] { false };
						final ASTParameter tempBound = builder.findExpression(shapeType, isGlobal);
						if (tempBound == null) {
							system.error("Shape name does not bind to a rule variable", getWhere());
							return null;
						}
						bound = tempBound;
                        if (bound.getType() != ExpType.Rule) {
                            system.error("Shape name does not bind to a rule variable", getWhere());
                            system.error("this is what it binds to", bound.getWhere());
                        }
                        if (bound.getStackIndex() != -1) {
							stackIndex = bound.getStackIndex() - (isGlobal[0] ? 0 : builder.getLocalStackDepth());
                            constant = false;
                            locality = bound.getLocality();
                        }
                        if (arguments != null && arguments.getType() != ExpType.None) {
                            system.error("Can't bind parameters twice", arguments.getWhere());
                        }
                        return null;
                    }
                    case NoArgs -> {
                        assert (arguments == null || arguments.getType() == ExpType.None);
                        constant = true;
                        locality = Locality.PureLocal;
                        return null;
                    }
					case SimpleArgs, ParentArgs -> {
						//do nothing
						return null;
					}
                    case DynamicArgs -> {
                        final ASTDefine[] func = new ASTDefine[] {null};
                        final List<ASTParameter>[] signature = new List[] {null};
                        //TODO do we need to call getTypeInfo?
                        final String name = builder.getTypeInfo(shapeType, func, signature);
                        typeSignature = signature[0];
                        if (typeSignature != null && typeSignature.isEmpty()) {
                            typeSignature = null;
                        }
                        if (func[0] != null) {
                            if (func[0].getExpType() == ExpType.Rule) {
                                argSource = ArgSource.ShapeArgs;
                                arguments = new ASTUserFunction(system, getWhere(), shapeType, arguments, func[0]);
                                arguments = compile(builder, phase);
                                constant = false;
                                locality = arguments.getLocality();
                            } else {
                                system.error("Function does not return a shape", arguments != null ? arguments.getWhere() : getWhere());
                            }
                            if (arguments != null) {
                                arguments.entropy(entropy);
                            }
                            return null;
                        }

						final boolean[] isGlobal = new boolean[] { false };
						final ASTParameter tempBound = builder.findExpression(shapeType, isGlobal);
						if (tempBound != null) {
							bound = tempBound;
						}
                        if (bound != null && bound.getType() == ExpType.Rule) {
                            // Shape was a stack variable but the variable type
                            // was not known to be a ruleSpec until now. Convert
                            // to a StackArgs and recompile as such.
                            argSource = ArgSource.StackArgs;
                            compile(builder, phase);
                            return null;
                        }

                        if (arguments != null && arguments.getType() == ExpType.Reuse) {
                            argSource = ArgSource.ParentArgs;
							if (typeSignature == null || parentSignature == null) {
								system.error("Parameter reuse only allowed when shape has parameters to reuse.", getWhere());
							} else if (Objects.equals(typeSignature, parentSignature)) {
                                final Iterator<ASTParameter> paramIt = typeSignature.iterator();
								final Iterator<ASTParameter> parentIt = parentSignature.iterator();
                                ASTParameter param;
                                ASTParameter parent;
                                while (paramIt.hasNext() && parentIt.hasNext()) {
                                    param = paramIt.next();
                                    parent = parentIt.next();
                                    if (param != parent) {
                                        system.error("Parameter reuse only allowed when type signature is identical", getWhere());
                                        system.error("target shape parameter type", param.getWhere());
                                        system.error("does not equal source shape parameter type", parent.getWhere());
										break;
                                    }
                                }
                                if (!paramIt.hasNext() && parentIt.hasNext()) {
									parent = parentIt.next();
                                    system.error("Source shape has more parameters than target shape.", getWhere());
                                    system.error("extra source parameters start here", parent.getWhere());
                                }
                                if (paramIt.hasNext() && !parentIt.hasNext()) {
									param = paramIt.next();
                                    system.error("Target shape has more parameters than source shape.", getWhere());
                                    system.error("extra target parameters start here", param.getWhere());
                                }
                            }
                            constant = true;
                            locality = Locality.PureLocal;
                            return null;
                        }

                        argSize = ASTParameter.checkType(builder, getWhere(), typeSignature, arguments, true);
                        if (argSize < 0) {
                            argSource = ArgSource.NoArgs;
                            return null;
                        }

                        if (arguments != null && arguments.getType() != ExpType.None) {
                            if (arguments.isConstant()) {
                                constant = true;
                                locality = Locality.PureLocal;
                            } else {
                                constant = false;
                                locality = arguments.getLocality();
                            }
                            arguments.entropy(entropy);
                        } else {
                            argSource = ArgSource.NoArgs;
                            simpleRule = CFStack.createStackRule(shapeType, 0, null);
                            constant = true;
                            locality = Locality.PureLocal;
                        }
                    }
                    default -> {
                    }
                }
            }
			case Simplify -> {
				if (argSource == ArgSource.StackArgs) {
					if (bound.getStackIndex() == -1) {
						if (bound.getDefinition() == null) {
							system.error("Error processing shape variable.", where);
							return null;
						}
						if (!(bound.getDefinition().getExp() instanceof ASTRuleSpecifier)) {
							system.error("Error processing shape variable.", where);
							return null;
						}
						grab((ASTRuleSpecifier) bound.getDefinition().getExp());
						locality = Locality.PureLocal;
					}
				}

				// do nothing
			}
            default -> {
            }
        }
		return null;
	}

	public String getEntropy() {
		return entropy.toString();
	}

	public void setEntropy(String name) {
		entropy.setLength(0);
		entropy.append(name);
	}
}
