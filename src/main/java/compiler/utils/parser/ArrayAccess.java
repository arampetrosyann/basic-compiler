package compiler.utils.parser;

public class ArrayAccess implements Expression {
    private final String arrayName;
    private final Expression index;

    public ArrayAccess(String arrayName, Expression index) {
        this.arrayName = arrayName;
        this.index = index;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("ArrayAccess", null);
        node.addChild(new ASTNodeImpl("Identifier", arrayName));
        ASTNodeImpl indexNode = new ASTNodeImpl("Index", null);
        indexNode.addChild(index.toASTNode());
        node.addChild(indexNode);
        return node;
    }

}
