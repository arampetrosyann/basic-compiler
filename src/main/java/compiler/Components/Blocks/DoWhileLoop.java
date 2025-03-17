package compiler.Components.Blocks;

public class DoWhileLoop implements Statement {
    private final Expression condition;
    private final Block body;

    public DoWhileLoop(Expression condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("DoWhileLoop", null);
        node.addChild(body.toASTNode());
        node.addChild(condition.toASTNode());
        return node;
    }
}