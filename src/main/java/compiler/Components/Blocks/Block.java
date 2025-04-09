package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.List;

public class Block extends ASTNodeImpl implements Statement {
    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        super("Block", null);
        this.statements = statements;
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public Block toASTNode() {
        for (Statement statement : statements) {
            addChild(statement.toASTNode());
        }
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
