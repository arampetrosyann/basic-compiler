package compiler.Components.Blocks;

public class ASTPrinter {

    private int indentLevel = 0;

    public void printAST(ASTNodeImpl root) {
        if (root instanceof Block block) {
            for (Statement stmt : block.getStatements()) {
                print((ASTNodeImpl) stmt);
            }
        } else {
            print(root);
        }
    }

    public void print(ASTNodeImpl node) {
        switch (node) {
            case Block block -> {
                printIndented("Block");
                indent(() -> {
                    for (Statement stmt : block.getStatements()) {
                        print((ASTNodeImpl) stmt);
                    }
                });
            }
            case Method method -> {
                printIndented("Method, " + method.getName());
                indent(() -> {
                    if (method.getReturnType() != null) {
                        printIndented("Type, " + method.getReturnType().getIdentifier());
                    } else {
                        printIndented("ReturnType, void");
                    }
                    if(!method.getParameters().isEmpty()) {
                        printIndented("Parameters");
                    }
                    indent(() -> {
                        for (Param param : method.getParameters()) {
                            printIndented("Param");
                            indent(() -> {
                                printIndented("Type, " + param.getType().getIdentifier());
                                printIndented("Identifier, " + param.getName());
                            });
                        }
                    });
                    printIndented("Body");
                    indent(() -> print(method.getBody()));
                });
            }
            case RecordDefinition record -> {
                printIndented("Record, " + record.getName());
                indent(() -> {
                    for (RecordField field : record.getFields()) {
                        printIndented("Field, " + field.getName());
                        indent(() -> printIndented("Type, " + field.getType().getIdentifier()));
                    }
                });
            }
            case VariableDeclaration declaration -> {
                printIndented("VariableDeclaration");
                indent(() -> {
                    printIndented("Type, " + declaration.getType().getIdentifier());
                    printIndented("Identifier, " + declaration.getIdentifier());
                    if (declaration.getValue() != null) {
                        printExpression(declaration.getValue());
                    }
                    printIndented("Final, " + declaration.isFinal());
                });
            }
            case Assignment assign -> {
                printIndented("Assignment");
                indent(() -> {
                    if (assign.getTarget() instanceof VarReference var) {
                        printIndented("Identifier, " + var.getName());
                    }
                    printExpression(assign.getValue());
                });
            }
            case FunctionCall call -> {
                printIndented("FunctionCall, " + call.getFunctionName());
                indent(() -> {
                    for (Expression arg : call.getArguments()) {
                        printExpression(arg);
                    }
                });
            }
            case ReturnStatement ret -> {
                printIndented("ReturnStatement");
                indent(() -> printExpression(ret.getReturnValue()));
            }
            case ForLoop loop -> {
                printIndented("ForLoop");
                indent(() -> {
                    printIndented("LoopVariable, " + loop.getVariable());
                    printIndented("InitialValue");
                    indent(() -> printExpression(loop.getStart()));
                    printIndented("MaximumValue");
                    indent(() -> printExpression(loop.getMaxValue()));
                    printIndented("Step");
                    indent(() -> printExpression(loop.getStep()));
                    printIndented("Body");
                    indent(() -> print(loop.getBody()));
                });
            }
            case WhileLoop loop -> {
                printIndented("WhileLoop");
                indent(() -> {
                    printIndented("Condition");
                    indent(() -> printExpression(loop.getCondition()));
                    printIndented("Body");
                    indent(() -> print(loop.getBody()));
                });
            }
            case IfStatement ifs -> {
                printIndented("IfStatement");
                indent(() -> {
                    printIndented("Condition");
                    indent(() -> printExpression(ifs.getCondition()));
                    printIndented("ThenBlock");
                    if(!ifs.getThenBlock().getStatements().isEmpty()) {
                        indent(() -> print(ifs.getThenBlock()));
                    }
                    printIndented("ElseBlock");
                    if (!ifs.getElseBlock().getStatements().isEmpty()) {
                        indent(() -> print(ifs.getElseBlock()));
                    }
                });
            }
            case null, default -> printIndented("Unknown node.");
        }
    }

    private void printExpression(Expression expr) {
        switch (expr) {
            case Literal lit -> printIndented(capitalize(lit.getType()) + ", " + lit.getValue());
            case VarReference var -> printIndented("Identifier, " + var.getName());
            case BinaryExpression bin -> {
                printIndented("Expr");
                indent(() -> {
                    printExpression(bin.getLeft());
                    printIndented("ArithmeticOperator, " + bin.getOperator());
                    printExpression(bin.getRight());
                });
            }
            case UnaryExpression unary -> {
                printIndented("UnaryExpr");
                indent(() -> {
                    printIndented("UnaryOperator, " + unary.getOperator());
                    printExpression(unary.getOperand());
                });
            }
            case CallExpression call -> {
                printIndented("CallExpression, " + call.getType());
                indent(() -> {
                    for (Expression arg : call.getArguments()) {
                        printExpression(arg);
                    }
                });
            }
            case ArrayAccess access -> {
                printIndented("ArrayAccess");
                indent(() -> {
                    printIndented("Identifier, " + access.getArrayName());
                    printIndented("Index");
                    indent(() -> printExpression(access.getIndex()));
                });
            }
            case RecordFieldAccess access -> {
                printIndented("RecordFieldAccess");
                indent(() -> {
                    printExpression(access.getRecord());
                    printIndented("FieldName, " + access.getFieldName());
                });
            }
            case ArrayCreation creation -> {
                printIndented("ArrayCreation");
                indent(() -> {
                    printIndented("Size");
                    indent(() -> printExpression(creation.getSize()));
                    printIndented("ElementType, " + creation.getElementType().getIdentifier());
                });
            }
            case null, default -> printIndented("Unknown type for expression");
        }
    }

    private void printIndented(String line) {
        System.out.println(" ".repeat(indentLevel) + line);
    }

    private void indent(Runnable r) {
        indentLevel++;
        r.run();
        indentLevel--;
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}