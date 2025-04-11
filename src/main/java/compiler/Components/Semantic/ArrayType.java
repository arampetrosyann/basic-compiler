package compiler.Components.Semantic;

import java.util.Objects;

public class ArrayType extends VarType {
    private VarType elementType;
    private int size;

    public ArrayType(VarType elementType, int size) {
        super(TypeName.ARRAY);

        this.elementType = elementType;
        this.size = size;
    }

    public VarType getElementType() {
        return elementType;
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArrayType other)) return false;
        return Objects.equals(this.getElementType(), other.getElementType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElementType());
    }

    @Override
    public String toString() {
        return "ArrayType(" + elementType + ")";
    }
}
