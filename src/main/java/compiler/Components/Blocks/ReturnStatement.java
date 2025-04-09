package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

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
    public ReturnStatement toASTNode() {
        if (returnValue != null) {
            addChild(returnValue.toASTNode());
        }
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}