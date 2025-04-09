package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

public class VariableDeclaration extends ASTNodeImpl implements Statement {
    private final String identifier;
    private final Type type;
    private final Expression value;
    private final boolean isFinal;

    public VariableDeclaration(String identifier, Type type, Expression value, boolean isFinal) {
        super("VariableDeclaration", null);
        this.identifier = identifier;
        this.type = type;
        this.value = value;
        this.isFinal = isFinal;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Type getType() {
        return type;
    }

    public Expression getValue() {
        return value;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public VariableDeclaration toASTNode() {
        addChild(type.toASTNode());
        addChild(new ASTNodeImpl("Identifier", identifier));
        addChild(value != null ? value.toASTNode() : new ASTNodeImpl("Value", null));
        addChild(new ASTNodeImpl("Final", isFinal ? "true" : "false"));
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
