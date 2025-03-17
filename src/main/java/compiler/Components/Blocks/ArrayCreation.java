package compiler.Components.Blocks;

public class ArrayCreation implements Expression {
    private final Expression size;
    private final Type elementType;

    public ArrayCreation(Expression size, Type elementType) {
        this.size = size;
        this.elementType = elementType;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("ArrayCreation", null);

        ASTNodeImpl sizeNode = new ASTNodeImpl("Size", null);
        sizeNode.addChild(size.toASTNode());
        node.addChild(sizeNode);

        ASTNodeImpl typeNode = new ASTNodeImpl("ElementType", elementType.getIdentifier());
        node.addChild(typeNode);

        return node;
    }
}
