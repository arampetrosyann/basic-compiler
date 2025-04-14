package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class ArrayAccess extends ASTNodeImpl implements Expression {
    private final String arrayName;
    private final Expression index;

    public ArrayAccess(String arrayName, Expression index) {
        super("ArrayAccess", null);
        this.arrayName = arrayName;
        this.index = index;
    }

    public Expression getIndex() {
        return index;
    }

    public String getArrayName() {
        return arrayName;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
