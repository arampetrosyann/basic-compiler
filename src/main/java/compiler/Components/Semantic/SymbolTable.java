package compiler.Components.Semantic;

import compiler.Components.Blocks.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class SymbolTable {
    private final SymbolTable parent;
    private final SymbolTableType type;
    private final Map<String, VarType> identifiers = new HashMap<>();
    private final Map<Statement, SymbolTable> descendants = new HashMap<>();

    public SymbolTable(@Nonnull SymbolTableType type, @Nullable SymbolTable parentSymbolTable) {
        this.type = type;
        this.parent = parentSymbolTable;
    }

    public void insert(String identifier, VarType type) {
        identifiers.put(identifier, type);
    }

    public boolean contains(String identifier) {
        return identifiers.containsKey(identifier);
    }

    public void add(Statement elem, SymbolTable st) {
        if (st.getParent() != this) {
            throw new IllegalStateException("Internal Error");
        }

        descendants.put(elem, st);
    }

    public SymbolTable getSymbolTable(Statement elem) {
        return descendants.get(elem);
    }

    public VarType lookup(String identifier) {
        VarType varType = identifiers.get(identifier);

        if (varType == null && parent != null) {
            return parent.lookup(identifier);
        }

        return varType;
    }

    public SymbolTable getParent() {
        return parent;
    }

    public SymbolTableType getTableType() {
        return type;
    }
}
