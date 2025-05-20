package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class ArrayCreation extends ASTNodeImpl implements Expression {
    private final Expression size;
    private final Type elementType;

    public ArrayCreation(Expression size, Type elementType) {
        super("ArrayCreation", null);
        this.size = size;
        this.elementType = elementType;
    }

    public Expression getSize() {
        return size;
    }

    public Type getElementType() {
        return elementType;
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
