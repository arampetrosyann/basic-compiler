package compiler.Components.Semantic;

public abstract class VarType {
    private final TypeName name;

    public VarType(TypeName name) {
        this.name = name;
    }

    public TypeName getName() {
        return name;
    }
}

