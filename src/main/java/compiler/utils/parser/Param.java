package compiler.utils.parser;

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

        ASTNodeImpl typeNode = new ASTNodeImpl("Type", null);
        typeNode.addChild(type.toASTNode());
        node.addChild(typeNode);

        node.addChild(new ASTNodeImpl("Identifier", name));
        return node;
    }
}
