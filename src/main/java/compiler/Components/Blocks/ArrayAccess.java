package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class ArrayAccess extends ASTNodeImpl implements Expression {
    private final Expression arrayExpr;
    private final Expression index;

    public ArrayAccess(Expression arrayExpr, Expression index) {
        super("ArrayAccess", null);
        this.arrayExpr = arrayExpr;
        this.index = index;
    }

    public Expression getIndex() {
        return index;
    }

    public Expression getArrayExpr() { return arrayExpr; }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }

    @Override
    public void accept(Generator generator) {
        generator.generateBlock(this);
    }
}
