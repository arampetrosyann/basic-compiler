package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class FreeStatement extends ASTNodeImpl implements Statement {
    private final String variableName;

    public FreeStatement(String variableName) {
        super("FreeStatement", null);
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
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
