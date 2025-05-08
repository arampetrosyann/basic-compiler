package compiler;

import compiler.Components.SymbolTableManager;
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
    private static SymbolTableManager symbolTableManager;
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
        symbolTableManager = SymbolTableManager.getInstance();
        symbolTableManager.reset();

        setupBuiltins();
    }

    private void setupBuiltins() {
        symbolTableManager.getGlobalTable().insert("!", new FunctionType(PrimitiveType.BOOL, List.of(PrimitiveType.BOOL)));
        symbolTableManager.getGlobalTable().insert("chr", new FunctionType(PrimitiveType.STRING, List.of(PrimitiveType.INT)));
        symbolTableManager.getGlobalTable().insert("floor", new FunctionType(PrimitiveType.INT, List.of(PrimitiveType.FLOAT)));
        symbolTableManager.getGlobalTable().insert("len", new FunctionType(PrimitiveType.INT, List.of(PrimitiveType.STRING)));
        symbolTableManager.getGlobalTable().insert("readInt", new FunctionType(PrimitiveType.INT, List.of()));
        symbolTableManager.getGlobalTable().insert("readFloat", new FunctionType(PrimitiveType.FLOAT, List.of()));
        symbolTableManager.getGlobalTable().insert("readString", new FunctionType(PrimitiveType.STRING, List.of()));
        symbolTableManager.getGlobalTable().insert("writeInt", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.INT)));
        symbolTableManager.getGlobalTable().insert("writeFloat", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.FLOAT)));
        symbolTableManager.getGlobalTable().insert("write", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.STRING)));
        symbolTableManager.getGlobalTable().insert("writeln", new FunctionType(ReturnType.VOID, List.of(PrimitiveType.STRING)));
    }

    public void analyze(Block n) {
        n.accept(this);
    }

    public void check(ASTNodeImpl node) {}

    private VarType mapToVarType(Type type) {
        String id = type.getIdentifier();

        switch (type.getCategory()) {
            case PRIMITIVE:
                return mapToPrimitiveType(id);
            case RECORD:
                VarType found = symbolTableManager.getGlobalTable().lookup(id);
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

    public void check(Assignment elem) {
        Expression target = elem.getTarget();
        VarType lhsType;

        if (target instanceof VarReference ref) {
            lhsType = symbolTableManager.lookup(ref.getName());
        } else if (target instanceof ArrayAccess access) {
            VarType arrayType = symbolTableManager.lookup(access.getArrayName());
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
            case LOGICAL_OR -> rightType;
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
        VarType arrayType = symbolTableManager.lookup(elem.getArrayName());

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
        FunctionType functionType = (FunctionType) symbolTableManager.lookup(elem.getFunctionName());
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
        VarType lookedUp = symbolTableManager.lookup(name);

        if (lookedUp instanceof FunctionType functionType) {
            List<VarType> expectedArgs = functionType.getParameters();
            List<Expression> actualArgs = elem.getArguments();

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
            List<Expression> args = elem.getArguments();

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
        return symbolTableManager.lookup(elem.getName());
    }

    public void check(VariableDeclaration elem) {
        if (symbolTableManager.getCurrentScope().contains(elem.getIdentifier())) {
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

        symbolTableManager.getCurrentScope().insert(elem.getIdentifier(), declaredType);

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

        symbolTableManager.getGlobalTable().insert(elem.getName(), functionType);

        SymbolTable newSymbolTable = new SymbolTable(SymbolTableType.SCOPE, symbolTableManager.getCurrentScope());

        symbolTableManager.getCurrentScope().add(elem, newSymbolTable);
        symbolTableManager.enterSymbolTable(elem);

        currentFunctionType = functionType;

        for (int i = 0; i < elem.getParameters().size(); i++) {
            Param param = elem.getParameters().get(i);
            symbolTableManager.getCurrentScope().insert(param.getName(), paramTypes.get(i));
        }

        elem.getBody().accept(this);

        currentFunctionType = null;
        symbolTableManager.leaveSymbolTable();
    }

    public void check(FreeStatement elem) {
        symbolTableManager.lookup(elem.getVariableName());
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
        if (symbolTableManager.getGlobalTable().contains(elem.getName())) {
            throw new RecordError("Record '" + elem.getName() + "' is already defined");
        }

        if (symbolTableManager.getGlobalTable().lookup(elem.getName()) instanceof RecordType) {
            return;
        } else if (symbolTableManager.getGlobalTable().lookup(elem.getName()) != null) {
            throw new RecordError("Record " + elem.getName() + " already exists");
        }

        Map<String, VarType> fields = new HashMap<>();
        for (RecordField field : elem.getFields()) {
            fields.put(field.getName(), mapToVarType(field.getType()));
        }

        symbolTableManager.getGlobalTable().insert(elem.getName(), new RecordType(elem.getName(), fields));
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
        VarType loopVarType = symbolTableManager.lookup(elem.getVariable());

        VarType startType = check(elem.getStart());
        VarType endType = check(elem.getMaxValue());
        VarType stepType = check(elem.getStep());

        if (!(loopVarType.equals(startType) && startType.equals(endType) && Objects.equals(stepType, endType))) {
            throw new TypeError("For loop control variables and bounds must have same type");
        }

        elem.getBody().accept(this);
    }

    public void check(Block block) {
        SymbolTable newSymbolTable = new SymbolTable(SymbolTableType.SCOPE, symbolTableManager.getCurrentScope());
        symbolTableManager.getCurrentScope().add(block, newSymbolTable);
        symbolTableManager.enterSymbolTable(block);

        for (Statement stmt : block.getStatements()) {
            stmt.accept(this);
        }

        symbolTableManager.leaveSymbolTable();
    }
}
