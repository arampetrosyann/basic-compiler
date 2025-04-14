package compiler.Analyzer;

import compiler.Components.Token;
import compiler.Components.Blocks.*;
import compiler.Components.Semantic.*;
import compiler.Exceptions.Semantic.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

public class Analyzer {
    private static Analyzer instance;
    private SymbolTable globalTable;
    private SymbolTable currentScope;
    // for checking the return type
    private FunctionType currentFunctionType;

    private Analyzer() {
        reset();
    }

    public static Analyzer getInstance() {
        if (instance == null) instance = new Analyzer();
        return instance;
    }

    public void reset() {
        globalTable = new SymbolTable(SymbolTableType.GLOBAL, null);
        setupBuiltins();
        currentScope = globalTable;
    }

    private void setupBuiltins() {
        globalTable.insert("!", new FunctionType(PrimitiveType.BOOL, List.of(PrimitiveType.BOOL)));
        globalTable.insert("chr", new FunctionType(PrimitiveType.STRING, List.of(PrimitiveType.INT)));
        globalTable.insert("floor", new FunctionType(PrimitiveType.INT, List.of(PrimitiveType.FLOAT)));
        globalTable.insert("len", new FunctionType(PrimitiveType.INT, List.of(PrimitiveType.STRING)));
        globalTable.insert("readInt", new FunctionType(PrimitiveType.INT, List.of()));
        globalTable.insert("readFloat", new FunctionType(PrimitiveType.FLOAT, List.of()));
        globalTable.insert("readString", new FunctionType(PrimitiveType.STRING, List.of()));
        globalTable.insert("writeInt", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.INT)));
        globalTable.insert("writeFloat", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.FLOAT)));
        globalTable.insert("write", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.STRING)));
        globalTable.insert("writeln", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.STRING)));
    }

    public void analyze(ASTNodeImpl node) {
        node.accept(this);
    }

    // root
    public void check(ASTNodeImpl node) {
        for (ASTNodeImpl child : node.getChildren()) {
            child.accept(this);
        }
    }

    private VarType mapToVarType(Type type) {
        String id = type.getIdentifier();

        switch (type.getCategory()) {
            case PRIMITIVE:
                return mapToPrimitiveType(id);
            case RECORD:
                VarType found = globalTable.lookup(id);
                if (!(found instanceof RecordType)) {
                    throw new TypeError("Unknown record type " + id);
                }
                return found;
            case ARRAY:
                Type element = type.getArrayElementType();
                return new ArrayType(mapToVarType(element), -1);
            default:
                throw new TypeError("Unsupported type for " + id);
        }
    }

    private PrimitiveType mapToPrimitiveType(String id) {
        return switch (id.toLowerCase()) {
            case "int", "integer" -> PrimitiveType.INT;
            case "float" -> PrimitiveType.FLOAT;
            case "bool", "boolean" -> PrimitiveType.BOOL;
            case "string" -> PrimitiveType.STRING;
            default -> throw new TypeError("Illegal type " + id);
        };
    }

    private VarType check(Expression expr) {
        if (expr instanceof Literal lit) return check(lit);
        if (expr instanceof VarReference ref) return check(ref);
        if (expr instanceof BinaryExpression bin) return check(bin);
        if (expr instanceof ArrayCreation arrCreation) return check(arrCreation);
        if (expr instanceof ArrayAccess access) return check(access);
        if (expr instanceof FunctionCall call) return check(call);
        if (expr instanceof CallExpression callExpr) return check(callExpr);
        if (expr instanceof RecordFieldAccess fieldAccess) return check(fieldAccess);
        if (expr instanceof UnaryExpression unary) return check(unary);
        if (expr instanceof ReturnStatement ret) return check(ret);

        throw new TypeError("Unknown expression type " + expr.getClass().getSimpleName());
    }

    private VarType lookup(String identifier) {
        SymbolTable scope = currentScope;
        while (scope != null) {
            VarType type = scope.lookup(identifier);
            if (type != null) return type;
            scope = scope.getParent();
        }
        throw new ScopeError("Variable " + identifier + " is not defined");
    }

    public void check(Assignment elem) {
        Expression target = elem.getTarget();
        VarType lhsType;

        if (target instanceof VarReference ref) {
            lhsType = lookup(ref.getName());
        } else if (target instanceof ArrayAccess access) {
            VarType arrayType = lookup(access.getArrayName());
            if (!(arrayType instanceof ArrayType arr)) {
                throw new TypeError("Trying to index a non-array value");
            }

            VarType indexType = check(access.getIndex());
            if (indexType == null || !indexType.equals(PrimitiveType.INT)) {
                throw new TypeError("Array index must be of type int");
            }

            lhsType = arr.getElementType();
        } else {
            throw new TypeError("Invalid assignment target");
        }

        VarType rhsType = check(elem.getValue());

        if (!lhsType.equals(rhsType)) {
            throw new TypeError("Mismatched types in assignment");
        }
    }

    public VarType check(BinaryExpression elem) {
        VarType leftType = check(elem.getLeft());
        VarType rightType = check(elem.getRight());

        if (!leftType.equals(rightType)) {
            throw new OperatorError("Mismatched operand types for operator " + elem.getOperator());
        }

        return switch (elem.getOperator()) {
            case LESS, GREATER, LESS_OR_EQUAL, GREATER_OR_EQUAL, EQUAL, NOT_EQUAL -> PrimitiveType.BOOL;
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO -> leftType;
            default -> throw new OperatorError("Unknown operator " + elem.getOperator());
        };
    }

    public VarType check(Literal elem) {
        return mapToPrimitiveType(elem.getType());
    }

    public VarType check(ArrayCreation elem) {
        VarType elementType = mapToVarType(elem.getElementType());
        VarType sizeType = check(elem.getSize());

        if (!sizeType.equals(PrimitiveType.INT)) {
            throw new TypeError("Array size must be an integer");
        }

        int size = -1;
        if (elem.getSize() instanceof Literal lit && lit.getType().equalsIgnoreCase("int")) {
            size = Integer.parseInt(lit.getValue());
        }

        return new ArrayType(elementType, size);
    }

    public VarType check(ArrayAccess elem) {
        VarType arrayType = lookup(elem.getArrayName());

        if (!(arrayType instanceof ArrayType typedArray)) {
            throw new TypeError("Trying to index a non-array value");
        }

        VarType indexType = check(elem.getIndex());
        if (!indexType.equals(PrimitiveType.INT)) {
            throw new TypeError("Array index must be of type int");
        }

        return typedArray.getElementType();
    }

    public VarType check(FunctionCall elem) {
        FunctionType functionType = (FunctionType) lookup(elem.getFunctionName());
        List<VarType> paramTypes = functionType.getParameters();
        String name = elem.getFunctionName();

        if (name.equals("writeln")) {
            if (elem.getArguments().size() != 1) {
                throw new ArgumentError("writeln expects exactly one argument");
            }

            VarType argType = check(elem.getArguments().getFirst());
            if (!(argType instanceof PrimitiveType)) {
                throw new ArgumentError("writeln only accepts primitive types");
            }

            return functionType.getReturnType();
        }

        if (name.equals("write")) {
            if (elem.getArguments().size() != 1) {
                throw new ArgumentError("write expects exactly one argument");
            }

            VarType argType = check(elem.getArguments().getFirst());
            if (!PrimitiveType.STRING.equals(argType)) {
                throw new ArgumentError("write only accepts string");
            }

            return functionType.getReturnType();
        }

        System.out.println(paramTypes.size() + "   " + elem.getArguments().size());

        if (paramTypes.size() != elem.getArguments().size()) {
            throw new ArgumentError("Incorrect number of arguments for function " + elem.getFunctionName());
        }

        for (int i = 0; i < paramTypes.size(); i++) {
            VarType expectedType = paramTypes.get(i);
            VarType actualType = check(elem.getArguments().get(i));

            if (!expectedType.equals(actualType)) {
                throw new ArgumentError("Argument type mismatch for function " + elem.getFunctionName());
            }
        }

        return functionType.getReturnType();
    }

    public void check(IfStatement elem) {
        VarType conditionType = check(elem.getCondition());

        if (!conditionType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Non-boolean condition in if statement");
        }

        elem.getThenBlock().accept(this);

        if (elem.getElseBlock() != null) elem.getElseBlock().accept(this);
    }

    public VarType check(ReturnStatement elem) {
        VarType returnType = elem.getReturnValue() == null ? ReturnType.VOID : check(elem.getReturnValue());
        VarType functionReturnType = currentFunctionType.getReturnType();

        if (!functionReturnType.equals(returnType)) {
            throw new ReturnError("Return value and return type don't match");
        }

        return returnType;
    }

    public VarType check(CallExpression elem) {
        String name = elem.getType();
        VarType lookedUp = lookup(name);

        if (lookedUp instanceof FunctionType functionType) {
            List<VarType> expectedArgs = functionType.getParameters();
            List<Expression> actualArgs = elem.getChildren().stream()
                    .map(child -> (Expression) child)
                    .toList();

            if (expectedArgs.size() != actualArgs.size()) {
                throw new ArgumentError("Incorrect number of arguments for function " + name);
            }

            for (int i = 0; i < expectedArgs.size(); i++) {
                VarType expected = expectedArgs.get(i);
                VarType actual = check(actualArgs.get(i));
                if (!expected.equals(actual)) {
                    throw new ArgumentError("Argument " + (i + 1) + " type mismatch.");
                }
            }

            return functionType.getReturnType();
        } else if (lookedUp instanceof RecordType recordType) {
            Map<String, VarType> fields = recordType.getFields();
            List<Expression> args = elem.getChildren().stream()
                    .map(child -> (Expression) child)
                    .toList();

            if (fields.size() != args.size()) {
                throw new ArgumentError("Wrong number of arguments for record constructor " + name);
            }

            int i = 0;
            for (VarType expected : fields.values()) {
                VarType actual = check(args.get(i));
                if (!expected.equals(actual)) {
                    throw new ArgumentError("Field " + (i + 1) + " type mismatch in record constructor " + name);
                }
                i++;
            }

            return recordType;
        } else {
            throw new TypeError(name + " is not a function or record");
        }
    }

    public VarType check(RecordFieldAccess elem) {
        VarType recordType = check(elem.getRecord());

        if (!(recordType instanceof RecordType rec)) {
            throw new TypeError("Attempting to access field of non-record type");
        }

        String fieldName = elem.getFieldName();

        if (!rec.hasField(fieldName)) {
            throw new TypeError("Record does not contain field '" + fieldName + "'");
        }

        return rec.getFieldValue(fieldName);
    }

    public VarType check(VarReference elem) {
        return lookup(elem.getName());
    }

    public void check(VariableDeclaration elem) {
        if (currentScope.contains(elem.getIdentifier())) {
            throw new ScopeError("Variable '" + elem.getIdentifier() + "' is already defined");
        }

        VarType declaredType;

        switch (elem.getType().getCategory()) {
            case ARRAY -> {
                Type inner = elem.getType().getArrayElementType();
                declaredType = new ArrayType(mapToVarType(inner), -1);
            }
            case PRIMITIVE, RECORD -> {
                declaredType = mapToVarType(elem.getType());
            }
            default -> throw new TypeError("Unsupported type category for '" + elem.getIdentifier() + "'");
        }

        currentScope.insert(elem.getIdentifier(), declaredType);

        if (elem.getValue() != null) {
            VarType valueType = check(elem.getValue());

            if (!declaredType.equals(valueType)) {
                throw new TypeError("Mismatched types in variable declaration for '" + elem.getIdentifier() + "'");
            }
        }
    }


    public void check(Method elem) {
        List<VarType> paramTypes = new ArrayList<>();
        for (Param param : elem.getParameters()) {
            paramTypes.add(mapToVarType(param.getType()));
        }
        VarType returnType = new ReturnType(TypeName.VOID);
        if (elem.getReturnType() != null) {
            returnType = mapToVarType(elem.getReturnType());
        }

        FunctionType functionType = new FunctionType(returnType, paramTypes);

        globalTable.insert(elem.getName(), functionType);

        currentScope = new SymbolTable(SymbolTableType.SCOPE, currentScope);
        currentFunctionType = functionType;

        for (int i = 0; i < elem.getParameters().size(); i++) {
            Param param = elem.getParameters().get(i);
            currentScope.insert(param.getName(), paramTypes.get(i));
        }

        elem.getBody().accept(this);

        currentFunctionType = null;
        currentScope = currentScope.getParent();
    }

    public void check(FreeStatement elem) {
        lookup(elem.getVariableName());
    }

    public VarType check(UnaryExpression elem) {
        VarType operandType = check(elem.getOperand());

        if (elem.getOperator() == Token.SUBTRACT) {
            if (!(operandType.equals(PrimitiveType.INT) || operandType.equals(PrimitiveType.FLOAT))) {
                throw new TypeError("Operator '-' requires an integer or float operand");
            }
        }

        return operandType;
    }

    public void check(Type elem) {}

    public void check(RecordDefinition elem) {
        if (currentScope.contains(elem.getName())) {
            throw new RecordError("Record '" + elem.getName() + "' is already defined");
        }

        if (globalTable.lookup(elem.getName()) instanceof RecordType) {
            return;
        } else if (globalTable.lookup(elem.getName()) != null) {
            throw new RecordError("Record " + elem.getName() + " already exists");
        }

        Map<String, VarType> fields = new HashMap<>();
        for (RecordField field : elem.getFields()) {
            fields.put(field.getName(), mapToVarType(field.getType()));
        }

        globalTable.insert(elem.getName(), new RecordType(fields));
    }

    public void check(Param elem) {}

    public void check(RecordField elem) {}

    public void check(WhileLoop elem) {
        VarType condType = check(elem.getCondition());
        if (!condType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Non-boolean condition in while's condition statement");
        }

        elem.getBody().accept(this);
    }

    public void check(DoWhileLoop elem) {
        elem.getBody().accept(this);

        VarType condType = check(elem.getCondition());
        if (!condType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Non-boolean condition in do-while's condition statement");
        }
    }

    public void check(ForLoop elem) {
        VarType loopVarType = lookup(elem.getVariable());

        VarType startType = check(elem.getStart());
        VarType endType = check(elem.getMaxValue());
        VarType stepType = check(elem.getStep());

        if (!(loopVarType.equals(startType) && startType.equals(endType) && Objects.equals(stepType, endType))) {
            throw new TypeError("For loop control variables and bounds must have same type");
        }

        elem.getBody().accept(this);
    }

    public void check(Block block) {
        currentScope = new SymbolTable(SymbolTableType.SCOPE, currentScope);

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }

        currentScope = currentScope.getParent();
    }
}
