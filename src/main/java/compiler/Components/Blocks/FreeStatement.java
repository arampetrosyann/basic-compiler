package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class FreeStatement extends ASTNodeImpl implements Statement {
    private final String variableName;

    public FreeStatement(String variableName) {
        super("FreeStatement", null);
        this.variableName = variableName;
    }

    @Override
    public FreeStatement toASTNode() {
        addChild(new ASTNodeImpl("Variable", variableName));
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
