package compiler.utils.parser;

import java.util.List;

public class Block implements Statement {
    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Block", null);
        for (Statement statement : statements) {
            node.addChild(statement.toASTNode());
        }
        return node;
    }


}
