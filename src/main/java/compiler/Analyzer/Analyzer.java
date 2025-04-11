package compiler.Analyzer;

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

    public void check(ASTNodeImpl node) {
        for (ASTNodeImpl child : node.getChildren()) {
            if (child instanceof RecordDefinition record) {
                Map<String, VarType> fields = new HashMap<>();
                for (RecordField field : record.getFields()) {
                    fields.put(field.getName(), mapToVarType(field.getType()));
                }
                globalTable.insert(record.getName(), new RecordType(fields));
            }
        }

        for (ASTNodeImpl child : node.getChildren()) {
            if (child instanceof Method method) {
                List<VarType> paramTypes = new ArrayList<>();
                for (Param param : method.getParameters()) {
                    paramTypes.add(mapToVarType(param.getType()));
                }
                VarType returnType = new ReturnType(TypeName.VOID);
                if (method.getReturnType() != null) {
                    returnType = mapToVarType(method.getReturnType());
                }
                globalTable.insert(method.getName(), new FunctionType(returnType, paramTypes));
            }
        }

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
                    throw new TypeError("Type Error: Unknown record type " + id);
                }
                return found;
            case ARRAY:
                Type element = type.getArrayElementType();
                return new ArrayType(mapToVarType(element), -1);
            default:
                throw new TypeError("Type Error: Unsupported type category for " + id);
        }
    }

    private PrimitiveType mapToPrimitiveType(String id) {
        return switch (id.toLowerCase()) {
            case "int", "integer" -> PrimitiveType.INT;
            case "float" -> PrimitiveType.FLOAT;
            case "bool", "boolean" -> PrimitiveType.BOOL;
            case "string" -> PrimitiveType.STRING;
            default -> throw new TypeError("Type Error: Illegal type " + id);
        };
    }

    private VarType getType(Expression expr) {
        if (expr instanceof Literal lit) return typeFromLiteral(lit);
        if (expr instanceof VarReference ref) return typeFromVarReference(ref);
        if (expr instanceof BinaryExpression bin) return typeFromBinaryExpression(bin);
        if (expr instanceof ArrayCreation arrCreation) return typeFromArrayCreation(arrCreation);
        if (expr instanceof ArrayAccess access) return typeFromArrayAccess(access);
        if (expr instanceof FunctionCall call) return typeFromFunctionCall(call);
        if (expr instanceof CallExpression callExpr) return typeFromCallExpression(callExpr);
        if (expr instanceof RecordFieldAccess fieldAccess) return typeFromRecordFieldAccess(fieldAccess);

        throw new TypeError("Type Error: Unknown expression type " + expr.getClass().getSimpleName());
    }

    private VarType typeFromLiteral(Literal lit){
        return mapToPrimitiveType(lit.getType());
    }

    private VarType typeFromVarReference(VarReference ref) {
        return lookup(ref.getName());
    }

    private VarType typeFromBinaryExpression(BinaryExpression bin) {
        VarType left = getType(bin.getLeft());
        VarType right = getType(bin.getRight());

        if (left == null || right == null) {
            throw new TypeError("Type Error: Cannot infer type of binary expression (null operand)");
        }

        if (!left.equals(right)) {
            throw new OperatorError("Operator Error: Mismatched types in binary expression");
        }

        // handle the operator return type
        return switch (bin.getOperator()) {
            // Comparison ops: return bool
            case LESS, GREATER, LESS_OR_EQUAL, GREATER_OR_EQUAL, EQUAL, NOT_EQUAL -> PrimitiveType.BOOL;
            // Arithmetic ops: return same as operands
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO -> left;
            default -> throw new OperatorError("Operator Error: Unknown operator " + bin.getOperator());
        };
    }

    private VarType typeFromArrayCreation(ArrayCreation arrayExpr) {
        VarType elementType = mapToVarType(arrayExpr.getElementType());
        VarType sizeType = getType(arrayExpr.getSize());

        if (!sizeType.equals(PrimitiveType.INT)) {
            throw new TypeError("Type Error: Array size must be an integer.");
        }

        int size = -1;
        if (arrayExpr.getSize() instanceof Literal lit && lit.getType().equalsIgnoreCase("int")) {
            size = Integer.parseInt(lit.getValue());
        }

        return new ArrayType(elementType, size);
    }

    private VarType typeFromArrayAccess(ArrayAccess access) {
        VarType arrayType = lookup(access.getArrayName());

        if (!(arrayType instanceof ArrayType typedArray)) {
            throw new TypeError("Type Error: Trying to index a non-array value.");
        }

        VarType indexType = getType(access.getIndex());
        if (!indexType.equals(PrimitiveType.INT)) {
            throw new TypeError("Type Error: Array index must be of type int.");
        }

        return typedArray.getElementType();
    }

    private VarType typeFromFunctionCall(FunctionCall call) {
        VarType type = lookup(call.getFunctionName());

        if (!(type instanceof FunctionType functionType)) {
            throw new TypeError("TypeError: Called identifier is not a function: " + call.getFunctionName());
        }

        List<VarType> paramTypes = functionType.getParameters();
        List<Expression> args = call.getArguments();

        if (paramTypes.size() != args.size()) {
            throw new ArgumentError("ArgumentError: Incorrect number of arguments for function " + call.getFunctionName());
        }

        for (int i = 0; i < args.size(); i++) {
            VarType actual = getType(args.get(i));
            if (!paramTypes.get(i).equals(actual)) {
                throw new ArgumentError("ArgumentError: Mismatched argument type for parameter " + i + " in function call to " + call.getFunctionName());
            }
        }

        return functionType.getReturnType();
    }

    private VarType typeFromCallExpression(CallExpression callExpr) {
        String name = callExpr.getType();
        VarType lookedUp = lookup(name);

        if (lookedUp instanceof FunctionType functionType) {
            List<VarType> expectedArgs = functionType.getParameters();
            List<Expression> actualArgs = callExpr.getChildren().stream()
                    .map(child -> (Expression) child)
                    .toList();

            if (expectedArgs.size() != actualArgs.size()) {
                throw new ArgumentError("ArgumentError: Incorrect number of arguments for function " + name);
            }

            for (int i = 0; i < expectedArgs.size(); i++) {
                VarType expected = expectedArgs.get(i);
                VarType actual = getType(actualArgs.get(i));
                if (!expected.equals(actual)) {
                    throw new ArgumentError("ArgumentError: Argument " + (i + 1) + " type mismatch.");
                }
            }

            return functionType.getReturnType();
        } else if (lookedUp instanceof RecordType recordType) {
            Map<String, VarType> fields = recordType.getFields();
            List<Expression> args = callExpr.getChildren().stream()
                    .map(child -> (Expression) child)
                    .toList();

            if (fields.size() != args.size()) {
                throw new ArgumentError("ArgumentError: Wrong number of arguments for record constructor " + name);
            }

            int i = 0;
            for (VarType expected : fields.values()) {
                VarType actual = getType(args.get(i));
                if (!expected.equals(actual)) {
                    throw new ArgumentError("ArgumentError: Field " + (i + 1) + " type mismatch in record constructor " + name);
                }
                i++;
            }

            return recordType;
        } else {
            throw new TypeError("TypeError: " + name + " is not a function or record.");
        }
    }

    private VarType typeFromRecordFieldAccess(RecordFieldAccess fieldAccess) {
        VarType recordType = getType(fieldAccess.getRecord());

        if (!(recordType instanceof RecordType rec)) {
            throw new TypeError("TypeError: Attempting to access field of non-record type.");
        }

        String fieldName = fieldAccess.getFieldName();

        if (!rec.hasField(fieldName)) {
            throw new TypeError("TypeError: Record does not contain field '" + fieldName + "'");
        }

        return rec.getFieldValue(fieldName);
    }

    private VarType lookup(String identifier) {
        SymbolTable scope = currentScope;
        while (scope != null) {
            VarType type = scope.lookup(identifier);
            if (type != null) return type;
            scope = scope.getParent();
        }
        throw new ScopeError("Scope Error: Variable " + identifier + " is not defined.");
    }

    public void check(Assignment elem) {
        Expression target = elem.getTarget();
        VarType lhsType;

        if (target instanceof VarReference ref) {
            lhsType = lookup(ref.getName());
        } else if (target instanceof ArrayAccess access) {
            VarType arrayType = lookup(access.getArrayName());
            if (!(arrayType instanceof ArrayType arr)) {
                throw new TypeError("Type Error: Trying to index a non-array value.");
            }

            // Check index is integer
            VarType indexType = getType(access.getIndex());
            if (indexType == null || !indexType.equals(PrimitiveType.INT)) {
                throw new TypeError("Type Error: Array index must be of type int.");
            }

            lhsType = arr.getElementType();
        } else {
            throw new TypeError("Type Error: Invalid assignment target.");
        }

        VarType rhsType = getType(elem.getValue());

        if (!lhsType.equals(rhsType)) {
            throw new TypeError("Type Error: Mismatched types in assignment.");
        }
    }

    public void check(BinaryExpression elem) {
        VarType leftType = getType(elem.getLeft());
        VarType rightType = getType(elem.getRight());

        if (!leftType.equals(rightType)) {
            throw new OperatorError("Operator Error: Mismatched operand types for operator " + elem.getOperator());
        }
    }

    public void check(FunctionCall elem) {
        FunctionType functionType = (FunctionType) lookup(elem.getFunctionName());
        List<VarType> paramTypes = functionType.getParameters();
        String name = elem.getFunctionName();

        if (name.equals("writeln")) {
            if (elem.getArguments().size() != 1) {
                throw new ArgumentError("writeln expects exactly one argument.");
            }

            VarType argType = getType(elem.getArguments().getFirst());
            if (!(argType instanceof PrimitiveType)) {
                throw new ArgumentError("writeln only accepts primitive types.");
            }

            return;
        }

        if (name.equals("write")) {
            if (elem.getArguments().size() != 1) {
                throw new ArgumentError("write expects exactly one argument.");
            }

            VarType argType = getType(elem.getArguments().getFirst());
            if (!PrimitiveType.STRING.equals(argType)) {
                throw new ArgumentError("write only accepts string.");
            }

            return;
        }

        if (paramTypes.size() != elem.getArguments().size()) {
            throw new ArgumentError("Argument Error: Incorrect number of arguments for function " + elem.getFunctionName());
        }

        for (int i = 0; i < paramTypes.size(); i++) {
            VarType expectedType = paramTypes.get(i);
            VarType actualType = getType(elem.getArguments().get(i));

            if (!expectedType.equals(actualType)) {
                throw new ArgumentError("Argument Error: Argument type mismatch for function " + elem.getFunctionName());
            }
        }
    }

    public void check(IfStatement elem) {
        VarType conditionType = getType(elem.getCondition());

        if (!conditionType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Missing Condition Error: Non-boolean condition in if statement.");
        }
    }

    public void check(VarReference elem) {
        if (lookup(elem.getName()) == null) {
            throw new ScopeError("Scope Error: Variable " + elem.getName() + " is not defined in the current scope.");
        }
    }

    public void check(VariableDeclaration elem) {
        VarType declaredType;

        switch (elem.getType().getCategory()) {
            case ARRAY -> {
                Type inner = elem.getType().getArrayElementType();
                declaredType = new ArrayType(mapToVarType(inner), -1);
            }
            case PRIMITIVE, RECORD -> {
                declaredType = mapToVarType(elem.getType());
            }
            default -> throw new TypeError("Type Error: Unsupported type category for '" + elem.getIdentifier() + "'");
        }

        currentScope.insert(elem.getIdentifier(), declaredType);

        if (elem.getValue() != null) {
            VarType valueType = getType(elem.getValue());

            if (!declaredType.equals(valueType)) {
                throw new TypeError("Type Error: Mismatched types in variable declaration for '" + elem.getIdentifier() + "'");
            }
        }
    }


    public void check(Method elem) {
        List<VarType> paramTypes = new ArrayList<>();
        for (Param param : elem.getParameters()) {
            paramTypes.add(mapToVarType(param.getType()));
        }
        VarType returnType = new ReturnType(TypeName.VOID);
        if(elem.getReturnType() != null) {
            returnType = mapToVarType(elem.getReturnType());
        }
        globalTable.insert(elem.getName(), new FunctionType(returnType, paramTypes));

        currentScope = new SymbolTable(SymbolTableType.SCOPE, currentScope);

        for (int i = 0; i < elem.getParameters().size(); i++) {
            Param param = elem.getParameters().get(i);
            currentScope.insert(param.getName(), paramTypes.get(i));
        }

        elem.getBody().accept(this);

        currentScope = currentScope.getParent();
    }

    public void check(RecordDefinition elem) {
        if (globalTable.lookup(elem.getName()) instanceof RecordType) {
            return;
        } else if (globalTable.lookup(elem.getName()) != null) {
            throw new RecordError("Record Error: Record " + elem.getName() + " already exists.");
        }

        Map<String, VarType> fields = new HashMap<>();
        for (RecordField field : elem.getFields()) {
            fields.put(field.getName(), mapToVarType(field.getType()));
        }

        globalTable.insert(elem.getName(), new RecordType(fields));
    }

    public void check(WhileLoop elem) {
        VarType condType = getType(elem.getCondition());
        if (!condType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Missing Condition Error: Non-boolean condition in while's condition statement.");
        }
    }

    public void check(DoWhileLoop elem) {
        check(elem.getBody());

        VarType condType = getType(elem.getCondition());
        if (!condType.equals(PrimitiveType.BOOL)) {
            throw new MissingConditionError("Missing Condition Error: Non-boolean condition in do-while's condition statement.");
        }
    }

    public void check(ForLoop elem) {
        VarType loopVarType = lookup(elem.getVariable());

        VarType startType = getType(elem.getStart());
        VarType endType = getType(elem.getMaxValue());
        VarType stepType = getType(elem.getStep());

        if (!(loopVarType.equals(startType) && startType.equals(endType) && Objects.equals(stepType, endType))) {
            throw new TypeError("Type Error: For loop control variables and bounds must have same type.");
        }
    }

    public void check(Block block) {
        currentScope = new SymbolTable(SymbolTableType.SCOPE, currentScope);

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }

        currentScope = currentScope.getParent();
    }

}
