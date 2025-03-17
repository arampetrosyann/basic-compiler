package compiler.Components.Blocks;

public class WhileLoop implements Statement {
    private final Expression condition;
    private final Block body;

    public WhileLoop(Expression condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("WhileLoop", null);

        ASTNodeImpl conditionNode = new ASTNodeImpl("Condition", null);
        conditionNode.addChild(condition.toASTNode());
        node.addChild(conditionNode);

        ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
        bodyNode.addChild(body.toASTNode());
        node.addChild(bodyNode);

        return node;
    }
}