package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class DoWhileLoop extends ASTNodeImpl implements Statement {
    private final Expression condition;
    private final Block body;

    public DoWhileLoop(Expression condition, Block body) {
        super("DoWhileLoop", null);
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

    @Override
    public void accept(Generator generator) {
        generator.generateBlock(this);
    }
}