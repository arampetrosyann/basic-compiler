package compiler.Components.Blocks;

public class Param implements ASTNode {
    private final Type type;
    private final String name;

    public Param(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Param", null);
        node.addChild(type.toASTNode());

        node.addChild(new ASTNodeImpl("Identifier", name));
        return node;
    }
}
