package compiler.Components.Blocks;

public class Type implements ASTNode {
    private final String identifier;

    public Type(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        return new ASTNodeImpl("Identifier", identifier);
    }
}
