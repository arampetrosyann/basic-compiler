package compiler.utils.parser;

import java.util.List;

public class FunctionCall implements Expression, Statement {
    private final String functionName;
    private final List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }


    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("FunctionCall", functionName);

        for (Expression arg : arguments) {
            node.addChild(arg.toASTNode());
        }

        return node;
    }

}