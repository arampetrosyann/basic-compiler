package compiler.Components.Semantic;

public class ReturnType extends VarType {
    public static final ReturnType VOID = new ReturnType(TypeName.VOID);

    public ReturnType(TypeName name) {
        super(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReturnType that = (ReturnType) o;
        return this.getName() == that.getName();
    }

    @Override
    public int hashCode() {
        return this.getName() != null ? this.getName().hashCode() : 0;
    }
}