package compiler.Components.Semantic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private final SymbolTable parent;
    private final SymbolTableType type;
    private final Map<String, VarType> identifiers = new HashMap<>();

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
