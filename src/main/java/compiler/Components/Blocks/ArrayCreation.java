package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

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
    public ArrayCreation toASTNode() {
        ASTNodeImpl sizeNode = new ASTNodeImpl("Size", null);
        sizeNode.addChild(size.toASTNode());
        addChild(sizeNode);

        ASTNodeImpl typeNode = new ASTNodeImpl("ElementType", elementType.getIdentifier());
        addChild(typeNode);

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
