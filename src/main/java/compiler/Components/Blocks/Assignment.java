package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class Assignment extends ASTNodeImpl implements Statement {
    private final Expression target;
    private final Expression value;

    public Assignment(Expression target, Expression value) {
        super("Assignment", null);
        this.target = target;
        this.value = value;
    }

    public Expression getTarget() {
        return target;
    }

    public Expression getValue() {
        return value;
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