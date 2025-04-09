package compiler.Components.Blocks;

import compiler.Analyzer.Analyzer;
import compiler.Components.Token;

public class UnaryExpression extends ASTNodeImpl implements Expression {
    private final Token operator;
    private final Expression operand;

    public UnaryExpression(Token operator, Expression operand) {
        super("UnaryExpression", null);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public UnaryExpression toASTNode() {
        addChild(new ASTNodeImpl("UnaryOperator", operator.toString()));
        addChild(operand.toASTNode());

        return this;
    }

    @Override
    public void accept(Analyzer analyzer) {
        analyzer.check(this);
    }
}
