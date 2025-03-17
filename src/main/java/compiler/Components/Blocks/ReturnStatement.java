package compiler.Components.Blocks;

public class ReturnStatement implements Statement {
    private final Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("ReturnStatement", null);
        if (returnValue != null) {
            node.addChild(returnValue.toASTNode());
        }
        return node;
    }
}