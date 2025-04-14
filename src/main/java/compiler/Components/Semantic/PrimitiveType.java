package compiler.Components.Semantic;

// INT FLOAT BOOLEAN STRING
public class PrimitiveType extends VarType {
    public static final PrimitiveType INT = new PrimitiveType(TypeName.INTEGER);
    public static final PrimitiveType FLOAT = new PrimitiveType(TypeName.FLOAT);
    public static final PrimitiveType BOOL = new PrimitiveType(TypeName.BOOLEAN);
    public static final PrimitiveType STRING = new PrimitiveType(TypeName.STRING);

    public PrimitiveType(TypeName name) {
        super(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveType that = (PrimitiveType) o;
        return this.getName() == that.getName();
    }

    @Override
    public int hashCode() {
        return this.getName() != null ? this.getName().hashCode() : 0;
    }
}
