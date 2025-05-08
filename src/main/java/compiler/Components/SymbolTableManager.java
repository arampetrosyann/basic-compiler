package compiler.Components;

import compiler.Components.Blocks.Statement;
import compiler.Components.Semantic.SymbolTable;
import compiler.Components.Semantic.SymbolTableType;
import compiler.Components.Semantic.VarType;
import compiler.Exceptions.Semantic.ScopeError;

public class SymbolTableManager {
    private static SymbolTableManager instance;
    private SymbolTable globalTable;
    private SymbolTable currentScope;

    private SymbolTableManager() {
        reset();
    }

    public void reset() {
        globalTable = new SymbolTable(SymbolTableType.GLOBAL, null);
        currentScope = globalTable;
    }

    public static SymbolTableManager getInstance() {
        if (instance == null) instance = new SymbolTableManager();
        return instance;
    }

    public void enterSymbolTable(Statement st) {
        currentScope = currentScope.getSymbolTable(st);
    }

    public VarType lookup(String identifier) {
        SymbolTable scope = currentScope;
        while (scope != null) {
            VarType type = scope.lookup(identifier);
            if (type != null) return type;
            scope = scope.getParent();
        }
        throw new ScopeError("Variable " + identifier + " is not defined");
    }

    public void leaveSymbolTable() {
        currentScope = currentScope.getParent();
    }

    public SymbolTable getGlobalTable() {
        return globalTable;
    }

    public SymbolTable getCurrentScope() {
        return currentScope;
    }
}
