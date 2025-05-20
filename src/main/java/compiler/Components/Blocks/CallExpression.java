package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Generator;

import java.util.List;

public class CallExpression extends ASTNodeImpl implements Expression {
    private final String type;
    private final List<Expression> arguments;

    public CallExpression(String type, List<Expression> arguments) {
        super("CallExpression", type);
        this.type = type;
        this.arguments = arguments;
    }

    public String getType() {
        return type;
    }

    public List<Expression> getArguments() {
        return arguments;
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
