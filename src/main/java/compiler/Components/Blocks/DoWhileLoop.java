package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class DoWhileLoop extends ASTNodeImpl implements Statement {
    private final Expression condition;
    private final Block body;

    public DoWhileLoop(Expression condition, Block body) {
        super("DoWhileLoop", null);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public DoWhileLoop toASTNode() {
        addChild(body.toASTNode());
        addChild(condition.toASTNode());
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}