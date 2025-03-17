package compiler.Components.Blocks;

import compiler.Components.Token;

public class BinaryExpression implements Expression {
    private final Expression left;
    private final Token operator;
    private final Expression right;

    public BinaryExpression(Expression left, Token operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public ASTNodeImpl toASTNode() {
        ASTNodeImpl node = new ASTNodeImpl("Expr", null);
        node.addChild(left.toASTNode());
        node.addChild(new ASTNodeImpl("ArithmeticOperator", operator.toString()));
        node.addChild(right.toASTNode());
        return node;
    }
}
