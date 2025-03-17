package compiler.Components.Blocks;

public class RecordField {
    private final String name;
    private final Type type;

    public RecordField(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Field", name);
        node.addChild(new ASTNodeImpl("Type", type.getIdentifier()));
        return node;
    }
}

