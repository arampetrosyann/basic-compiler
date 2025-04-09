package compiler.Components.Blocks;

public class Type extends ASTNodeImpl {
    private final String identifier;
    private final TypeCategory category;

    public Type(String identifier, TypeCategory category) {
        super("Type", identifier);
        this.identifier = identifier;
        this.category = category;
    }

    public String getIdentifier() {
        return identifier;
    }

    public TypeCategory getCategory() {
        return category;
    }

    @Override
    public Type toASTNode() {
        return this;
    }
}
