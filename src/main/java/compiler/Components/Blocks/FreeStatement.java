package compiler.Components.Blocks;

public class FreeStatement implements Statement {
    private final String variableName;

    public FreeStatement(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("FreeStatement", null);
        node.addChild(new ASTNodeImpl("Variable", variableName));
        return node;
    }
}
