package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class ReturnStatement extends ASTNodeImpl implements Statement {
    private final Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        super("ReturnStatement", null);
        this.returnValue = returnValue;
    }

    public Expression getReturnValue() {
        return returnValue;
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