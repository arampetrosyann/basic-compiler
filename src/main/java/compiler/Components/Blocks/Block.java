package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

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
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }

    @Override
    public void accept(Generator generator) {
        generator.generateBlock(this);
    }
}
