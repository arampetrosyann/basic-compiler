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
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}