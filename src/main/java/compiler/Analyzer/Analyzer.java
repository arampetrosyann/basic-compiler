package compiler.Analyzer;

import compiler.Components.Blocks.*;
import compiler.Components.Semantic.*;
import compiler.Exceptions.Semantic.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analyzer {
    private static Analyzer instance;
    private final SymbolTable globalTable;
    private SymbolTable currentScope;

    private Analyzer() {
        globalTable = new SymbolTable(SymbolTableType.GLOBAL, null);
        currentScope = globalTable;
    }

    public static Analyzer getInstance() {
        if (instance == null) instance = new Analyzer();
        return instance;
    }

    public void analyze(ASTNodeImpl node) {
        node.accept(this);
    }

    public void check(ASTNodeImpl node) {
        for (ASTNodeImpl child : node.getChildren()) {
            child.accept(this);
        }
    }

    // @TODO not complete
    private VarType getType(Expression expr) {
        if (expr instanceof Literal) {
            return new PrimitiveType(TypeName.valueOf(((Literal) expr).getType().toUpperCase()));
        } else if (expr instanceof VarReference) {
            return lookup(((VarReference) expr).getName());
        }
        return null;
    }

    private VarType lookup(String identifier) {
        SymbolTable scope = currentScope;
        while (scope != null) {
            VarType type = scope.lookup(identifier);
            if (type != null) return type;
            scope = scope.getParent();
        }
        throw new ScopeError("ScopeError: Variable " + identifier + " is not defined.");
    }

    public void check(Assignment elem) {
        VarType lhsType = getType(elem.getTarget());
        VarType rhsType = getType(elem.getValue());

        if (!lhsType.equals(rhsType)) {
            throw new TypeError("TypeError: Mismatched types in assignment.");
        }
    }

    public void check(BinaryExpression elem) {
        VarType leftType = getType(elem.getLeft());
        VarType rightType = getType(elem.getRight());

        if (!leftType.equals(rightType)) {
            throw new OperatorError("OperatorError: Mismatched operand types for operator " + elem.getOperator());
        }
    }

    public void check(FunctionCall elem) {
        FunctionType functionType = (FunctionType) lookup(elem.getFunctionName());
        List<VarType> paramTypes = functionType.getParameters();

        if (paramTypes.size() != elem.getArguments().size()) {
            throw new ArgumentError("ArgumentError: Incorrect number of arguments for function " + elem.getFunctionName());
        }

        for (int i = 0; i < paramTypes.size(); i++) {
            VarType expectedType = paramTypes.get(i);
            VarType actualType = getType(elem.getArguments().get(i));

            if (!expectedType.equals(actualType)) {
                throw new ArgumentError("ArgumentError: Argument type mismatch for function " + elem.getFunctionName());
            }
        }
    }

    public void check(IfStatement elem) {
        VarType conditionType = getType(elem.getCondition());

        if (!conditionType.equals(new PrimitiveType(TypeName.BOOLEAN))) {
            throw new MissingConditionError("MissingConditionError: Non-boolean condition in if statement.");
        }
    }

//    public void check(ReturnStatement elem) {
//        VarType returnType =
//        VarType returnValueType = getType(elem.getReturnValue());
//
//        if (!returnType.equals(returnValueType)) {
//            throw new ReturnError("ReturnError: Incorrect return type.");
//        }
//    }

    public void check(VarReference elem) {
        if (lookup(elem.getName()) == null) {
            throw new ScopeError("ScopeError: Variable " + elem.getName() + " is not defined in the current scope.");
        }
    }

    public void check(VariableDeclaration elem) {
        VarType varType = new PrimitiveType(TypeName.valueOf(elem.getType().getIdentifier().toUpperCase()));
        currentScope.insert(elem.getIdentifier(), varType);
    }

    public void check(Method elem) {
        SymbolTable methodScope = new SymbolTable(SymbolTableType.SCOPE, currentScope);
        currentScope = methodScope;

        for (Param param : elem.getParameters()) {
            VarType paramType = new PrimitiveType(TypeName.valueOf(param.getType().getIdentifier().toUpperCase()));
            currentScope.insert(param.getName(), paramType);
        }

        // Analyze body
        elem.getBody().accept(this);

        currentScope = currentScope.getParent();
    }

    public void check(RecordDefinition elem) {
        if (globalTable.lookup(elem.getName()) != null) {
            throw new RecordError("RecordError: Record " + elem.getName() + " already exists.");
        }

        Map<String, VarType> fields = new HashMap<>();
        for (RecordField field : elem.getFields()) {
            fields.put(field.getName(), new PrimitiveType(TypeName.valueOf(field.getType().getIdentifier().toUpperCase())));
        }
        globalTable.insert(elem.getName(), new RecordType(fields));
    }
}
