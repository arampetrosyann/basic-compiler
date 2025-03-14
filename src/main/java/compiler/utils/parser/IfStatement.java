package compiler.utils.parser;

public class IfStatement implements Statement {
    private final Expression condition;
    private final Block thenBlock;
    private final Block elseBlock; // Nullable (optional)

    public IfStatement(Expression condition, Block thenBlock, Block elseBlock) {
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("IfStatement", null);

        ASTNodeImpl conditionNode = new ASTNodeImpl("Condition", null);
        conditionNode.addChild(condition.toASTNode());
        node.addChild(conditionNode);

        ASTNodeImpl thenNode = new ASTNodeImpl("ThenBlock", null);
        if(!thenBlock.getStatements().isEmpty()) {
            thenNode.addChild(thenBlock.toASTNode());
        }
        node.addChild(thenNode);

        if (elseBlock != null) {
            ASTNodeImpl elseNode = new ASTNodeImpl("ElseBlock", null);
            if(!elseBlock.getStatements().isEmpty()) {
                elseNode.addChild(elseBlock.toASTNode());
            }
            node.addChild(elseNode);
        }

        return node;
    }

}