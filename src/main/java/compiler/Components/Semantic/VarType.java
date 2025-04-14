package compiler.Components.Semantic;

public abstract class VarType {
    private final TypeName name;

    public VarType(TypeName name) {
        this.name = name;
    }

    public TypeName getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o); // or leave abstract if needed
    }

}

