package compiler.Components.Blocks;

import java.util.List;

public class CallExpression implements Expression {
    private final String type;
    private final List<Expression> arguments;

    public CallExpression(String type, List<Expression> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("CallExpression", type);
        for (Expression arg : arguments) {
            node.addChild(arg.toASTNode());
        }
        return node;
    }
}
