package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class ASTNodeImpl implements ASTNode {
    private final String type;
    private final String value;

    public ASTNodeImpl(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type + (value != null ? ", " + value : "");
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
