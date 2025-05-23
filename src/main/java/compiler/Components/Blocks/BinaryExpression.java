package compiler.Components.Blocks;

import compiler.Analyzer;
import compiler.Components.Token;
import compiler.Generator;

public class BinaryExpression extends ASTNodeImpl implements Expression {
    private final Expression left;
    private final Token operator;
    private final Expression right;

    public BinaryExpression(Expression left, Token operator, Expression right) {
        super("Expr", null);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public Token getOperator() {
        return operator;
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
