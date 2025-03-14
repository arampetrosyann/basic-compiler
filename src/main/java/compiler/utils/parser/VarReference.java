package compiler.utils.parser;

public class VarReference implements Expression {
    private final String name;

    public VarReference(String name) {

        this.name = name;
    }

    @Override
    public ASTNodeImpl toASTNode() {

        return new ASTNodeImpl("Identifier", name);
    }

}
