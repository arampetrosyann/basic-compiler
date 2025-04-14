package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class IfStatement extends ASTNodeImpl implements Statement {
    private final Expression condition;
    private final Block thenBlock;
    private final Block elseBlock; // Nullable (optional)

    public IfStatement(Expression condition, Block thenBlock, Block elseBlock) {
        super("IfStatement", null);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public Block getThenBlock() {
        return thenBlock;
    }

    public Block getElseBlock() {
        return elseBlock;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}