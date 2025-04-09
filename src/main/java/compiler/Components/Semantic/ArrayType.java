package compiler.Components.Semantic;

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
}
