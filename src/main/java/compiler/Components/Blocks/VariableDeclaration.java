package compiler.Components.Blocks;

public class VariableDeclaration implements Statement{
    private final String identifier;
    private final Type type;
    private final Expression value;
    private final boolean isFinal;

    public VariableDeclaration(String identifier, Type type, Expression value, boolean isFinal) {
        this.identifier = identifier;
        this.type = type;
        this.value = value;
        this.isFinal = isFinal;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("VariableDeclaration", null);
        node.addChild(type.toASTNode());
        node.addChild(new ASTNodeImpl("Identifier", identifier));
        node.addChild(value != null ? value.toASTNode() : new ASTNodeImpl("Value", null));
        node.addChild(new ASTNodeImpl("Final", isFinal ? "true" : "false"));
        return node;
    }
}
