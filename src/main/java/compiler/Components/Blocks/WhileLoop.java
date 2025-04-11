package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class WhileLoop extends ASTNodeImpl implements Statement {
    private final Expression condition;
    private final Block body;

    public WhileLoop(Expression condition, Block body) {
        super("WhileLoop", null);
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public Block getBody() {
        return body;
    }

    @Override
    public WhileLoop toASTNode() {
        ASTNodeImpl conditionNode = new ASTNodeImpl("Condition", null);
        conditionNode.addChild(condition.toASTNode());
        addChild(conditionNode);

        ASTNodeImpl bodyNode = new ASTNodeImpl("Body", null);
        bodyNode.addChild(body.toASTNode());
        addChild(bodyNode);

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}