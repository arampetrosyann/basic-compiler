package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

public class VarReference extends ASTNodeImpl implements Expression {
    private final String name;

    public VarReference(String name) {
        super("Identifier", name);
        this.name = name;
    }

    public String getName() {
        return name;
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
