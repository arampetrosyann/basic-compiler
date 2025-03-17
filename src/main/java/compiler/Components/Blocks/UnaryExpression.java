package compiler.Components.Blocks;

import compiler.Components.Token;

public class UnaryExpression implements Expression {
    private final Token operator;
    private final Expression operand;

    public UnaryExpression(Token operator, Expression operand) {
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("UnaryExpression", null);

        node.addChild(new ASTNodeImpl("UnaryOperator", operator.toString()));
        node.addChild(operand.toASTNode());

        return node;
    }
}
