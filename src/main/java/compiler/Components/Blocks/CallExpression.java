package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;

import java.util.List;

public class CallExpression extends ASTNodeImpl implements Expression {
    private final String type;
    private final List<Expression> arguments;

    public CallExpression(String type, List<Expression> arguments) {
        super("CallExpression", type);
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public CallExpression toASTNode() {
        for (Expression arg : arguments) {
            addChild(arg.toASTNode());
        }
        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
